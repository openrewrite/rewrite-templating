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
 * OpenRewrite recipes created for Refaster template {@code foo.RefasterVarargs}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class RefasterVarargsRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public RefasterVarargsRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`RefasterVarargs` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster template recipes for `foo.RefasterVarargs`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StreamOfToListRecipe(),
                new MinOfVarargsRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code RefasterVarargs.StreamOfToList}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class StreamOfToListRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StreamOfToListRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `RefasterVarargs.StreamOfToList`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class StreamOfToList<T> {\n    \n    @BeforeTemplate\n    List<T> before(@Repeated\n    T value) {\n        return Stream.of(Refaster.asVarargs(value)).toList();\n    }\n    \n    @AfterTemplate\n    List<T> after(@Repeated\n    T value) {\n        return Arrays.asList(value);\n    }\n}\n```\n.";
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
                        before = JavaTemplate.builder("java.util.stream.Stream.of(#{value:anyArray(T)}).toList()")
                                .bindType("java.util.List<T>")
                                .genericTypes("T").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.stream.Stream");
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.Arrays.asList(#{value:anyArray(T)})")
                                    .bindType("java.util.List<T>")
                                    .genericTypes("T").build();
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
                            new UsesType<>("java.util.List", true),
                            new UsesType<>("java.util.stream.Stream", true),
                            new UsesMethod<>("java.util.stream.Stream of(..)", true),
                            new UsesMethod<>("java.util.stream.Stream toList(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code RefasterVarargs.MinOfVarargs}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class MinOfVarargsRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public MinOfVarargsRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `RefasterVarargs.MinOfVarargs`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class MinOfVarargs<S, T extends S> {\n    \n    @BeforeTemplate\n    T before(@Repeated\n    T value, Comparator<S> cmp) {\n        return Stream.of(Refaster.asVarargs(value)).min(cmp).orElseThrow();\n    }\n    \n    @AfterTemplate\n    T after(@Repeated\n    T value, Comparator<S> cmp) {\n        return Collections.min(Arrays.asList(value), cmp);\n    }\n}\n```\n.";
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
                        before = JavaTemplate.builder("java.util.stream.Stream.of(#{value:anyArray(T)}).min(#{cmp:any(java.util.Comparator<S>)}).orElseThrow()")
                                .bindType("T")
                                .genericTypes("S", "T extends S").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.stream.Stream");
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.Collections.min(java.util.Arrays.asList(#{value:anyArray(T)}), #{cmp:any(java.util.Comparator<S>)})")
                                    .bindType("T")
                                    .genericTypes("S", "T extends S").build();
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
                            new UsesType<>("java.util.Comparator", true),
                            new UsesType<>("java.util.stream.Stream", true),
                            new UsesMethod<>("java.util.Optional orElseThrow(..)", true),
                            new UsesMethod<>("java.util.stream.Stream min(..)", true),
                            new UsesMethod<>("java.util.stream.Stream of(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                    ),
                    javaVisitor
            );
        }
    }

}
