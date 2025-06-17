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
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import javax.annotation.Generated;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.SHORTEN_NAMES;

/**
 * OpenRewrite recipe created for Refaster template {@code AnnotatedUnusedArgument}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class AnnotatedUnusedArgumentRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public AnnotatedUnusedArgumentRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `AnnotatedUnusedArgument`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class AnnotatedUnusedArgument {\n    \n    @BeforeTemplate\n    public int before1(int a, @Matches(value = MethodInvocationMatcher.class)\n    int b) {\n        return a;\n    }\n    \n    @BeforeTemplate\n    public int before2(int a, @NotMatches(value = MethodInvocationMatcher.class)\n    int c) {\n        return a;\n    }\n    \n    @AfterTemplate\n    public int after(int a) {\n        return a;\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractRefasterJavaVisitor() {
            JavaTemplate before1;
            JavaTemplate before2;
            JavaTemplate after;

            @Override
            public J visitExpression(Expression elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before1 == null) {
                    before1 = JavaTemplate.builder("#{a:any(int)}").build();
                }
                if ((matcher = before1.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{a:any(int)}").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                if (before2 == null) {
                    before2 = JavaTemplate.builder("#{a:any(int)}").build();
                }
                if ((matcher = before2.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{a:any(int)}").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitExpression(elem, ctx);
            }

        };
    }
}
