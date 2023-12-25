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
import org.openrewrite.java.template.Primitive;
import org.slf4j.LoggerFactory;

import static java.util.regex.Pattern.DOTALL;
import static org.slf4j.LoggerFactory.getLogger;

public class ShouldAddClasspath {

    class Unqualified {
        @BeforeTemplate
        void before(String message) {
            System.out.println(message);
        }

        @AfterTemplate
        void after(String message) {
            getLogger(message);
        }
    }

    class FullyQualified {
        @BeforeTemplate
        void before(String message) {
            System.out.println(message);
        }

        @AfterTemplate
        void after(String message) {
            getLogger(message);
        }
    }

    class FullyQualifiedField {
        @BeforeTemplate
        void before(String message) {
            java.util.regex.Pattern.compile(message, DOTALL);
        }

        @AfterTemplate
        void after(String message) {
            System.out.println(message);
        }
    }

    class Primitive {
        @BeforeTemplate
        void before(@org.openrewrite.java.template.Primitive int i) {
            System.out.println(i);
        }

        @AfterTemplate
        void after(@org.openrewrite.java.template.Primitive int i) {
            System.out.print(i);
        }
    }

}