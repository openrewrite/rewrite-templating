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

import com.sun.source.tree.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import org.jetbrains.annotations.Nullable;
import org.openrewrite.java.template.internal.FQNPretty;
import org.openrewrite.java.template.internal.ImportDetector;
import org.openrewrite.java.template.internal.JavacResolution;
import org.openrewrite.java.template.internal.UsedMethodDetector;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
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
            "com.google.errorprone.refaster.annotation.AlsoNegation",
            "com.google.errorprone.refaster.annotation.AllowCodeBetweenLines",
            "com.google.errorprone.refaster.annotation.Matches",
            "com.google.errorprone.refaster.annotation.MayOptionallyUse",
            "com.google.errorprone.refaster.annotation.NoAutoboxing",
            "com.google.errorprone.refaster.annotation.NotMatches",
            "com.google.errorprone.refaster.annotation.OfKind",
            "com.google.errorprone.refaster.annotation.Placeholder",
            "com.google.errorprone.refaster.annotation.Repeated",
            "com.google.errorprone.refaster.annotation.UseImportPolicy",
            "com.google.errorprone.annotations.DoNotCall"
    ).collect(Collectors.toSet());

    static ClassValue<List<String>> LST_TYPE_MAP = new ClassValue<List<String>>() {
        @Override
        protected List<String> computeValue(Class<?> type) {
            if (JCTree.JCUnary.class.isAssignableFrom(type)) {
                return singletonList("J.Unary");
            } else if (JCTree.JCBinary.class.isAssignableFrom(type)) {
                return singletonList("J.Binary");
            } else if (JCTree.JCMethodInvocation.class.isAssignableFrom(type)) {
                return singletonList("J.MethodInvocation");
            } else if (JCTree.JCFieldAccess.class.isAssignableFrom(type)) {
                return Arrays.asList("J.FieldAccess", "J.Identifier");
            } else if (JCTree.JCExpression.class.isAssignableFrom(type)) {
                // catch all for expressions
                return singletonList("Expression");
            } else if (JCTree.JCStatement.class.isAssignableFrom(type)) {
                // catch all for statements
                return singletonList("Statement");
            }
            throw new IllegalArgumentException(type.toString());
        }
    };

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

        new TreeScanner() {
            final Map<JCTree.JCMethodDecl, Set<String>> imports = new HashMap<>();
            final Map<JCTree.JCMethodDecl, Set<String>> staticImports = new HashMap<>();
            final Map<String, String> recipes = new LinkedHashMap<>();

            @Override
            public void visitClassDef(JCTree.JCClassDecl classDecl) {
                super.visitClassDef(classDecl);

                TemplateDescriptor descriptor = getTemplateDescriptor(classDecl, context, cu);
                if (descriptor != null) {

                    TreeMaker treeMaker = TreeMaker.instance(context).forToplevel(cu);
                    List<JCTree> membersWithoutConstructor = classDecl.getMembers().stream()
                            .filter(m -> !(m instanceof JCTree.JCMethodDecl) || !((JCTree.JCMethodDecl) m).name.contentEquals("<init>"))
                            .collect(Collectors.toList());
                    JCTree.JCClassDecl copy = treeMaker.ClassDef(classDecl.mods, classDecl.name, classDecl.typarams, classDecl.extending, classDecl.implementing, com.sun.tools.javac.util.List.from(membersWithoutConstructor));

                    processingEnv.getMessager().printMessage(Kind.NOTE, "Generating template for " + descriptor.classDecl.getSimpleName());

                    String templateName = classDecl.sym.fullname.toString().substring(classDecl.sym.packge().fullname.length() + 1);
                    String templateFqn = classDecl.sym.fullname.toString() + "Recipe";
                    String templateCode = copy.toString().trim();
                    String displayName = cu.docComments.getComment(classDecl) != null ? cu.docComments.getComment(classDecl).getText().trim() : "Refaster template `" + templateName + '`';
                    if (displayName.endsWith(".")) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }

                    for (JCTree.JCMethodDecl template : descriptor.beforeTemplates) {
                        for (Symbol anImport : ImportDetector.imports(template)) {
                            if (anImport instanceof Symbol.ClassSymbol) {
                                imports.computeIfAbsent(template, k -> new TreeSet<>())
                                        .add(anImport.getQualifiedName().toString().replace('$', '.'));
                            } else if (anImport instanceof Symbol.VarSymbol || anImport instanceof Symbol.MethodSymbol) {
                                staticImports.computeIfAbsent(template, k -> new TreeSet<>())
                                        .add(anImport.owner.getQualifiedName().toString().replace('$', '.') + '.' + anImport.flatName().toString());
                            } else {
                                throw new AssertionError(anImport.getClass());
                            }
                        }
                    }
                    for (Symbol anImport : ImportDetector.imports(descriptor.afterTemplate)) {
                        if (anImport instanceof Symbol.ClassSymbol) {
                            imports.computeIfAbsent(descriptor.afterTemplate, k -> new TreeSet<>())
                                    .add(anImport.getQualifiedName().toString().replace('$', '.'));
                        } else if (anImport instanceof Symbol.VarSymbol || anImport instanceof Symbol.MethodSymbol) {
                            staticImports.computeIfAbsent(descriptor.afterTemplate, k -> new TreeSet<>())
                                    .add(anImport.owner.getQualifiedName().toString().replace('$', '.') + '.' + anImport.flatName().toString());
                        } else {
                            throw new AssertionError(anImport.getClass());
                        }
                    }

                    for (Set<String> imports : imports.values()) {
                        imports.removeIf(i -> "java.lang".equals(i.substring(0, i.lastIndexOf('.'))));
                        imports.remove(BEFORE_TEMPLATE);
                        imports.remove(AFTER_TEMPLATE);
                    }

                    Map<String, JCTree.JCMethodDecl> befores = new LinkedHashMap<>();
                    for (JCTree.JCMethodDecl templ : descriptor.beforeTemplates) {
                        String name = templ.getName().toString();
                        if (befores.containsKey(name)) {
                            String base = name;
                            for (int i = 0; ; i++) {
                                name = base + i;
                                if (!befores.containsKey(name)) {
                                    break;
                                }
                            }
                        }
                        befores.put(name, templ);
                    }
                    String after = descriptor.afterTemplate.getName().toString();

                    StringBuilder recipe = new StringBuilder();
                    String recipeName = templateFqn.substring(templateFqn.lastIndexOf('.') + 1);
                    String modifiers = classDecl.getModifiers().getFlags().stream().map(Modifier::toString).collect(Collectors.joining(" "));
                    if (!modifiers.isEmpty()) {
                        modifiers += " ";
                    }
                    recipe.append("@NonNullApi\n");
                    recipe.append(modifiers).append("class ").append(recipeName).append(" extends Recipe {\n");
                    recipe.append("\n");
                    recipe.append("    @Override\n");
                    recipe.append("    public String getDisplayName() {\n");
                    recipe.append("        return \"").append(escape(displayName)).append("\";\n");
                    recipe.append("    }\n");
                    recipe.append("\n");
                    recipe.append("    @Override\n");
                    recipe.append("    public String getDescription() {\n");
                    recipe.append("        return \"Recipe created for the following Refaster template:\\n```java\\n").append(escape(templateCode)).append("\\n```\\n.\";\n");
                    recipe.append("    }\n");
                    recipe.append("\n");
                    recipe.append("    @Override\n");
                    recipe.append("    public TreeVisitor<?, ExecutionContext> getVisitor() {\n");
                    recipe.append("        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {\n");
                    for (Map.Entry<String, JCTree.JCMethodDecl> entry : befores.entrySet()) {
                        recipe.append("            final Supplier<JavaTemplate> ")
                                .append(entry.getKey())
                                .append(" = memoize(() -> Semantics.")
                                .append(statementType(entry.getValue()))
                                .append("(this, \"")
                                .append(entry.getKey()).append("\", ")
                                .append(toLambda(entry.getValue()))
                                .append(").build());\n");
                    }
                    recipe.append("            final Supplier<JavaTemplate> ")
                            .append(after)
                            .append(" = memoize(() -> Semantics.")
                            .append(statementType(descriptor.afterTemplate))
                            .append("(this, \"")
                            .append(after)
                            .append("\", ")
                            .append(toLambda(descriptor.afterTemplate))
                            .append(").build());\n");
                    recipe.append("\n");

                    List<String> lstTypes = LST_TYPE_MAP.get(getType(descriptor.beforeTemplates.get(0)));
                    String parameters = parameters(descriptor);
                    for (String lstType : lstTypes) {
                        String methodSuffix = lstType.startsWith("J.") ? lstType.substring(2) : lstType;
                        recipe.append("            @Override\n");
                        recipe.append("            public J visit").append(methodSuffix).append("(").append(lstType).append(" elem, ExecutionContext ctx) {\n");
                        if (lstType.equals("Statement")) {
                            recipe.append("                if (elem instanceof J.Block) {;\n");
                            recipe.append("                    // FIXME workaround\n");
                            recipe.append("                    return elem;\n");
                            recipe.append("                }\n");
                        }

                        recipe.append("                JavaTemplate.Matcher matcher;\n");
                        for (Map.Entry<String, JCTree.JCMethodDecl> entry : befores.entrySet()) {
                            recipe.append("                if (" + "(matcher = matcher(").append(entry.getKey()).append(", getCursor())).find()").append(") {\n");
                            com.sun.tools.javac.util.List<JCTree.JCVariableDecl> jcVariableDecls = entry.getValue().getParameters();
                            for (int i = 0; i < jcVariableDecls.size(); i++) {
                                JCTree.JCVariableDecl param = jcVariableDecls.get(i);
                                com.sun.tools.javac.util.List<JCTree.JCAnnotation> annotations = param.getModifiers().getAnnotations();
                                for (JCTree.JCAnnotation jcAnnotation : annotations) {
                                    String annotationType = jcAnnotation.attribute.type.tsym.getQualifiedName().toString();
                                    if (annotationType.equals("org.openrewrite.java.template.NotMatches")) {
                                        String matcher = ((Type.ClassType) jcAnnotation.attribute.getValue().values.get(0).snd.getValue()).tsym.getQualifiedName().toString();
                                        recipe.append("                    if (new ").append(matcher).append("().matches((Expression) matcher.parameter(").append(i).append("))) {\n");
                                        recipe.append("                        return super.visit").append(methodSuffix).append("(elem, ctx);\n");
                                        recipe.append("                    }\n");
                                    } else if (annotationType.equals("org.openrewrite.java.template.Matches")) {
                                        String matcher = ((Type.ClassType) jcAnnotation.attribute.getValue().values.get(0).snd.getValue()).tsym.getQualifiedName().toString();
                                        recipe.append("                    if (!new ").append(matcher).append("().matches((Expression) matcher.parameter(").append(i).append("))) {\n");
                                        recipe.append("                        return super.visit").append(methodSuffix).append("(elem, ctx);\n");
                                        recipe.append("                    }\n");
                                    }
                                }
                            }
                            Set<String> beforeImports = imports.entrySet().stream()
                                    .filter(e -> entry.getValue().equals(e.getKey()))
                                    .map(Map.Entry::getValue)
                                    .flatMap(Set::stream)
                                    .collect(Collectors.toSet());
                            Set<String> afterImports = imports.entrySet().stream()
                                    .filter(e -> descriptor.afterTemplate == e.getKey())
                                    .map(Map.Entry::getValue)
                                    .flatMap(Set::stream)
                                    .collect(Collectors.toSet());
                            maybeRemoveImport(imports, beforeImports, afterImports, recipe);
                            beforeImports = staticImports.entrySet().stream()
                                    .filter(e -> entry.getValue().equals(e.getKey()))
                                    .map(Map.Entry::getValue)
                                    .flatMap(Set::stream)
                                    .collect(Collectors.toSet());
                            afterImports = staticImports.entrySet().stream()
                                    .filter(e -> descriptor.afterTemplate == e.getKey())
                                    .map(Map.Entry::getValue)
                                    .flatMap(Set::stream)
                                    .collect(Collectors.toSet());
                            maybeRemoveImport(staticImports, beforeImports, afterImports, recipe);
                            if (parameters.isEmpty()) {
                                recipe.append("                    return embed(apply(").append(after).append(", getCursor(), elem.getCoordinates().replace()), getCursor(), ctx);\n");
                            } else {
                                recipe.append("                    return embed(\n");
                                recipe.append("                            apply(").append(after).append(", getCursor(), elem.getCoordinates().replace(), ").append(parameters).append("),\n");
                                recipe.append("                            getCursor(),\n");
                                recipe.append("                            ctx\n");
                                recipe.append("                    );\n");
                            }
                            recipe.append("                }\n");
                        }
                        recipe.append("                return super.visit").append(methodSuffix).append("(elem, ctx);\n");
                        recipe.append("            }\n");
                        recipe.append("\n");
                    }
                    recipe.append("        };\n");

                    String preconditions = generatePreconditions(descriptor.beforeTemplates, imports, 16);
                    if (preconditions == null) {
                        recipe.append("        return javaVisitor;\n");
                    } else {
                        recipe.append("        return Preconditions.check(\n");
                        recipe.append("                ").append(preconditions).append(",\n");
                        recipe.append("                javaVisitor\n");
                        recipe.append("        );\n");
                    }
                    recipe.append("    }\n");
                    recipe.append("}\n");
                    recipes.put(recipeName, recipe.toString());
                }

                if (classDecl.sym != null && classDecl.sym.getNestingKind() == NestingKind.TOP_LEVEL && !recipes.isEmpty()) {
                    boolean outerClassRequired = descriptor == null;
                    try {
                        String inputOuterFQN = outerClassRequired ? classDecl.sym.fullname.toString() : descriptor.classDecl.sym.fullname.toString();
                        String className = inputOuterFQN + (outerClassRequired ? "Recipes" : "Recipe");
                        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className);
                        try (Writer out = new BufferedWriter(builderFile.openWriter())) {
                            out.write("package " + classDecl.sym.packge().toString() + ";\n");
                            out.write("\n");
                            out.write("import org.openrewrite.ExecutionContext;\n");
                            out.write("import org.openrewrite.Preconditions;\n");
                            out.write("import org.openrewrite.Recipe;\n");
                            out.write("import org.openrewrite.TreeVisitor;\n");
                            out.write("import org.openrewrite.internal.lang.NonNullApi;\n");
                            out.write("import org.openrewrite.java.JavaTemplate;\n");
                            out.write("import org.openrewrite.java.JavaVisitor;\n");
                            out.write("import org.openrewrite.java.search.*;\n");
                            out.write("import org.openrewrite.java.template.Primitive;\n");
                            out.write("import org.openrewrite.java.template.Semantics;\n");
                            out.write("import org.openrewrite.java.template.function.*;\n");
                            out.write("import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;\n");
                            out.write("import org.openrewrite.java.tree.*;\n");
                            out.write("\n");
                            out.write("import java.util.function.Supplier;\n");
                            if (outerClassRequired) {
                                out.write("\n");
                                out.write("import java.util.Arrays;\n");
                                out.write("import java.util.List;\n");
                            }

                            out.write("\n");

                            if (!imports.isEmpty()) {
                                for (String anImport : imports.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
                                    out.write("import " + anImport + ";\n");
                                }
                                out.write("\n");
                            }
                            if (!staticImports.isEmpty()) {
                                for (String anImport : staticImports.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
                                    out.write("import static " + anImport + ";\n");
                                }
                                out.write("\n");
                            }

                            if (outerClassRequired) {
                                String outerClassName = className.substring(className.lastIndexOf('.') + 1);
                                out.write("public final class " + outerClassName + " extends Recipe {\n");

                                String simpleInputOuterFQN = inputOuterFQN.substring(inputOuterFQN.lastIndexOf('.') + 1);
                                out.write("\n" +
                                          "    @Override\n" +
                                          "    public String getDisplayName() {\n" +
                                          "        return \"`" + simpleInputOuterFQN + "` Refaster recipes\";\n" +
                                          "    }\n" +
                                          "\n" +
                                          "    @Override\n" +
                                          "    public String getDescription() {\n" +
                                          "        return \"Refaster template recipes for `" + inputOuterFQN + "`.\";\n" +
                                          "    }\n" +
                                          "\n");
                                String recipesAsList = recipes.keySet().stream()
                                        .map(r -> "                new " + r.substring(r.lastIndexOf('.') + 1) + "()")
                                        .collect(Collectors.joining(",\n"));
                                out.write(
                                        "    @Override\n" +
                                        "    public List<Recipe> getRecipeList() {\n" +
                                        "        return Arrays.asList(\n" +
                                        recipesAsList + '\n' +
                                        "        );\n" +
                                        "    }\n\n");

                                for (String r : recipes.values()) {
                                    out.write(r.replaceAll("(?m)^(.+)$", "    $1"));
                                    out.write('\n');
                                }
                                out.write("}\n");
                            } else {
                                for (String r : recipes.values()) {
                                    out.write(r);
                                    out.write('\n');
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            private void maybeRemoveImport(Map<JCTree.JCMethodDecl, Set<String>> imports, Set<String> beforeImports, Set<String> afterImports, StringBuilder recipe) {
                for (String anImport : imports.values().stream().flatMap(Set::stream).collect(Collectors.toSet())) {
                    if (anImport.startsWith("java.lang.")) {
                        continue;
                    }
                    //noinspection StatementWithEmptyBody
                    if (beforeImports.contains(anImport) && afterImports.contains(anImport)) {
                        // do nothing
                    } else if (beforeImports.contains(anImport)) {
                        recipe.append("                    maybeRemoveImport(\"").append(anImport).append("\");\n");
                    }
                }
            }

            /* Generate the minimal precondition that would allow to match each before template individually. */
            @SuppressWarnings("SameParameterValue")
            @Nullable
            private String generatePreconditions(List<JCTree.JCMethodDecl> beforeTemplates,
                                                 Map<JCTree.JCMethodDecl, Set<String>> imports,
                                                 int indent) {
                Map<JCTree.JCMethodDecl, Set<String>> preconditions = new LinkedHashMap<>();
                for (JCTree.JCMethodDecl beforeTemplate : beforeTemplates) {
                    Set<String> usesVisitors = new LinkedHashSet<>();

                    Set<String> localImports = imports.getOrDefault(beforeTemplate, Collections.emptySet());
                    for (String anImport : localImports) {
                        usesVisitors.add("new UsesType<>(\"" + anImport + "\", true)");
                    }
                    List<Symbol.MethodSymbol> usedMethods = UsedMethodDetector.usedMethods(beforeTemplate);
                    for (Symbol.MethodSymbol method : usedMethods) {
                        usesVisitors.add("new UsesMethod<>(\"" + method.owner.getQualifiedName().toString() + ' ' + method.name.toString() + "(..)\")");
                    }

                    preconditions.put(beforeTemplate, usesVisitors);
                }

                if (preconditions.size() == 1) {
                    return joinPreconditions(preconditions.values().iterator().next(), "and", indent + 4);
                } else if (preconditions.size() > 1) {
                    Set<String> common = new LinkedHashSet<>();
                    for (String dep : preconditions.values().iterator().next()) {
                        if (preconditions.values().stream().allMatch(v -> v.contains(dep))) {
                            common.add(dep);
                        }
                    }
                    common.forEach(dep -> preconditions.values().forEach(v -> v.remove(dep)));
                    preconditions.values().removeIf(Collection::isEmpty);

                    if (common.isEmpty()) {
                        return joinPreconditions(preconditions.values().stream().map(v -> joinPreconditions(v, "and", indent + 4)).collect(Collectors.toList()), "or", indent + 4);
                    } else {
                        if (!preconditions.isEmpty()) {
                            String uniqueConditions = joinPreconditions(preconditions.values().stream().map(v -> joinPreconditions(v, "and", indent + 12)).collect(Collectors.toList()), "or", indent + 8);
                            common.add(uniqueConditions);
                        }
                        return joinPreconditions(common, "and", indent + 4);
                    }
                }
                return null;
            }

            private String joinPreconditions(Collection<String> preconditions, String op, int indent) {
                if (preconditions.isEmpty()) {
                    return null;
                } else if (preconditions.size() == 1) {
                    return preconditions.iterator().next();
                }
                char[] indentChars = new char[indent];
                Arrays.fill(indentChars, ' ');
                String indentStr = new String(indentChars);
                return "Preconditions." + op + "(\n" + indentStr + String.join(",\n" + indentStr, preconditions) + "\n" + indentStr.substring(0, indent - 4) + ')';
            }
        }.scan(cu);
    }

    private String escape(String string) {
        return string.replace("\"", "\\\"").replaceAll("\\R", "\\\\n");
    }

    private String parameters(TemplateDescriptor descriptor) {
        List<Integer> afterParams = new ArrayList<>();
        new TreeScanner() {
            @Override
            public void scan(JCTree jcTree) {
                if (jcTree instanceof JCTree.JCIdent) {
                    JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcTree;
                    if (jcIdent.sym instanceof Symbol.VarSymbol
                        && jcIdent.sym.owner instanceof Symbol.MethodSymbol
                        && ((Symbol.MethodSymbol) jcIdent.sym.owner).params.contains(jcIdent.sym)) {
                        afterParams.add(((Symbol.MethodSymbol) jcIdent.sym.owner).params.indexOf(jcIdent.sym));
                    }
                }
                super.scan(jcTree);
            }
        }.scan(descriptor.afterTemplate.body);

        StringJoiner joiner = new StringJoiner(", ");
        for (Integer param : afterParams) {
            joiner.add("matcher.parameter(" + param + ")");
        }
        return joiner.toString();
    }

    private Class<? extends JCTree> getType(JCTree.JCMethodDecl method) {
        JCTree.JCStatement statement = method.getBody().getStatements().get(0);
        Class<? extends JCTree> type = statement.getClass();
        if (statement instanceof JCTree.JCReturn) {
            type = ((JCTree.JCReturn) statement).expr.getClass();
        } else if (statement instanceof JCTree.JCExpressionStatement) {
            type = ((JCTree.JCExpressionStatement) statement).expr.getClass();
        }
        return type;
    }

    private String statementType(JCTree.JCMethodDecl method) {
        // for now excluding assignment expressions and prefix and postfix -- and ++
        Set<Class<? extends JCTree>> expressionStatementTypes = Stream.of(
                JCTree.JCMethodInvocation.class,
                JCTree.JCNewClass.class).collect(Collectors.toSet());

        Class<? extends JCTree> type = getType(method);
        if (expressionStatementTypes.contains(type)) {
            if (type == JCTree.JCMethodInvocation.class
                && method.getBody().getStatements().last() instanceof JCTree.JCExpressionStatement
                && !(method.getReturnType().type instanceof Type.JCVoidType)) {
                return "expression";
            }
            if (method.restype.type instanceof Type.JCVoidType || !JCTree.JCExpression.class.isAssignableFrom(type)) {
                return "statement";
            }
        }
        return "expression";
    }

    private String toLambda(JCTree.JCMethodDecl method) {
        StringBuilder builder = new StringBuilder();

        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        for (JCTree.JCVariableDecl parameter : method.getParameters()) {
            String paramType = parameter.getType().type.tsym.getQualifiedName().toString();

            switch (paramType) {
                case "boolean":
                    paramType = "@Primitive Boolean";
                    break;
                case "byte":
                    paramType = "@Primitive Byte";
                    break;
                case "char":
                    paramType = "@Primitive Character";
                    break;
                case "double":
                    paramType = "@Primitive Double";
                    break;
                case "float":
                    paramType = "@Primitive Float";
                    break;
                case "int":
                    paramType = "@Primitive Integer";
                    break;
                case "long":
                    paramType = "@Primitive Long";
                    break;
                case "short":
                    paramType = "@Primitive Short";
                    break;
                case "void":
                    paramType = "@Primitive Void";
                    break;
            }

            if (paramType.startsWith("java.lang.")) {
                paramType = paramType.substring("java.lang.".length());
            }
            joiner.add(paramType + " " + parameter.getName());
        }
        builder.append(joiner);
        builder.append(" -> ");

        JCTree.JCStatement statement = method.getBody().getStatements().get(0);
        if (statement instanceof JCTree.JCReturn) {
            builder.append(FQNPretty.toString(((JCTree.JCReturn) statement).getExpression()));
        } else if (statement instanceof JCTree.JCThrow) {
            String string = FQNPretty.toString(statement);
            builder.append("{ ").append(string).append(" }");
        } else {
            String string = FQNPretty.toString(statement);
            builder.append(string);
        }
        return builder.toString();
    }

    @Nullable
    private TemplateDescriptor getTemplateDescriptor(JCTree.JCClassDecl tree, Context context, JCCompilationUnit cu) {
        TemplateDescriptor result = new TemplateDescriptor(tree);
        for (JCTree member : tree.getMembers()) {
            if (member instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) member;
                List<JCTree.JCAnnotation> annotations = getTemplateAnnotations(method, BEFORE_TEMPLATE::equals);
                if (!annotations.isEmpty()) {
                    result.beforeTemplate(method);
                }
                annotations = getTemplateAnnotations(method, AFTER_TEMPLATE::equals);
                if (!annotations.isEmpty()) {
                    result.afterTemplate(method);
                }
            }
        }
        return result.validate(context, cu);
    }

    class TemplateDescriptor {
        final JCTree.JCClassDecl classDecl;
        final List<JCTree.JCMethodDecl> beforeTemplates = new ArrayList<>();
        JCTree.JCMethodDecl afterTemplate;

        public TemplateDescriptor(JCTree.JCClassDecl classDecl) {
            this.classDecl = classDecl;
        }

        @Nullable
        private TemplateDescriptor validate(Context context, JCCompilationUnit cu) {
            if (beforeTemplates.isEmpty() || afterTemplate == null) {
                return null;
            }

            boolean valid = true;
            for (JCTree member : classDecl.getMembers()) {
                if (member instanceof JCTree.JCMethodDecl && !beforeTemplates.contains(member) && member != afterTemplate) {
                    for (JCTree.JCAnnotation annotation : getTemplateAnnotations(((JCTree.JCMethodDecl) member), UNSUPPORTED_ANNOTATIONS::contains)) {
                        processingEnv.getMessager().printMessage(Kind.NOTE, "The @" + annotation.annotationType + " is currently not supported", ((JCTree.JCMethodDecl) member).sym);
                        valid = false;
                    }
                }
            }

            // resolve so that we can inspect the template body
            valid &= resolve(context, cu);
            if (valid) {
                for (JCTree.JCMethodDecl template : beforeTemplates) {
                    valid &= validateTemplateMethod(template);
                }
                valid &= validateTemplateMethod(afterTemplate);
            }
            return valid ? this : null;
        }

        private boolean validateTemplateMethod(JCTree.JCMethodDecl template) {
            boolean valid = true;
            // TODO: support all Refaster method-level annotations
            for (JCTree.JCAnnotation annotation : getTemplateAnnotations(template, UNSUPPORTED_ANNOTATIONS::contains)) {
                processingEnv.getMessager().printMessage(Kind.NOTE, "The @" + annotation.annotationType + " is currently not supported", template.sym);
                valid = false;
            }
            // TODO: support all Refaster parameter-level annotations
            for (JCTree.JCVariableDecl parameter : template.getParameters()) {
                for (JCTree.JCAnnotation annotation : getTemplateAnnotations(parameter, UNSUPPORTED_ANNOTATIONS::contains)) {
                    processingEnv.getMessager().printMessage(Kind.NOTE, "The @" + annotation.annotationType + " annotation is currently not supported", template.sym);
                    valid = false;
                }
                if (parameter.vartype instanceof ParameterizedTypeTree || parameter.vartype.type instanceof Type.TypeVar) {
                    processingEnv.getMessager().printMessage(Kind.NOTE, "Generics are currently not supported", template.sym);
                    valid = false;
                }
            }
            if (template.restype instanceof ParameterizedTypeTree || template.restype.type instanceof Type.TypeVar) {
                processingEnv.getMessager().printMessage(Kind.NOTE, "Generics are currently not supported", template.sym);
                valid = false;
            }
            valid &= new TreeScanner() {
                boolean valid = true;

                boolean validate(JCTree tree) {
                    scan(tree);
                    return valid;
                }

                @Override
                public void visitIdent(JCTree.JCIdent jcIdent) {
                    if (jcIdent.sym != null
                        && jcIdent.sym.packge().getQualifiedName().contentEquals("com.google.errorprone.refaster")) {
                        processingEnv.getMessager().printMessage(Kind.NOTE, jcIdent.type.tsym.getQualifiedName() + " is not supported", template.sym);
                        valid = false;
                    }
                }
            }.validate(template.getBody());
            return valid;
        }

        public void beforeTemplate(JCTree.JCMethodDecl method) {
            beforeTemplates.add(method);
        }

        public void afterTemplate(JCTree.JCMethodDecl method) {
            afterTemplate = method;
        }

        private boolean resolve(Context context, JCCompilationUnit cu) {
            try {
                JavacResolution res = new JavacResolution(context);
                beforeTemplates.replaceAll(key -> {
                    Map<JCTree, JCTree> resolved = res.resolveAll(context, cu, singletonList(key));
                    return (JCTree.JCMethodDecl) resolved.get(key);
                });
                Map<JCTree, JCTree> resolved = res.resolveAll(context, cu, singletonList(afterTemplate));
                afterTemplate = (JCTree.JCMethodDecl) resolved.get(afterTemplate);
            } catch (Throwable t) {
                processingEnv.getMessager().printMessage(Kind.WARNING, "Had trouble type attributing the template.");
                return false;
            }
            return true;
        }

    }

    private static List<JCTree.JCAnnotation> getTemplateAnnotations(MethodTree method, Predicate<String> typePredicate) {
        List<JCTree.JCAnnotation> result = new ArrayList<>();
        for (AnnotationTree annotation : method.getModifiers().getAnnotations()) {
            Tree type = annotation.getAnnotationType();
            if (type.getKind() == Tree.Kind.IDENTIFIER && ((JCTree.JCIdent) type).sym != null
                && typePredicate.test(((JCTree.JCIdent) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            } else if (type.getKind() == Tree.Kind.IDENTIFIER && ((JCTree.JCAnnotation) annotation).attribute != null
                       && ((JCTree.JCAnnotation) annotation).attribute.type instanceof Type.ClassType
                       && ((JCTree.JCAnnotation) annotation).attribute.type.tsym != null
                       && typePredicate.test(((JCTree.JCAnnotation) annotation).attribute.type.tsym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            } else if (type.getKind() == Tree.Kind.MEMBER_SELECT && type instanceof JCTree.JCFieldAccess
                       && ((JCTree.JCFieldAccess) type).sym != null
                       && typePredicate.test(((JCTree.JCFieldAccess) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            }
        }
        return result;
    }

    private static List<JCTree.JCAnnotation> getTemplateAnnotations(VariableTree parameter, Predicate<String> typePredicate) {
        List<JCTree.JCAnnotation> result = new ArrayList<>();
        for (AnnotationTree annotation : parameter.getModifiers().getAnnotations()) {
            Tree type = annotation.getAnnotationType();
            if (type.getKind() == Tree.Kind.IDENTIFIER
                && ((JCTree.JCIdent) type).sym != null
                && typePredicate.test(((JCTree.JCIdent) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            } else if (type.getKind() == Tree.Kind.MEMBER_SELECT && type instanceof JCTree.JCFieldAccess
                       && ((JCTree.JCFieldAccess) type).sym != null
                       && typePredicate.test(((JCTree.JCFieldAccess) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            }
        }
        return result;
    }
}