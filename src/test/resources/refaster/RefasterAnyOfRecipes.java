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
package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.NonNullApi;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.ArrayList;

/**
 * OpenRewrite recipes created for Refaster template {@code foo.RefasterAnyOf}.
 */
@SuppressWarnings("all")
public class RefasterAnyOfRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public RefasterAnyOfRecipes() {}

    @Override
    public String getDisplayName() {
        return "`RefasterAnyOf` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.RefasterAnyOf`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringIsEmptyRecipe(),
                new EmptyListRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code RefasterAnyOf.StringIsEmpty}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class StringIsEmptyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIsEmptyRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `RefasterAnyOf.StringIsEmpty`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class StringIsEmpty {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return Refaster.anyOf(s.length() < 1, s.length() == 0);\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return s.isEmpty();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before$0 = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.length() < 1")
                        .build();
                final JavaTemplate before$1 = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.length() == 0")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.isEmpty()")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
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
                    new UsesMethod<>("java.lang.String length(..)"),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code RefasterAnyOf.EmptyList}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class EmptyListRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public EmptyListRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `RefasterAnyOf.EmptyList`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class EmptyList {\n    \n    @BeforeTemplate()\n    List before() {\n        return Refaster.anyOf(new java.util.LinkedList(), java.util.Collections.emptyList());\n    }\n    \n    @AfterTemplate()\n    List after() {\n        return new java.util.ArrayList();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before$0 = JavaTemplate
                        .builder("new java.util.LinkedList()")
                        .build();
                final JavaTemplate before$1 = JavaTemplate
                        .builder("java.util.Collections.emptyList()")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("new java.util.ArrayList()")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before$0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.LinkedList");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = before$1.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Collections");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
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
                            Preconditions.or(
                                    Preconditions.and(
                                            new UsesType<>("java.util.LinkedList", true),
                                            new UsesMethod<>("java.util.LinkedList <constructor>(..)")
                                    ),
                                    Preconditions.and(
                                            new UsesType<>("java.util.Collections", true),
                                            new UsesMethod<>("java.util.Collections emptyList(..)")
                                    )
                            )
                    ),
                    javaVisitor
            );
        }
    }

}
