package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.internal.template.AbstractRefasterJavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

import java.util.function.Supplier;

import java.util.Arrays;
import java.util.List;

import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;

public final class MultipleDereferencesRecipes extends Recipe {

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

                Supplier<JavaTemplate> before = memoize(() -> JavaTemplate.compile(this, "before", (JavaTemplate.P1<?>) (java.nio.file.Path p) -> java.nio.file.Files.delete(p)).build());

                Supplier<JavaTemplate> after = memoize(() -> JavaTemplate.compile(this, "after", (JavaTemplate.P1<?>) (java.nio.file.Path p) -> java.nio.file.Files.delete(p)).build());

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = matcher(before, getCursor())).find()) {
                        return embed(
                                apply(after, getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx
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

                Supplier<JavaTemplate> before = memoize(() -> JavaTemplate.compile(this, "before", (JavaTemplate.F1<?, ?>) (String s) -> s.isEmpty()).build());

                Supplier<JavaTemplate> after = memoize(() -> JavaTemplate.compile(this, "after", (String s) -> s != null && s.length() == 0).build());

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = matcher(before, getCursor())).find()) {
                        return embed(
                                apply(after, getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(0)),
                                getCursor(),
                                ctx
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

                Supplier<JavaTemplate> before = memoize(() -> JavaTemplate.compile(this, "before", (Object o) -> o == o).build());

                Supplier<JavaTemplate> after = memoize(() -> JavaTemplate.compile(this, "after", (Object o) -> true).build());

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = matcher(before, getCursor())).find()) {
                        return embed(apply(after, getCursor(), elem.getCoordinates().replace()), getCursor(), ctx);
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
            return javaVisitor;
        }
    }

}
