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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

class ComplexGenerics<S extends Serializable & Comparable<? super S>, T extends S, U extends T> {
    @BeforeTemplate
    boolean before(Stream<S> stream, List<U> list, Collector<S, ?, ? extends List<T>> collector) {
        return stream.collect(collector).containsAll(list);
    }

    @AfterTemplate
    boolean after(Stream<S> stream, List<U> list, Collector<S, ?, ? extends Iterable<T>> collector) {
        return stream.collect(collector).equals(list);
    }
}
