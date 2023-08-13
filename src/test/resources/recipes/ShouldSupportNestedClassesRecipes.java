package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.ShortenFullyQualifiedTypeReferences;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.tree.*;

public final class ShouldSupportNestedClassesRecipes {
    public static final class NestedClassRecipe extends Recipe {

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
            return new JavaVisitor<ExecutionContext>() {
                final JavaTemplate before = JavaTemplate.compile(this, "before", (String s) -> s.length() > 0).build();
                final JavaTemplate after = JavaTemplate.compile(this, "after", (String s) -> !s.isEmpty()).build();

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        doAfterVisit(new ShortenFullyQualifiedTypeReferences().getVisitor());
                        return after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0));
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
        }
    }

}
