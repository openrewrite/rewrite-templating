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
 * OpenRewrite recipe created for Refaster template {@code CollectionIsEmpty}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class CollectionIsEmptyRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public CollectionIsEmptyRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `CollectionIsEmpty`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\nclass CollectionIsEmpty<T> {\n    \n    @BeforeTemplate()\n    boolean before(Collection<T> collection) {\n        return Refaster.anyOf(collection.size() == 0, collection.size() <= 0, collection.size() < 1);\n    }\n    \n    @AfterTemplate()\n    boolean after(Collection<T> collection) {\n        return collection.isEmpty();\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final JavaTemplate before$0 = JavaTemplate
                    .builder("#{collection:any(java.util.Collection<T>)}.size() == 0")
                    .build();
            final JavaTemplate before$1 = JavaTemplate
                    .builder("#{collection:any(java.util.Collection<T>)}.size() <= 0")
                    .build();
            final JavaTemplate before$2 = JavaTemplate
                    .builder("#{collection:any(java.util.Collection<T>)}.size() < 1")
                    .build();
            final JavaTemplate after = JavaTemplate
                    .builder("#{collection:any(java.util.Collection<T>)}.isEmpty()")
                    .build();

            @Override
            public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = before$0.matcher(getCursor())).find()) {
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                if ((matcher = before$1.matcher(getCursor())).find()) {
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                if ((matcher = before$2.matcher(getCursor())).find()) {
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
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.Collection", true),
                        new UsesMethod<>("java.util.Collection size(..)", true)
                ),
                javaVisitor
        );
    }
}
