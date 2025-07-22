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
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

/**
 * OpenRewrite recipe created for Refaster template {@code FindListAdd}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class FindListAddRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public FindListAddRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Find list add";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Find list add.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            JavaTemplate after;

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("#{l:any(java.util.List<java.lang.String>)}.add(#{o:any(java.lang.String)})").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    return SearchResult.found(elem);
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.List", true),
                        new UsesMethod<>("java.util.List add(..)", true)
                ),
                javaVisitor
        );
    }
}
