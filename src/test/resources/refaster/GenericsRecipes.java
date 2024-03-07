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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipes created for Refaster template {@code foo.Generics}.
 */
@SuppressWarnings("all")
public class GenericsRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public GenericsRecipes() {}

    @Override
    public String getDisplayName() {
        return "`Generics` Refaster recipes";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.Generics`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new FirstElementRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Generics.FirstElement}.
     */
    @SuppressWarnings("all")
    @NonNullApi
    public static class FirstElementRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public FirstElementRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `Generics.FirstElement`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class FirstElement {\n    \n    @BeforeTemplate()\n    String before(List<String> l) {\n        return l.iterator().next();\n    }\n    \n    @AfterTemplate()\n    String after(List<String> l) {\n        return l.get(0);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{l:any(java.util.List<java.lang.String>)}.iterator().next()")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{l:any(java.util.List<java.lang.String>)}.get(0)")
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
                            new UsesMethod<>("java.util.Iterator next(..)"),
                            new UsesMethod<>("java.util.List iterator(..)")
                    ),
                    javaVisitor
            );
        }
    }

}
