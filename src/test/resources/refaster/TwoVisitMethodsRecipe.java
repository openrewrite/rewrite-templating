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

import java.util.*;
import javax.annotation.Generated;
import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code TwoVisitMethods}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class TwoVisitMethodsRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public TwoVisitMethodsRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `TwoVisitMethods`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class TwoVisitMethods {\n    \n    @BeforeTemplate\n    boolean lengthIsZero(String s) {\n        return s.length() == 0;\n    }\n    \n    @BeforeTemplate\n    boolean equalsEmptyString(String s) {\n        return s.equals(\"\");\n    }\n    \n    @AfterTemplate\n    boolean isEmpty(String s) {\n        return s.isEmpty();\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate lengthIsZero;
            JavaTemplate equalsEmptyString;
            JavaTemplate after;

            @Override
            public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (lengthIsZero == null) {
                    lengthIsZero = JavaTemplate.builder("#{s:any(java.lang.String)}.length() == 0").build();
                }
                if ((matcher = lengthIsZero.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{s:any(java.lang.String)}.isEmpty()").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitBinary(elem, ctx);
            }

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (equalsEmptyString == null) {
                    equalsEmptyString = JavaTemplate.builder("#{s:any(java.lang.String)}.equals(\"\")").build();
                }
                if ((matcher = equalsEmptyString.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{s:any(java.lang.String)}.isEmpty()").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>("java.lang.String equals(..)", true),
                        new UsesMethod<>("java.lang.String length(..)", true)
                ),
                javaVisitor
        );
    }
}
