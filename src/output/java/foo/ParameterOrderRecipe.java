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

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code ParameterOrder}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class ParameterOrderRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public ParameterOrderRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `ParameterOrder`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class ParameterOrder {\n    \n    @BeforeTemplate\n    public int parameters(int b, int a) {\n        return a + b;\n    }\n    \n    @AfterTemplate\n    public int output(int a, int b) {\n        return a + a + b;\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractRefasterJavaVisitor() {
            JavaTemplate parameters;
            JavaTemplate after;

            @Override
            public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (parameters == null) {
                    parameters = JavaTemplate.builder("#{a:any(int)} + #{b:any(int)}").build();
                }
                if ((matcher = parameters.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{a:any(int)} + #{a} + #{b:any(int)}").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                            getCursor(),
                            ctx,
                            EmbeddingOption.SHORTEN_NAMES
                    );
                }
                return super.visitBinary(elem, ctx);
            }

        };
    }
}
