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
import org.openrewrite.java.template.Semantics;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * OpenRewrite recipes created for Refaster template {@code foo.MultipleDereferences}.
 */
@SuppressWarnings("all")
public class MultipleDereferencesRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public MultipleDereferencesRecipes() {}

    @Override
    public String getDisplayName() {
        return "`MultipleDereferences` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.MultipleDereferences`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new VoidTypeRecipe(),
                new StringIsEmptyRecipe(),
                new EqualsItselfRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code MultipleDereferences.VoidType}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class VoidTypeRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public VoidTypeRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `MultipleDereferences.VoidType`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class VoidType {\n    \n    @BeforeTemplate()\n    void before(Path p) throws IOException {\n        Files.delete(p);\n    }\n    \n    @AfterTemplate()\n    void after(Path p) throws IOException {\n        Files.delete(p);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("java.nio.file.Files.delete(foo.MultipleDereferences.VoidType.before.p)")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("java.nio.file.Files.delete(foo.MultipleDereferences.VoidType.after.p)")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
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
                            new UsesType<>("java.nio.file.Files", true),
                            new UsesType<>("java.nio.file.Path", true),
                            new UsesMethod<>("java.nio.file.Files delete(..)")
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code MultipleDereferences.StringIsEmpty}.
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
            return "Refaster template `MultipleDereferences.StringIsEmpty`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class StringIsEmpty {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return s.isEmpty();\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return s != null && s.length() == 0;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("foo.MultipleDereferences.StringIsEmpty.before.s.isEmpty()")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("foo.MultipleDereferences.StringIsEmpty.after.s != null && foo.MultipleDereferences.StringIsEmpty.after.s.length() == 0")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
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
                    new UsesMethod<>("java.lang.String isEmpty(..)"),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code MultipleDereferences.EqualsItself}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class EqualsItselfRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public EqualsItselfRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `MultipleDereferences.EqualsItself`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class EqualsItself {\n    \n    @BeforeTemplate()\n    boolean before(Object o) {\n        return o == o;\n    }\n    \n    @AfterTemplate()\n    boolean after(Object o) {\n        return true;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("foo.MultipleDereferences.EqualsItself.before.o == foo.MultipleDereferences.EqualsItself.before.o")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("true")
                        .build();

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace()),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
            return javaVisitor;
        }
    }

}
