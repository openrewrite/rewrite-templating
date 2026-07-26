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
package foo;

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
 * OpenRewrite recipes created for Refaster template {@code foo.ErrorProneMatching}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class ErrorProneMatchingRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public ErrorProneMatchingRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Error Prone matchers";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "A set of recipes guarded by Error Prone matchers.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new OptionalOrElseGetRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code ErrorProneMatching.OptionalOrElseGet}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class OptionalOrElseGetRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public OptionalOrElseGetRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Use `Optional#orElseGet(Supplier)`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Use `Optional#orElseGet(Supplier)` instead of a ternary on `Optional#isPresent()`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before2;
                JavaTemplate after;

                @Override
                public J visitTernary(J.Ternary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{optional:any(java.util.Optional<java.lang.String>)}.isPresent() ? #{optional}.get() : #{supplier:any(java.util.function.Supplier<java.lang.String>)}.get()")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (!new org.openrewrite.java.template.matchers.IsLambdaExpressionOrMethodReference().matches((Expression) matcher.parameter(1))) {
                            return super.visitTernary(elem, ctx);
                        }
                        if (after == null) {
                            after = JavaTemplate.builder("#{optional:any(java.util.Optional<java.lang.String>)}.orElseGet(#{supplier:any(java.util.function.Supplier<java.lang.String>)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if (before2 == null) {
                        before2 = JavaTemplate.builder("#{optional:any(java.util.Optional<java.lang.String>)}.isPresent() ? #{optional}.get() : #{supplier:any(java.util.function.Supplier<java.lang.String>)}.get()")
                                .bindType("java.lang.String").build();
                    }
                    if ((matcher = before2.matcher(getCursor())).find()) {
                        if (new org.openrewrite.java.template.matchers.IsLambdaExpressionOrMethodReference().matches((Expression) matcher.parameter(1))) {
                            return super.visitTernary(elem, ctx);
                        }
                        if (after == null) {
                            after = JavaTemplate.builder("#{optional:any(java.util.Optional<java.lang.String>)}.orElseGet(#{supplier:any(java.util.function.Supplier<java.lang.String>)})")
                                    .bindType("java.lang.String").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitTernary(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("java.util.Optional", true),
                            new UsesType<>("java.util.function.Supplier", true),
                            new UsesMethod<>("java.util.Optional get(..)", true),
                            new UsesMethod<>("java.util.Optional isPresent(..)", true),
                            new UsesMethod<>("java.util.function.Supplier get(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true)),
                            Preconditions.not(new UsesType<>("org.openrewrite.java.template.Semantics", true))
                    ),
                    javaVisitor
            );
        }
    }

}
