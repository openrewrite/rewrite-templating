/*
 * Copyright 2022 the original author or authors.
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
package org.openrewrite.java.template;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class RefasterTemplateProcessorTest {

    @Test
    void generateRecipe() {
        // As per https://github.com/google/compile-testing/blob/c24c262e75498f89e56685b27e1d68e47b23d236/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        Compilation compilation = javac()
          .withProcessors(new RefasterTemplateProcessor())
          .compile(JavaFileObjects.forSourceString("HelloWorld",
            "                    package org.openrewrite.java.migrate.lang;\n" +
            "\n" +
            "                    import com.google.errorprone.refaster.annotation.AfterTemplate;\n" +
            "                    import com.google.errorprone.refaster.annotation.BeforeTemplate;\n" +
            "\n" +
            "                    public class UseStringIsEmpty {\n" +
            "                        @BeforeTemplate\n" +
            "                        boolean before(String s) {\n" +
            "                            return s.length() > 0;\n" +
            "                        }\n" +
            "\n" +
            "                        @AfterTemplate\n" +
            "                        boolean after(String s) {\n" +
            "                            return !s.isEmpty();\n" +
            "                        }\n" +
            "                    }"));
        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation)
          .generatedSourceFile("UseStringIsEmptyRecipe")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("UseStringIsEmptyRecipe.java"));
    }

}