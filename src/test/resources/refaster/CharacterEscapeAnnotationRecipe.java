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
 * OpenRewrite recipe created for Refaster template {@code MultilineAnnotation}.
 */
@SuppressWarnings("all")
@NonNullApi
public class CharacterEscapeAnnotationRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public MultilineAnnotationRecipe() {}

    @Override
    public String getDisplayName() {
        return "Multiline Annotation\nContains a newline character!";
    }

    @Override
    public String getDescription() {
        return "A multiline annotation.\nSupported here too!\nIt also supports escaped quotations: \"I think therefore I am\" - Descartes.\nAnd escaped backslashes: C:\\Users\\JohnDoe\\Documents\\\nAnd escaped tabs: \"This is a string with a tab character\t\".\nAnd escaped carriage returns: \"This is a string with a carriage return character\r\".\nAnd escaped form feeds: \"This is a string with a form feed character\f\".\nAnd escaped backspace characters: \"This is a string with a backspace character\b\".\nAnd escaped null characters: \"This is a string with a null character\u0000\".\nAnd escaped octal characters: \"This is a string with an octal characterS\".\nAnd escaped unicode characters: \"This is a string with a unicode character\u1234\".\nAnd raw emoji: \"This is a string with an emoji\uD83D\uDE00\".\nAnd emojis: \"This is a string with an emoji\uD83D\uDE00\".";
    }

    @Override
    public Set<String> getTags() {
        return new HashSet<>(Arrays.asList("tag1", "tag2", "tag with\nnewline character"));
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            final JavaTemplate before = Semantics.expression(this, "before", () -> "The answer to life, the universe, and everything").build();
            final JavaTemplate after = Semantics.expression(this, "after", () -> "42").build();

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
