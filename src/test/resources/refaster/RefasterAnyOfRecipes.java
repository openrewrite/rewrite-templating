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
 * OpenRewrite recipes created for Refaster template {@code foo.RefasterAnyOf}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class RefasterAnyOfRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public RefasterAnyOfRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`RefasterAnyOf` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster template recipes for `foo.RefasterAnyOf`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringIsEmptyRecipe(),
                new EmptyListRecipe(),
                new NewStringFromCharArraySubSequenceRecipe(),
                new ChangeOrderParametersRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code RefasterAnyOf.StringIsEmpty}.
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
            return "Refaster template `RefasterAnyOf.StringIsEmpty`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class StringIsEmpty {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return Refaster.anyOf(s.length() < 1, s.length() == 0);\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return s.isEmpty();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before$0;
                JavaTemplate before$1;
                JavaTemplate after;

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before$0 == null) {
                        before$0 = JavaTemplate.builder("#{s:any(java.lang.String)}.length() < 1").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{s:any(java.lang.String)}.isEmpty()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("#{s:any(java.lang.String)}.length() == 0").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{s:any(java.lang.String)}.isEmpty()").build();
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
     * OpenRewrite recipe created for Refaster template {@code RefasterAnyOf.EmptyList}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class EmptyListRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public EmptyListRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `RefasterAnyOf.EmptyList`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class EmptyList {\n    \n    @BeforeTemplate()\n    List before() {\n        return Refaster.anyOf(new LinkedList(), java.util.Collections.emptyList());\n    }\n    \n    @AfterTemplate()\n    List after() {\n        return new java.util.ArrayList();\n    }\n}\n```\n.";
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
                        before$0 = JavaTemplate.builder("new java.util.LinkedList()").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.LinkedList");
                        if (after == null) {
                            after = JavaTemplate.builder("new java.util.ArrayList()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("java.util.Collections.emptyList()").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Collections");
                        if (after == null) {
                            after = JavaTemplate.builder("new java.util.ArrayList()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
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
                        before$0 = JavaTemplate.builder("new java.util.LinkedList()").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.LinkedList");
                        if (after == null) {
                            after = JavaTemplate.builder("new java.util.ArrayList()").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("java.util.Collections.emptyList()").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Collections");
                        if (after == null) {
                            after = JavaTemplate.builder("new java.util.ArrayList()").build();
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
                    Preconditions.and(
                            new UsesType<>("java.util.List", true),
                            Preconditions.or(
                                    Preconditions.and(
                                            new UsesType<>("java.util.Collections", true),
                                            new UsesMethod<>("java.util.Collections emptyList(..)", true)
                                    ),
                                    Preconditions.and(
                                            new UsesType<>("java.util.LinkedList", true),
                                            new UsesMethod<>("java.util.LinkedList <init>(..)", true)
                                    )
                            )
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code RefasterAnyOf.NewStringFromCharArraySubSequence}.
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
            return "Refaster template `RefasterAnyOf.NewStringFromCharArraySubSequence`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class NewStringFromCharArraySubSequence {\n    \n    @BeforeTemplate()\n    String before(char[] data, int offset, int count) {\n        return Refaster.anyOf(String.valueOf(data, offset, count), String.copyValueOf(data, offset, count));\n    }\n    \n    @AfterTemplate()\n    String after(char[] data, int offset, int count) {\n        return new String(data, offset, count);\n    }\n}\n```\n.";
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
                        before$0 = JavaTemplate.builder("String.valueOf(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("String.copyValueOf(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("new String(#{data:any(char[])}, #{offset:any(int)}, #{count:any(int)})").build();
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
     * OpenRewrite recipe created for Refaster template {@code RefasterAnyOf.ChangeOrderParameters}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class ChangeOrderParametersRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public ChangeOrderParametersRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `RefasterAnyOf.ChangeOrderParameters`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class ChangeOrderParameters {\n    \n    @BeforeTemplate()\n    Duration before(OffsetDateTime a, OffsetDateTime b) {\n        return Refaster.anyOf(Duration.between(a.toInstant(), b.toInstant()), Duration.ofSeconds(b.toEpochSecond() - a.toEpochSecond()));\n    }\n    \n    @AfterTemplate()\n    Duration after(OffsetDateTime a, OffsetDateTime b) {\n        return Duration.between(a, b);\n    }\n}\n```\n.";
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
                        before$0 = JavaTemplate.builder("java.time.Duration.between(#{a:any(java.time.OffsetDateTime)}.toInstant(), #{b:any(java.time.OffsetDateTime)}.toInstant())").build();
                    }
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("java.time.Duration.between(#{a:any(java.time.OffsetDateTime)}, #{b:any(java.time.OffsetDateTime)})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before$1 == null) {
                        before$1 = JavaTemplate.builder("java.time.Duration.ofSeconds(#{b:any(java.time.OffsetDateTime)}.toEpochSecond() - #{a:any(java.time.OffsetDateTime)}.toEpochSecond())").build();
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("java.time.Duration.between(#{a:any(java.time.OffsetDateTime)}, #{b:any(java.time.OffsetDateTime)})").build();
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
                    Preconditions.and(
                            new UsesType<>("java.time.Duration", true),
                            new UsesType<>("java.time.OffsetDateTime", true),
                            Preconditions.or(
                                    Preconditions.and(
                                            new UsesMethod<>("java.time.Duration between(..)", true),
                                            new UsesMethod<>("java.time.OffsetDateTime toInstant(..)", true)
                                    ),
                                    Preconditions.and(
                                            new UsesMethod<>("java.time.Duration ofSeconds(..)", true),
                                            new UsesMethod<>("java.time.OffsetDateTime toEpochSecond(..)", true)
                                    )
                            )
                    ),
                    javaVisitor
            );
        }
    }

}
