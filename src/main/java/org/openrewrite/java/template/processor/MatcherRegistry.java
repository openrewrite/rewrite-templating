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
package org.openrewrite.java.template.processor;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Resolves the {@code org.openrewrite.java.template.Matcher} implementation that guards a
 * {@code @BeforeTemplate} parameter annotated with {@code @Matches} or {@code @NotMatches}.
 * <p>
 * Two flavors of those annotations exist. The ones declared by this project take an
 * {@code org.openrewrite.java.template.Matcher}, which operates on an OpenRewrite LST and can
 * therefore be used verbatim in a generated recipe. Error Prone's take a
 * {@code com.google.errorprone.matchers.Matcher}, which operates on javac trees and a
 * {@code VisitorState}; neither is available while a recipe runs, so those matchers can not be
 * invoked directly. Instead we map each known Error Prone matcher onto an equivalent OpenRewrite
 * matcher shipped with this project.
 */
final class MatcherRegistry {

    private static final String MATCHES = "org.openrewrite.java.template.Matches";
    private static final String NOT_MATCHES = "org.openrewrite.java.template.NotMatches";
    private static final String ERROR_PRONE_MATCHES = "com.google.errorprone.refaster.annotation.Matches";
    private static final String ERROR_PRONE_NOT_MATCHES = "com.google.errorprone.refaster.annotation.NotMatches";

    /**
     * Maps an Error Prone {@code Matcher} onto the OpenRewrite {@code Matcher} that implements the
     * same predicate against an LST. Only matchers listed here are supported; templates using any
     * other matcher are skipped, as generating a recipe without the guard would broaden it.
     * <p>
     * Values are kept as names rather than class literals on purpose: this class runs on the
     * annotation processor path, where {@code rewrite-java} is typically absent, so loading a
     * {@code Matcher} implementation here would fail. {@code MatcherRegistryTest} verifies that
     * every name still resolves.
     */
    static final Map<String, String> ERROR_PRONE_MATCHERS;

    static {
        Map<String, String> matchers = new LinkedHashMap<>();
        matchers.put(
                "tech.picnic.errorprone.refaster.matchers.IsLambdaExpressionOrMethodReference",
                "org.openrewrite.java.template.matchers.IsLambdaExpressionOrMethodReference");
        ERROR_PRONE_MATCHERS = unmodifiableMap(matchers);
    }

    private MatcherRegistry() {
    }

    /**
     * @return whether the annotation guards a parameter with a matcher, in either flavor.
     */
    static boolean isMatcherAnnotation(String annotationType) {
        return MATCHES.equals(annotationType) || NOT_MATCHES.equals(annotationType) ||
                isErrorProneMatcherAnnotation(annotationType);
    }

    static boolean isErrorProneMatcherAnnotation(String annotationType) {
        return ERROR_PRONE_MATCHES.equals(annotationType) || ERROR_PRONE_NOT_MATCHES.equals(annotationType);
    }

    /**
     * @return whether a match by the matcher should prevent the template from being applied.
     */
    static boolean isNegated(String annotationType) {
        return NOT_MATCHES.equals(annotationType) || ERROR_PRONE_NOT_MATCHES.equals(annotationType);
    }

    /**
     * @return the fully qualified name of the matcher class passed to the annotation, or
     * {@code null} if it can not be read.
     */
    static @Nullable String matcherClass(JCTree.JCAnnotation annotation) {
        if (annotation.attribute == null || annotation.attribute.getValue().values.isEmpty()) {
            return null;
        }
        Object value = annotation.attribute.getValue().values.get(0).snd.getValue();
        if (!(value instanceof Type.ClassType)) {
            return null;
        }
        return ((Type.ClassType) value).tsym.getQualifiedName().toString();
    }

    /**
     * @return the fully qualified name of the OpenRewrite {@code Matcher} equivalent to the given
     * Error Prone one, or {@code null} if no equivalent is registered.
     */
    static @Nullable String errorProneEquivalent(@Nullable String errorProneMatcherClass) {
        return errorProneMatcherClass == null ? null : ERROR_PRONE_MATCHERS.get(errorProneMatcherClass);
    }

    /**
     * @param annotationType the resolved type of {@code annotation}, which callers already know
     * @return the fully qualified name of the OpenRewrite {@code Matcher} to instantiate in the
     * generated recipe, or {@code null} when the annotation references an Error Prone matcher
     * without a known equivalent.
     */
    static @Nullable String resolveMatcher(JCTree.JCAnnotation annotation, String annotationType) {
        String matcherClass = matcherClass(annotation);
        return isErrorProneMatcherAnnotation(annotationType) ? errorProneEquivalent(matcherClass) : matcherClass;
    }
}
