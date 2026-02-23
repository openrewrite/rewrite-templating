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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Name;
import org.jspecify.annotations.Nullable;
import org.openrewrite.java.template.internal.ImportDetector;

import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.*;
import static org.openrewrite.java.template.internal.StringUtils.indent;
import static org.openrewrite.java.template.internal.StringUtils.indentNewLine;
import static org.openrewrite.java.template.processor.RefasterTemplateProcessor.*;

class RecipeWriter {
    private static final String GENERATOR_NAME = RefasterTemplateProcessor.class.getName();
    private static final String USE_IMPORT_POLICY = "com.google.errorprone.refaster.annotation.UseImportPolicy";
    private static final Precondition NOT_REFASTER_TEMPLATE = new Precondition.Not(
            new Precondition.Rule("new UsesType<>(\"com.google.errorprone.refaster.annotation.BeforeTemplate\", true)")
    );
    private static final Precondition NOT_SEMANTICS = new Precondition.Not(
            new Precondition.Rule("new UsesType<>(\"org.openrewrite.java.template.Semantics\", true)")
    );

    private final JavacProcessingEnvironment processingEnv;
    private final JCTree.JCCompilationUnit cu;
    private boolean anySearchRecipe;

    private final Map<TemplateDescriptor, Set<String>> imports = new HashMap<>();
    private final Map<TemplateDescriptor, Set<String>> staticImports = new HashMap<>();
    private final Map<String, String> recipes = new LinkedHashMap<>();

    public RecipeWriter(JavacProcessingEnvironment processingEnv, JCTree.JCCompilationUnit cu) {
        this.processingEnv = processingEnv;
        this.cu = cu;
    }

    private String escapeTemplate(JCTree.JCClassDecl classDecl) {
        TreeMaker treeMaker = TreeMaker.instance(processingEnv.getContext()).forToplevel(cu);
        List<JCTree> membersWithoutConstructor = classDecl.getMembers().stream()
                .filter(m -> !(m instanceof JCTree.JCMethodDecl && ((JCTree.JCMethodDecl) m).name.contentEquals("<init>")))
                .collect(toList());
        return treeMaker.ClassDef(
                        classDecl.mods,
                        classDecl.name,
                        classDecl.typarams,
                        classDecl.extending,
                        classDecl.implementing,
                        com.sun.tools.javac.util.List.from(membersWithoutConstructor))
                .toString()
                .trim()
                .replace("@BeforeTemplate()", "@BeforeTemplate")
                .replace("@AfterTemplate()", "@AfterTemplate")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replaceAll("\\R", "\\\\n");
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

    private static String matchParameters(Map<Name, Integer> beforeParameters, Map<Name, Integer> afterParameters) {
        return afterParameters.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .map(e -> beforeParameters.get(e.getKey()))
                .map(e -> "matcher.parameter(" + e + ")")
                .collect(joining(", "));
    }

    public void writeRecipeForClassDeclaration(JCTree.JCClassDecl classDecl, @Nullable RuleDescriptor descriptor) {
        if (descriptor != null) {
            collectRecipes(classDecl, descriptor);
        }
        if (classDecl.sym != null && classDecl.sym.getNestingKind() == NestingKind.TOP_LEVEL && !recipes.isEmpty()) {
            boolean outerClassRequired = descriptor == null;
            writeRecipeClass(classDecl, outerClassRequired, descriptor);
        }
    }

    private void collectRecipes(JCTree.JCClassDecl classDecl, RuleDescriptor descriptor) {
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

        if (descriptor.afterTemplate == null) {
            anySearchRecipe = true;
        } else {
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
                return endIndex < 0 || "java.lang".equals(i.substring(0, endIndex)) || i.startsWith("com.google.errorprone.refaster");
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

        String templateFqn = classDecl.sym.fullname.toString() + "Recipe";
        String escapedTemplateCode = escapeTemplate(classDecl);

        StringBuilder recipe = new StringBuilder();
        Symbol.PackageSymbol pkg = classDecl.sym.packge();
        String typeName = classDecl.sym.fullname.toString();
        String refasterRuleClassName = pkg.isUnnamed() ? typeName : typeName.substring(pkg.fullname.length() + 1);
        recipe.append("/**\n * OpenRewrite recipe created for Refaster template {@code ").append(refasterRuleClassName).append("}.\n */\n");
        String recipeName = templateFqn.substring(templateFqn.lastIndexOf('.') + 1);
        recipe.append("@SuppressWarnings(\"all\")\n");
        recipe.append("@NullMarked\n");
        if (descriptor.classDecl.sym.outermostClass() == classDecl.sym) {
            recipe.append("@Generated(\"").append(GENERATOR_NAME).append("\")\n");
            recipe.append("public class ");
        } else {
            recipe.append("public static class ");
        }
        recipe.append(recipeName).append(" extends Recipe {\n\n");
        recipe.append("    /**\n");
        recipe.append("     * Instantiates a new instance.\n");
        recipe.append("     */\n");
        recipe.append("    public ").append(recipeName).append("() {}\n\n");
        recipe.append(recipeDescriptor(classDecl, descriptor,
                "Refaster template `" + refasterRuleClassName + '`',
                "Recipe created for the following Refaster template:\\n```java\\n" + escapedTemplateCode + "\\n```\\n."
        ));
        recipe.append("    @Override\n");
        recipe.append("    public TreeVisitor<?, ExecutionContext> getVisitor() {\n");

        String javaVisitor = newAbstractRefasterJavaVisitor(beforeTemplates, descriptor);

        Precondition preconditions = generatePreconditions(descriptor.beforeTemplates);
        Precondition allPreconditions;
        if (preconditions == null) {
            allPreconditions = new Precondition.And(NOT_REFASTER_TEMPLATE, NOT_SEMANTICS);
        } else if (preconditions instanceof Precondition.And) {
            allPreconditions = ((Precondition.And) preconditions)
                    .addPrecondition(NOT_REFASTER_TEMPLATE)
                    .addPrecondition(NOT_SEMANTICS);
        } else {
            allPreconditions = new Precondition.And(preconditions, NOT_REFASTER_TEMPLATE, NOT_SEMANTICS);
        }
        recipe.append(String.format("        JavaVisitor<ExecutionContext> javaVisitor = %s;\n", javaVisitor));
        recipe.append("        return Preconditions.check(\n");
        recipe.append(indent(allPreconditions.toString(), 16)).append(",\n");
        recipe.append("                javaVisitor\n");
        recipe.append("        );\n");
        recipe.append("    }\n");
        recipe.append("}\n");
        recipes.put(recipeName, recipe.toString());
    }

    private void writeRecipeClass(
            JCTree.JCClassDecl classDecl,
            boolean outerClassRequired,
            @Nullable RuleDescriptor descriptor) {
        try {
            Symbol.PackageSymbol pkg = classDecl.sym.packge();
            String inputOuterFQN = outerClassRequired ? classDecl.sym.fullname.toString() : descriptor.classDecl.sym.fullname.toString();
            String className = inputOuterFQN + (outerClassRequired ? "Recipes" : "Recipe");
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className);

            try (Writer out = new BufferedWriter(builderFile.openWriter())) {
                if (!pkg.isUnnamed()) {
                    out.write("package " + pkg.fullname + ";\n");
                    out.write("\n");
                }
                writeImports(out);

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
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeImports(Writer out) throws IOException {
        // Pass in `-Arewrite.generatedAnnotation=jakarta.annotation.Generated` to override the default
        String generatedAnnotation = processingEnv.getOptions().get(REWRITE_GENERATED_ANNOTATION);
        if (generatedAnnotation == null) {
            generatedAnnotation = "javax.annotation.Generated";
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
    }

    private String newAbstractRefasterJavaVisitor(Map<String, TemplateDescriptor> beforeTemplates, RuleDescriptor descriptor) {
        StringBuilder visitor = new StringBuilder();
        visitor.append("new AbstractRefasterJavaVisitor() {\n");

        // Create fields for the lazily initialized before/after templates used when matching
        for (Map.Entry<String, TemplateDescriptor> entry : beforeTemplates.entrySet()) {
            int arity = entry.getValue().getArity();
            for (int i = 0; i < arity; i++) {
                visitor.append("            JavaTemplate ")
                        .append(entry.getKey()).append(arity > 1 ? "$" + i : "")
                        .append(";\n");
            }
        }
        if (descriptor.afterTemplate != null && !descriptor.afterTemplate.method.body.stats.isEmpty()) {
            visitor.append("            JavaTemplate after;\n\n");
        }

        // Determine which visitMethods we should generate
        Map<String, Map<String, TemplateDescriptor>> templatesByLstType = new TreeMap<>();
        for (Map.Entry<String, TemplateDescriptor> entry : beforeTemplates.entrySet()) {
            for (String lstType : entry.getValue().getTypes()) {
                templatesByLstType.computeIfAbsent(lstType, k -> new TreeMap<>())
                        .put(entry.getKey(), entry.getValue());
            }
        }
        templatesByLstType.forEach((lstType, typeBeforeTemplates) ->
                visitor.append(generateVisitMethod(typeBeforeTemplates, descriptor, lstType)));
        visitor.append("        }");
        return visitor.toString();
    }

    private String generateVisitMethod(Map<String, TemplateDescriptor> beforeTemplates, RuleDescriptor descriptor, String lstType) {
        StringBuilder visitMethod = new StringBuilder();
        String methodSuffix = lstType.startsWith("J.") ? lstType.substring(2) : lstType;
        visitMethod.append("            @Override\n");
        visitMethod.append("            public J visit").append(methodSuffix).append("(").append(lstType).append(" elem, ExecutionContext ctx) {\n");
        if ("Statement".equals(lstType)) {
            visitMethod.append("                if (elem instanceof J.Block) {\n");
            visitMethod.append("                    // FIXME workaround\n");
            visitMethod.append("                    return elem;\n");
            visitMethod.append("                }\n");
        }

        visitMethod.append("                JavaTemplate.Matcher matcher;\n");

        // Check if any before template needs the type assignability guard
        String hoistedGuardType = null;
        if (descriptor.afterTemplate != null) {
            Types types = Types.instance(processingEnv.getContext());
            Type afterReturnType = descriptor.afterTemplate.method.getReturnType().type;
            if (!(afterReturnType instanceof Type.JCVoidType)) {
                for (TemplateDescriptor bt : beforeTemplates.values()) {
                    Type beforeReturnType = bt.method.getReturnType().type;
                    if (!(beforeReturnType instanceof Type.JCVoidType) &&
                            !types.isSubtype(types.erasure(afterReturnType), types.erasure(beforeReturnType))) {
                        hoistedGuardType = types.erasure(afterReturnType).tsym.getQualifiedName().toString();
                        break;
                    }
                }
            }
        }
        if (hoistedGuardType != null) {
            visitMethod.append("                if (!isAssignableToTargetType(\"")
                    .append(hoistedGuardType).append("\")) {\n");
            visitMethod.append("                    return super.visit").append(methodSuffix).append("(elem, ctx);\n");
            visitMethod.append("                }\n");
        }

        for (Map.Entry<String, TemplateDescriptor> entry : beforeTemplates.entrySet()) {
            int arity = entry.getValue().getArity();
            for (int i = 0; i < arity; i++) {
                // Add lazy initialization of the before template
                String variableName = entry.getKey() + (arity > 1 ? "$" + i : "");
                visitMethod
                        .append("                if (").append(variableName).append(" == null) {\n")
                        .append("                    ").append(variableName).append(" = ")
                        .append(indentNewLine(entry.getValue().toJavaTemplateBuilder(i), 20))
                        .append(".build();\n")
                        .append("                }\n")
                        .append("                if ((matcher = ").append(variableName).append(".matcher(getCursor())).find()) {\n");

                Map<Name, Integer> beforeParameters = RefasterTemplateProcessor.findParameterOrder(entry.getValue().method, i);
                for (JCTree.JCVariableDecl param : entry.getValue().method.getParameters()) {
                    com.sun.tools.javac.util.List<JCTree.JCAnnotation> annotations = param.getModifiers().getAnnotations();
                    for (JCTree.JCAnnotation jcAnnotation : annotations) {
                        String annotationType = jcAnnotation.attribute.type.tsym.getQualifiedName().toString();
                        if (!beforeParameters.containsKey(param.name) && annotationType.startsWith("org.openrewrite.java.template")) {
                            printNoteOnce(processingEnv, "Ignoring annotation " + annotationType + " on unused parameter " + param.name, entry.getValue().classDecl.sym);
                        } else if ("org.openrewrite.java.template.NotMatches".equals(annotationType)) {
                            String matcher = ((Type.ClassType) jcAnnotation.attribute.getValue().values.get(0).snd.getValue()).tsym.getQualifiedName().toString();
                            visitMethod.append("                    if (new ").append(matcher).append("().matches((Expression) matcher.parameter(").append(beforeParameters.get(param.name)).append("))) {\n");
                            visitMethod.append("                        return super.visit").append(methodSuffix).append("(elem, ctx);\n");
                            visitMethod.append("                    }\n");
                        } else if ("org.openrewrite.java.template.Matches".equals(annotationType)) {
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
                    if (!getMethodTreeAnnotations(descriptor.afterTemplate.method, USE_IMPORT_POLICY::equals).isEmpty()) {
                        // Assume ImportPolicy.STATIC_IMPORT_ALWAYS, as that's all we see in error-prone-support
                        embedOptions.add("STATIC_IMPORT_ALWAYS");
                    }

                    if (descriptor.afterTemplate.method.body.stats.isEmpty()) {
                        visitMethod.append("                    return null;\n");
                    } else {
                        visitMethod
                                .append("                    if (after == null) {\n")
                                .append("                        after = ")
                                .append(indentNewLine(descriptor.afterTemplate.toJavaTemplateBuilder(0), 24))
                                .append(".build();\n")
                                .append("                    }\n")
                                .append("                    return embed(\n")
                                .append("                            after.apply(getCursor(), elem.getCoordinates().replace()");
                        Map<Name, Integer> afterParameters = RefasterTemplateProcessor.findParameterOrder(descriptor.afterTemplate.method, 0);
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
                }
                visitMethod.append("                }\n");
            }
        }
        visitMethod.append("                return super.visit").append(methodSuffix).append("(elem, ctx);\n");
        visitMethod.append("            }\n");
        visitMethod.append("\n");
        return visitMethod.toString();
    }

    private static boolean simplifyBooleans(JCTree.JCMethodDecl template) {
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

    private String recipeDescriptor(JCTree.JCClassDecl classDecl, RuleDescriptor descriptor, String defaultDisplayName, String defaultDescription) {
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

    private static void addRspecTags(JCTree.JCAnnotation annotation, Set<String> tags) {
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

    private static void maybeRemoveImports(Map<TemplateDescriptor, Set<String>> importsByTemplate, StringBuilder recipe, TemplateDescriptor beforeTemplate, int pos, TemplateDescriptor afterTemplate) {
        Set<String> beforeImports = beforeTemplate.usedTypes(pos).stream().map(sym -> sym.fullname.toString()).collect(toCollection(LinkedHashSet::new));
        beforeImports.removeAll(getImportsAsStrings(importsByTemplate, afterTemplate));
        beforeImports.removeIf(i -> i.startsWith("java.lang.") || i.startsWith("com.google.errorprone.refaster."));
        beforeImports.forEach(anImport -> recipe.append("                    maybeRemoveImport(\"").append(anImport).append("\");\n"));
    }

    private static void maybeRemoveStaticImports(Map<TemplateDescriptor, Set<String>> importsByTemplate, StringBuilder recipe, TemplateDescriptor beforeTemplate, int pos, TemplateDescriptor afterTemplate) {
        Set<String> beforeImports = beforeTemplate.usedMembers(pos).stream().map(symbol -> symbol.owner.getQualifiedName() + "." + symbol.name).collect(toCollection(LinkedHashSet::new));
        beforeImports.removeAll(getImportsAsStrings(importsByTemplate, afterTemplate));
        beforeImports.removeIf(i -> i.startsWith("java.lang.") || i.startsWith("com.google.errorprone.refaster."));
        beforeImports.forEach(anImport -> recipe.append("                    maybeRemoveImport(\"").append(anImport, 0, anImport.lastIndexOf('.')).append("\");\n"));
        beforeImports.forEach(anImport -> recipe.append("                    maybeRemoveImport(\"").append(anImport).append("\");\n"));
    }

    private static Set<String> getImportsAsStrings(Map<TemplateDescriptor, Set<String>> importsByTemplate, TemplateDescriptor templateMethod) {
        return importsByTemplate.entrySet().stream()
                .filter(e -> templateMethod == e.getKey())
                .map(Map.Entry::getValue)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    /* Generate the minimal precondition that would allow to match each before template individually. */
    private static @Nullable Precondition generatePreconditions(List<TemplateDescriptor> beforeTemplates) {
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
