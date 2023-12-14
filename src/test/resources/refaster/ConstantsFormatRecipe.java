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

import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.Constants;

@NonNullApi
public class ConstantsFormatRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refaster template `ConstantsFormat`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\nclass ConstantsFormat {\n    \n    @BeforeTemplate()\n    String before(String value) {\n        return String.format(\"\\\"%s\\\"\", Convert.quote(value));\n    }\n    \n    @AfterTemplate()\n    String after(String value) {\n        return Constants.format(value);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final JavaTemplate before = Semantics.expression(this, "before", (String value) -> String.format("\"%s\"", com.sun.tools.javac.util.Convert.quote(value))).build();
            final JavaTemplate after = Semantics.expression(this, "after", (String value) -> com.sun.tools.javac.util.Constants.format(value)).build();

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = before.matcher(getCursor())).find()) {
                    maybeRemoveImport("com.sun.tools.javac.util.Convert");
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("com.sun.tools.javac.util.Convert", true),
                        new UsesMethod<>("java.lang.String format(..)"),
                        new UsesMethod<>("com.sun.tools.javac.util.Convert quote(..)")
                ),
                javaVisitor
        );
    }
}
