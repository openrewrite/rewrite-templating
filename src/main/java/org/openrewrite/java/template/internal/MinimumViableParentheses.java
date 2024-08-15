/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.template.internal;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
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
