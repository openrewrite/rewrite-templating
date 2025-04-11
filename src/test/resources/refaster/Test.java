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
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.util.Comparator;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Map.Entry.comparingByKey;

public class Test<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> one() {
        return Refaster.anyOf(comparing(Map.Entry::getKey), comparingByKey(naturalOrder()));
    }
}
