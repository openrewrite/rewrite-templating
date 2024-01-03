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
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.template.Semantics;

import static java.util.regex.Pattern.DOTALL;
import static org.slf4j.LoggerFactory.getLogger;

public class ShouldAddClasspathRecipes {

    class UnqualifiedRecipe {
        JavaIsoVisitor visitor = new JavaIsoVisitor<ExecutionContext>() {
            JavaTemplate.Builder before = Semantics.statement(this, "before", (String message) -> System.out.println(message));
            JavaTemplate.Builder after = Semantics.statement(this, "after", (String message) -> getLogger(message));
        };
    }

    class FullyQualifiedRecipe {
        JavaIsoVisitor visitor = new JavaIsoVisitor<ExecutionContext>() {
            JavaTemplate.Builder before = Semantics.statement(this, "before", (String message) -> System.out.println(message));
            JavaTemplate.Builder after = Semantics.statement(this, "after", (String message) -> org.slf4j.LoggerFactory.getLogger(message));
        };
    }

    class FullyQualifiedFieldRecipe {
        JavaIsoVisitor visitor = new JavaIsoVisitor<ExecutionContext>() {
            JavaTemplate.Builder before = Semantics.statement(this, "before", (String message) -> java.util.regex.Pattern.compile(message, DOTALL));
            JavaTemplate.Builder after = Semantics.statement(this, "after", (String message) -> System.out.println(message));
        };
    }

    class PrimitiveRecipe {
        JavaIsoVisitor visitor = new JavaIsoVisitor<ExecutionContext>() {
            JavaTemplate.Builder before = Semantics.statement(this, "before", (@org.openrewrite.java.template.Primitive Integer i) -> System.out.println(i));
            JavaTemplate.Builder after = Semantics.statement(this, "after", (@org.openrewrite.java.template.Primitive Integer i) -> System.out.print(i));
        };
    }

}
