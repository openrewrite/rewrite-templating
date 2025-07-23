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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
        name = "Multiline Annotation\nContains a newline character!",
        description = "A multiline annotation.\nSupported here too!\n" +
                      "It also supports escaped quotations: \"I think therefore I am\" - Descartes.\n" +
                      "And escaped backslashes: C:\\Users\\JohnDoe\\Documents\\\n" +
                      "And escaped tabs: \"This is a string with a tab character\t\".\n" +
                      "And escaped carriage returns: \"This is a string with a carriage return character\r\".\n" +
                      "And escaped form feeds: \"This is a string with a form feed character\f\".\n" +
                      "And escaped backspace characters: \"This is a string with a backspace character\b\".\n" +
                      "And escaped null characters: \"This is a string with a null character\0\".\n" +
                      "And escaped octal characters: \"This is a string with an octal character\123\".\n" +
                      "And escaped unicode characters: \"This is a string with a unicode character\u1234\".\n" +
                      "And raw emoji: \"This is a string with an emoji😀\".\n" +
                      "And emojis: \"This is a string with an emoji\uD83D\uDE00\".",
        tags = {"tag1", "tag2", "tag with\nnewline character"}
)
public class CharacterEscapeAnnotation {

    @BeforeTemplate
    String before() {
        return "The answer to life, the universe, and everything";
    }

    @AfterTemplate
    String after() {
        return "42";
    }
}
