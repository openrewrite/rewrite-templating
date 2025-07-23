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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MultipleDereferences {

    public static class VoidType {
        @BeforeTemplate
        void before(Path p) throws IOException {
            Files.delete(p);
        }

        @AfterTemplate
        void after(Path p)throws IOException {
            Files.delete(p);
        }
    }

    public static class StringIsEmpty {
        @BeforeTemplate
        boolean before(String s) {
            return s.isEmpty();
        }

        @AfterTemplate
        boolean after(String s) {
            return s != null && s.length() == 0;
        }
    }

    public static class EqualsItself {
        @BeforeTemplate
        boolean before(Object o) {
            return o == o;
        }

        @AfterTemplate
        boolean after(Object o) {
            return true;
        }
    }

}
