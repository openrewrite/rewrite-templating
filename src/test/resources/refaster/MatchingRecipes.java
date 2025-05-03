/*
 * Copyright 2025 the original author or authors.
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
 * OpenRewrite recipes created for Refaster template {@code foo.Matching}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class MatchingRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public MatchingRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Static analysis";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "A set of static analysis recipes.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("sast");
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringIsEmptyRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Matching.StringIsEmpty}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringIsEmptyRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringIsEmptyRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Use String length comparison";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Use String#length() == 0 instead of String#isEmpty().";
        }

        @Override
        public Set<String> getTags() {
            return new HashSet<>(Arrays.asList("sast", "strings"));
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate before2;
                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{s:any(java.lang.String)}.substring(#{i:any(int)}).isEmpty()").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(0))) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        return embed(
                                JavaTemplate.builder("(#{s:any(java.lang.String)} != null && #{s}.length() == 0)").build()
                                .apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                REMOVE_PARENS, SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    if (before2 == null) {
                        before2 = JavaTemplate.builder("#{s:any(java.lang.String)}.substring(#{i:any(int)}).isEmpty()").build();
                    }
                    if ((matcher = before2.matcher(getCursor())).find()) {
                        if (!new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(0))) {
                            return super.visitMethodInvocation(elem, ctx);
                        }
                        return embed(
                                JavaTemplate.builder("(#{s:any(java.lang.String)} != null && #{s}.length() == 0)").build()
                                .apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                REMOVE_PARENS, SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                        new UsesMethod<>("java.lang.String isEmpty(..)", true),
                        new UsesMethod<>("java.lang.String substring(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

}
