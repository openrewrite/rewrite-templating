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
 * OpenRewrite recipes created for Refaster template {@code foo.Lambdas}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class LambdasRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public LambdasRecipes() {}

    @Override
    public String getDisplayName() {
        return "`Lambdas` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.Lambdas`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new UsedLambdaRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Lambdas.UsedLambda}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsedLambdaRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsedLambdaRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `Lambdas.UsedLambda`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsedLambda {\n    \n    @BeforeTemplate()\n    void before(List<Integer> is) {\n        is.sort((x,y)->x - y);\n    }\n    \n    @AfterTemplate()\n    void after(List<Integer> is) {\n        is.sort(Comparator.comparingInt((x)->x));\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{is:any(java.util.List)}.sort((x,y)->x - y);")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{is:any(java.util.List)}.sort(java.util.Comparator.comparingInt((x)->x));")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("java.util.List", true),
                            new UsesMethod<>("java.util.List sort(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

}
