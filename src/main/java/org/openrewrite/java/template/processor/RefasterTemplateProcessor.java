/*
 * Copyright 2023 the original author or authors.
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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Name;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.openrewrite.java.template.processor.RefasterTemplateProcessor.AFTER_TEMPLATE;
import static org.openrewrite.java.template.processor.RefasterTemplateProcessor.BEFORE_TEMPLATE;

/**
 * For steps to debug this annotation processor, see
 * <a href="https://medium.com/@joachim.beckers/debugging-an-annotation-processor-using-intellij-idea-in-2018-cde72758b78a">this blog post</a>.
 */
@SupportedAnnotationTypes({BEFORE_TEMPLATE, AFTER_TEMPLATE})
public class RefasterTemplateProcessor extends TypeAwareProcessor {

    static final String BEFORE_TEMPLATE = "com.google.errorprone.refaster.annotation.BeforeTemplate";
    static final String AFTER_TEMPLATE = "com.google.errorprone.refaster.annotation.AfterTemplate";
    static Set<String> UNSUPPORTED_ANNOTATIONS = Stream.of(
            "com.google.errorprone.refaster.annotation.AllowCodeBetweenLines",
            "com.google.errorprone.refaster.annotation.Matches",
            "com.google.errorprone.refaster.annotation.MayOptionallyUse",
            "com.google.errorprone.refaster.annotation.NoAutoboxing",
            "com.google.errorprone.refaster.annotation.NotMatches",
            "com.google.errorprone.refaster.annotation.OfKind",
            "com.google.errorprone.refaster.annotation.Placeholder",
            "com.google.errorprone.refaster.annotation.Repeated"
    ).collect(toSet());

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            JCCompilationUnit cu = toUnit(element);
            if (cu != null) {
                RecipeWriter recipeWriter = new RecipeWriter(javacProcessingEnv, cu);
                new TreeScanner() {
                    @Override
                    public void visitClassDef(JCTree.JCClassDecl classDecl) {
                        super.visitClassDef(classDecl);
                        RuleDescriptor descriptor = RuleDescriptor.create(javacProcessingEnv, cu, classDecl);
                        recipeWriter.writeRecipeForClassDeclaration(classDecl, descriptor);
                    }
                }.scan(cu);
            }
        }

        // Inform how many rules were skipped and why; useful for debugging, but not enabled by default
        //printedMessages.entrySet().stream().sorted(Map.Entry.comparingByValue())
        //        .forEach(entry -> processingEnv.getMessager().printMessage(Kind.NOTE, entry.toString()));

        // Give other annotation processors a chance to process the same annotations, for dual use of Refaster templates
        return false;
    }

    public static Map<Name, Integer> findParameterOrder(JCTree.JCMethodDecl method, int arity) {
        AtomicInteger parameterOccurrence = new AtomicInteger();
        Map<Name, Integer> parameterOrder = new HashMap<>();
        new TreeScanner() {
            @Override
            public void scan(JCTree jcTree) {
                if (jcTree instanceof JCTree.JCIdent) {
                    JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcTree;
                    if (jcIdent.sym instanceof Symbol.VarSymbol &&
                            jcIdent.sym.owner instanceof Symbol.MethodSymbol &&
                            ((Symbol.MethodSymbol) jcIdent.sym.owner).params.contains(jcIdent.sym) &&
                            !parameterOrder.containsKey(jcIdent.sym.name)) {
                        parameterOrder.put(jcIdent.sym.name, parameterOccurrence.getAndIncrement());
                    }
                } else if (jcTree instanceof JCTree.JCMethodInvocation) {
                    JCTree.JCMethodInvocation jcMethodInvocation = (JCTree.JCMethodInvocation) jcTree;
                    if (isAnyOfCall(jcMethodInvocation)) {
                        super.scan(jcMethodInvocation.getArguments().get(arity));
                        return;
                    }
                }
                super.scan(jcTree);
            }
        }.scan(method);
        return parameterOrder;
    }

    public static JCTree.@Nullable JCExpression getReturnExpression(JCTree.JCMethodDecl method) {
        JCTree.JCStatement statement = method.getBody().getStatements().last();
        if (statement instanceof JCTree.JCReturn) {
            return ((JCTree.JCReturn) statement).expr;
        }
        if (statement instanceof JCTree.JCExpressionStatement) {
            return ((JCTree.JCExpressionStatement) statement).expr;
        }
        return null;
    }

    public static boolean isAnyOfCall(JCTree.JCMethodInvocation call) {
        JCTree.JCExpression meth = call.meth;
        if (meth instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) meth;
            return "anyOf".equals(fieldAccess.name.toString()) &&
                    "Refaster".equals(((JCTree.JCIdent) fieldAccess.selected).name.toString());
        }
        return false;
    }

    private static final Map<String, Integer> printedMessages = new TreeMap<>();

    /**
     * @param processingEnv The processing environment to use for printing messages
     * @param message       The message to print
     * @param symbol        The symbol to attach the message to; printed as clickable link to file
     */
    public static void printNoteOnce(ProcessingEnvironment processingEnv, String message, Symbol.ClassSymbol symbol) {
        if (printedMessages.compute(message, (k, v) -> v == null ? 1 : v + 1) == 1) {
            processingEnv.getMessager().printMessage(Kind.NOTE, message, symbol);
        }
    }

    public static List<JCTree.JCAnnotation> getMethodTreeAnnotations(MethodTree method, Predicate<String> typePredicate) {
        List<JCTree.JCAnnotation> result = new ArrayList<>();
        for (AnnotationTree annotation : method.getModifiers().getAnnotations()) {
            Tree type = annotation.getAnnotationType();
            if (type.getKind() == Tree.Kind.IDENTIFIER && ((JCTree.JCIdent) type).sym != null &&
                    typePredicate.test(((JCTree.JCIdent) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            } else if (type.getKind() == Tree.Kind.IDENTIFIER && ((JCTree.JCAnnotation) annotation).attribute != null &&
                    ((JCTree.JCAnnotation) annotation).attribute.type instanceof Type.ClassType &&
                    ((JCTree.JCAnnotation) annotation).attribute.type.tsym != null &&
                    typePredicate.test(((JCTree.JCAnnotation) annotation).attribute.type.tsym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            } else if (type.getKind() == Tree.Kind.MEMBER_SELECT && type instanceof JCTree.JCFieldAccess &&
                    ((JCTree.JCFieldAccess) type).sym != null &&
                    typePredicate.test(((JCTree.JCFieldAccess) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            }
        }
        return result;
    }
}
