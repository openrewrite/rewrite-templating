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

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;

import java.util.*;
import java.util.stream.Stream;

public class RefasterVarargs {

    public static class StreamOfToList<T> {
        @BeforeTemplate
        List<T> before(@Repeated T value) {
            return Stream.of(Refaster.asVarargs(value)).toList();
        }

        @AfterTemplate
        List<T> after(@Repeated T value) {
            return Arrays.asList(value);
        }
    }

    public static class MinOfVarargs<S, T extends S> {
        @BeforeTemplate
        T before(@Repeated T value, Comparator<S> cmp) {
            return Stream.of(Refaster.asVarargs(value)).min(cmp).orElseThrow();
        }

        @AfterTemplate
        T after(@Repeated T value, Comparator<S> cmp) {
            return Collections.min(Arrays.asList(value), cmp);
        }
    }
}
