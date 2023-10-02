package org.openrewrite.java.template.internal;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

public class MinimumViableParentheses extends JavaVisitor<ExecutionContext> {

    @Override
    public J visitExpression(Expression expression, ExecutionContext ctx) {
        if (expression instanceof J.Binary) {
            SourceFile sourceFile = getCursor().firstEnclosing(SourceFile.class);
            if (new MaybeAddParentheses(expression).visit(sourceFile, ctx) != sourceFile) {
                return new J.Parentheses<>(Tree.randomId(), expression.getPrefix(), Markers.EMPTY,
                        JRightPadded.build(expression.withPrefix(Space.EMPTY)));
            }
        }
        return expression;
    }

    /**
     * We have to zoom out beyond the expression that we are considering adding parentheses to in order to
     * see whether the parentheses are necessary. This visitor starts from the top of the tree and works its
     * way down to the expression in question, adding parentheses as if they are certainly necessary and then
     * running unnecessary parentheses cleanup to see if they are actually necessary.
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    private static class MaybeAddParentheses extends JavaVisitor<ExecutionContext> {
        Expression scope;

        @Override
        public @Nullable J visit(@Nullable Tree tree, ExecutionContext ctx) {
            JavaVisitor<ExecutionContext> unnecessaryParens = new UnnecessaryParenthesesVisitor<>();

            // First, a cleanup of unnecessary parentheses on the entire tree, so as not to capture
            // other unnecessary parentheses as a change when we only really care about the parentheses
            // we are considering adding.
            J j = unnecessaryParens.visit(tree, ctx);

            j = super.visit(j, ctx);
            if (j instanceof SourceFile) {
                if (unnecessaryParens.visit(j, ctx) != j) {
                    return (J) tree;
                }
            }
            return j;
        }

        @Override
        public J visitExpression(Expression expression, ExecutionContext ctx) {
            if (expression == scope) {
                return new J.Parentheses<>(Tree.randomId(), expression.getPrefix(), Markers.EMPTY,
                        JRightPadded.build(expression.withPrefix(Space.EMPTY)));
            }
            return expression;
        }
    }
}
