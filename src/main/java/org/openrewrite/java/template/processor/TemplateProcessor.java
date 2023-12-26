/*
 * Copyright 2022 the original author or authors.
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

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import org.openrewrite.java.template.internal.JavacResolution;
import org.openrewrite.java.template.internal.TemplateCode;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * For steps to debug this annotation processor, see
 * <a href="https://medium.com/@joachim.beckers/debugging-an-annotation-processor-using-intellij-idea-in-2018-cde72758b78a">this blog post</a>.
 */
@SupportedAnnotationTypes("*")
public class TemplateProcessor extends TypeAwareProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            JCCompilationUnit jcCompilationUnit = toUnit(element);
            if (jcCompilationUnit != null) {
                maybeGenerateTemplateSources(jcCompilationUnit);
            }
        }

        return true;
    }

    void maybeGenerateTemplateSources(JCCompilationUnit cu) {
        Context context = javacProcessingEnv.getContext();
        JavacResolution res = new JavacResolution(context);

        new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation tree) {
                JCTree.JCExpression jcSelect = tree.getMethodSelect();
                String name = jcSelect instanceof JCTree.JCFieldAccess ?
                        ((JCTree.JCFieldAccess) jcSelect).name.toString() :
                        ((JCTree.JCIdent) jcSelect).getName().toString();

                if (("expression".equals(name) || "statement".equals(name)) && tree.getArguments().size() == 3) {
                    JCTree.JCMethodInvocation resolvedMethod;
                    Map<JCTree, JCTree> resolved;
                    try {
                        resolved = res.resolveAll(context, cu, singletonList(tree));
                        resolvedMethod = (JCTree.JCMethodInvocation) resolved.get(tree);
                    } catch (Throwable t) {
                        processingEnv.getMessager().printMessage(Kind.WARNING, "Had trouble type attributing the template.");
                        return;
                    }

                    JCTree.JCExpression arg2 = tree.getArguments().get(2);
                    if (isOfClassType(resolvedMethod.type, "org.openrewrite.java.JavaTemplate.Builder") &&
                        (arg2 instanceof JCTree.JCLambda || arg2 instanceof JCTree.JCTypeCast && ((JCTree.JCTypeCast) arg2).getExpression() instanceof JCTree.JCLambda)) {

                        JCTree.JCLambda template = arg2 instanceof JCTree.JCLambda ? (JCTree.JCLambda) arg2 : (JCTree.JCLambda) ((JCTree.JCTypeCast) arg2).getExpression();

                        List<JCTree.JCVariableDecl> parameters;
                        if (template.getParameters().isEmpty()) {
                            parameters = emptyList();
                        } else {
                            Map<JCTree, JCTree> parameterResolution = res.resolveAll(context, cu, template.getParameters());
                            parameters = new ArrayList<>(template.getParameters().size());
                            for (VariableTree p : template.getParameters()) {
                                parameters.add((JCTree.JCVariableDecl) parameterResolution.get((JCTree) p));
                            }
                        }

                        try {
                            JCTree.JCLiteral templateName = (JCTree.JCLiteral) tree.getArguments().get(1);
                            if (templateName.value == null) {
                                processingEnv.getMessager().printMessage(Kind.WARNING, "Can't compile a template with a null name.");
                                return;
                            }

                            // this could be a visitor in the case that the visitor is in its own file or
                            // named inner class, or a recipe if the visitor is defined in an anonymous class
                            JCTree.JCClassDecl classDecl = cursor(cu, template).stream()
                                    .filter(JCTree.JCClassDecl.class::isInstance)
                                    .map(JCTree.JCClassDecl.class::cast)
                                    .reduce((next, acc) -> next)
                                    .orElseThrow(() -> new IllegalStateException("Expected to find an enclosing class"));

                            String templateFqn;

                            if (isOfClassType(classDecl.type, "org.openrewrite.java.JavaVisitor")) {
                                templateFqn = classDecl.sym.fullname.toString() + "_" + templateName.getValue().toString();
                            } else {
                                JCTree.JCNewClass visitorClass = cursor(cu, template).stream()
                                        .filter(JCTree.JCNewClass.class::isInstance)
                                        .map(JCTree.JCNewClass.class::cast)
                                        .reduce((next, acc) -> next)
                                        .orElse(null);

                                JCTree.JCNewClass resolvedVisitorClass = (JCTree.JCNewClass) resolved.get(visitorClass);

                                if (resolvedVisitorClass != null && isOfClassType(resolvedVisitorClass.clazz.type, "org.openrewrite.java.JavaVisitor")) {
                                    templateFqn = ((Symbol.ClassSymbol) resolvedVisitorClass.type.tsym).flatname.toString() + "_" +
                                                  templateName.getValue().toString();
                                } else {
                                    processingEnv.getMessager().printMessage(Kind.WARNING, "Can't compile a template outside of a visitor or recipe.");
                                    return;
                                }
                            }

                            String templateCode = TemplateCode.process(resolved.get(template.getBody()), parameters);

                            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(templateFqn);
                            try (Writer out = new BufferedWriter(builderFile.openWriter())) {
                                out.write("package " + classDecl.sym.packge().toString() + ";\n");
                                out.write("import org.openrewrite.java.*;\n");

                                for (JCTree.JCVariableDecl parameter : parameters) {
                                    if (parameter.type.tsym instanceof Symbol.ClassSymbol) {
                                        String paramType = parameter.type.tsym.getQualifiedName().toString();
                                        if (!paramType.startsWith("java.lang")) {
                                            out.write("import " + paramType + ";\n");
                                        }
                                    }
                                }

                                out.write("\n");
                                out.write("/**\n * OpenRewrite `" + templateName.getValue() + "` template created for {@code " + templateFqn.split("_")[0] + "}.\n */\n");
                                String templateClassName = templateFqn.substring(templateFqn.lastIndexOf('.') + 1);
                                out.write("@SuppressWarnings(\"all\")\n");
                                out.write("public class " + templateClassName + " {\n");
                                out.write("    /**\n");
                                out.write("     * Instantiates a new instance.\n");
                                out.write("     */\n");
                                out.write("    public " + templateClassName + "() {}\n\n");
                                out.write("    /**\n");
                                out.write("     * Get the {@code JavaTemplate.Builder} to match or replace.\n");
                                out.write("     * @return the JavaTemplate builder.\n");
                                out.write("     */\n");
                                out.write("    public static JavaTemplate.Builder getTemplate() {\n");
                                out.write("        return " + indent(templateCode, 12) + ";\n");
                                out.write("    }\n");
                                out.write("}\n");
                                out.flush();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                super.visitApply(tree);
            }

            private String indent(String code, int width) {
                char[] indent = new char[width];
                Arrays.fill(indent, ' ');
                String replacement = "$1" + new String(indent);
                return code.replaceAll("(?m)(\\R)", replacement);
            }
        }.scan(cu);
    }

    private boolean isOfClassType(Type type, String fqn) {
        return type instanceof Type.ClassType && (((Symbol.ClassSymbol) type.tsym)
                                                          .fullname.contentEquals(fqn) || isOfClassType(((Type.ClassType) type).supertype_field, fqn));
    }

    private Stack<Tree> cursor(JCCompilationUnit cu, Tree t) {
        AtomicReference<Stack<Tree>> matching = new AtomicReference<>();
        new TreePathScanner<Stack<Tree>, Stack<Tree>>() {
            @Override
            public Stack<Tree> scan(Tree tree, Stack<Tree> parent) {
                Stack<Tree> cursor = new Stack<>();
                cursor.addAll(parent);
                cursor.push(tree);
                if (tree == t) {
                    matching.set(cursor);
                    return cursor;
                }
                return super.scan(tree, cursor);
            }
        }.scan(cu, new Stack<>());
        return matching.get();
    }
}
