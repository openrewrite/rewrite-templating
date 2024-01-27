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
 * OpenRewrite recipe created for Refaster template {@code UnnamedPackage}.
 */
@SuppressWarnings("all")
@NonNullApi
public class UnnamedPackageRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public UnnamedPackageRecipe() {}

    @Override
    public String getDisplayName() {
        return "Refaster template `UnnamedPackage`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\npublic class UnnamedPackage {\n    \n    @BeforeTemplate()\n    String before() {\n        return \"This class is located in the default package\";\n    }\n    \n    @AfterTemplate()\n    String after() {\n        return \"And that doesn\\'t cause any problems\";\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final JavaTemplate before = Semantics.expression(this, "before", () -> "This class is located in the default package").build();
            final JavaTemplate after = Semantics.expression(this, "after", () -> "And that doesn\'t cause any problems").build();

            @Override
            public J visitExpression(Expression elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = before.matcher(getCursor())).find()) {
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace()),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitExpression(elem, ctx);
            }

        };
        return javaVisitor;
    }
}
