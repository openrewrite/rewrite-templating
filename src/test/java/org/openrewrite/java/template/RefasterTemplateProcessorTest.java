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
package org.openrewrite.java.template;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.java.template.processor.RefasterTemplateProcessor;

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
      "NestedPreconditions"
    })
    void generateRecipe(String recipeName) {
        // As per https://github.com/google/compile-testing/blob/v0.21.0/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        Compilation compilation = javac()
          .withProcessors(new RefasterTemplateProcessor())
          .withClasspath(classpath())
          .compile(JavaFileObjects.forResource("refaster/" + recipeName + ".java"));
        assertThat(compilation).succeeded();
        compilation.generatedSourceFiles().forEach(System.out::println);
        assertThat(compilation)
          .generatedSourceFile("foo/" + recipeName + "Recipe")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("refaster/" + recipeName + "Recipe.java"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "ShouldSupportNestedClasses",
      "ShouldAddImports",
      "MultipleDereferences",
      "Matching",
    })
    void nestedRecipes(String recipeName) {
        Compilation compilation = javac()
          .withProcessors(new RefasterTemplateProcessor())
          .withClasspath(classpath())
          .compile(JavaFileObjects.forResource("refaster/" + recipeName + ".java"));
        assertThat(compilation).succeeded();
        compilation.generatedSourceFiles().forEach(System.out::println);
        assertThat(compilation) // Recipes (plural)
          .generatedSourceFile("foo/" + recipeName + "Recipes")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("refaster/" + recipeName + "Recipes.java"));
    }

    @NotNull
    static Collection<File> classpath() {
        return Arrays.asList(
          fileForClass(BeforeTemplate.class),
          fileForClass(AfterTemplate.class),
          fileForClass(com.sun.tools.javac.tree.JCTree.class),
          fileForClass(org.openrewrite.Recipe.class),
          fileForClass(org.openrewrite.java.JavaTemplate.class),
          fileForClass(org.slf4j.Logger.class),
          fileForClass(Primitive.class)
        );
    }

    // As per https://github.com/google/auto/blob/auto-value-1.10.2/factory/src/test/java/com/google/auto/factory/processor/AutoFactoryProcessorTest.java#L99
    static File fileForClass(Class<?> c) {
        URL url = c.getProtectionDomain().getCodeSource().getLocation();
        assert url.getProtocol().equals("file");
        return new File(url.getPath());
    }
}
