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

import java.util.Objects;
import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static java.util.Objects.hash;

@SuppressWarnings("all")
public class ShouldAddImportsRecipes extends Recipe {
    public ShouldAddImportsRecipes() {}

    @Override
    public String getDisplayName() {
        return "`ShouldAddImports` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.ShouldAddImports`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringValueOfRecipe(),
                new ObjectsEqualsRecipe(),
                new StaticImportObjectsHashRecipe(),
                new FileExistsRecipe()
        );
    }

    @SuppressWarnings("all")
    @NonNullApi
    public static class StringValueOfRecipe extends Recipe {

        public StringValueOfRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldAddImports.StringValueOf`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class StringValueOf {\n    \n    @BeforeTemplate()\n    String before(String s) {\n        return String.valueOf(s);\n    }\n    \n    @AfterTemplate()\n    String after(String s) {\n        return Objects.toString(s);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = Semantics.expression(this, "before", (String s) -> String.valueOf(s)).build();
                final JavaTemplate after = Semantics.expression(this, "after", (String s) -> String.valueOf(s)).build();

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
                    new UsesMethod<>("java.lang.String valueOf(..)"),
                    javaVisitor
            );
        }
    }

    @SuppressWarnings("all")
    @NonNullApi
    public static class ObjectsEqualsRecipe extends Recipe {

        public ObjectsEqualsRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldAddImports.ObjectsEquals`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class ObjectsEquals {\n    \n    @BeforeTemplate()\n    boolean equals(int a, int b) {\n        return Objects.equals(a, b);\n    }\n    \n    @BeforeTemplate()\n    boolean compareZero(int a, int b) {\n        return Integer.compare(a, b) == 0;\n    }\n    \n    @AfterTemplate()\n    boolean isis(int a, int b) {\n        return a == b;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate equals = Semantics.expression(this, "equals", (@Primitive Integer a, @Primitive Integer b) -> java.util.Objects.equals(a, b)).build();
                final JavaTemplate compareZero = Semantics.expression(this, "compareZero", (@Primitive Integer a, @Primitive Integer b) -> Integer.compare(a, b) == 0).build();
                final JavaTemplate isis = Semantics.expression(this, "isis", (@Primitive Integer a, @Primitive Integer b) -> a == b).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = equals.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Objects");
                        return embed(
                                isis.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    if ((matcher = compareZero.matcher(getCursor())).find()) {
                        return embed(
                                isis.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.or(
                            Preconditions.and(
                                    new UsesType<>("java.util.Objects", true),
                                    new UsesMethod<>("java.util.Objects equals(..)")
                            ),
                            new UsesMethod<>("java.lang.Integer compare(..)")
                    ),
                    javaVisitor
            );
        }
    }

    @SuppressWarnings("all")
    @NonNullApi
    public static class StaticImportObjectsHashRecipe extends Recipe {

        public StaticImportObjectsHashRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldAddImports.StaticImportObjectsHash`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class StaticImportObjectsHash {\n    \n    @BeforeTemplate()\n    int before(String s) {\n        return hash(s);\n    }\n    \n    @AfterTemplate()\n    int after(String s) {\n        return s.hashCode();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = Semantics.expression(this, "before", (String s) -> hash(s)).build();
                final JavaTemplate after = Semantics.expression(this, "after", (String s) -> s.hashCode()).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Objects.hash");
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
                    new UsesMethod<>("java.util.Objects hash(..)"),
                    javaVisitor
            );
        }
    }

    @SuppressWarnings("all")
    @NonNullApi
    public static class FileExistsRecipe extends Recipe {

        public FileExistsRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldAddImports.FileExists`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class FileExists {\n    \n    @BeforeTemplate()\n    boolean before(Path path) {\n        return path.toFile().exists();\n    }\n    \n    @AfterTemplate()\n    @UseImportPolicy(value = ImportPolicy.STATIC_IMPORT_ALWAYS)\n    boolean after(Path path) {\n        return exists(path);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = Semantics.expression(this, "before", (java.nio.file.Path path) -> path.toFile().exists()).build();
                final JavaTemplate after = Semantics.expression(this, "after", (java.nio.file.Path path) -> exists(path)).build();

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
                    Preconditions.and(
                            new UsesType<>("java.nio.file.Path", true),
                            new UsesMethod<>("java.io.File exists(..)"),
                            new UsesMethod<>("java.nio.file.Path toFile(..)")
                    ),
                    javaVisitor
            );
        }
    }

}
