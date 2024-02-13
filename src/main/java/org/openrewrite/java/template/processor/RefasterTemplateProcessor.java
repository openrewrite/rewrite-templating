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
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.template.internal.FQNPretty;
import org.openrewrite.java.template.internal.ImportDetector;
import org.openrewrite.java.template.internal.JavacResolution;
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
            "com.google.errorprone.refaster.annotation.Repeated"
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
            } else if (JCTree.JCConditional.class.isAssignableFrom(type)) {
                return singletonList("J.Ternary");
            } else if (JCTree.JCNewClass.class.isAssignableFrom(type)) {
                return singletonList("J.NewClass");
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

        // Inform how many rules were skipped and why; useful for debugging, but not enabled by default
        //printedMessages.entrySet().stream().sorted(Map.Entry.comparingByValue())
        //        .forEach(entry -> processingEnv.getMessager().printMessage(Kind.NOTE, entry.toString()));

        // Give other annotation processors a chance to process the same annotations, for dual use of Refaster templates
        return false;
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

                    String templateFqn = classDecl.sym.fullname.toString() + "Recipe";
                    String templateCode = copy.toString().trim();

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
                        imports.removeIf(i -> {
                            int endIndex = i.lastIndexOf('.');
                            return endIndex < 0 || "java.lang".equals(i.substring(0, endIndex));
                        });
                        imports.remove(BEFORE_TEMPLATE);
                        imports.remove(AFTER_TEMPLATE);
                    }

                    Map<String, JCTree.JCMethodDecl> beforeTemplates = new LinkedHashMap<>();
                    for (JCTree.JCMethodDecl templ : descriptor.beforeTemplates) {
                        String name = templ.getName().toString();
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
                    String after = descriptor.afterTemplate.getName().toString();

                    StringBuilder recipe = new StringBuilder();
                    Symbol.PackageSymbol pkg = classDecl.sym.packge();
                    String typeName = classDecl.sym.fullname.toString();
                    String refasterRuleClassName = pkg.isUnnamed() ? typeName : typeName.substring(pkg.fullname.length() + 1);
                    recipe.append("/**\n * OpenRewrite recipe created for Refaster template {@code ").append(refasterRuleClassName).append("}.\n */\n");
                    String recipeName = templateFqn.substring(templateFqn.lastIndexOf('.') + 1);
                    recipe.append("@SuppressWarnings(\"all\")\n");
                    recipe.append("@NonNullApi\n");
                    recipe.append(descriptor.classDecl.sym.outermostClass() == descriptor.classDecl.sym ?
                            "public class " : "public static class ").append(recipeName).append(" extends Recipe {\n\n");
                    recipe.append("    /**\n");
                    recipe.append("     * Instantiates a new instance.\n");
                    recipe.append("     */\n");
                    recipe.append("    public ").append(recipeName).append("() {}\n\n");
                    recipe.append(recipeDescriptor(classDecl,
                            "Refaster template `" + refasterRuleClassName + '`',
                            "Recipe created for the following Refaster template:\\n```java\\n" + escape(templateCode) + "\\n```\\n."
                    ));
                    recipe.append("    @Override\n");
                    recipe.append("    public TreeVisitor<?, ExecutionContext> getVisitor() {\n");
                    recipe.append("        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {\n");
                    for (Map.Entry<String, JCTree.JCMethodDecl> entry : beforeTemplates.entrySet()) {
                        recipe.append("            final JavaTemplate ")
                                .append(entry.getKey())
                                .append(" = Semantics.")
                                .append(statementType(entry.getValue()))
                                .append("(this, \"")
                                .append(entry.getKey()).append("\", ")
                                .append(toLambda(entry.getValue()))
                                .append(").build();\n");
                    }
                    recipe.append("            final JavaTemplate ")
                            .append(after)
                            .append(" = Semantics.")
                            .append(statementType(descriptor.afterTemplate))
                            .append("(this, \"")
                            .append(after)
                            .append("\", ")
                            .append(toLambda(descriptor.afterTemplate))
                            .append(").build();\n");
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
                        for (Map.Entry<String, JCTree.JCMethodDecl> entry : beforeTemplates.entrySet()) {
                            recipe.append("                if (" + "(matcher = ").append(entry.getKey()).append(".matcher(getCursor())).find()").append(") {\n");
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

                            maybeRemoveImports(imports, recipe, entry.getValue(), descriptor.afterTemplate);
                            maybeRemoveImports(staticImports, recipe, entry.getValue(), descriptor.afterTemplate);

                            List<String> embedOptions = new ArrayList<>();
                            JCTree.JCExpression afterReturn = getReturnExpression(descriptor.afterTemplate);
                            if (afterReturn instanceof JCTree.JCParens ||
                                afterReturn instanceof JCTree.JCUnary && ((JCTree.JCUnary) afterReturn).getExpression() instanceof JCTree.JCParens) {
                                embedOptions.add("REMOVE_PARENS");
                            }
                            // TODO check if after template contains type or member references
                            embedOptions.add("SHORTEN_NAMES");
                            if (simplifyBooleans(descriptor.afterTemplate)) {
                                embedOptions.add("SIMPLIFY_BOOLEANS");
                            }

                            recipe.append("                    return embed(\n");
                            recipe.append("                            ").append(after).append(".apply(getCursor(), elem.getCoordinates().replace()");
                            if (!parameters.isEmpty()) {
                                recipe.append(", ").append(parameters);
                            }
                            recipe.append("),\n");
                            recipe.append("                            getCursor(),\n");
                            recipe.append("                            ctx,\n");
                            recipe.append("                            ").append(String.join(", ", embedOptions)).append("\n");
                            recipe.append("                    );\n");
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
                        Symbol.PackageSymbol pkg = classDecl.sym.packge();
                        String inputOuterFQN = outerClassRequired ? classDecl.sym.fullname.toString() : descriptor.classDecl.sym.fullname.toString();
                        String className = inputOuterFQN + (outerClassRequired ? "Recipes" : "Recipe");
                        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className);
                        try (Writer out = new BufferedWriter(builderFile.openWriter())) {
                            if (!pkg.isUnnamed()) {
                                out.write("package " + pkg.fullname + ";\n");
                                out.write("\n");
                            }
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
                            out.write("import java.util.*;\n");
                            out.write("\n");
                            out.write("import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;\n");

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
                                out.write("/**\n * OpenRewrite recipes created for Refaster template {@code " + inputOuterFQN + "}.\n */\n");
                                String outerClassName = className.substring(className.lastIndexOf('.') + 1);
                                out.write("@SuppressWarnings(\"all\")\n");
                                out.write("public class " + outerClassName + " extends Recipe {\n");
                                out.write("    /**\n");
                                out.write("     * Instantiates a new instance.\n");
                                out.write("     */\n");
                                out.write("    public " + outerClassName + "() {}\n\n");
                                out.write(recipeDescriptor(classDecl,
                                        String.format("`%s` Refaster recipes", inputOuterFQN.substring(inputOuterFQN.lastIndexOf('.') + 1)),
                                        String.format("Refaster template recipes for `%s`.", inputOuterFQN)));
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
                    public void visitUnary(JCTree.JCUnary jcUnary) {
                        found |= jcUnary.type.getTag() == TypeTag.BOOLEAN;
                        super.visitUnary(jcUnary);
                    }
                }.find(template.getBody());
            }

            private String recipeDescriptor(JCTree.JCClassDecl classDecl, String defaultDisplayName, String defaultDescription) {
                String displayName = defaultDisplayName;
                String description = defaultDescription;
                Set<String> tags = new LinkedHashSet<>();

                // Extract from JavaDoc
                Tokens.Comment comment = cu.docComments.getComment(classDecl);
                if (comment != null && comment.getText() != null && !comment.getText().isEmpty()) {
                    String commentText = comment.getText()
                            .replaceAll("\\{@\\S+\\s+(.*?)}", "`$1`")
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\b", "\\b")
                            .replace("\t", "\\t")
                            .replace("\f", "\\f")
                            .replace("\r", "\\r");
                    String[] lines = commentText.split("\\.\\R+", 2);
                    displayName = lines[0].trim().replace("\n", "");
                    if (displayName.endsWith(".")) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }
                    if (lines.length > 1 && !lines[1].trim().isEmpty()) {
                        description = lines[1].trim().replace("\n", "\\n");
                        if (!description.endsWith(".")) {
                            description += '.';
                        }
                    }
                }

                // Extract from the RecipeDescriptor annotation
                for (JCTree.JCAnnotation annotation : classDecl.getModifiers().getAnnotations()) {
                    if (annotation.type.toString().equals("org.openrewrite.java.template.RecipeDescriptor")) {
                        for (JCTree.JCExpression argExpr : annotation.getArguments()) {
                            JCTree.JCAssign arg = (JCTree.JCAssign) argExpr;
                            switch (arg.lhs.toString()) {
                                case "name":
                                    displayName = escapeJava(((JCTree.JCLiteral) arg.rhs).getValue().toString());
                                    break;
                                case "description":
                                    description = escapeJava(((JCTree.JCLiteral) arg.rhs).getValue().toString());
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
                    }
                }

                String recipeDescriptor = "    @Override\n" +
                                          "    public String getDisplayName() {\n" +
                                          "        return \"" + displayName + "\";\n" +
                                          "    }\n" +
                                          "\n" +
                                          "    @Override\n" +
                                          "    public String getDescription() {\n" +
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

            private void maybeRemoveImports(Map<JCTree.JCMethodDecl, Set<String>> importsByTemplate, StringBuilder recipe, JCTree.JCMethodDecl beforeTemplate, JCTree.JCMethodDecl afterTemplate) {
                Set<String> beforeImports = getBeforeImportsAsStrings(importsByTemplate, beforeTemplate);
                beforeImports.removeAll(getImportsAsStrings(importsByTemplate, afterTemplate));
                beforeImports.removeIf(i -> i.startsWith("java.lang."));
                beforeImports.forEach(anImport -> recipe.append("                    maybeRemoveImport(\"").append(anImport).append("\");\n"));
            }

            private Set<String> getBeforeImportsAsStrings(Map<JCTree.JCMethodDecl, Set<String>> importsByTemplate, JCTree.JCMethodDecl templateMethod) {
                Set<String> beforeImports = getImportsAsStrings(importsByTemplate, templateMethod);
                for (JCTree.JCMethodDecl beforeTemplate : importsByTemplate.keySet()) {
                    // add fully qualified imports inside the template to the "before imports" set,
                    // since in the code that is being matched the type may not be fully qualified
                    new TreeScanner() {
                        @Override
                        public void scan(JCTree tree) {
                            if (tree instanceof JCTree.JCFieldAccess &&
                                ((JCTree.JCFieldAccess) tree).sym instanceof Symbol.ClassSymbol) {
                                if (tree.toString().equals(((JCTree.JCFieldAccess) tree).sym.toString())) {
                                    beforeImports.add(((JCTree.JCFieldAccess) tree).sym.toString());
                                }
                            }
                            super.scan(tree);
                        }
                    }.scan(beforeTemplate.getBody());
                }
                return beforeImports;
            }

            private Set<String> getImportsAsStrings(Map<JCTree.JCMethodDecl, Set<String>> importsByTemplate, JCTree.JCMethodDecl templateMethod) {
                return importsByTemplate.entrySet().stream()
                        .filter(e -> templateMethod == e.getKey())
                        .map(Map.Entry::getValue)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
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
                        String methodName = method.name.toString();
                        methodName = methodName.equals("<init>") ? "<constructor>" : methodName;
                        usesVisitors.add("new UsesMethod<>(\"" + method.owner.getQualifiedName().toString() + ' ' + methodName + "(..)\")");
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
        return string.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\R", "\\\\n");
    }

    private String parameters(TemplateDescriptor descriptor) {
        List<Integer> afterParams = new ArrayList<>();
        Set<Symbol> seenParams = new HashSet<>();
        new TreeScanner() {
            @Override
            public void scan(JCTree jcTree) {
                if (jcTree instanceof JCTree.JCIdent) {
                    JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcTree;
                    if (jcIdent.sym instanceof Symbol.VarSymbol
                        && jcIdent.sym.owner instanceof Symbol.MethodSymbol
                        && ((Symbol.MethodSymbol) jcIdent.sym.owner).params.contains(jcIdent.sym)
                        && seenParams.add(jcIdent.sym)) {
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
        JCTree.JCExpression returnExpression = getReturnExpression(method);
        return returnExpression != null ? returnExpression.getClass() : method.getBody().getStatements().last().getClass();
    }

    @Nullable
    private JCTree.JCExpression getReturnExpression(JCTree.JCMethodDecl method) {
        JCTree.JCStatement statement = method.getBody().getStatements().last();
        if (statement instanceof JCTree.JCReturn) {
            return ((JCTree.JCReturn) statement).expr;
        } else if (statement instanceof JCTree.JCExpressionStatement) {
            return ((JCTree.JCExpressionStatement) statement).expr;
        }
        return null;
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
            String paramType = parameter.getType().type.toString();
            if (!getBoxedPrimitive(paramType).equals(paramType)) {
                paramType = "@Primitive " + getBoxedPrimitive(paramType);
            } else if (paramType.startsWith("java.lang.")) {
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

            if (classDecl.typarams != null && !classDecl.typarams.isEmpty()) {
                printNoteOnce("Generic type parameters are currently not supported", classDecl.sym);
                return null;
            }

            for (JCTree member : classDecl.getMembers()) {
                if (member instanceof JCTree.JCMethodDecl && !beforeTemplates.contains(member) && member != afterTemplate) {
                    for (JCTree.JCAnnotation annotation : getTemplateAnnotations(((JCTree.JCMethodDecl) member), UNSUPPORTED_ANNOTATIONS::contains)) {
                        printNoteOnce("@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                        return null;
                    }
                }
            }

            // resolve so that we can inspect the template body
            boolean valid = resolve(context, cu);
            if (valid) {
                for (JCTree.JCMethodDecl template : beforeTemplates) {
                    valid = valid && validateTemplateMethod(template);
                }
                valid = valid && validateTemplateMethod(afterTemplate);
            }
            return valid ? this : null;
        }

        private boolean validateTemplateMethod(JCTree.JCMethodDecl template) {
            if (template.typarams != null && !template.typarams.isEmpty()) {
                printNoteOnce("Generic type parameters are currently not supported", classDecl.sym);
                return false;
            }
            for (JCTree.JCAnnotation annotation : getTemplateAnnotations(template, UNSUPPORTED_ANNOTATIONS::contains)) {
                printNoteOnce("@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                return false;
            }
            for (JCTree.JCVariableDecl parameter : template.getParameters()) {
                for (JCTree.JCAnnotation annotation : getTemplateAnnotations(parameter, UNSUPPORTED_ANNOTATIONS::contains)) {
                    printNoteOnce("@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                    return false;
                }
                if (parameter.vartype.type instanceof Type.TypeVar) {
                    printNoteOnce("Generic type parameters are currently not supported", classDecl.sym);
                    return false;
                }
            }
            if (template.restype.type instanceof Type.TypeVar) {
                printNoteOnce("Generic type parameters are currently not supported", classDecl.sym);
                return false;
            }
            if (template.body.stats.get(0) instanceof JCTree.JCIf) {
                printNoteOnce("If statements are currently not supported", classDecl.sym);
                return false;
            }
            if (template.body.stats.get(0) instanceof JCTree.JCReturn) {
                JCTree.JCExpression expr = ((JCTree.JCReturn) template.body.stats.get(0)).expr;
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

                boolean validate(JCTree tree) {
                    scan(tree);
                    return valid;
                }

                @Override
                public void visitIdent(JCTree.JCIdent jcIdent) {
                    if (valid
                        && jcIdent.sym != null
                        && jcIdent.sym.packge().getQualifiedName().contentEquals("com.google.errorprone.refaster")) {
                        printNoteOnce(jcIdent.type.tsym.getQualifiedName() + " is currently not supported", classDecl.sym);
                        valid = false;
                    }
                }
            }.validate(template.getBody());
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
