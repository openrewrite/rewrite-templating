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
 * OpenRewrite recipe created for Refaster template {@code MatchOrder}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class MatchOrderRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public MatchOrderRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `MatchOrder`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class MatchOrder {\n    \n    @BeforeTemplate()\n    boolean before1(@Matches(value = MethodInvocationMatcher.class)\n    String literal, @NotMatches(value = MethodInvocationMatcher.class)\n    String str) {\n        return str.equals(literal);\n    }\n    \n    @BeforeTemplate()\n    boolean before2(@NotMatches(value = MethodInvocationMatcher.class)\n    String str, @Matches(value = MethodInvocationMatcher.class)\n    String literal) {\n        return str.equals(literal);\n    }\n    \n    @AfterTemplate()\n    boolean after(String literal, String str) {\n        return literal.equals(str);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = JavaTemplate
                    .builder("#{str:any(java.lang.String)}.equals(#{literal:any(java.lang.String)})").build()
                    .matcher(getCursor())).find()) {
                    if (!new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(1))) {
                        return super.visitMethodInvocation(elem, ctx);
                    }
                    if (new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(0))) {
                        return super.visitMethodInvocation(elem, ctx);
                    }
                    return embed(
                            JavaTemplate
                    .builder("#{literal:any(java.lang.String)}.equals(#{str:any(java.lang.String)})").build()
                            .apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(1), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                if ((matcher = JavaTemplate
                    .builder("#{str:any(java.lang.String)}.equals(#{literal:any(java.lang.String)})").build()
                    .matcher(getCursor())).find()) {
                    if (new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(0))) {
                        return super.visitMethodInvocation(elem, ctx);
                    }
                    if (!new org.openrewrite.java.template.MethodInvocationMatcher().matches((Expression) matcher.parameter(1))) {
                        return super.visitMethodInvocation(elem, ctx);
                    }
                    return embed(
                            JavaTemplate
                    .builder("#{literal:any(java.lang.String)}.equals(#{str:any(java.lang.String)})").build()
                            .apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(1), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                new UsesMethod<>("java.lang.String equals(..)", true),
                javaVisitor
        );
    }
}
