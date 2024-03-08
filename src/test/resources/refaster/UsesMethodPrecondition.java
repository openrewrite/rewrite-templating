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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

public class UsesMethodPrecondition {
    public static class Primitive {
        @BeforeTemplate
        BigDecimal before(double d) {
            return new BigDecimal(d);
        }

        @AfterTemplate
        BigDecimal after(double d) {
            return BigDecimal.valueOf(d);
        }
    }

    public static class Class {
        @BeforeTemplate
        String before(String s1, String s2) {
            return s1.concat(s2);
        }

        @AfterTemplate
        String after(String s1, String s2) {
            return s1 + s2;
        }
    }

    public static class Parameterized {
        @BeforeTemplate
        List<String> before(String s) {
            return Collections.singletonList(s);
        }

        @AfterTemplate
        List<String> after(String s) {
            return Arrays.asList(s);
        }
    }

    public static class Varargs {
        @BeforeTemplate
        String before(String format, String arg0) {
            return new Formatter().format(format, arg0).toString();
        }

        @AfterTemplate
        String after(String format, String arg0) {
            return String.format(format, arg0);
        }
    }
}
