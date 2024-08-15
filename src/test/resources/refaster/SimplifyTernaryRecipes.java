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
 * OpenRewrite recipes created for Refaster template {@code foo.SimplifyTernary}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class SimplifyTernaryRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public SimplifyTernaryRecipes() {}

    @Override
    public String getDisplayName() {
        return "`SimplifyTernary` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.SimplifyTernary`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new SimplifyTernaryTrueFalseRecipe(),
                new SimplifyTernaryFalseTrueRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SimplifyTernary.SimplifyTernaryTrueFalse}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class SimplifyTernaryTrueFalseRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public SimplifyTernaryTrueFalseRecipe() {}

        @Override
        public String getDisplayName() {
            return "Simplify ternary expressions";
        }

        @Override
        public String getDescription() {
            return "Simplify `expr ? true : false` to `expr`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{expr:any(boolean)} ? true : false")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{expr:any(boolean)}")
                        .build();

                @Override
                public J visitTernary(J.Ternary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitTernary(elem, ctx);
                }

            };
            return javaVisitor;
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SimplifyTernary.SimplifyTernaryFalseTrue}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class SimplifyTernaryFalseTrueRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public SimplifyTernaryFalseTrueRecipe() {}

        @Override
        public String getDisplayName() {
            return "Simplify ternary expressions";
        }

        @Override
        public String getDescription() {
            return "Simplify `expr ? false : true` to `!expr`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{expr:any(boolean)} ? false : true")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("!(#{expr:any(boolean)})")
                        .build();

                @Override
                public J visitTernary(J.Ternary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                REMOVE_PARENS, SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitTernary(elem, ctx);
                }

            };
            return javaVisitor;
        }
    }

}
