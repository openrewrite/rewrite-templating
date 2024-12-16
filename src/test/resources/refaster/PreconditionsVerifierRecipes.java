/*
 * Copyright 2024 the original author or authors.
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
package foo;

import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import javax.annotation.Generated;
import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipes created for Refaster template {@code foo.PreconditionsVerifier}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class PreconditionsVerifierRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public PreconditionsVerifierRecipes() {}

    @Override
    public String getDisplayName() {
        return "A refaster template to test when a `UsesType`and Preconditions.or should or should not be applied to the Preconditions check";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.PreconditionsVerifier`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe(),
                new UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBodyRecipe(),
                new UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBodyRecipe(),
                new NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe(),
                new NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe(),
                new UsesTypeMapWhenAllBeforeTemplatesContainsMapRecipe(),
                new UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString {\n    \n    @BeforeTemplate()\n    void doubleAndInt(double actual, int ignore) {\n        double s = actual;\n    }\n    \n    @BeforeTemplate()\n    void stringAndString(String actual, String ignore) {\n        String s = actual;\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new AbstractRefasterJavaVisitor() {
                final JavaTemplate doubleAndInt = JavaTemplate
                        .builder("double s = #{actual:any(double)};")
                        .build();
                final JavaTemplate stringAndString = JavaTemplate
                        .builder("String s = #{actual:any(java.lang.String)};")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
                        .build();

                @Override
                public J visitStatement(Statement elem, ExecutionContext ctx) {
                    if (elem instanceof J.Block) {
                        // FIXME workaround
                        return elem;
                    }
                    JavaTemplate.Matcher matcher;
                    if ((matcher = doubleAndInt.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = stringAndString.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitStatement(elem, ctx);
                }

            };
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBody}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBodyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBodyRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreconditionsVerifier.UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBody`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBody {\n    \n    @BeforeTemplate()\n    String string(String value) {\n        return Convert.quote(value);\n    }\n    \n    @BeforeTemplate()\n    String _int(int value) {\n        return String.valueOf(value);\n    }\n    \n    @AfterTemplate()\n    Object after(Object actual) {\n        return Convert.quote(String.valueOf(actual));\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate string = JavaTemplate
                        .builder("com.sun.tools.javac.util.Convert.quote(#{value:any(java.lang.String)})")
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();
                final JavaTemplate _int = JavaTemplate
                        .builder("String.valueOf(#{value:any(int)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("com.sun.tools.javac.util.Convert.quote(String.valueOf(#{actual:any(java.lang.Object)}))")
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = string.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = _int.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.or(
                            Preconditions.and(
                                    new UsesType<>("com.sun.tools.javac.util.Convert", true),
                                    new UsesMethod<>("com.sun.tools.javac.util.Convert quote(..)", true)
                            ),
                            new UsesMethod<>("java.lang.String valueOf(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBody}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBodyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBodyRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreconditionsVerifier.UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBody`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBody {\n    \n    @BeforeTemplate()\n    String string(String value) {\n        return Convert.quote(value);\n    }\n    \n    @BeforeTemplate()\n    String _int(int value) {\n        return Convert.quote(String.valueOf(value));\n    }\n    \n    @AfterTemplate()\n    Object after(Object actual) {\n        return Convert.quote(String.valueOf(actual));\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate string = JavaTemplate
                        .builder("com.sun.tools.javac.util.Convert.quote(#{value:any(java.lang.String)})")
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();
                final JavaTemplate _int = JavaTemplate
                        .builder("com.sun.tools.javac.util.Convert.quote(String.valueOf(#{value:any(int)}))")
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("com.sun.tools.javac.util.Convert.quote(String.valueOf(#{actual:any(java.lang.Object)}))")
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = string.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = _int.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("com.sun.tools.javac.util.Convert", true),
                            new UsesMethod<>("com.sun.tools.javac.util.Convert quote(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType {\n    \n    @BeforeTemplate()\n    void _int(int actual) {\n        int s = actual;\n    }\n    \n    @BeforeTemplate()\n    void map(Map<?, ?> actual) {\n        Map<?, ?> m = actual;\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new AbstractRefasterJavaVisitor() {
                final JavaTemplate _int = JavaTemplate
                        .builder("int s = #{actual:any(int)};")
                        .build();
                final JavaTemplate map = JavaTemplate
                        .builder("java.util.Map<?, ?> m = #{actual:any(java.util.Map<?,?>)};")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
                        .build();

                @Override
                public J visitStatement(Statement elem, ExecutionContext ctx) {
                    if (elem instanceof J.Block) {
                        // FIXME workaround
                        return elem;
                    }
                    JavaTemplate.Matcher matcher;
                    if ((matcher = _int.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = map.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitStatement(elem, ctx);
                }

            };
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType {\n    \n    @BeforeTemplate()\n    void string(String actual) {\n        String s = actual;\n    }\n    \n    @BeforeTemplate()\n    void map(Map<?, ?> actual) {\n        Map<?, ?> m = (Map<?, ?>)actual;\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new AbstractRefasterJavaVisitor() {
                final JavaTemplate string = JavaTemplate
                        .builder("String s = #{actual:any(java.lang.String)};")
                        .build();
                final JavaTemplate map = JavaTemplate
                        .builder("java.util.Map<?, ?> m = (java.util.Map<?, ?>)#{actual:any(java.util.Map<?,?>)};")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
                        .build();

                @Override
                public J visitStatement(Statement elem, ExecutionContext ctx) {
                    if (elem instanceof J.Block) {
                        // FIXME workaround
                        return elem;
                    }
                    JavaTemplate.Matcher matcher;
                    if ((matcher = string.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = map.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitStatement(elem, ctx);
                }

            };
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.UsesTypeMapWhenAllBeforeTemplatesContainsMap}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsesTypeMapWhenAllBeforeTemplatesContainsMapRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeMapWhenAllBeforeTemplatesContainsMapRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreconditionsVerifier.UsesTypeMapWhenAllBeforeTemplatesContainsMap`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeMapWhenAllBeforeTemplatesContainsMap {\n    \n    @BeforeTemplate()\n    void mapWithGeneric(Map<?, ?> actual) {\n        Map<?, ?> m = actual;\n    }\n    \n    @BeforeTemplate()\n    void mapWithGenericTwo(Map<?, ?> actual) {\n        Map<?, ?> m = actual;\n    }\n    \n    @AfterTemplate()\n    void mapWithoutGeneric(Map actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate mapWithGeneric = JavaTemplate
                        .builder("java.util.Map<?, ?> m = #{actual:any(java.util.Map<?,?>)};")
                        .build();
                final JavaTemplate mapWithGenericTwo = JavaTemplate
                        .builder("java.util.Map<?, ?> m = #{actual:any(java.util.Map<?,?>)};")
                        .build();
                final JavaTemplate mapWithoutGeneric = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.util.Map)});")
                        .build();

                @Override
                public J visitStatement(Statement elem, ExecutionContext ctx) {
                    if (elem instanceof J.Block) {
                        // FIXME workaround
                        return elem;
                    }
                    JavaTemplate.Matcher matcher;
                    if ((matcher = mapWithGeneric.matcher(getCursor())).find()) {
                        return embed(
                                mapWithoutGeneric.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = mapWithGenericTwo.matcher(getCursor())).find()) {
                        return embed(
                                mapWithoutGeneric.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitStatement(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesType<>("java.util.Map", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreconditionsVerifier.UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList {\n    \n    @BeforeTemplate()\n    void list(List<?> actual) {\n        List<?> l = actual;\n    }\n    \n    @BeforeTemplate()\n    void map(Map<?, ?> actual) {\n        Map<?, ?> m = actual;\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate list = JavaTemplate
                        .builder("java.util.List<?> l = #{actual:any(java.util.List<?>)};")
                        .build();
                final JavaTemplate map = JavaTemplate
                        .builder("java.util.Map<?, ?> m = #{actual:any(java.util.Map<?,?>)};")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
                        .build();

                @Override
                public J visitStatement(Statement elem, ExecutionContext ctx) {
                    if (elem instanceof J.Block) {
                        // FIXME workaround
                        return elem;
                    }
                    JavaTemplate.Matcher matcher;
                    if ((matcher = list.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.List");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = map.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitStatement(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.or(
                            new UsesType<>("java.util.List", true),
                            new UsesType<>("java.util.Map", true)
                    ),
                    javaVisitor
            );
        }
    }

}