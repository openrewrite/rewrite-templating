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
import java.io.IOException;
import java.nio.file.Path;

public class MultipleDereferencesRecipes extends Recipe {
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

    @NonNullApi
    public static class VoidTypeRecipe extends Recipe {

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
                final JavaTemplate before = Semantics.statement(this, "before", (java.nio.file.Path p) -> java.nio.file.Files.delete(p)).build();
                final JavaTemplate after = Semantics.statement(this, "after", (java.nio.file.Path p) -> java.nio.file.Files.delete(p)).build();

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
                            new UsesType<>("java.io.IOException", true),
                            new UsesType<>("java.nio.file.Files", true),
                            new UsesType<>("java.nio.file.Path", true),
                            new UsesMethod<>("java.nio.file.Files delete(..)")
                    ),
                    javaVisitor
            );
        }
    }

    @NonNullApi
    public static class StringIsEmptyRecipe extends Recipe {

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
                final JavaTemplate before = Semantics.expression(this, "before", (String s) -> s.isEmpty()).build();
                final JavaTemplate after = Semantics.expression(this, "after", (String s) -> s != null && s.length() == 0).build();

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

    @NonNullApi
    public static class EqualsItselfRecipe extends Recipe {

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
                final JavaTemplate before = Semantics.expression(this, "before", (Object o) -> o == o).build();
                final JavaTemplate after = Semantics.expression(this, "after", (Object o) -> true).build();

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(after.apply(getCursor(), elem.getCoordinates().replace()), getCursor(), ctx);
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
            return javaVisitor;
        }
    }

}
