/*
 * Copyright 2026 the original author or authors.
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
package org.openrewrite.java.template.matchers;

import org.openrewrite.java.template.Matcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * Matches lambda expressions and method references.
 * <p>
 * LST equivalent of {@code tech.picnic.errorprone.refaster.matchers.IsLambdaExpressionOrMethodReference}.
 */
public class IsLambdaExpressionOrMethodReference implements Matcher<Expression> {

    @Override
    public boolean matches(Expression expression) {
        return expression instanceof J.Lambda || expression instanceof J.MemberReference;
    }
}
