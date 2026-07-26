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

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IsLambdaExpressionOrMethodReferenceTest {

    private final IsLambdaExpressionOrMethodReference matcher = new IsLambdaExpressionOrMethodReference();

    @Test
    void matchesLambdaExpressionsAndMethodReferences() {
        Map<String, Expression> arguments = argumentsOfOrElseGetByEnclosingMethod(
          """
            import java.util.Optional;
            import java.util.function.Supplier;

            class A {
                String lambda(Optional<String> optional) {
                    return optional.orElseGet(() -> "");
                }

                String methodReference(Optional<String> optional) {
                    return optional.orElseGet(String::new);
                }

                String identifier(Optional<String> optional, Supplier<String> supplier) {
                    return optional.orElseGet(supplier);
                }

                String methodInvocation(Optional<String> optional, Supplier<Supplier<String>> supplier) {
                    return optional.orElseGet(supplier.get());
                }
            }
            """);

        assertThat(arguments).containsOnlyKeys("lambda", "methodReference", "identifier", "methodInvocation");
        assertThat(arguments.get("lambda")).isInstanceOf(J.Lambda.class);
        assertThat(arguments.get("methodReference")).isInstanceOf(J.MemberReference.class);

        assertThat(matcher.matches(arguments.get("lambda"))).isTrue();
        assertThat(matcher.matches(arguments.get("methodReference"))).isTrue();
        assertThat(matcher.matches(arguments.get("identifier"))).isFalse();
        assertThat(matcher.matches(arguments.get("methodInvocation"))).isFalse();
    }

    /**
     * @return the sole argument of each {@code orElseGet} call, keyed by the method it appears in,
     * so that the assertions do not depend on traversal order.
     */
    private static Map<String, Expression> argumentsOfOrElseGetByEnclosingMethod(String source) {
        ExecutionContext ctx = new InMemoryExecutionContext(Throwable::printStackTrace);
        J.CompilationUnit cu = JavaParser.fromJavaVersion().build()
          .parse(ctx, source)
          .map(J.CompilationUnit.class::cast)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Failed to parse source"));

        Map<String, Expression> arguments = new LinkedHashMap<>();
        new JavaIsoVisitor<Integer>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, Integer p) {
                if ("orElseGet".equals(method.getSimpleName())) {
                    J.MethodDeclaration enclosing = getCursor().firstEnclosingOrThrow(J.MethodDeclaration.class);
                    arguments.put(enclosing.getSimpleName(), method.getArguments().get(0));
                }
                return super.visitMethodInvocation(method, p);
            }
        }.visit(cu, 0);
        return arguments;
    }
}
