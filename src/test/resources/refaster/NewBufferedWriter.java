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

import java.io.BufferedWriter;
import java.io.IOException;

class NewBufferedWriter {
    @BeforeTemplate
    BufferedWriter before(String f, Boolean b) throws IOException {
        return new BufferedWriter(new java.io.FileWriter(f, b));
    }

    @AfterTemplate
    BufferedWriter after(String f, Boolean b) throws IOException {
        return java.nio.file.Files.newBufferedWriter(new java.io.File(f).toPath(), b ?
                java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE);
    }
}
