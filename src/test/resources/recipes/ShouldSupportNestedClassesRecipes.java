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


public final class ShouldSupportNestedClassesRecipes extends Recipe {

    @Override
    public String getDisplayName() {
        return "`ShouldSupportNestedClasses` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.ShouldSupportNestedClasses`.";
    }


    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new NestedClassRecipe(),
                new AnotherClassRecipe()
        );
    }

    public static class NestedClassRecipe extends Recipe {

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldSupportNestedClasses.NestedClass`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class NestedClass {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return s.length() > 0;\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return !s.isEmpty();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
                private JavaTemplate before;
                private JavaTemplate beforeTemplate() {
                    if (before == null)
                        before = JavaTemplate.compile(this, "before", (String s) -> s.length() > 0).build();
                    return before;
                };

                JavaTemplate after;
                JavaTemplate afterTemplate() {
                    if (after == null)
                        after = JavaTemplate.compile(this, "after", (String s) -> !s.isEmpty()).build();
                    return after;
                };

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = beforeTemplate().matcher(getCursor())).find()) {
                        return embed(afterTemplate().apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)), ctx);
                    }
                    return super.visitBinary(elem, ctx);
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
                    new UsesMethod<>("java.lang.String length(..)"),
                    javaVisitor);
        }
    }

    static class AnotherClassRecipe extends Recipe {

        @Override
        public String getDisplayName() {
            return "Refaster template `ShouldSupportNestedClasses.AnotherClass`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\nstatic class AnotherClass {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return s.length() == 0;\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return s.isEmpty();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
                private JavaTemplate before;
                private JavaTemplate beforeTemplate() {
                    if (before == null)
                        before = JavaTemplate.compile(this, "before", (String s) -> s.length() == 0).build();
                    return before;
                };

                JavaTemplate after;
                JavaTemplate afterTemplate() {
                    if (after == null)
                        after = JavaTemplate.compile(this, "after", (JavaTemplate.F1<?, ?>) (String s) -> s.isEmpty()).build();
                    return after;
                };

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = beforeTemplate().matcher(getCursor())).find()) {
                        return embed(afterTemplate().apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)), ctx);
                    }
                    return super.visitBinary(elem, ctx);
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
                    new UsesMethod<>("java.lang.String length(..)"),
                    javaVisitor);
        }
    }

}
