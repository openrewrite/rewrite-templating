/*
 * Copyright 2024 the original author or authors.
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
package com.yourorg;

import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import javax.annotation.Generated;
import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code TwoVisitMethods}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class TwoVisitMethods extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public TwoVisitMethods() {}

    @Override
    public String getDisplayName() {
        return "Standardize empty String checks";
    }

    @Override
    public String getDescription() {
        return "Replace calls to `String.length() == 0` with `String.isEmpty()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final JavaTemplate lengthEqualsZero = JavaTemplate
                    .builder("#{s:any(java.lang.String)}.length() == 0")
                    .build();
            final JavaTemplate equalsEmptyString = JavaTemplate
                    .builder("#{s:any(java.lang.String)}.equals(\"\")")
                    .build();
            final JavaTemplate isEmpty = JavaTemplate
                    .builder("#{s:any(java.lang.String)}.isEmpty()")
                    .build();

            @Override
            public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = lengthEqualsZero.matcher(getCursor())).find()) {
                    return embed(
                            isEmpty.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                if ((matcher = equalsEmptyString.matcher(getCursor())).find()) {
                    return embed(
                            isEmpty.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitBinary(elem, ctx);
            }

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = equalsEmptyString.matcher(getCursor())).find()) {
                    return embed(
                            isEmpty.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>("java.lang.String length(..)", true),
                        new UsesMethod<>("java.lang.String equals(..)", true)
                ),
                javaVisitor
        );
    }
}
