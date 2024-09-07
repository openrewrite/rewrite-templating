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
 * OpenRewrite recipes created for Refaster template {@code foo.Parameters}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class ParametersRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public ParametersRecipes() {}

    @Override
    public String getDisplayName() {
        return "`Parameters` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.Parameters`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new ReuseRecipe(),
                new OrderRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Parameters.Reuse}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class ReuseRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public ReuseRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `Parameters.Reuse`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic class Reuse {\n    \n    @BeforeTemplate()\n    boolean before(String s) {\n        return s == s;\n    }\n    \n    @AfterTemplate()\n    boolean after(String s) {\n        return s.equals(s);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)} == #{s}")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.equals(#{s})")
                        .build();

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Parameters.Order}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class OrderRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public OrderRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `Parameters.Order`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic class Order {\n    \n    @BeforeTemplate()\n    boolean before1(int a, int b) {\n        return a == b;\n    }\n    \n    @BeforeTemplate()\n    boolean before2(int a, int b) {\n        return b == a;\n    }\n    \n    @AfterTemplate()\n    boolean after(int a, int b) {\n        return a == b;\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            return new AbstractRefasterJavaVisitor() {
                final JavaTemplate before1 = JavaTemplate
                        .builder("#{a:any(int)} == #{b:any(int)}")
                        .build();
                final JavaTemplate before2 = JavaTemplate
                        .builder("#{b:any(int)} == #{a:any(int)}")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{a:any(int)} == #{b:any(int)}")
                        .build();

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before1.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    if ((matcher = before2.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(1), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
        }
    }

}
