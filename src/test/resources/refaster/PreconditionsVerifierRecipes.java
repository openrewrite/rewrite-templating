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
        //language=markdown
        return "`PreconditionsVerifier` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "A refaster template to test when a `UsesType`and Preconditions.or should or should not be applied to the Preconditions check.";
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
    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString {\n    \n    @BeforeTemplate\n    void before(double actual, int ignore) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate\n    void before(String actual, String ignore) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before0;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("System.out.println(#{actual:any(double)});").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0 == null) {
                        before0 = JavaTemplate.builder("System.out.println(#{actual:any(java.lang.String)});").build();
                    }
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
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
                            new UsesMethod<>("java.io.PrintStream println(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBody}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBodyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBodyRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `PreconditionsVerifier.UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBody`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBody {\n    \n    @BeforeTemplate\n    String before(String value) {\n        return Strings.nullToEmpty(value);\n    }\n    \n    @BeforeTemplate\n    String before(int value) {\n        return String.valueOf(value);\n    }\n    \n    @AfterTemplate\n    Object after(Object value) {\n        return Strings.nullToEmpty(String.valueOf(value));\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before0;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("com.google.common.base.Strings.nullToEmpty(#{value:any(java.lang.String)})")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (!isAssignableToTargetType("java.lang.Object")) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Strings.nullToEmpty(String.valueOf(#{value:any(java.lang.Object)}))")
                                    .bindType("java.lang.Object")
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                    .build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0 == null) {
                        before0 = JavaTemplate.builder("String.valueOf(#{value:any(int)})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        if (!isAssignableToTargetType("java.lang.Object")) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Strings.nullToEmpty(String.valueOf(#{value:any(java.lang.Object)}))")
                                    .bindType("java.lang.Object")
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                    .build();
                        }
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
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true)),
                            Preconditions.or(
                                    new UsesMethod<>("java.lang.String valueOf(..)", true),
                                    Preconditions.and(
                                            new UsesType<>("com.google.common.base.Strings", true),
                                            new UsesMethod<>("com.google.common.base.Strings nullToEmpty(..)", true)
                                    )
                            )
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
    public static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBodyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBodyRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `PreconditionsVerifier.UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBody`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBody {\n    \n    @BeforeTemplate\n    String before(String value) {\n        return Strings.nullToEmpty(value);\n    }\n    \n    @BeforeTemplate\n    String before(int value) {\n        return Strings.nullToEmpty(String.valueOf(value));\n    }\n    \n    @AfterTemplate\n    Object after(Object value) {\n        return Strings.nullToEmpty(String.valueOf(value));\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before0;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("com.google.common.base.Strings.nullToEmpty(#{value:any(java.lang.String)})")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (!isAssignableToTargetType("java.lang.Object")) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Strings.nullToEmpty(String.valueOf(#{value:any(java.lang.Object)}))")
                                    .bindType("java.lang.Object")
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                    .build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0 == null) {
                        before0 = JavaTemplate.builder("com.google.common.base.Strings.nullToEmpty(String.valueOf(#{value:any(int)}))")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        if (!isAssignableToTargetType("java.lang.Object")) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Strings.nullToEmpty(String.valueOf(#{value:any(java.lang.Object)}))")
                                    .bindType("java.lang.Object")
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                    .build();
                        }
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
                            new UsesType<>("com.google.common.base.Strings", true),
                            new UsesMethod<>("com.google.common.base.Strings nullToEmpty(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
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
    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType {\n    \n    @BeforeTemplate\n    void before(int actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate\n    void before(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before0;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("System.out.println(#{actual:any(int)});").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0 == null) {
                        before0 = JavaTemplate.builder("System.out.println(#{actual:any(java.util.Map<?, ?>)});").build();
                    }
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
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
                            new UsesMethod<>("java.io.PrintStream println(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `PreconditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType {\n    \n    @BeforeTemplate\n    void before(String actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate\n    void before(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before0;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("System.out.println(#{actual:any(java.lang.String)});").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0 == null) {
                        before0 = JavaTemplate.builder("System.out.println(#{actual:any(java.util.Map<?, ?>)});").build();
                    }
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
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
                            new UsesMethod<>("java.io.PrintStream println(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.UsesTypeMapWhenAllBeforeTemplatesContainsMap}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class UsesTypeMapWhenAllBeforeTemplatesContainsMapRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeMapWhenAllBeforeTemplatesContainsMapRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `PreconditionsVerifier.UsesTypeMapWhenAllBeforeTemplatesContainsMap`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeMapWhenAllBeforeTemplatesContainsMap {\n    \n    @BeforeTemplate\n    void mapWithGeneric(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate\n    void mapWithGenericTwo(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate\n    void mapWithoutGeneric(Map actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate mapWithGeneric;
                JavaTemplate mapWithGenericTwo;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (mapWithGeneric == null) {
                        mapWithGeneric = JavaTemplate.builder("System.out.println(#{actual:any(java.util.Map<?, ?>)});").build();
                    }
                    if ((matcher = mapWithGeneric.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.util.Map)});").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (mapWithGenericTwo == null) {
                        mapWithGenericTwo = JavaTemplate.builder("System.out.println(#{actual:any(java.util.Map<?, ?>)});").build();
                    }
                    if ((matcher = mapWithGenericTwo.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.util.Map)});").build();
                        }
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
                            new UsesType<>("java.util.Map", true),
                            new UsesMethod<>("java.io.PrintStream println(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreconditionsVerifier.UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `PreconditionsVerifier.UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList {\n    \n    @BeforeTemplate\n    void before(List<?> actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate\n    void before(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before0;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("System.out.println(#{actual:any(java.util.List<?>)});").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.List");
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0 == null) {
                        before0 = JavaTemplate.builder("System.out.println(#{actual:any(java.util.Map<?, ?>)});").build();
                    }
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
                        if (after == null) {
                            after = JavaTemplate.builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});").build();
                        }
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
                            new UsesMethod<>("java.io.PrintStream println(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true)),
                            Preconditions.or(
                                    new UsesType<>("java.util.List", true),
                                    new UsesType<>("java.util.Map", true)
                            )
                    ),
                    javaVisitor
            );
        }
    }

}
