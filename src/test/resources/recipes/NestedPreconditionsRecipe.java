
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;

public class NestedPreconditionsRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster template `NestedPreconditions`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\npublic class NestedPreconditions {\n    \n    @BeforeTemplate()\n    Map hashMap(int size) {\n        return new HashMap(size);\n    }\n    \n    @BeforeTemplate()\n    Map linkedHashMap(int size) {\n        return new LinkedHashMap(size);\n    }\n    \n    @AfterTemplate()\n    Map hashtable(int size) {\n        return new Hashtable(size);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new JavaVisitor<ExecutionContext>() {
            final JavaTemplate hashMap = JavaTemplate.compile(this, "hashMap", (JavaTemplate.F1<?, ?>) (@Primitive Integer size) -> new HashMap(size)).build();
            final JavaTemplate linkedHashMap = JavaTemplate.compile(this, "linkedHashMap", (JavaTemplate.F1<?, ?>) (@Primitive Integer size) -> new LinkedHashMap(size)).build();
            final JavaTemplate hashtable = JavaTemplate.compile(this, "hashtable", (JavaTemplate.F1<?, ?>) (@Primitive Integer size) -> new Hashtable(size)).build();

            @Override
            public J visitExpression(Expression elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = hashMap.matcher(getCursor())).find() || (matcher = linkedHashMap.matcher(getCursor())).find()) {
                    maybeRemoveImport("java.util.LinkedHashMap");
                    maybeRemoveImport("java.util.HashMap");
                    maybeAddImport("java.util.Hashtable");
                    doAfterVisit(new org.openrewrite.java.ShortenFullyQualifiedTypeReferences().getVisitor());
                    doAfterVisit(new org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor());
                    return hashtable.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0));
                }
                return super.visitExpression(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.or(
                        Preconditions.and(new UsesType<>("java.util.HashMap", false), new UsesType<>("java.util.Map", false)),
                        Preconditions.and(new UsesType<>("java.util.LinkedHashMap", false), new UsesType<>("java.util.Map", false))),
                javaVisitor);
    }
}
