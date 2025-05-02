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
 * OpenRewrite recipe created for Refaster template {@code MultimapGet}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class MultimapGetRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public MultimapGetRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `MultimapGet`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\n@SuppressWarnings(value = \"unchecked\")\nclass MultimapGet<K, V> {\n    \n    @BeforeTemplate()\n    boolean before(Map<K, V> multimap, K key) {\n        return Refaster.anyOf(multimap.keySet(), multimap.values()).contains(key);\n    }\n    \n    @AfterTemplate()\n    boolean after(Map<K, V> multimap, K key) {\n        return multimap.containsKey(key);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = JavaTemplate
                        .builder("#{multimap:any(java.util.Map<K, V>)}.keySet().contains(#{key:any(K)})")
                        .genericTypes("K", "V")
                        .build().matcher(getCursor())).find()) {
                    return embed(
                            JavaTemplate
                                    .builder("#{multimap:any(java.util.Map<K, V>)}.containsKey(#{key:any(K)})")
                                    .genericTypes("K", "V")
                                    .build().apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                if ((matcher = JavaTemplate
                        .builder("#{multimap:any(java.util.Map<K, V>)}.values().contains(#{key:any(K)})")
                        .genericTypes("K", "V")
                        .build().matcher(getCursor())).find()) {
                    return embed(
                            JavaTemplate
                                    .builder("#{multimap:any(java.util.Map<K, V>)}.containsKey(#{key:any(K)})")
                                    .genericTypes("K", "V")
                                    .build().apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.Map", true),
                        new UsesMethod<>("java.util.Collection contains(..)", true),
                        Preconditions.or(
                                new UsesMethod<>("java.util.Map keySet(..)", true),
                                new UsesMethod<>("java.util.Map values(..)", true)
                        )
                ),
                javaVisitor
        );
    }
}
