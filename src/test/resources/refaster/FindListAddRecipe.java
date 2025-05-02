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
import org.openrewrite.marker.SearchResult;

import javax.annotation.Generated;
import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;


/**
 * OpenRewrite recipe created for Refaster template {@code FindListAdd}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class FindListAddRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public FindListAddRecipe() {}

    @Override
    public String getDisplayName() {
        return "Find list add";
    }

    @Override
    public String getDescription() {
        return "Find list add.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = JavaTemplate
                        .builder("#{l:any(java.util.List<java.lang.String>)}.add(#{o:any(java.lang.String)})")
                        .build().matcher(getCursor())).find()) {
                    return SearchResult.found(elem);
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.List", true),
                        new UsesMethod<>("java.util.List add(..)", true)
                ),
                javaVisitor
        );
    }
}
