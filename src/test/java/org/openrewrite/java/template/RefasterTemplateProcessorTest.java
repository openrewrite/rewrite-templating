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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class RefasterTemplateProcessorTest {

    @ParameterizedTest
    @ValueSource(strings = {
      "UseStringIsEmpty",
    })
    void generateRecipe(String recipeName) {
        // As per https://github.com/google/compile-testing/blob/v0.21.0/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        Compilation compilation = javac()
          .withProcessors(new RefasterTemplateProcessor())
          .withClasspath(classpath())
          .compile(JavaFileObjects.forResource("recipes/" + recipeName + ".java"));
        assertThat(compilation).succeeded();
        compilation.generatedSourceFiles().forEach(System.out::println);
        assertThat(compilation)
          .generatedSourceFile("foo/" + recipeName + "Recipe")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("recipes/" + recipeName + "Recipe.java"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "ShouldSupportNestedClasses",
      "ShouldAddImports",
      "MultipleDereferences",
    })
    void nestedRecipes(String recipeName) {
        Compilation compilation = javac()
          .withProcessors(new RefasterTemplateProcessor())
          .withClasspath(classpath())
          .compile(JavaFileObjects.forResource("recipes/" + recipeName + ".java"));
        assertThat(compilation).succeeded();
        compilation.generatedSourceFiles().forEach(System.out::println);
        assertThat(compilation) // Recipes (plural)
          .generatedSourceFile("foo/" + recipeName + "Recipes")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("recipes/" + recipeName + "Recipes.java"));
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

    // As per https://github.com/google/auto/blob/auto-value-1.10.2/factory/src/test/java/com/google/auto/factory/processor/AutoFactoryProcessorTest.java#L99
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