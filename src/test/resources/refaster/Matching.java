/*
 * Copyright 2023 the original author or authors.
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
import org.openrewrite.java.template.Matches;
import org.openrewrite.java.template.MethodInvocationMatcher;
import org.openrewrite.java.template.NotMatches;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
        name = "Static analysis",
        description = "A set of static analysis recipes.",
        tags = "sast"
)
public class Matching {

    @RecipeDescriptor(
            name = "Use String length comparison",
            description = "Use String#length() == 0 instead of String#isEmpty().",
            tags = {"sast", "strings"}
    )
    public static class StringIsEmpty {
        @BeforeTemplate
        boolean before(int i, @NotMatches(MethodInvocationMatcher.class) String s) {
            return s.substring(i).isEmpty();
        }

        @BeforeTemplate
        boolean before2(int i, @Matches(MethodInvocationMatcher.class) String s) {
            return s.substring(i).isEmpty();
        }

        @AfterTemplate
        boolean after(int i, String s) {
            return s != null && s.length() == 0;
        }
    }

}
