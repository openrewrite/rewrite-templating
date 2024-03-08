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

/**
 * OpenRewrite recipes created for Refaster template {@code foo.UsesMethodPrecondition}.
 */
@SuppressWarnings("all")
public class UsesMethodPreconditionRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public UsesMethodPreconditionRecipes() {}

    @Override
    public String getDisplayName() {
        return "`UsesMethodPrecondition` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.UsesMethodPrecondition`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new PrimitiveRecipe(),
                new ClassRecipe(),
                new ParameterizedRecipe(),
                new VarargsRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code UsesMethodPrecondition.Primitive}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class PrimitiveRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public PrimitiveRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `UsesMethodPrecondition.Primitive`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class Primitive {\n    \n    @BeforeTemplate()\n    BigDecimal before(double d) {\n        return new BigDecimal(d);\n    }\n    \n    @AfterTemplate()\n    BigDecimal after(double d) {\n        return BigDecimal.valueOf(d);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("new java.math.BigDecimal(#{d:any(double)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("java.math.BigDecimal.valueOf(#{d:any(double)})")
                        .build();

                @Override
                public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
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
                    Preconditions.and(
                            new UsesType<>("java.math.BigDecimal", true),
                            new UsesMethod<>("java.math.BigDecimal <constructor>(double)")
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code UsesMethodPrecondition.Class}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class ClassRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public ClassRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `UsesMethodPrecondition.Class`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class Class {\n    \n    @BeforeTemplate()\n    String before(String s1, String s2) {\n        return s1.concat(s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s1, String s2) {\n        return s1 + s2;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s1:any(java.lang.String)}.concat(#{s2:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s1:any(java.lang.String)} + #{s2:any(java.lang.String)}")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
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
                    new UsesMethod<>("java.lang.String concat(java.lang.String)"),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code UsesMethodPrecondition.Parameterized}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class ParameterizedRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public ParameterizedRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `UsesMethodPrecondition.Parameterized`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class Parameterized {\n    \n    @BeforeTemplate()\n    List<String> before(String s) {\n        return Collections.singletonList(s);\n    }\n    \n    @AfterTemplate()\n    List<String> after(String s) {\n        return Arrays.asList(s);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("java.util.Collections.singletonList(#{s:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("java.util.Arrays.asList(#{s:any(java.lang.String)})")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Collections");
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
                            new UsesType<>("java.util.Collections", true),
                            new UsesType<>("java.util.List", true),
                            new UsesMethod<>("java.util.Collections singletonList(..)")
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code UsesMethodPrecondition.Varargs}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class VarargsRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public VarargsRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `UsesMethodPrecondition.Varargs`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class Varargs {\n    \n    @BeforeTemplate()\n    String before(String format, String arg0) {\n        return new Formatter().format(format, arg0).toString();\n    }\n    \n    @AfterTemplate()\n    String after(String format, String arg0) {\n        return String.format(format, arg0);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("new java.util.Formatter().format(#{format:any(java.lang.String)}, #{arg0:any(java.lang.String)}).toString()")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("String.format(#{format:any(java.lang.String)}, #{arg0:any(java.lang.String)})")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Formatter");
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
                            new UsesType<>("java.util.Formatter", true),
                            new UsesMethod<>("java.util.Formatter toString()"),
                            new UsesMethod<>("java.util.Formatter format(..)"),
                            new UsesMethod<>("java.util.Formatter <constructor>()")
                    ),
                    javaVisitor
            );
        }
    }

}
