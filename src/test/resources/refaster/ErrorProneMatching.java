/*
 * Copyright 2026 the original author or authors.
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
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.NotMatches;
import org.openrewrite.java.template.RecipeDescriptor;
import tech.picnic.errorprone.refaster.matchers.IsLambdaExpressionOrMethodReference;

import java.util.Optional;
import java.util.function.Supplier;

@RecipeDescriptor(
        name = "Error Prone matchers",
        description = "A set of recipes guarded by Error Prone matchers."
)
public class ErrorProneMatching {

    @RecipeDescriptor(
            name = "Use `Optional#orElseGet(Supplier)`",
            description = "Use `Optional#orElseGet(Supplier)` instead of a ternary on `Optional#isPresent()`."
    )
    public static class OptionalOrElseGet {
        @BeforeTemplate
        String before(Optional<String> optional, @Matches(IsLambdaExpressionOrMethodReference.class) Supplier<String> supplier) {
            return optional.isPresent() ? optional.get() : supplier.get();
        }

        @BeforeTemplate
        String before2(Optional<String> optional, @NotMatches(IsLambdaExpressionOrMethodReference.class) Supplier<String> supplier) {
            return optional.isPresent() ? optional.get() : supplier.get();
        }

        @AfterTemplate
        String after(Optional<String> optional, Supplier<String> supplier) {
            return optional.orElseGet(supplier);
        }
    }

}
