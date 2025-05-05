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
import org.jspecify.annotations.Nullable;
import org.openrewrite.java.template.RecipeDescriptor;

public class Parameters {

    @RecipeDescriptor(name = "Parameters.Annotated", description = "Parameters with annotations.")
    public class Annotated {
        @BeforeTemplate
        boolean before(@Nullable String s) {
            return s == s;
        }

        @AfterTemplate
        boolean after(@Nullable String s) {
            return s.equals(s);
        }
    }

    @RecipeDescriptor(name = "Parameters.AnnotatedArray", description = "Parameters with annotations.")
    public class AnnotatedArray {
        @BeforeTemplate
        boolean before(@Nullable String[] s) {
            return s == s;
        }

        @AfterTemplate
        boolean after(@Nullable String[] s) {
            return s.equals(s);
        }
    }

    public class Reuse {
        @BeforeTemplate
        boolean before(String s) {
            return s == s;
        }

        @AfterTemplate
        boolean after(String s) {
            return s.equals(s);
        }
    }

    public class Order {
        @BeforeTemplate
        boolean before1(int a, int b) {
            return a == b;
        }

        @BeforeTemplate
        boolean before2(int a, int b) {
            return b == a;
        }

        @AfterTemplate
        boolean after(int a, int b) {
            return a == b;
        }
    }

}
