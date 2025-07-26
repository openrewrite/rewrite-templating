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
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
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
        //language=markdown
        return "`SimplifyTernary` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
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
            //language=markdown
            return "Simplify ternary expressions";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Simplify `expr ? true : false` to `expr`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitTernary(J.Ternary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{expr:any(boolean)} ? true : false").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{expr:any(boolean)}").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                EmbeddingOption.SHORTEN_NAMES, EmbeddingOption.SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitTernary(elem, ctx);
                }

            };
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
            //language=markdown
            return "Simplify ternary expressions";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Simplify `expr ? false : true` to `!expr`.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new AbstractRefasterJavaVisitor() {
                JavaTemplate before;
                JavaTemplate after;

                @Override
                public J visitTernary(J.Ternary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{expr:any(boolean)} ? false : true").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("!(#{expr:any(boolean)})").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                EmbeddingOption.REMOVE_PARENS, EmbeddingOption.SHORTEN_NAMES, EmbeddingOption.SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitTernary(elem, ctx);
                }

            };
        }
    }

}
