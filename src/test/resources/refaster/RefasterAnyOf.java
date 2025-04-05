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

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;

public class RefasterAnyOf {
    public static class StringIsEmpty {
        @BeforeTemplate
        boolean before(String s) {
            return Refaster.anyOf(s.length() < 1, s.length() == 0);
        }

        @AfterTemplate
        boolean after(String s) {
            return s.isEmpty();
        }
    }

    public static class EmptyList {
        @BeforeTemplate
        List before() {
            return Refaster.anyOf(new LinkedList(), java.util.Collections.emptyList());
        }

        @AfterTemplate
        List after() {
            return new java.util.ArrayList();
        }
    }

    public static class NewStringFromCharArraySubSequence {
        @BeforeTemplate
        String before(char[] data, int offset, int count) {
            return Refaster.anyOf(String.valueOf(data, offset, count), String.copyValueOf(data, offset, count));
        }

        @AfterTemplate
        String after(char[] data, int offset, int count) {
            return new String(data, offset, count);
        }
    }

    public static class ChangeOrderParameters {
        @BeforeTemplate
        Duration before(OffsetDateTime a, OffsetDateTime b) {
            return Refaster.anyOf(
                    Duration.between(a.toInstant(), b.toInstant()),
                    Duration.ofSeconds(b.toEpochSecond() - a.toEpochSecond()));
        }

        @AfterTemplate
        Duration after(OffsetDateTime a, OffsetDateTime b) {
            return Duration.between(a, b);
        }
    }
}
