
package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

import java.util.Arrays;
import java.util.List;


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
            return "Recipe created for the following Refaster template:\n```java\npublic static class StringValueOf {\n    \n    @BeforeTemplate()\n    String before(String s) {\n        return String.valueOf(s);\n    }\n    \n    @AfterTemplate()\n    String after(String s) {\n        return java.util.Objects.toString(s);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (JavaTemplate.F1<?, ?>) (String s) -> String.valueOf(s)).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (JavaTemplate.F1<?, ?>) (String s) -> java.util.Objects.toString(s)).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        doAfterVisit(new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor());
                        doAfterVisit(new org.openrewrite.staticanalysis.UnnecessaryParentheses().getVisitor());
                        return after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0));
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
        }
    }

    public static class ObjectsEqualsRecipe extends Recipe {

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldAddImports.ObjectsEquals`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class ObjectsEquals {\n    \n    @BeforeTemplate()\n    boolean before(int a, int b) {\n        return java.util.Objects.equals(a, b);\n    }\n    \n    @AfterTemplate()\n    boolean after(int a, int b) {\n        return a == b;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (JavaTemplate.F2<?, ?, ?>) (@Primitive Integer a, @Primitive Integer b) -> java.util.Objects.equals(a, b)).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (@Primitive Integer a, @Primitive Integer b) -> a == b).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        doAfterVisit(new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor());
                        doAfterVisit(new org.openrewrite.staticanalysis.UnnecessaryParentheses().getVisitor());
                        return after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1));
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
        }
    }

}
