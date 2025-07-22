package foo;

import java.util.*;
import javax.annotation.Generated;
import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code NestedPreconditions}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class NestedPreconditionsRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public NestedPreconditionsRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `NestedPreconditions`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class NestedPreconditions {\n    \n    @BeforeTemplate\n    Map hashMap(int size) {\n        return new HashMap(size);\n    }\n    \n    @BeforeTemplate\n    Map linkedHashMap(int size) {\n        return new LinkedHashMap(size);\n    }\n    \n    @AfterTemplate\n    Map hashtable(int size) {\n        return new Hashtable(size);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate hashMap;
            JavaTemplate linkedHashMap;
            JavaTemplate after;

            @Override
            public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (hashMap == null) {
                    hashMap = JavaTemplate.builder("new java.util.HashMap(#{size:any(int)})")
                            .bindType("java.util.Map").build();
                }
                if ((matcher = hashMap.matcher(getCursor())).find()) {
                    maybeRemoveImport("java.util.HashMap");
                    if (after == null) {
                        after = JavaTemplate.builder("new java.util.Hashtable(#{size:any(int)})")
                                .bindType("java.util.Map").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                if (linkedHashMap == null) {
                    linkedHashMap = JavaTemplate.builder("new java.util.LinkedHashMap(#{size:any(int)})")
                            .bindType("java.util.Map").build();
                }
                if ((matcher = linkedHashMap.matcher(getCursor())).find()) {
                    maybeRemoveImport("java.util.LinkedHashMap");
                    if (after == null) {
                        after = JavaTemplate.builder("new java.util.Hashtable(#{size:any(int)})")
                                .bindType("java.util.Map").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitNewClass(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.Map", true),
                        Preconditions.or(
                                Preconditions.and(
                                        new UsesType<>("java.util.HashMap", true),
                                        new UsesMethod<>("java.util.HashMap <init>(..)", true)
                                ),
                                Preconditions.and(
                                        new UsesType<>("java.util.LinkedHashMap", true),
                                        new UsesMethod<>("java.util.LinkedHashMap <init>(..)", true)
                                )
                        )
                ),
                javaVisitor
        );
    }
}
