package foo;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.NonNullApi;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.Semantics;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import java.util.function.Supplier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;

@NonNullApi
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
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final Supplier<JavaTemplate> hashMap = memoize(() -> Semantics.expression(this, "hashMap", (@Primitive Integer size) -> new java.util.HashMap(size)).build());
            final Supplier<JavaTemplate> linkedHashMap = memoize(() -> Semantics.expression(this, "linkedHashMap", (@Primitive Integer size) -> new java.util.LinkedHashMap(size)).build());
            final Supplier<JavaTemplate> hashtable = memoize(() -> Semantics.expression(this, "hashtable", (@Primitive Integer size) -> new java.util.Hashtable(size)).build());

            @Override
            public J visitExpression(Expression elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = matcher(hashMap, getCursor())).find()) {
                    maybeRemoveImport("java.util.HashMap");
                    return embed(
                            apply(hashtable, getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx
                    );
                }
                if ((matcher = matcher(linkedHashMap, getCursor())).find()) {
                    maybeRemoveImport("java.util.LinkedHashMap");
                    return embed(
                            apply(hashtable, getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx
                    );
                }
                return super.visitExpression(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.Map", true),
                        Preconditions.or(
                                new UsesType<>("java.util.HashMap", true),
                                new UsesType<>("java.util.LinkedHashMap", true)
                        )
                ),
                javaVisitor
        );
    }
}
