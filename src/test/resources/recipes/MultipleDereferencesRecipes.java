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


public final class MultipleDereferencesRecipes extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster recipes for `foo.MultipleDereferences`";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.MultipleDereferences`.";
    }


    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringIsEmptyRecipe(),
                new EqualsItselfRecipe()
        );
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
            JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (JavaTemplate.F1<?, ?>) (String s) -> s.isEmpty()).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (String s) -> s != null && s.length() == 0).build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        doAfterVisit(new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor());
                        doAfterVisit(new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor());
                        doAfterVisit(new org.openrewrite.staticanalysis.SimplifyBooleanExpression().getVisitor());
                        return after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(0));
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return javaVisitor;
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
            JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (Object o) -> o == o).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (Object o) -> true).build();

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        doAfterVisit(new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor());
                        doAfterVisit(new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor());
                        doAfterVisit(new org.openrewrite.staticanalysis.SimplifyBooleanExpression().getVisitor());
                        return after.apply(getCursor(), elem.getCoordinates().replace());
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
            return javaVisitor;
        }
    }

}
