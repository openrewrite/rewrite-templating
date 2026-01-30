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
 * OpenRewrite recipe created for Refaster template {@code UseStringIsEmpty}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class UseStringIsEmptyRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public UseStringIsEmptyRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Replace `s.length() > 0` with `!s.isEmpty()`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Second line that should show up in description only.\n May contain \" and ' and \\\" and \\\\\" and \\n.\n Or even references to `String`.\n Or unicode 🐛.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            JavaTemplate after;

            @Override
            public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("#{s:any(java.lang.String)}.length() > 0").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("!(#{s:any(java.lang.String)}.isEmpty())").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                            getCursor(),
                            ctx,
                            REMOVE_PARENS, SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitBinary(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesMethod<>("java.lang.String length(..)", true),
                        Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                ),
                javaVisitor
        );
    }
}
