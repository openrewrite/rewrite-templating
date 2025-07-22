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

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MinimumViableParenthesesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new OnePlusTwo());
    }

    @Test
    void minimumViable() {
        rewriteRun(
          java(
            "class Test {\n" +
            "    int n = 1 + 2;\n" +
            "    int o = 1 + 2 + 3;\n" +
            "    int p = -(1 + 2);\n" +
            "}",
            "class Test {\n" +
            "    int n = 1 + 2;\n" +
            "    int o = (1 + 2) + 3;\n" +
            "    int p = -(1 + 2);\n" +
            "}"
          )
        );
    }

    public static class OnePlusTwo extends Recipe {
        @Override
        public String getDisplayName() {
            return "One plus two";
        }

        @Override
        public String getDescription() {
            return "Minimum parentheses to keep 1+2 together.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new JavaVisitor<ExecutionContext>() {
                @Override
                @SuppressWarnings("ConstantConditions")
                public J visitBinary(J.Binary binary, ExecutionContext ctx) {
                    if (binary.getLeft() instanceof J.Literal &&
                        (Integer) ((J.Literal) binary.getLeft()).getValue() == 1 &&
                        binary.getRight() instanceof J.Literal &&
                        (Integer) ((J.Literal) binary.getRight()).getValue() == 2 &&
                        !(getCursor().getParentTreeCursor().getValue() instanceof J.Parentheses)) {
                        return new MinimumViableParentheses().visitNonNull(binary, ctx, getCursor().getParentOrThrow());
                    }
                    return super.visitBinary(binary, ctx);
                }
            };
        }
    }
}
