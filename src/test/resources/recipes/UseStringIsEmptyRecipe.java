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


public class UseStringIsEmptyRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster template `UseStringIsEmpty`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\npublic class UseStringIsEmpty {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return s.length() > 0;\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return !s.isEmpty();\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
            final JavaTemplate before = JavaTemplate.compile(this, "before", (String s) -> s.length() > 0).build();
            final JavaTemplate after = JavaTemplate.compile(this, "after", (String s) -> !s.isEmpty()).build();

            @Override
            public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = before.matcher(getCursor())).find()) {
                    return embed(after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)), ctx);
                }
                return super.visitBinary(elem, ctx);
            }

            private J embed(J j, ExecutionContext ctx) {
                doAfterVisit(new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor());
                j = new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor().visit(j, ctx, getCursor().getParent());
                j = new org.openrewrite.java.cleanup.SimplifyBooleanExpressionVisitor().visit(j, ctx, getCursor().getParent());
                return j;
            }
        };
        return Preconditions.check(
                new UsesMethod<>("java.lang.String length(..)"),
                javaVisitor);
    }
}
