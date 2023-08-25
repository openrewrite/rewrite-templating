package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

import java.util.Arrays;
import java.util.List;

import java.util.Objects;

import static java.util.Objects.hash;

public final class ShouldAddImportsRecipes extends Recipe {

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
                new StaticImportObjectsHashRecipe()
        );
    }

    public static class StringValueOfRecipe extends Recipe {

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
            JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (JavaTemplate.F1<?, ?>) (String s) -> String.valueOf(s)).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (JavaTemplate.F1<?, ?>) (String s) -> Objects.toString(s)).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeAddImport("java.util.Objects");
                        return embed(after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)), ctx);
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

                private J embed(J j, ExecutionContext ctx) {
                    TreeVisitor<?, ExecutionContext> visitor;
                    if (!getAfterVisit().contains(visitor = new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor())) {
                        doAfterVisit(visitor);
                    }
                    if (!getAfterVisit().contains(visitor = new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor())) {
                        doAfterVisit(visitor);
                    }
                    j = new org.openrewrite.java.cleanup.SimplifyBooleanExpressionVisitor().visit(j, ctx, getCursor().getParent());
                    return j;
                }
            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String valueOf(..)"),
                    javaVisitor);
        }
    }

    public static class ObjectsEqualsRecipe extends Recipe {

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
            JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
                final JavaTemplate equals = JavaTemplate.compile(this, "equals", (JavaTemplate.F2<?, ?, ?>) (@Primitive Integer a, @Primitive Integer b) -> Objects.equals(a, b)).build();
                final JavaTemplate compareZero = JavaTemplate.compile(this, "compareZero", (@Primitive Integer a, @Primitive Integer b) -> Integer.compare(a, b) == 0).build();
                final JavaTemplate isis = JavaTemplate.compile(this, "isis", (@Primitive Integer a, @Primitive Integer b) -> a == b).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = equals.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Objects");
                        return embed(isis.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)), ctx);
                    }
                    if ((matcher = compareZero.matcher(getCursor())).find()) {
                        return embed(isis.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)), ctx);
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

                private J embed(J j, ExecutionContext ctx) {
                    TreeVisitor<?, ExecutionContext> visitor;
                    if (!getAfterVisit().contains(visitor = new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor())) {
                        doAfterVisit(visitor);
                    }
                    if (!getAfterVisit().contains(visitor = new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor())) {
                        doAfterVisit(visitor);
                    }
                    j = new org.openrewrite.java.cleanup.SimplifyBooleanExpressionVisitor().visit(j, ctx, getCursor().getParent());
                    return j;
                }
            };
            return Preconditions.check(
                    Preconditions.or(Preconditions.and(new UsesType<>("java.util.Objects", true), new UsesMethod<>("java.util.Objects equals(..)")), new UsesMethod<>("java.lang.Integer compare(..)")),
                    javaVisitor);
        }
    }

    public static class StaticImportObjectsHashRecipe extends Recipe {

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
            JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (JavaTemplate.F1<?, ?>) (String s) -> hash(s)).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (JavaTemplate.F1<?, ?>) (String s) -> s.hashCode()).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Objects.hash");
                        return embed(after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)), ctx);
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

                private J embed(J j, ExecutionContext ctx) {
                    TreeVisitor<?, ExecutionContext> visitor;
                    if (!getAfterVisit().contains(visitor = new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor())) {
                        doAfterVisit(visitor);
                    }
                    if (!getAfterVisit().contains(visitor = new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor())) {
                        doAfterVisit(visitor);
                    }
                    j = new org.openrewrite.java.cleanup.SimplifyBooleanExpressionVisitor().visit(j, ctx, getCursor().getParent());
                    return j;
                }
            };
            return Preconditions.check(
                    new UsesMethod<>("java.util.Objects hash(..)"),
                    javaVisitor);
        }
    }

}
