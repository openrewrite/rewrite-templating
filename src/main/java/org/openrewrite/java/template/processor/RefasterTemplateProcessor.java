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
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import org.jspecify.annotations.Nullable;
import org.openrewrite.java.template.internal.ImportDetector;
import org.openrewrite.java.template.internal.JavacResolution;
import org.openrewrite.java.template.internal.TemplateCode;
import org.openrewrite.java.template.internal.UsedMethodDetector;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static org.openrewrite.java.template.internal.StringUtils.indent;
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
            } else if (JCTree.JCConditional.class.isAssignableFrom(type)) {
                return singletonList("J.Ternary");
            } else if (JCTree.JCNewClass.class.isAssignableFrom(type)) {
                return singletonList("J.NewClass");
            } else if (JCTree.JCLambda.class.isAssignableFrom(type)) {
                return singletonList("J.Lambda");
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

    private static final String GENERATOR_NAME = RefasterTemplateProcessor.class.getName();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            JCCompilationUnit jcCompilationUnit = toUnit(element);
            if (jcCompilationUnit != null) {
                Context context = javacProcessingEnv.getContext();
                new RecipeWriter(context, jcCompilationUnit).scan(jcCompilationUnit);
            }
        }

        // Inform how many rules were skipped and why; useful for debugging, but not enabled by default
        //printedMessages.entrySet().stream().sorted(Map.Entry.comparingByValue())
        //        .forEach(entry -> processingEnv.getMessager().printMessage(Kind.NOTE, entry.toString()));

        // Give other annotation processors a chance to process the same annotations, for dual use of Refaster templates
        return false;
    }

    private class RecipeWriter extends TreeScanner {
        private final Context context;
        private final JCCompilationUnit cu;
        boolean anySearchRecipe;
        final Map<TemplateDescriptor, Set<String>> imports;
        final Map<TemplateDescriptor, Set<String>> staticImports;
        final Map<String, String> recipes;

        public RecipeWriter(Context context, JCCompilationUnit cu) {
            this.context = context;
            this.cu = cu;
            imports = new HashMap<>();
            staticImports = new HashMap<>();
            recipes = new LinkedHashMap<>();
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl classDecl) {
            super.visitClassDef(classDecl);

            RuleDescriptor descriptor = getRuleDescriptor(classDecl, context, cu);
            if (descriptor != null) {
                anySearchRecipe |= descriptor.afterTemplate == null;

                TreeMaker treeMaker = TreeMaker.instance(context).forToplevel(cu);
                List<JCTree> membersWithoutConstructor = classDecl.getMembers().stream()
                        .filter(m -> !(m instanceof JCTree.JCMethodDecl) || !((JCTree.JCMethodDecl) m).name.contentEquals("<init>"))
                        .collect(toList());
                JCTree.JCClassDecl copy = treeMaker.ClassDef(classDecl.mods, classDecl.name, classDecl.typarams, classDecl.extending, classDecl.implementing, com.sun.tools.javac.util.List.from(membersWithoutConstructor));

                String templateFqn = classDecl.sym.fullname.toString() + "Recipe";
                String templateCode = copy.toString().trim();

                for (TemplateDescriptor template : descriptor.beforeTemplates) {
                    for (Symbol anImport : ImportDetector.imports(template.method)) {
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

                if (descriptor.afterTemplate != null) {
                    for (Symbol anImport : ImportDetector.imports(descriptor.afterTemplate.method)) {
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
                }

                for (Set<String> imports : imports.values()) {
                    imports.removeIf(i -> {
                        int endIndex = i.lastIndexOf('.');
                        return endIndex < 0 || "java.lang".equals(i.substring(0, endIndex)) || "com.google.errorprone.refaster".equals(i.substring(0, endIndex));
                    });
                }
                for (Set<String> imports : staticImports.values()) {
                    imports.removeIf(i -> i.startsWith("java.lang.") || i.startsWith("com.google.errorprone.refaster."));
                }

                Map<String, TemplateDescriptor> beforeTemplates = new LinkedHashMap<>();
                for (TemplateDescriptor templ : descriptor.beforeTemplates) {
                    String name = templ.method.name.toString();
                    if (beforeTemplates.containsKey(name)) {
                        String base = name;
                        for (int i = 0; ; i++) {
                            name = base + i;
                            if (!beforeTemplates.containsKey(name)) {
                                break;
                            }
                        }
                    }
                    beforeTemplates.put(name, templ);
                }
                String after = descriptor.afterTemplate == null ? null :
                        descriptor.afterTemplate.method.name.toString();

                StringBuilder recipe = new StringBuilder();
                Symbol.PackageSymbol pkg = classDecl.sym.packge();
                String typeName = classDecl.sym.fullname.toString();
                String refasterRuleClassName = pkg.isUnnamed() ? typeName : typeName.substring(pkg.fullname.length() + 1);
                recipe.append("/**\n * OpenRewrite recipe created for Refaster template {@code ").append(refasterRuleClassName).append("}.\n */\n");
                String recipeName = templateFqn.substring(templateFqn.lastIndexOf('.') + 1);
                recipe.append("@SuppressWarnings(\"all\")\n");
                recipe.append("@NullMarked\n");
                recipe.append("@Generated(\"").append(GENERATOR_NAME).append("\")\n");
                recipe.append(descriptor.classDecl.sym.outermostClass() == descriptor.classDecl.sym ?
                        "public class " : "public static class ").append(recipeName).append(" extends Recipe {\n\n");
                recipe.append("    /**\n");
                recipe.append("     * Instantiates a new instance.\n");
                recipe.append("     */\n");
                recipe.append("    public ").append(recipeName).append("() {}\n\n");
                recipe.append(recipeDescriptor(classDecl, descriptor,
                        "Refaster template `" + refasterRuleClassName + '`',
                        "Recipe created for the following Refaster template:\\n```java\\n" + escape(templateCode) + "\\n```\\n."
                ));
                recipe.append("    @Override\n");
                recipe.append("    public TreeVisitor<?, ExecutionContext> getVisitor() {\n");

                String javaVisitor = newAbstractRefasterJavaVisitor(beforeTemplates, after, descriptor);

                Precondition preconditions = generatePreconditions(descriptor.beforeTemplates, 16);
                if (preconditions == null) {
                    recipe.append(String.format("        return %s;\n", javaVisitor));
                } else {
                    recipe.append(String.format("        JavaVisitor<ExecutionContext> javaVisitor = %s;\n", javaVisitor));
                    recipe.append("        return Preconditions.check(\n");
                    recipe.append(indent(preconditions.toString(), 16)).append(",\n");
                    recipe.append("                javaVisitor\n");
                    recipe.append("        );\n");
                }
                recipe.append("    }\n");
                recipe.append("}\n");
                recipes.put(recipeName, recipe.toString());
            }

            if (classDecl.sym != null && classDecl.sym.getNestingKind() == NestingKind.TOP_LEVEL && !recipes.isEmpty()) {
                boolean outerClassRequired = descriptor == null;
                writeRecipeClass(classDecl, outerClassRequired, descriptor);
            }
        }

        private void writeRecipeClass(JCTree.JCClassDecl classDecl, boolean outerClassRequired, RuleDescriptor descriptor) {
            try {
                Symbol.PackageSymbol pkg = classDecl.sym.packge();
                String inputOuterFQN = outerClassRequired ? classDecl.sym.fullname.toString() : descriptor.classDecl.sym.fullname.toString();
                String className = inputOuterFQN + (outerClassRequired ? "Recipes" : "Recipe");
                JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className);

                // Pass in `-Arewrite.generatedAnnotation=jakarta.annotation.Generated` to override the default
                String generatedAnnotation = processingEnv.getOptions().get("rewrite.generatedAnnotation");
                if (generatedAnnotation == null) {
                    generatedAnnotation = "javax.annotation.Generated";
                }

                try (Writer out = new BufferedWriter(builderFile.openWriter())) {
                    if (!pkg.isUnnamed()) {
                        out.write("package " + pkg.fullname + ";\n");
                        out.write("\n");
                    }
                    out.write("import org.jspecify.annotations.NullMarked;\n");
                    out.write("import org.openrewrite.ExecutionContext;\n");
                    out.write("import org.openrewrite.Preconditions;\n");
                    out.write("import org.openrewrite.Recipe;\n");
                    out.write("import org.openrewrite.TreeVisitor;\n");
                    out.write("import org.openrewrite.java.JavaParser;\n");
                    out.write("import org.openrewrite.java.JavaTemplate;\n");
                    out.write("import org.openrewrite.java.JavaVisitor;\n");
                    out.write("import org.openrewrite.java.search.*;\n");
                    out.write("import org.openrewrite.java.template.Primitive;\n");
                    out.write("import org.openrewrite.java.template.function.*;\n");
                    out.write("import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;\n");
                    out.write("import org.openrewrite.java.tree.*;\n");
                    if (anySearchRecipe) {
                        out.write("import org.openrewrite.marker.SearchResult;\n");
                    }
                    out.write("\n");

                    out.write("import " + generatedAnnotation + ";\n");
                    out.write("import java.util.*;\n");
                    out.write("\n");
                    out.write("import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;\n");

                    out.write("\n");

                    if (outerClassRequired) {
                        out.write("/**\n * OpenRewrite recipes created for Refaster template {@code " + inputOuterFQN + "}.\n */\n");
                        String outerClassName = className.substring(className.lastIndexOf('.') + 1);
                        out.write("@SuppressWarnings(\"all\")\n");
                        out.write("@Generated(\"" + GENERATOR_NAME + "\")\n");
                        out.write("public class " + outerClassName + " extends Recipe {\n");
                        out.write("    /**\n");
                        out.write("     * Instantiates a new instance.\n"); // For -Xdoclint
                        out.write("     */\n");
                        out.write("    public " + outerClassName + "() {}\n\n");
                        out.write(recipeDescriptor(classDecl, descriptor,
                                String.format("`%s` Refaster recipes", inputOuterFQN.substring(inputOuterFQN.lastIndexOf('.') + 1)),
                                String.format("Refaster template recipes for `%s`.", inputOuterFQN)));
                        String recipesAsList = recipes.keySet().stream()
                                .map(r -> "                new " + r.substring(r.lastIndexOf('.') + 1) + "()")
                                .collect(joining(",\n"));
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

        private String newAbstractRefasterJavaVisitor(Map<String, TemplateDescriptor> beforeTemplates, String after, RuleDescriptor descriptor) {
            StringBuilder visitor = new StringBuilder();
            visitor.append("new AbstractRefasterJavaVisitor() {\n");
            for (Map.Entry<String, TemplateDescriptor> entry : beforeTemplates.entrySet()) {
                int arity = entry.getValue().getArity();
                for (int i = 0; i < arity; i++) {
                    visitor.append("            final JavaTemplate ")
                            .append(entry.getKey()).append(arity > 1 ? "$" + i : "")
                            .append(" = ")
                            .append(entry.getValue().toJavaTemplateBuilder(i))
                            .append("\n                    .build();\n");
                }
            }
            if (after != null) {
                visitor.append("            final JavaTemplate ")
                        .append(after)
                        .append(" = ")
                        .append(descriptor.afterTemplate.toJavaTemplateBuilder(0))
                        .append("\n                    .build();\n");
            }
            visitor.append("\n");

            // Determine which visitMethods we should generate
            Map<String, Map<String, TemplateDescriptor>> templatesByLstType = new TreeMap<>();
            for (Map.Entry<String, TemplateDescriptor> entry : beforeTemplates.entrySet()) {
                for (String lstType : entry.getValue().getTypes()) {
                    templatesByLstType.computeIfAbsent(lstType, k -> new TreeMap<>())
                            .put(entry.getKey(), entry.getValue());
                }
            }
            templatesByLstType.forEach((lstType, typeBeforeTemplates) ->
                    visitor.append(generateVisitMethod(typeBeforeTemplates, after, descriptor, lstType)));
            visitor.append("        }");
            return visitor.toString();
        }

        private String generateVisitMethod(Map<String, TemplateDescriptor> beforeTemplates, String after, RuleDescriptor descriptor, String lstType) {
            StringBuilder visitMethod = new StringBuilder();
            String methodSuffix = lstType.startsWith("J.") ? lstType.substring(2) : lstType;
            visitMethod.append("            @Override\n");
            visitMethod.append("            public J visit").append(methodSuffix).append("(").append(lstType).append(" elem, ExecutionContext ctx) {\n");
            if (lstType.equals("Statement")) {
                visitMethod.append("                if (elem instanceof J.Block) {\n");
                visitMethod.append("                    // FIXME workaround\n");
                visitMethod.append("                    return elem;\n");
                visitMethod.append("                }\n");
            }

            visitMethod.append("                JavaTemplate.Matcher matcher;\n");
            for (Map.Entry<String, TemplateDescriptor> entry : beforeTemplates.entrySet()) {
                int arity = entry.getValue().getArity();
                for (int i = 0; i < arity; i++) {
                    Map<Name, Integer> beforeParameters = findParameterOrder(entry.getValue().method, i);
                    visitMethod.append("                if (" + "(matcher = ").append(entry.getKey()).append(arity > 1 ? "$" + i : "").append(".matcher(getCursor())).find()").append(") {\n");
                    com.sun.tools.javac.util.List<JCTree.JCVariableDecl> jcVariableDecls = entry.getValue().method.getParameters();
                    for (JCTree.JCVariableDecl param : jcVariableDecls) {
                        com.sun.tools.javac.util.List<JCTree.JCAnnotation> annotations = param.getModifiers().getAnnotations();
                        for (JCTree.JCAnnotation jcAnnotation : annotations) {
                            String annotationType = jcAnnotation.attribute.type.tsym.getQualifiedName().toString();
                            if (!beforeParameters.containsKey(param.name) && annotationType.startsWith("org.openrewrite.java.template")) {
                                printNoteOnce("Ignoring annotation " + annotationType + " on unused parameter " + param.name, entry.getValue().classDecl.sym);
                            } else if (annotationType.equals("org.openrewrite.java.template.NotMatches")) {
                                String matcher = ((Type.ClassType) jcAnnotation.attribute.getValue().values.get(0).snd.getValue()).tsym.getQualifiedName().toString();
                                visitMethod.append("                    if (new ").append(matcher).append("().matches((Expression) matcher.parameter(").append(beforeParameters.get(param.name)).append("))) {\n");
                                visitMethod.append("                        return super.visit").append(methodSuffix).append("(elem, ctx);\n");
                                visitMethod.append("                    }\n");
                            } else if (annotationType.equals("org.openrewrite.java.template.Matches")) {
                                String matcher = ((Type.ClassType) jcAnnotation.attribute.getValue().values.get(0).snd.getValue()).tsym.getQualifiedName().toString();
                                visitMethod.append("                    if (!new ").append(matcher).append("().matches((Expression) matcher.parameter(").append(beforeParameters.get(param.name)).append("))) {\n");
                                visitMethod.append("                        return super.visit").append(methodSuffix).append("(elem, ctx);\n");
                                visitMethod.append("                    }\n");
                            }
                        }
                    }

                    if (descriptor.afterTemplate == null) {
                        visitMethod.append("                    return SearchResult.found(elem);\n");
                    } else {
                        maybeRemoveImports(imports, visitMethod, entry.getValue(), i, descriptor.afterTemplate);
                        maybeRemoveStaticImports(staticImports, visitMethod, entry.getValue(), i, descriptor.afterTemplate);

                        List<String> embedOptions = new ArrayList<>();
                        JCTree.JCExpression afterReturn = getReturnExpression(descriptor.afterTemplate.method);
                        if (afterReturn instanceof JCTree.JCParens ||
                                afterReturn instanceof JCTree.JCUnary && ((JCTree.JCUnary) afterReturn).getExpression() instanceof JCTree.JCParens) {
                            embedOptions.add("REMOVE_PARENS");
                        }
                        // TODO check if after template contains type or member references
                        embedOptions.add("SHORTEN_NAMES");
                        if (simplifyBooleans(descriptor.afterTemplate.method)) {
                            embedOptions.add("SIMPLIFY_BOOLEANS");
                        }

                        visitMethod.append("                    return embed(\n");
                        visitMethod.append("                            ").append(after).append(".apply(getCursor(), elem.getCoordinates().replace()");
                        Map<Name, Integer> afterParameters = findParameterOrder(descriptor.afterTemplate.method, 0);
                        String parameters = matchParameters(beforeParameters, afterParameters);
                        if (!parameters.isEmpty()) {
                            visitMethod.append(", ").append(parameters);
                        }
                        visitMethod.append("),\n");
                        visitMethod.append("                            getCursor(),\n");
                        visitMethod.append("                            ctx,\n");
                        visitMethod.append("                            ").append(String.join(", ", embedOptions)).append("\n");
                        visitMethod.append("                    );\n");
                    }
                    visitMethod.append("                }\n");
                }
            }
            visitMethod.append("                return super.visit").append(methodSuffix).append("(elem, ctx);\n");
            visitMethod.append("            }\n");
            visitMethod.append("\n");
            return visitMethod.toString();
        }

        private boolean simplifyBooleans(JCTree.JCMethodDecl template) {
            if (template.getReturnType().type.getTag() == TypeTag.BOOLEAN) {
                return true;
            }
            return new TreeScanner() {
                boolean found;

                boolean find(JCTree tree) {
                    scan(tree);
                    return found;
                }

                @Override
                public void visitBinary(JCTree.JCBinary jcBinary) {
                    found |= jcBinary.type.getTag() == TypeTag.BOOLEAN;
                    super.visitBinary(jcBinary);
                }

                @Override
                public void visitConditional(JCTree.JCConditional jcConditional) {
                    found = true;
                }

                @Override
                public void visitUnary(JCTree.JCUnary jcUnary) {
                    found |= jcUnary.type.getTag() == TypeTag.BOOLEAN;
                    super.visitUnary(jcUnary);
                }
            }.find(template.getBody());
        }

        private String recipeDescriptor(JCTree.JCClassDecl classDecl, @Nullable RuleDescriptor descriptor, String defaultDisplayName, String defaultDescription) {
            String displayName = defaultDisplayName;
            StringBuilder description = new StringBuilder(defaultDescription);
            Set<String> tags = new LinkedHashSet<>();

            // Extract from JavaDoc
            Tokens.Comment comment = cu.docComments.getComment(classDecl);
            if (comment != null && comment.getText() != null && !comment.getText().isEmpty()) {
                String commentText = comment.getText()
                        .replace("<p>", "")
                        .replace("<pre>{@code", "```java")
                        .replace("}</pre>", "```\n")
                        .replaceAll("(?s)\\{@\\S+\\s+(.*?)}", "`$1`")
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\b", "\\b")
                        .replace("\t", "\\t")
                        .replace("\f", "\\f")
                        .replace("\r", "\\r");
                String[] lines = commentText.split("\\.\\R+", 2);
                if (lines.length == 1 || lines[1].trim().isEmpty()) {
                    String firstLine = lines[0].trim().replace("\n", "");
                    description = firstLine.endsWith(".") ? new StringBuilder(firstLine) : new StringBuilder(firstLine).append('.');
                } else {
                    String firstLine = lines[0].trim().replace("\n", "");
                    displayName = firstLine.endsWith(".") ? firstLine.substring(0, firstLine.length() - 1) : firstLine;
                    description = new StringBuilder(lines[1].trim().replace("\n", "\\n"));
                    if (!description.toString().endsWith(".")) {
                        if (description.toString().endsWith("```")) {
                            description.append("\\n");
                        }
                        description.append('.');
                    }
                }
            }

            // Extract from the RecipeDescriptor annotation
            for (JCTree.JCAnnotation annotation : classDecl.getModifiers().getAnnotations()) {
                String annotationFqn = annotation.type.toString();
                if ("org.openrewrite.java.template.RecipeDescriptor".equals(annotationFqn)) {
                    for (JCTree.JCExpression argExpr : annotation.getArguments()) {
                        JCTree.JCAssign arg = (JCTree.JCAssign) argExpr;
                        switch (arg.lhs.toString()) {
                            case "name":
                                displayName = escapeJava(((JCTree.JCLiteral) arg.rhs).getValue().toString());
                                break;
                            case "description":
                                description = new StringBuilder(escapeJava(((JCTree.JCLiteral) arg.rhs).getValue().toString()));
                                break;
                            case "tags":
                                if (arg.rhs instanceof JCTree.JCLiteral) {
                                    tags.add(escapeJava(((JCTree.JCLiteral) arg.rhs).getValue().toString()));
                                } else if (arg.rhs instanceof JCTree.JCNewArray) {
                                    for (JCTree.JCExpression e : ((JCTree.JCNewArray) arg.rhs).elems) {
                                        tags.add(escapeJava(((JCTree.JCLiteral) e).getValue().toString()));
                                    }
                                }
                                break;
                        }
                    }
                    break;
                } else if ("tech.picnic.errorprone.refaster.annotation.OnlineDocumentation".equals(annotationFqn)) {
                    if (annotation.getArguments().isEmpty()) {
                        description.append("\\n[Source](https://error-prone.picnic.tech/refasterrules/").append(classDecl.name.toString()).append(").");
                    }
                } else if ("java.lang.SuppressWarnings".equals(annotationFqn)) {
                    addRspecTags(annotation, tags);
                }
            }

            if (descriptor != null) {
                for (TemplateDescriptor beforeTemplate : descriptor.beforeTemplates) {
                    for (JCTree.JCAnnotation annotation : beforeTemplate.method.getModifiers().getAnnotations()) {
                        if ("SuppressWarnings".equals(((JCTree.JCIdent) annotation.annotationType).getName().toString())) {
                            addRspecTags(annotation, tags);
                        }
                    }
                }
            }

            String recipeDescriptor = "    @Override\n" +
                    "    public String getDisplayName() {\n" +
                    "        //language=markdown\n" +
                    "        return \"" + displayName + "\";\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public String getDescription() {\n" +
                    "        //language=markdown\n" +
                    "        return \"" + description + "\";\n" +
                    "    }\n" +
                    "\n";

            if (tags.size() == 1) {
                recipeDescriptor += "    @Override\n" +
                        "    public Set<String> getTags() {\n" +
                        "        return Collections.singleton(\"" + String.join("\", \"", tags) + "\");\n" +
                        "    }\n" +
                        "\n";
            } else if (tags.size() > 1) {
                recipeDescriptor += "    @Override\n" +
                        "    public Set<String> getTags() {\n" +
                        "        return new HashSet<>(Arrays.asList(\"" + String.join("\", \"", tags) + "\"));\n" +
                        "    }\n" +
                        "\n";
            }

            return recipeDescriptor;
        }

        private void addRspecTags(JCTree.JCAnnotation annotation, Set<String> tags) {
            for (JCTree.JCExpression argExpr : annotation.getArguments()) {
                if (argExpr instanceof JCTree.JCAssign) {
                    Consumer<JCTree.JCExpression> addTag = expr -> {
                        if (expr instanceof JCTree.JCLiteral) {
                            String value = ((JCTree.JCLiteral) expr).getValue().toString();
                            if (value.startsWith("java:")) {
                                tags.add("RSPEC-" + value.substring("java:".length()));
                            }
                        }
                    };
                    JCTree.JCExpression rhs = ((JCTree.JCAssign) argExpr).rhs;
                    if (rhs instanceof JCTree.JCNewArray) {
                        ((JCTree.JCNewArray) rhs).elems.forEach(addTag);
                    } else {
                        addTag.accept(rhs);
                    }
                }
            }
        }

        private void maybeRemoveImports(Map<TemplateDescriptor, Set<String>> importsByTemplate, StringBuilder recipe, TemplateDescriptor beforeTemplate, int pos, TemplateDescriptor afterTemplate) {
            Set<String> beforeImports = beforeTemplate.usedTypes(pos).stream().map(sym -> sym.fullname.toString()).collect(toCollection(LinkedHashSet::new));
            beforeImports.removeAll(getImportsAsStrings(importsByTemplate, afterTemplate));
            beforeImports.removeIf(i -> i.startsWith("java.lang.") || i.startsWith("com.google.errorprone.refaster."));
            beforeImports.forEach(anImport -> recipe.append("                    maybeRemoveImport(\"").append(anImport).append("\");\n"));
        }

        private void maybeRemoveStaticImports(Map<TemplateDescriptor, Set<String>> importsByTemplate, StringBuilder recipe, TemplateDescriptor beforeTemplate, int pos, TemplateDescriptor afterTemplate) {
            Set<String> beforeImports = beforeTemplate.usedMembers(pos).stream().map(symbol -> symbol.owner.getQualifiedName() + "." + symbol.name).collect(toCollection(LinkedHashSet::new));
            beforeImports.removeAll(getImportsAsStrings(importsByTemplate, afterTemplate));
            beforeImports.removeIf(i -> i.startsWith("java.lang.") || i.startsWith("com.google.errorprone.refaster."));
            beforeImports.forEach(anImport -> recipe.append("                    maybeRemoveImport(\"").append(anImport).append("\");\n"));
        }

        private Set<String> getImportsAsStrings(Map<TemplateDescriptor, Set<String>> importsByTemplate, TemplateDescriptor templateMethod) {
            return importsByTemplate.entrySet().stream()
                    .filter(e -> templateMethod == e.getKey())
                    .map(Map.Entry::getValue)
                    .flatMap(Set::stream)
                    .collect(toSet());
        }

        /* Generate the minimal precondition that would allow to match each before template individually. */
        private @Nullable Precondition generatePreconditions(List<TemplateDescriptor> beforeTemplates, int indent) {
            Set<Set<Precondition>> preconditions = new HashSet<>();
            for (TemplateDescriptor beforeTemplate : beforeTemplates) {
                int arity = beforeTemplate.getArity();
                for (int i = 0; i < arity; i++) {
                    Set<Precondition> usesVisitors = new LinkedHashSet<>();

                    for (Symbol.ClassSymbol usedType : beforeTemplate.usedTypes(i)) {
                        String name = usedType.getQualifiedName().toString().replace('$', '.');
                        if (!name.startsWith("java.lang.") && !name.startsWith("com.google.errorprone.refaster.")) {
                            usesVisitors.add(new Precondition.Rule("new UsesType<>(\"" + name + "\", true)"));
                        }
                    }
                    for (Symbol.MethodSymbol method : beforeTemplate.usedMethods(i)) {
                        if (method.owner.getQualifiedName().toString().startsWith("com.google.errorprone.refaster.")) {
                            continue;
                        }
                        String methodName = method.name.toString();
                        methodName = methodName.equals("<init>") ? "<constructor>" : methodName;
                        usesVisitors.add(new Precondition.Rule(String.format("new UsesMethod<>(\"%s %s(..)\", true)",
                                method.owner.getQualifiedName().toString(), methodName)));
                    }

                    if (!usesVisitors.isEmpty()) {
                        preconditions.add(usesVisitors);
                    } else {
                        return null; // At least one of the before templates has no preconditions, so we can not use any preconditions
                    }
                }
            }

            if (preconditions.isEmpty()) {
                return null;
            }

            return new Precondition.Or(
                    preconditions.stream()
                            .map(Precondition.And::new)
                            .collect(toSet())
            ).prune();
        }
    }

    private String escape(String string) {
        return string.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\R", "\\\\n");
    }

    private Map<Name, Integer> findParameterOrder(JCTree.JCMethodDecl method, int arity) {
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

    private String matchParameters(Map<Name, Integer> beforeParameters, Map<Name, Integer> afterParameters) {
        return afterParameters.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .map(e -> beforeParameters.get(e.getKey()))
                .map(e -> "matcher.parameter(" + e + ")")
                .collect(Collectors.joining(", "));
    }

    private JCTree.@Nullable JCExpression getReturnExpression(JCTree.JCMethodDecl method) {
        JCTree.JCStatement statement = method.getBody().getStatements().last();
        if (statement instanceof JCTree.JCReturn) {
            return ((JCTree.JCReturn) statement).expr;
        } else if (statement instanceof JCTree.JCExpressionStatement) {
            return ((JCTree.JCExpressionStatement) statement).expr;
        }
        return null;
    }

    private @Nullable RuleDescriptor getRuleDescriptor(JCTree.JCClassDecl tree, Context context, JCCompilationUnit cu) {
        RuleDescriptor result = new RuleDescriptor(tree, cu, context);
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
        return result.validate();
    }

    class RuleDescriptor {
        final JCTree.JCClassDecl classDecl;
        private final JCCompilationUnit cu;
        private final Context context;
        final List<TemplateDescriptor> beforeTemplates = new ArrayList<>();

        @Nullable
        TemplateDescriptor afterTemplate;

        public RuleDescriptor(JCTree.JCClassDecl classDecl, JCCompilationUnit cu, Context context) {
            this.classDecl = classDecl;
            this.cu = cu;
            this.context = context;
        }

        private @Nullable RuleDescriptor validate() {
            if (beforeTemplates.isEmpty()) {
                return null;
            }

            for (JCTree member : classDecl.getMembers()) {
                if (member instanceof JCTree.JCMethodDecl && beforeTemplates.stream().noneMatch(t -> t.method == member) &&
                        (afterTemplate == null || member != afterTemplate.method)) {
                    for (JCTree.JCAnnotation annotation : getTemplateAnnotations(((JCTree.JCMethodDecl) member), UNSUPPORTED_ANNOTATIONS::contains)) {
                        printNoteOnce("@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                        return null;
                    }
                }
            }

            // resolve so that we can inspect the template body
            boolean valid = resolve();
            if (valid) {
                for (TemplateDescriptor template : beforeTemplates) {
                    valid &= template.validate();
                }
                if (afterTemplate != null) {
                    valid &= afterTemplate.validate();
                }
            }

            if (valid && afterTemplate != null) {
                Set<Name> requiredParameters = findParameterOrder(afterTemplate.method, 0).keySet();
                for (TemplateDescriptor beforeTemplate : beforeTemplates) {
                    for (int i = 0; i < beforeTemplate.getArity(); i++) {
                        Set<Name> providedParameters = findParameterOrder(beforeTemplate.method, i).keySet();
                        if (!providedParameters.containsAll(requiredParameters)) {
                            printNoteOnce("@AfterTemplate defines arguments that are not present in all @BeforeTemplate methods", classDecl.sym);
                            return null;
                        }
                    }
                }
            }
            return valid ? this : null;
        }

        public void beforeTemplate(JCTree.JCMethodDecl method) {
            beforeTemplates.add(new TemplateDescriptor(method, classDecl, cu, context));
        }

        public void afterTemplate(JCTree.JCMethodDecl method) {
            afterTemplate = new TemplateDescriptor(method, classDecl, cu, context);
        }

        private boolean resolve() {
            boolean valid = true;
            try {
                for (TemplateDescriptor beforeTemplate : beforeTemplates) {
                    valid &= beforeTemplate.resolve();
                }
                if (afterTemplate != null) {
                    valid &= afterTemplate.resolve();
                }
            } catch (Throwable t) {
                processingEnv.getMessager().printMessage(Kind.WARNING, "Had trouble type attributing the template.");
                valid = false;
            }
            return valid;
        }
    }

    class TemplateDescriptor {
        JCTree.JCMethodDecl method;
        private final JCTree.JCClassDecl classDecl;
        private final JCCompilationUnit cu;
        private final Context context;

        public TemplateDescriptor(JCTree.JCMethodDecl method, JCTree.JCClassDecl classDecl, JCCompilationUnit cu, Context context) {
            this.classDecl = classDecl;
            this.method = method;
            this.cu = cu;
            this.context = context;
        }

        public int getArity() {
            AtomicReference<JCTree.JCMethodInvocation> anyOfCall = new AtomicReference<>();
            new TreeScanner() {
                @Override
                public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                    if (isAnyOfCall(jcMethodInvocation)) {
                        anyOfCall.set(jcMethodInvocation);
                        return;
                    }
                    super.visitApply(jcMethodInvocation);
                }
            }.scan(method);
            return Optional.ofNullable(anyOfCall.get()).map(call -> call.args.size()).orElse(1);
        }

        public Collection<String> getTypes() {
            if (getArity() == 1) {
                JCTree.JCExpression returnExpression = getReturnExpression(method);
                Class<? extends JCTree> clazz = returnExpression != null ?
                        returnExpression.getClass() :
                        method.getBody().getStatements().last().getClass();
                return LST_TYPE_MAP.get(clazz);
            }
            Set<String> types = new HashSet<>();
            new TreeScanner() {
                @Override
                public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                    if (isAnyOfCall(jcMethodInvocation)) {
                        for (JCTree.JCExpression argument : jcMethodInvocation.getArguments()) {
                            types.addAll(LST_TYPE_MAP.get(argument.getClass()));
                        }
                        return;
                    }
                    super.visitApply(jcMethodInvocation);
                }
            }.scan(method);
            return types;
        }

        private String toJavaTemplateBuilder(int pos) {
            JCTree tree = method.getBody().getStatements().get(0);
            if (tree instanceof JCTree.JCReturn) {
                tree = ((JCTree.JCReturn) tree).getExpression();
            }

            List<JCTree.JCTypeParameter> typeParameters = classDecl.typarams == null ? Collections.emptyList() : classDecl.typarams;
            String javaTemplateBuilder = TemplateCode.process(tree, method.getParameters(), typeParameters, pos, method.restype.type instanceof Type.JCVoidType, true);
            return TemplateCode.indent(javaTemplateBuilder, 16);
        }

        boolean validate() {
            if (method.typarams != null && !method.typarams.isEmpty()) {
                printNoteOnce("Generic type parameters are only allowed at class level", classDecl.sym);
                return false;
            }
            for (JCTree.JCAnnotation annotation : getTemplateAnnotations(method, UNSUPPORTED_ANNOTATIONS::contains)) {
                printNoteOnce("@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                return false;
            }
            for (JCTree.JCVariableDecl parameter : method.getParameters()) {
                for (JCTree.JCAnnotation annotation : getTemplateAnnotations(parameter, UNSUPPORTED_ANNOTATIONS::contains)) {
                    printNoteOnce("@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                    return false;
                }
            }
            if (method.body.stats.get(0) instanceof JCTree.JCIf) {
                printNoteOnce("If statements are currently not supported", classDecl.sym);
                return false;
            }
            if (method.body.stats.get(0) instanceof JCTree.JCReturn) {
                JCTree.JCExpression expr = ((JCTree.JCReturn) method.body.stats.get(0)).expr;
                if (expr instanceof JCTree.JCLambda) {
                    printNoteOnce("Lambdas are currently not supported", classDecl.sym);
                    return false;
                } else if (expr instanceof JCTree.JCMemberReference) {
                    printNoteOnce("Method references are currently not supported", classDecl.sym);
                    return false;
                }
            }
            return new TreeScanner() {
                boolean valid = true;
                int anyOfCount = 0;

                boolean validate(JCTree tree) {
                    scan(tree);
                    return valid;
                }

                @Override
                public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
                    if (jcFieldAccess.selected.type.tsym.toString().equals("com.google.errorprone.refaster.Refaster") &&
                            jcFieldAccess.name.toString().equals("anyOf")) {
                        // exception for `Refaster.anyOf()`
                        if (++anyOfCount > 1) {
                            printNoteOnce("Refaster.anyOf() can only be used once per template", classDecl.sym);
                            valid = false;
                        }
                        return;
                    }
                    super.visitSelect(jcFieldAccess);
                }

                @Override
                public void visitIdent(JCTree.JCIdent jcIdent) {
                    if (valid &&
                            jcIdent.sym != null &&
                            jcIdent.sym.packge().getQualifiedName().contentEquals("com.google.errorprone.refaster")) {
                        printNoteOnce(jcIdent.type.tsym.getQualifiedName() + " is currently not supported", classDecl.sym);
                        valid = false;
                    }
                }
            }.validate(method.getBody());
        }

        private boolean resolve() {
            method = resolve(method);
            return method != null;
        }

        private JCTree.@Nullable JCMethodDecl resolve(JCTree.JCMethodDecl method) {
            JavacResolution res = new JavacResolution(context);
            try {
                classDecl.defs = classDecl.defs.prepend(method);
                JCTree.JCMethodDecl resolvedMethod = (JCTree.JCMethodDecl) requireNonNull(res.resolveAll(context, cu, singletonList(method))).get(method);
                classDecl.defs = classDecl.defs.tail;
                resolvedMethod.params = method.params;
                method = resolvedMethod;
                return method;
            } catch (Throwable t) {
                processingEnv.getMessager().printMessage(Kind.WARNING, "Had trouble type attributing the template method: " + method.name);
            }
            return null;
        }

        public List<Symbol.ClassSymbol> usedTypes(int i) {
            List<Symbol> imports;
            if (getArity() == 1) {
                imports = ImportDetector.imports(method);
            } else {
                Set<JCTree> skip = new HashSet<>();
                new TreeScanner() {
                    @Override
                    public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                        if (isAnyOfCall(jcMethodInvocation)) {
                            for (int j = 0; j < jcMethodInvocation.args.size(); j++) {
                                if (j != i) {
                                    skip.add(jcMethodInvocation.args.get(j));
                                }
                            }
                            return;
                        }
                        super.visitApply(jcMethodInvocation);
                    }
                }.scan(method);
                imports = ImportDetector.imports(method, t -> !skip.contains(t));
            }
            return imports.stream().filter(Symbol.ClassSymbol.class::isInstance).map(Symbol.ClassSymbol.class::cast).collect(toList());
        }

        public List<Symbol> usedMembers(int i) {
            List<Symbol> imports;
            if (getArity() == 1) {
                imports = ImportDetector.imports(method);
            } else {
                Set<JCTree> skip = new HashSet<>();
                new TreeScanner() {
                    @Override
                    public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                        if (isAnyOfCall(jcMethodInvocation)) {
                            for (int j = 0; j < jcMethodInvocation.args.size(); j++) {
                                if (j != i) {
                                    skip.add(jcMethodInvocation.args.get(j));
                                }
                            }
                            return;
                        }
                        super.visitApply(jcMethodInvocation);
                    }
                }.scan(method);
                imports = ImportDetector.imports(method, t -> !skip.contains(t));
            }
            return imports.stream().filter(sym -> sym instanceof Symbol.VarSymbol || sym instanceof Symbol.MethodSymbol).collect(toList());
        }

        public List<Symbol.MethodSymbol> usedMethods(int i) {
            if (getArity() == 1) {
                return UsedMethodDetector.usedMethods(method);
            } else {
                Set<JCTree> skip = new HashSet<>();
                new TreeScanner() {
                    @Override
                    public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                        if (isAnyOfCall(jcMethodInvocation)) {
                            for (int j = 0; j < jcMethodInvocation.args.size(); j++) {
                                if (j != i) {
                                    skip.add(jcMethodInvocation.args.get(j));
                                }
                            }
                            return;
                        }
                        super.visitApply(jcMethodInvocation);
                    }
                }.scan(method);
                return UsedMethodDetector.usedMethods(method, t -> !skip.contains(t));
            }
        }
    }

    private boolean isAnyOfCall(JCTree.JCMethodInvocation call) {
        JCTree.JCExpression meth = call.meth;
        if (meth instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) meth;
            return fieldAccess.name.toString().equals("anyOf") &&
                    ((JCTree.JCIdent) fieldAccess.selected).name.toString().equals("Refaster");
        }
        return false;
    }

    private final Map<String, Integer> printedMessages = new TreeMap<>();

    /**
     * @param message The message to print
     * @param symbol  The symbol to attach the message to; printed as clickable link to file
     */
    private void printNoteOnce(String message, Symbol.ClassSymbol symbol) {
        if (printedMessages.compute(message, (k, v) -> v == null ? 1 : v + 1) == 1) {
            processingEnv.getMessager().printMessage(Kind.NOTE, message, symbol);
        }
    }

    private static List<JCTree.JCAnnotation> getTemplateAnnotations(MethodTree method, Predicate<String> typePredicate) {
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

    private static List<JCTree.JCAnnotation> getTemplateAnnotations(VariableTree parameter, Predicate<String> typePredicate) {
        List<JCTree.JCAnnotation> result = new ArrayList<>();
        for (AnnotationTree annotation : parameter.getModifiers().getAnnotations()) {
            Tree type = annotation.getAnnotationType();
            if (type.getKind() == Tree.Kind.IDENTIFIER &&
                    ((JCTree.JCIdent) type).sym != null &&
                    typePredicate.test(((JCTree.JCIdent) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            } else if (type.getKind() == Tree.Kind.MEMBER_SELECT && type instanceof JCTree.JCFieldAccess &&
                    ((JCTree.JCFieldAccess) type).sym != null &&
                    typePredicate.test(((JCTree.JCFieldAccess) type).sym.getQualifiedName().toString())) {
                result.add((JCTree.JCAnnotation) annotation);
            }
        }
        return result;
    }

    private static String escapeJava(String input) {
        // List copied from org.apache.commons.lang3.StringEscapeUtils.escapeJava(String)
        // Missing JavaUnicodeEscaper.outsideOf(32, 0x7f)
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\f", "\\f")
                .replace("\r", "\\r");
    }
}
