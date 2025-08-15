/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.template.processor;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import org.jspecify.annotations.Nullable;
import org.openrewrite.java.template.internal.JavacResolution;

import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.openrewrite.java.template.processor.RefasterTemplateProcessor.*;

class RuleDescriptor {

    public final JCTree.JCClassDecl classDecl;
    public final List<TemplateDescriptor> beforeTemplates;
    public final @Nullable TemplateDescriptor afterTemplate;

    private RuleDescriptor(
            JCTree.JCClassDecl classDecl,
            List<TemplateDescriptor> beforeTemplates,
            @Nullable TemplateDescriptor afterTemplate) {
        this.classDecl = classDecl;
        this.beforeTemplates = beforeTemplates;
        this.afterTemplate = afterTemplate;
    }

    public static @Nullable RuleDescriptor create(
            JavacProcessingEnvironment processingEnv,
            JCTree.JCCompilationUnit cu,
            JCTree.JCClassDecl classDecl) {
        List<TemplateDescriptor> beforeTemplates = new ArrayList<>();
        TemplateDescriptor afterTemplate = null;
        for (JCTree member : classDecl.getMembers()) {
            if (member instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) member;
                List<JCTree.JCAnnotation> annotations = getMethodTreeAnnotations(method, BEFORE_TEMPLATE::equals);
                if (!annotations.isEmpty()) {
                    beforeTemplates.add(new TemplateDescriptor(processingEnv, cu, classDecl, method));
                }
                annotations = getMethodTreeAnnotations(method, AFTER_TEMPLATE::equals);
                if (!annotations.isEmpty()) {
                    afterTemplate = new TemplateDescriptor(processingEnv, cu, classDecl, method);
                }
            }
        }
        return new RuleDescriptor(classDecl, beforeTemplates, afterTemplate)
                .validate(processingEnv, cu, classDecl);
    }

    private @Nullable RuleDescriptor validate(
            JavacProcessingEnvironment processingEnv,
            JCTree.JCCompilationUnit cu,
            JCTree.JCClassDecl classDecl) {
        if (beforeTemplates.isEmpty()) {
            return null;
        }

        for (JCTree member : classDecl.getMembers()) {
            if (member instanceof JCTree.JCMethodDecl && beforeTemplates.stream().noneMatch(t -> t.method == member) &&
                    (afterTemplate == null || member != afterTemplate.method)) {
                for (JCTree.JCAnnotation annotation : getMethodTreeAnnotations(((JCTree.JCMethodDecl) member), RefasterTemplateProcessor.UNSUPPORTED_ANNOTATIONS::contains)) {
                    printNoteOnce(processingEnv, "@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                    return null;
                }
            }
        }

        // resolve so that we can inspect the template body
        boolean valid = resolveAll(processingEnv, cu);
        if (!valid) {
            return null;
        }
        if (afterTemplate != null) {
            Set<Name> requiredParameters = findParameterOrder(afterTemplate.method, 0).keySet();
            for (TemplateDescriptor beforeTemplate : beforeTemplates) {
                for (int i = 0; i < beforeTemplate.getArity(); i++) {
                    Set<Name> providedParameters = findParameterOrder(beforeTemplate.method, i).keySet();
                    if (!providedParameters.containsAll(requiredParameters)) {
                        printNoteOnce(processingEnv, "@AfterTemplate defines arguments that are not present in all @BeforeTemplate methods", classDecl.sym);
                        return null;
                    }
                }
            }
        }
        return this;
    }

    private boolean resolveAll(JavacProcessingEnvironment processingEnv, JCTree.JCCompilationUnit cu) {
        try {
            Context context = processingEnv.getContext();
            JavacResolution javacResolution = new JavacResolution(context);
            for (TemplateDescriptor beforeTemplate : beforeTemplates) {
                JCTree.JCMethodDecl resolvedMethod = (JCTree.JCMethodDecl) javacResolution.resolveAll(context, cu, singletonList(beforeTemplate.method)).get(beforeTemplate.method);
                resolvedMethod.params = beforeTemplate.method.params;
                beforeTemplate.method = resolvedMethod;
                if (!beforeTemplate.validate()) {
                    return false;
                }
            }
            if (afterTemplate != null) {
                JCTree.JCMethodDecl resolvedMethod = (JCTree.JCMethodDecl) javacResolution.resolveAll(context, cu, singletonList(afterTemplate.method)).get(afterTemplate.method);
                resolvedMethod.params = afterTemplate.method.params;
                afterTemplate.method = resolvedMethod;
                if (!afterTemplate.validate()) {
                    return false;
                }
            }
        } catch (Throwable t) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Had trouble type attributing the template.");
            return false;
        }
        return true;
    }
}
