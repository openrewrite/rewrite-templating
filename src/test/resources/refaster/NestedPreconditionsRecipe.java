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

import javax.annotation.Generated;
import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code NestedPreconditions}.
 */
@SuppressWarnings("all")
@NonNullApi
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class NestedPreconditionsRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public NestedPreconditionsRecipe() {}

    @Override
    public String getDisplayName() {
        return "Refaster template `NestedPreconditions`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\npublic class NestedPreconditions {\n    \n    @BeforeTemplate()\n    Map hashMap(int size) {\n        return new HashMap(size);\n    }\n    \n    @BeforeTemplate()\n    Map linkedHashMap(int size) {\n        return new LinkedHashMap(size);\n    }\n    \n    @AfterTemplate()\n    Map hashtable(int size) {\n        return new Hashtable(size);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final JavaTemplate hashMap = JavaTemplate
                    .builder("new java.util.HashMap(#{size:any(int)})")
                    .build();
            final JavaTemplate linkedHashMap = JavaTemplate
                    .builder("new java.util.LinkedHashMap(#{size:any(int)})")
                    .build();
            final JavaTemplate hashtable = JavaTemplate
                    .builder("new java.util.Hashtable(#{size:any(int)})")
                    .build();

            @Override
            public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = hashMap.matcher(getCursor())).find()) {
                    maybeRemoveImport("java.util.HashMap");
                    return embed(
                            hashtable.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                if ((matcher = linkedHashMap.matcher(getCursor())).find()) {
                    maybeRemoveImport("java.util.LinkedHashMap");
                    return embed(
                            hashtable.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                    );
                }
                return super.visitNewClass(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.Map", true),
                        Preconditions.or(
                                Preconditions.and(
                                        new UsesType<>("java.util.HashMap", true),
                                        new UsesMethod<>("java.util.HashMap <constructor>(..)", true)
                                ),
                                Preconditions.and(
                                        new UsesType<>("java.util.LinkedHashMap", true),
                                        new UsesMethod<>("java.util.LinkedHashMap <constructor>(..)", true)
                                )
                        )
                ),
                javaVisitor
        );
    }
}
