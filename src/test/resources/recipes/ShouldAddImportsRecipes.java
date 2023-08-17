
package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.ShortenFullyQualifiedTypeReferences;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

import java.util.Arrays;
import java.util.List;

import java.util.Objects;

public final class ShouldAddImportsRecipes extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster recipes for `foo.ShouldAddImports`";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.ShouldAddImports`.";
    }


    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringValueOfRecipe(),
                new ObjectsEqualsRecipe()
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
                        doAfterVisit(new ShortenFullyQualifiedTypeReferences().getVisitor());
                        return after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0));
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }
            };
            return Preconditions.check(Preconditions.or(
                            Preconditions.and(new UsesType<>("java.util.Objects", false))),
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
                    if ((matcher = equals.matcher(getCursor())).find() || (matcher = compareZero.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Objects");
                        doAfterVisit(new ShortenFullyQualifiedTypeReferences().getVisitor());
                        return isis.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1));
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }
            };
            return Preconditions.check(Preconditions.or(
                            Preconditions.and(new UsesType<>("java.util.Objects", false))),
                    javaVisitor);
        }
    }

}
