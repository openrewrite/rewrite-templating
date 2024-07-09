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
import org.openrewrite.marker.SearchResult;

import javax.annotation.Generated;
import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipes created for Refaster template {@code foo.ShouldAddImports}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class ShouldAddImportsRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
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
                new FileExistsRecipe(),
                new FindStringIsEmptyRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code ShouldAddImports.StringValueOf}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringValueOfRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
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
                final JavaTemplate before = JavaTemplate
                        .builder("String.valueOf(#{s:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("java.util.Objects.toString(#{s:any(java.lang.String)})")
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
                    new UsesMethod<>("java.lang.String valueOf(..)"),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code ShouldAddImports.ObjectsEquals}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class ObjectsEqualsRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
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
                final JavaTemplate equals = JavaTemplate
                        .builder("java.util.Objects.equals(#{a:any(int)}, #{b:any(int)})")
                        .build();
                final JavaTemplate compareZero = JavaTemplate
                        .builder("Integer.compare(#{a:any(int)}, #{b:any(int)}) == 0")
                        .build();
                final JavaTemplate isis = JavaTemplate
                        .builder("#{a:any(int)} == #{b:any(int)}")
                        .build();

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

    /**
     * OpenRewrite recipe created for Refaster template {@code ShouldAddImports.StaticImportObjectsHash}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StaticImportObjectsHashRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
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
                final JavaTemplate before = JavaTemplate
                        .builder("java.util.Objects.hash(#{s:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.hashCode()")
                        .build();

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

    /**
     * OpenRewrite recipe created for Refaster template {@code ShouldAddImports.FileExists}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class FileExistsRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
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
                final JavaTemplate before = JavaTemplate
                        .builder("#{path:any(java.nio.file.Path)}.toFile().exists()")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("java.nio.file.Files.exists(#{path:any(java.nio.file.Path)})")
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
                    Preconditions.and(
                            new UsesType<>("java.nio.file.Path", true),
                            new UsesMethod<>("java.io.File exists(..)"),
                            new UsesMethod<>("java.nio.file.Path toFile(..)")
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code ShouldAddImports.FindStringIsEmpty}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class FindStringIsEmptyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public FindStringIsEmptyRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldAddImports.FindStringIsEmpty`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class FindStringIsEmpty {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return s.isEmpty();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.isEmpty()")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
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

}
