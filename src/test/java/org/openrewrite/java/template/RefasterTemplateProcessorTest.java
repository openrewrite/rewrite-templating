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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class RefasterTemplateProcessorTest {

    @Test
    void generateRecipe() {
        // As per https://github.com/google/compile-testing/blob/c24c262e75498f89e56685b27e1d68e47b23d236/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        Compilation compilation = javac()
          .withProcessors(new RefasterTemplateProcessor())
          .withClasspath(classpath())
          .compile(JavaFileObjects.forResource("UseStringIsEmpty.java"));
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("org/openrewrite/java/migrate/lang/UseStringIsEmptyRecipe")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("UseStringIsEmptyRecipe.java"));
    }

    @NotNull
    private static Collection<File> classpath() {
        return Arrays.asList(
          fileForClass(BeforeTemplate.class.getName()),
          fileForClass(AfterTemplate.class.getName()),
          fileForClass(com.sun.tools.javac.tree.JCTree.class.getName()),
          fileForClass(org.openrewrite.Recipe.class.getName()),
          fileForClass(org.openrewrite.java.JavaTemplate.class.getName()),
          fileForClass(org.slf4j.Logger.class.getName())
        );
    }

    static File fileForClass(String className) {
        Class<?> c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        URL url = c.getProtectionDomain().getCodeSource().getLocation();
        assert url.getProtocol().equals("file");
        return new File(url.getPath());
    }
}