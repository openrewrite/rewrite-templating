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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.NonNullApi;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.Semantics;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;


/**
 * OpenRewrite recipes created for Refaster template {@code foo.SpecificVisitMethods}.
 */
@SuppressWarnings("all")
public class SpecificVisitMethodsRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public SpecificVisitMethodsRecipes() {}

    @Override
    public String getDisplayName() {
        return "`SpecificVisitMethods` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.SpecificVisitMethods`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new TernaryRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SpecificVisitMethods.Ternary}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class TernaryRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public TernaryRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `SpecificVisitMethods.Ternary`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class Ternary {\n    \n    @BeforeTemplate()\n    int before(String s) {\n        return s.length() > 0 ? 1 : 0;\n    }\n    \n    @AfterTemplate()\n    int after(String s) {\n        return Integer.signum(s.length());\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = Semantics.expression(this, "before", (String s) -> s.length() > 0 ? 1 : 0).build();
                final JavaTemplate after = Semantics.expression(this, "after", (String s) -> Integer.signum(s.length())).build();

                @Override
                public J visitTernary(J.Ternary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitTernary(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String length(..)"),
                    javaVisitor
            );
        }
    }

}
