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
 * OpenRewrite recipes created for Refaster template {@code foo.StringRules}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class StringRulesRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public StringRulesRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`StringRules` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster rules related to expressions dealing with `String`s.\n[Source](https://error-prone.picnic.tech/refasterrules/StringRules).";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new EmptyStringRecipe(),
                new StringIdentityRecipe(),
                new StringIsEmptyRecipe(),
                new StringIsEmptyPredicateRecipe(),
                new StringIsNotEmptyPredicateRecipe(),
                new StringIsNullOrEmptyRecipe(),
                new StringIsBlankRecipe(),
                new OptionalNonEmptyStringRecipe(),
                new FilterEmptyStringRecipe(),
                new JoinStringsRecipe(),
                new StringValueOfRecipe(),
                new NewStringFromCharArraySubSequenceRecipe(),
                new NewStringFromCharArrayRecipe(),
                new StringValueOfMethodReferenceRecipe(),
                new SubstringRemainderRecipe(),
                new Utf8EncodedLengthRecipe(),
                new StringIndexOfCharRecipe(),
                new StringIndexOfStringRecipe(),
                new StringLastIndexOfCharRecipe(),
                new StringLastIndexOfStringRecipe(),
                new StringLastIndexOfCharWithIndexRecipe(),
                new StringLastIndexOfStringWithIndexRecipe(),
                new StringStartsWithRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.EmptyString}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class EmptyStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public EmptyStringRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.EmptyString`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Avoid unnecessary creation of new empty `String` objects; use the empty string literal instead.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S2129");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before$0;
                JavaTemplate before$1;
                JavaTemplate before$2;
                JavaTemplate before$3;
                JavaTemplate before$4;
                JavaTemplate after;

                @Override
                public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("new String()")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("\"\"")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("new String(new byte[0], java.nio.charset.StandardCharsets.UTF_8)")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.nio.charset.StandardCharsets");
                        maybeRemoveImport("java.nio.charset.StandardCharsets.UTF_8");
                        if (after == null) {
                            after = JavaTemplate.builder("\"\"")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$2 == null) {
                        before$2 = JavaTemplate.builder("new String(new byte[]{}, java.nio.charset.StandardCharsets.UTF_8)")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$2.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.nio.charset.StandardCharsets");
                        maybeRemoveImport("java.nio.charset.StandardCharsets.UTF_8");
                        if (after == null) {
                            after = JavaTemplate.builder("\"\"")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$3 == null) {
                        before$3 = JavaTemplate.builder("new String(new char[0])")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$3.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("\"\"")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$4 == null) {
                        before$4 = JavaTemplate.builder("new String(new char[]{})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$4.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("\"\"")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitNewClass(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String <init>(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIdentity}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIdentityRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIdentityRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIdentity`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Avoid unnecessary creation of new `String` objects.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S2129");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("new String(#{str:any(java.lang.String)})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{str:any(java.lang.String)}")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitNewClass(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String <init>(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIsEmpty}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIsEmptyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIsEmptyRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIsEmpty`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#isEmpty()` over alternatives that consult the string's length.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S7158");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before$0;
                JavaTemplate before$1;
                JavaTemplate before$2;
                JavaTemplate after;

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("#{str:any(java.lang.String)}.length() == 0").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{str:any(java.lang.String)}.isEmpty()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("#{str:any(java.lang.String)}.length() <= 0").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{str:any(java.lang.String)}.isEmpty()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    if (before$2 == null) {
                        before$2 = JavaTemplate.builder("#{str:any(java.lang.String)}.length() < 1").build();
                    }
                    if ((matcher = before$2.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{str:any(java.lang.String)}.isEmpty()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String length(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIsEmptyPredicate}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIsEmptyPredicateRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIsEmptyPredicateRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIsEmptyPredicate`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer a method reference to `String#isEmpty()` over the equivalent lambda function.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitLambda(J.Lambda elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("(s)->s.isEmpty()")
                                .bindType("java.util.function.Predicate<java.lang.String>").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("String::isEmpty")
                                    .bindType("java.util.function.Predicate<java.lang.String>").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitLambda(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("java.util.function.Predicate", true),
                            new UsesMethod<>("java.lang.String isEmpty(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIsNotEmptyPredicate}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIsNotEmptyPredicateRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIsNotEmptyPredicateRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIsNotEmptyPredicate`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer a method reference to `String#isEmpty()` over the equivalent lambda function.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitLambda(J.Lambda elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("(s)->!s.isEmpty()")
                                .bindType("java.util.function.Predicate<java.lang.String>").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.function.Predicate.not(String::isEmpty)")
                                    .bindType("java.util.function.Predicate<java.lang.String>").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, STATIC_IMPORT_ALWAYS
                        );
                    }
                    return super.visitLambda(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("java.util.function.Predicate", true),
                            new UsesMethod<>("java.lang.String isEmpty(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIsNullOrEmpty}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIsNullOrEmptyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIsNullOrEmptyRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIsNullOrEmpty`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `Strings#isNullOrEmpty(String)` over the more verbose alternative.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{str:any(java.lang.String)} == null || #{str}.isEmpty()")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "jspecify-1"))
                                .build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Strings.isNullOrEmpty(#{str:any(java.lang.String)})")
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                    .build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String isEmpty(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIsBlank}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIsBlankRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIsBlankRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIsBlank`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#isBlank()` over less efficient alternatives.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{str:any(java.lang.String)}.trim().isEmpty()").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{str:any(java.lang.String)}.isBlank()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesMethod<>("java.lang.String isEmpty(..)", true),
                            new UsesMethod<>("java.lang.String trim(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.OptionalNonEmptyString}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class OptionalNonEmptyStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public OptionalNonEmptyStringRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.OptionalNonEmptyString`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Don't use the ternary operator to create an optionally-absent string.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before$0;
                JavaTemplate before$1;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("com.google.common.base.Strings.isNullOrEmpty(#{str:any(java.lang.String)}) ? java.util.Optional.empty() : java.util.Optional.of(#{str})")
                                .bindType("java.util.Optional<java.lang.String>")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Strings");
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.Optional.ofNullable(#{str:any(java.lang.String)}).filter(java.util.function.Predicate.not(String::isEmpty))")
                                    .bindType("java.util.Optional<java.lang.String>").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("com.google.common.base.Strings.isNullOrEmpty(#{str:any(java.lang.String)}) ? java.util.Optional.empty() : java.util.Optional.ofNullable(#{str})")
                                .bindType("java.util.Optional<java.lang.String>")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Strings");
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.Optional.ofNullable(#{str:any(java.lang.String)}).filter(java.util.function.Predicate.not(String::isEmpty))")
                                    .bindType("java.util.Optional<java.lang.String>").build();
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
                            new UsesType<>("java.util.Optional", true),
                            new UsesMethod<>("com.google.common.base.Strings isNullOrEmpty(..)", true),
                            new UsesMethod<>("java.util.Optional empty(..)", true),
                            Preconditions.or(
                                    new UsesMethod<>("java.util.Optional of(..)", true),
                                    new UsesMethod<>("java.util.Optional ofNullable(..)", true)
                            )
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.FilterEmptyString}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class FilterEmptyStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public FilterEmptyStringRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.FilterEmptyString`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\nstatic final class FilterEmptyString {\n    \n    @BeforeTemplate\n    Optional<String> before(Optional<String> optional) {\n        return optional.map(Strings::emptyToNull);\n    }\n    \n    @AfterTemplate\n    @UseImportPolicy(value = STATIC_IMPORT_ALWAYS)\n    Optional<String> after(Optional<String> optional) {\n        return optional.filter(not(String::isEmpty));\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{optional:any(java.util.Optional<java.lang.String>)}.map(com.google.common.base.Strings::emptyToNull)")
                                .bindType("java.util.Optional<java.lang.String>")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Strings");
                        if (after == null) {
                            after = JavaTemplate.builder("#{optional:any(java.util.Optional<java.lang.String>)}.filter(java.util.function.Predicate.not(String::isEmpty))")
                                    .bindType("java.util.Optional<java.lang.String>").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, STATIC_IMPORT_ALWAYS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("com.google.common.base.Strings", true),
                            new UsesType<>("java.util.Optional", true),
                            new UsesMethod<>("java.util.Optional map(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.JoinStrings}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class JoinStringsRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public JoinStringsRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.JoinStrings`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#join(CharSequence, Iterable)` and variants over the Guava alternative.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before$0;
                JavaTemplate before$1;
                JavaTemplate before0$0;
                JavaTemplate before0$1;
                JavaTemplate before1;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("com.google.common.base.Joiner.on(#{delimiter:any(java.lang.String)}).join(#{elements:any(java.lang.CharSequence[])})")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Joiner");
                        if (after == null) {
                            after = JavaTemplate.builder("String.join(#{delimiter:any(java.lang.CharSequence)}, #{elements:any(java.lang.Iterable<? extends java.lang.CharSequence>)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("java.util.Arrays.stream(#{elements:any(java.lang.CharSequence[])}).collect(java.util.stream.Collectors.joining(#{delimiter:any(java.lang.String)}))")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Arrays");
                        maybeRemoveImport("java.util.stream.Collectors");
                        maybeRemoveImport("java.util.stream.Collectors.joining");
                        if (after == null) {
                            after = JavaTemplate.builder("String.join(#{delimiter:any(java.lang.CharSequence)}, #{elements:any(java.lang.Iterable<? extends java.lang.CharSequence>)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(1), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0$0 == null) {
                        before0$0 = JavaTemplate.builder("com.google.common.base.Joiner.on(#{delimiter:any(java.lang.String)}).join(#{elements:any(java.lang.Iterable<? extends java.lang.CharSequence>)})")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before0$0.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Joiner");
                        if (after == null) {
                            after = JavaTemplate.builder("String.join(#{delimiter:any(java.lang.CharSequence)}, #{elements:any(java.lang.Iterable<? extends java.lang.CharSequence>)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before0$1 == null) {
                        before0$1 = JavaTemplate.builder("com.google.common.collect.Streams.stream(#{elements:any(java.lang.Iterable<? extends java.lang.CharSequence>)}).collect(java.util.stream.Collectors.joining(#{delimiter:any(java.lang.String)}))")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before0$1.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.collect.Streams");
                        maybeRemoveImport("java.util.stream.Collectors");
                        maybeRemoveImport("java.util.stream.Collectors.joining");
                        if (after == null) {
                            after = JavaTemplate.builder("String.join(#{delimiter:any(java.lang.CharSequence)}, #{elements:any(java.lang.Iterable<? extends java.lang.CharSequence>)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(1), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before1 == null) {
                        before1 = JavaTemplate.builder("#{elements:any(java.util.Collection<? extends java.lang.CharSequence>)}.stream().collect(java.util.stream.Collectors.joining(#{delimiter:any(java.lang.CharSequence)}))")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before1.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Collection");
                        maybeRemoveImport("java.util.stream.Collectors");
                        maybeRemoveImport("java.util.stream.Collectors.joining");
                        if (after == null) {
                            after = JavaTemplate.builder("String.join(#{delimiter:any(java.lang.CharSequence)}, #{elements:any(java.lang.Iterable<? extends java.lang.CharSequence>)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(1), matcher.parameter(0)),
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
                                    new UsesType<>("com.google.common.base.Joiner", true),
                                    new UsesMethod<>("com.google.common.base.Joiner join(..)", true),
                                    new UsesMethod<>("com.google.common.base.Joiner on(..)", true)
                            ),
                            Preconditions.and(
                                    new UsesType<>("com.google.common.collect.Streams", true),
                                    new UsesMethod<>("com.google.common.collect.Streams stream(..)", true),
                                    new UsesMethod<>("java.util.stream.Collectors joining(..)", true),
                                    new UsesMethod<>("java.util.stream.Stream collect(..)", true)
                            ),
                            Preconditions.and(
                                    new UsesType<>("java.util.Arrays", true),
                                    new UsesMethod<>("java.util.Arrays stream(..)", true),
                                    new UsesMethod<>("java.util.stream.Collectors joining(..)", true),
                                    new UsesMethod<>("java.util.stream.Stream collect(..)", true)
                            ),
                            Preconditions.and(
                                    new UsesType<>("java.util.Collection", true),
                                    new UsesMethod<>("java.util.Collection stream(..)", true),
                                    new UsesMethod<>("java.util.stream.Collectors joining(..)", true),
                                    new UsesMethod<>("java.util.stream.Stream collect(..)", true)
                            )
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringValueOf}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringValueOfRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringValueOfRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringValueOf`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer direct invocation of `String#valueOf(Object)` over the indirection introduced by `Objects#toString(Object)`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("java.util.Objects.toString(#{object:any(java.lang.Object)})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Objects");
                        if (after == null) {
                            after = JavaTemplate.builder("String.valueOf(#{object:any(java.lang.Object)})")
                                    .bindType("java.lang.String").build();
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
                            new UsesType<>("java.util.Objects", true),
                            new UsesMethod<>("java.util.Objects toString(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.NewStringFromCharArraySubSequence}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NewStringFromCharArraySubSequenceRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NewStringFromCharArraySubSequenceRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.NewStringFromCharArraySubSequence`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer direct invocation of `String#String(char[], int, int)` over the indirection introduced by alternatives.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before$0;
                JavaTemplate before$1;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("String.valueOf(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("String.copyValueOf(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
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
                            new UsesMethod<>("java.lang.String copyValueOf(..)", true),
                            new UsesMethod<>("java.lang.String valueOf(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.NewStringFromCharArray}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NewStringFromCharArrayRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NewStringFromCharArrayRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.NewStringFromCharArray`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer direct invocation of `String#String(char[])` over the indirection introduced by alternatives.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before$0;
                JavaTemplate before$1;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("String.valueOf(#{data:any(char[])})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("new String(#{data:any(char[])}, 0, #{data}.length)")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])})")
                                    .bindType("java.lang.String").build();
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

                @Override
                public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("String.valueOf(#{data:any(char[])})")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("new String(#{data:any(char[])}, 0, #{data}.length)")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitNewClass(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.or(
                            new UsesMethod<>("java.lang.String <init>(..)", true),
                            new UsesMethod<>("java.lang.String valueOf(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringValueOfMethodReference}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringValueOfMethodReferenceRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringValueOfMethodReferenceRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringValueOfMethodReference`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer direct delegation to `String#valueOf(Object)` over the indirection introduced by `Objects#toString(Object)`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitExpression(Expression elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("java.util.Objects::toString")
                                .bindType("java.util.function.Function<java.lang.Object, java.lang.String>").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Objects");
                        if (after == null) {
                            after = JavaTemplate.builder("String::valueOf")
                                    .bindType("java.util.function.Function<java.lang.Object, java.lang.String>").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitExpression(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("java.util.Objects", true),
                            new UsesType<>("java.util.function.Function", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.SubstringRemainder}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class SubstringRemainderRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public SubstringRemainderRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.SubstringRemainder`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Don't unnecessarily use the two-argument `String#substring(int, int)`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{str:any(java.lang.String)}.substring(#{index:any(int)}, #{str}.length())")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{str:any(java.lang.String)}.substring(#{index:any(int)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
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
                            new UsesMethod<>("java.lang.String length(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.Utf8EncodedLength}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class Utf8EncodedLengthRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public Utf8EncodedLengthRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.Utf8EncodedLength`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `Utf8#encodedLength(CharSequence)` over less efficient alternatives.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitFieldAccess(J.FieldAccess elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{str:any(java.lang.String)}.getBytes(java.nio.charset.StandardCharsets.UTF_8).length").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.nio.charset.StandardCharsets");
                        maybeRemoveImport("java.nio.charset.StandardCharsets.UTF_8");
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Utf8.encodedLength(#{str:any(java.lang.String)})")
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
                    return super.visitFieldAccess(elem, ctx);
                }

                @Override
                public J visitIdentifier(J.Identifier elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{str:any(java.lang.String)}.getBytes(java.nio.charset.StandardCharsets.UTF_8).length").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.nio.charset.StandardCharsets");
                        maybeRemoveImport("java.nio.charset.StandardCharsets.UTF_8");
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Utf8.encodedLength(#{str:any(java.lang.String)})")
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
                    return super.visitIdentifier(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String getBytes(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIndexOfChar}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIndexOfCharRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIndexOfCharRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIndexOfChar`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#indexOf(int, int)` over less efficient alternatives.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S4635");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{string:any(java.lang.String)}.substring(#{fromIndex:any(int)}).indexOf(#{ch:any(int)})").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("Math.max(-1, #{string:any(java.lang.String)}.indexOf(#{ch:any(int)}, #{fromIndex:any(int)}) - #{fromIndex})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(2), matcher.parameter(1)),
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
                            new UsesMethod<>("java.lang.String indexOf(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringIndexOfString}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIndexOfStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIndexOfStringRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringIndexOfString`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#indexOf(String, int)` over less efficient alternatives.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S4635");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{string:any(java.lang.String)}.substring(#{fromIndex:any(int)}).indexOf(#{substring:any(java.lang.String)})").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("Math.max(-1, #{string:any(java.lang.String)}.indexOf(#{substring:any(java.lang.String)}, #{fromIndex:any(int)}) - #{fromIndex})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(2), matcher.parameter(1)),
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
                            new UsesMethod<>("java.lang.String indexOf(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringLastIndexOfChar}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringLastIndexOfCharRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringLastIndexOfCharRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringLastIndexOfChar`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#lastIndexOf(int, int)` over less efficient alternatives.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S4635");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{string:any(java.lang.String)}.substring(#{fromIndex:any(int)}).lastIndexOf(#{ch:any(int)})").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("Math.max(-1, #{string:any(java.lang.String)}.lastIndexOf(#{ch:any(int)}) - #{fromIndex:any(int)})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(2), matcher.parameter(1)),
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
                            new UsesMethod<>("java.lang.String lastIndexOf(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringLastIndexOfString}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringLastIndexOfStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringLastIndexOfStringRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringLastIndexOfString`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#lastIndexOf(String, int)` over less efficient alternatives.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S4635");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{string:any(java.lang.String)}.substring(#{fromIndex:any(int)}).lastIndexOf(#{substring:any(java.lang.String)})").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("Math.max(-1, #{string:any(java.lang.String)}.lastIndexOf(#{substring:any(java.lang.String)}) - #{fromIndex:any(int)})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(2), matcher.parameter(1)),
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
                            new UsesMethod<>("java.lang.String lastIndexOf(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringLastIndexOfCharWithIndex}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringLastIndexOfCharWithIndexRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringLastIndexOfCharWithIndexRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringLastIndexOfCharWithIndex`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#lastIndexOf(int, int)` over less efficient alternatives.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{string:any(java.lang.String)}.substring(0, #{fromIndex:any(int)}).lastIndexOf(#{ch:any(int)})").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{string:any(java.lang.String)}.lastIndexOf(#{ch:any(int)}, #{fromIndex:any(int)} - 1)").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(2), matcher.parameter(1)),
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
                            new UsesMethod<>("java.lang.String lastIndexOf(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringLastIndexOfStringWithIndex}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringLastIndexOfStringWithIndexRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringLastIndexOfStringWithIndexRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringLastIndexOfStringWithIndex`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#lastIndexOf(String, int)` over less efficient alternatives.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{string:any(java.lang.String)}.substring(0, #{fromIndex:any(int)}).lastIndexOf(#{substring:any(java.lang.String)})").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{string:any(java.lang.String)}.lastIndexOf(#{substring:any(java.lang.String)}, #{fromIndex:any(int)} - 1)").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(2), matcher.parameter(1)),
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
                            new UsesMethod<>("java.lang.String lastIndexOf(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code StringRules.StringStartsWith}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringStartsWithRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringStartsWithRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `StringRules.StringStartsWith`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Prefer `String#startsWith(String, int)` over less efficient alternatives.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S4635");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{string:any(java.lang.String)}.substring(#{fromIndex:any(int)}).startsWith(#{prefix:any(java.lang.String)})").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{string:any(java.lang.String)}.startsWith(#{prefix:any(java.lang.String)}, #{fromIndex:any(int)})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(2), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesMethod<>("java.lang.String startsWith(..)", true),
                            new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

}

