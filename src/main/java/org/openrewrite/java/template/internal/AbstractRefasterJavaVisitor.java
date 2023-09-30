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

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.ShortenFullyQualifiedTypeReferences;
import org.openrewrite.java.cleanup.SimplifyBooleanExpressionVisitor;
import org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor;
import org.openrewrite.java.tree.J;

import java.util.Arrays;
import java.util.EnumSet;

@SuppressWarnings("unused")
public abstract class AbstractRefasterJavaVisitor extends JavaVisitor<ExecutionContext> {

    @Deprecated
    // to be removed as soon as annotation processor generates required options
    protected J embed(J j, Cursor cursor, ExecutionContext ctx) {
        return embed(j, cursor, ctx, EmbeddingOption.values());
    }

    @SuppressWarnings({"DataFlowIssue", "SameParameterValue"})
    protected J embed(J j, Cursor cursor, ExecutionContext ctx, EmbeddingOption... options) {
        EnumSet<EmbeddingOption> optionsSet = options.length > 0 ? EnumSet.copyOf(Arrays.asList(options)) :
                EnumSet.noneOf(EmbeddingOption.class);

        TreeVisitor<?, ExecutionContext> visitor;
        if (optionsSet.contains(EmbeddingOption.REMOVE_PARENS) && !getAfterVisit().contains(visitor = new UnnecessaryParenthesesVisitor<>())) {
            doAfterVisit(visitor);
        }
        if (optionsSet.contains(EmbeddingOption.SHORTEN_NAMES)) {
            doAfterVisit(ShortenFullyQualifiedTypeReferences.modifyOnly(j));
        }
        if (optionsSet.contains(EmbeddingOption.SIMPLIFY_BOOLEANS)) {
            j = new SimplifyBooleanExpressionVisitor().visitNonNull(j, ctx, cursor.getParentOrThrow());
        }
        return j;
    }

    protected enum EmbeddingOption {
        SHORTEN_NAMES, SIMPLIFY_BOOLEANS, REMOVE_PARENS;
    }
}
