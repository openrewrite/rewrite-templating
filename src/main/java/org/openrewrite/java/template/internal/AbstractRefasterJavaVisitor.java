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
import org.openrewrite.java.UseStaticImport;
import org.openrewrite.java.cleanup.SimplifyBooleanExpressionVisitor;
import org.openrewrite.java.cleanup.UnnecessaryParenthesesVisitor;
import org.openrewrite.java.service.ImportService;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

import java.util.EnumSet;
import java.util.List;

import static org.openrewrite.java.MethodMatcher.methodPattern;

@SuppressWarnings("unused")
public abstract class AbstractRefasterJavaVisitor extends JavaVisitor<ExecutionContext> {

    /**
     * Check whether the after template's return type is assignable to the target type
     * expected by the surrounding context (e.g., receiver of a chained method call,
     * argument to a method, or right-hand side of an assignment).
     * Returns {@code true} if the replacement is safe: either the context doesn't constrain
     * the type (standalone expression, return statement, etc.) or the after type is assignable
     * to what the context expects. Returns {@code false} if the replacement would break
     * compilation (e.g., a chained method call that doesn't exist on the wider type).
     */
    protected boolean isAssignableToTargetType(String afterTypeFqn) {
        Cursor parentCursor = getCursor().getParentTreeCursor();
        Object parent = parentCursor.getValue();
        Object child = getCursor().getValue();

        if (parent instanceof J.MethodInvocation) {
            J.MethodInvocation mi = (J.MethodInvocation) parent;
            if (mi.getMethodType() == null) {
                return true;
            }
            // Expression is the receiver — the method must exist on the replacement type
            if (mi.getSelect() == child) {
                return TypeUtils.isAssignableTo(afterTypeFqn, mi.getMethodType().getDeclaringType());
            }
            // Expression is an argument — check resolved overload first, then other overloads
            List<Expression> args = mi.getArguments();
            int argIndex = -1;
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i) == child) {
                    argIndex = i;
                    break;
                }
            }
            List<JavaType> parameterTypes = mi.getMethodType().getParameterTypes();
            if (argIndex >= 0 && argIndex < parameterTypes.size()) {
                if (TypeUtils.isAssignableTo(afterTypeFqn, parameterTypes.get(argIndex))) {
                    return true;
                }
                // Check if any other overload on the declaring type accepts the wider type
                JavaType.FullyQualified declaringType = mi.getMethodType().getDeclaringType();
                String methodName = mi.getMethodType().getName();
                for (JavaType.Method method : declaringType.getMethods()) {
                    if (!method.getName().equals(methodName) || method.getParameterTypes().size() != args.size()) {
                        continue;
                    }
                    List<JavaType> paramTypes = method.getParameterTypes();
                    boolean allMatch = true;
                    for (int i = 0; i < args.size(); i++) {
                        JavaType argType = i == argIndex ? JavaType.buildType(afterTypeFqn) : args.get(i).getType();
                        if (argType == null || !TypeUtils.isAssignableTo(paramTypes.get(i), argType)) {
                            allMatch = false;
                            break;
                        }
                    }
                    if (allMatch) {
                        return true;
                    }
                }
                return false;
            }
        } else if (parent instanceof J.Assignment) {
            J.Assignment assignment = (J.Assignment) parent;
            if (assignment.getAssignment() == child) {
                return TypeUtils.isAssignableTo(afterTypeFqn, assignment.getType());
            }
        } else if (parent instanceof J.VariableDeclarations.NamedVariable) {
            J.VariableDeclarations.NamedVariable var = (J.VariableDeclarations.NamedVariable) parent;
            if (var.getInitializer() == child) {
                return TypeUtils.isAssignableTo(afterTypeFqn, var.getType());
            }
        }
        // No constraint from context (standalone expression, return statement, etc.)
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    protected J embed(J j, Cursor cursor, ExecutionContext ctx, EmbeddingOption... options) {
        EnumSet<EmbeddingOption> optionsSet = options.length > 0 ? EnumSet.of(options[0], options) :
                EnumSet.noneOf(EmbeddingOption.class);

        TreeVisitor<?, ExecutionContext> visitor;
        if (optionsSet.contains(EmbeddingOption.REMOVE_PARENS) && !getAfterVisit().contains(visitor = new UnnecessaryParenthesesVisitor<>())) {
            doAfterVisit(visitor);
        }
        if (optionsSet.contains(EmbeddingOption.SHORTEN_NAMES)) {
            doAfterVisit(service(ImportService.class).shortenFullyQualifiedTypeReferencesIn(j));
        }
        if (optionsSet.contains(EmbeddingOption.SIMPLIFY_BOOLEANS)) {
            j = new SimplifyBooleanExpressionVisitor().visitNonNull(j, ctx, cursor.getParentOrThrow());
        }
        if (optionsSet.contains(EmbeddingOption.STATIC_IMPORT_ALWAYS) && j instanceof J.MethodInvocation) {
            J.MethodInvocation mi = (J.MethodInvocation) j;
            if (mi.getSelect() != null && mi.getMethodType() != null) {
                doAfterVisit(new UseStaticImport(methodPattern(mi.getMethodType())).getVisitor());
            }
        }
        return j;
    }

    public enum EmbeddingOption {
        SHORTEN_NAMES,
        SIMPLIFY_BOOLEANS,
        STATIC_IMPORT_ALWAYS,
        REMOVE_PARENS,
    }
}
