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
import com.sun.tools.javac.util.Convert;

import java.util.List;
import java.util.Map;

/**
 * A refaster template to test when a `UsesType`and Preconditions.or should or should not be applied to the Preconditions check.
 */
public class PreconditionsVerifier {
    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString {
        @BeforeTemplate
        void before(double actual, int ignore) {
            System.out.println(actual);
        }

        @BeforeTemplate
        void before(String actual, String ignore) {
            System.out.println(actual);
        }

        @AfterTemplate
        void after(Object actual) {
            System.out.println("Changed: " + actual);
        }
    }

    public static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInSomeBeforeBody {
        @BeforeTemplate
        String before(String value) {
            return Convert.quote(value);
        }

        @BeforeTemplate
        String before(int value) {
            return String.valueOf(value);
        }

        @AfterTemplate
        Object after(Object value) {
            return Convert.quote(String.valueOf(value));
        }
    }

    public static class UsesTypeWhenBeforeTemplateContainsPrimitiveOrStringAndTypeInAllBeforeBody {
        @BeforeTemplate
        String before(String value) {
            return Convert.quote(value);
        }

        @BeforeTemplate
        String before(int value) {
            return Convert.quote(String.valueOf(value));
        }

        @AfterTemplate
        Object after(Object value) {
            return Convert.quote(String.valueOf(value));
        }
    }

    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType {
        @BeforeTemplate
        void before(int actual) {
            System.out.println(actual);
        }

        @BeforeTemplate
        void before(Map<?, ?> actual) {
            System.out.println(actual);
        }

        @AfterTemplate
        void after(Object actual) {
            System.out.println("Changed: " + actual);
        }
    }

    public static class NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType {
        @BeforeTemplate
        void before(String actual) {
            System.out.println(actual);
        }

        @BeforeTemplate
        void before(Map<?, ?> actual) {
            System.out.println(actual);
        }

        @AfterTemplate
        void after(Object actual) {
            System.out.println("Changed: " + actual);
        }
    }

    public static class UsesTypeMapWhenAllBeforeTemplatesContainsMap {
        @BeforeTemplate
        void mapWithGeneric(Map<?, ?> actual) {
            System.out.println(actual);
        }

        @BeforeTemplate
        void mapWithGenericTwo(Map<?, ?> actual) {
            System.out.println(actual);
        }

        @AfterTemplate
        void mapWithoutGeneric(Map actual) {
            System.out.println("Changed: " + actual);
        }
    }

    public static class UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList {
        @BeforeTemplate
        void before(List<?> actual) {
            System.out.println(actual);
        }

        @BeforeTemplate
        void before(Map<?, ?> actual) {
            System.out.println(actual);
        }

        @AfterTemplate
        void after(Object actual) {
            System.out.println("Changed: " + actual);
        }
    }
}
