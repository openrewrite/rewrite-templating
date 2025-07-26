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

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;

import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.Files.exists;
import static java.util.Objects.hash;

public class ShouldAddImports {

    public static class StringValueOf {
        @BeforeTemplate
        String before(String s) {
            return String.valueOf(s);
        }

        @AfterTemplate
        String after(String s) {
            return Objects.toString(s);
        }
    }

    public static class ObjectsEquals {
        @BeforeTemplate
        boolean equals(int a, int b) {
            return Objects.equals(a, b);
        }
        @BeforeTemplate
        boolean compareZero(int a, int b) {
            return Integer.compare(a, b) == 0;
        }

        @AfterTemplate
        boolean isis(int a, int b) {
            return a == b;
        }
    }

    public static class StaticImportObjectsHash {
        @BeforeTemplate
        int before(String s) {
            return hash(s);
        }

        @AfterTemplate
        int after(String s) {
            return s.hashCode();
        }
    }

    public static class FileExists {
        @BeforeTemplate
        boolean before(Path path) {
            return path.toFile().exists();
        }

        @AfterTemplate
        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
        boolean after(Path path) {
            return exists(path);
        }
    }

    public static class FindStringIsEmpty {
        @BeforeTemplate
        boolean before(String s) {
            return s.isEmpty();
        }
    }
}
