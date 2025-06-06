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
 * OpenRewrite recipe created for Refaster template {@code CharacterEscapeAnnotation}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class CharacterEscapeAnnotationRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public CharacterEscapeAnnotationRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Multiline Annotation\nContains a newline character!";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "A multiline annotation.\nSupported here too!\nIt also supports escaped quotations: \"I think therefore I am\" - Descartes.\nAnd escaped backslashes: C:\\Users\\JohnDoe\\Documents\\\nAnd escaped tabs: \"This is a string with a tab character\t\".\nAnd escaped carriage returns: \"This is a string with a carriage return character\r\".\nAnd escaped form feeds: \"This is a string with a form feed character\f\".\nAnd escaped backspace characters: \"This is a string with a backspace character\b\".\nAnd escaped null characters: \"This is a string with a null character \".\nAnd escaped octal characters: \"This is a string with an octal characterS\".\nAnd escaped unicode characters: \"This is a string with a unicode characterሴ\".\nAnd raw emoji: \"This is a string with an emoji😀\".\nAnd emojis: \"This is a string with an emoji😀\".";
    }

    @Override
    public Set<String> getTags() {
        return new HashSet<>(Arrays.asList("tag1", "tag2", "tag with\nnewline character"));
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            JavaTemplate after;

            @Override
            public J visitExpression(Expression elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("\"The answer to life, the universe, and everything\"")
                            .bindType("java.lang.String").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("\"42\"")
                                .bindType("java.lang.String").build();
                    }
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
    }
}
