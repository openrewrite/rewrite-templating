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

import java.util.*;

public class Generics {

    public static class FirstElement {
        @BeforeTemplate
        String before(List<String> l) {
            return l.iterator().next();
        }

        @AfterTemplate
        String after(List<String> l) {
            return l.get(0);
        }
    }

    public static class EmptyCollections<K, T> {
        @BeforeTemplate
        List<T> emptyList() {
            return Collections.emptyList();
        }

        @BeforeTemplate
        Collection<T> emptyMap() {
            return Collections.<K, T>emptyMap().values();
        }

        @BeforeTemplate
        List<T> newList() {
            return new ArrayList<>();
        }

        @BeforeTemplate
        Map<K, T> newMap() {
            return new HashMap<>();
        }
    }

    public static class Wilcards<T> {

        @BeforeTemplate
        Comparator<?> wilcard1(Comparator<?> cmp) {
            return cmp.thenComparingInt(null);
        }

        @BeforeTemplate
        Comparator<? extends Number> wilcard2(Comparator<? extends Number> cmp) {
            return cmp.thenComparingInt(null);
        }

        @BeforeTemplate
        Comparator<T> wilcard3(Comparator<T> cmp) {
            return cmp.thenComparingInt(null);
        }

        @BeforeTemplate
        Comparator<? extends T> wilcard4(Comparator<? extends T> cmp) {
            return cmp.thenComparingInt(null);
        }
    }
}
