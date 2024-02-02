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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.java.template.processor.RefasterTemplateProcessor;
import org.openrewrite.java.template.processor.TypeAwareProcessor;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RefasterTemplateProcessorTest {
    @ParameterizedTest
    @ValueSource(strings = {
      "Arrays",
      "MethodThrows",
      "NestedPreconditions",
      "ParameterReuse",
      "UseStringIsEmpty",
      "SimplifyBooleans",
    })
    void generateRecipe(String recipeName) {
        Compilation compilation = compile("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThat(compilation)
          .generatedSourceFile("foo/" + recipeName + "Recipe")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("refaster/" + recipeName + "Recipe.java"));
    }

    @Test
    void generateRecipeInDefaultPackage() {
        Compilation compilation = compile("refaster/UnnamedPackage.java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThat(compilation)
          .generatedSourceFile("UnnamedPackageRecipe")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("refaster/UnnamedPackageRecipe.java"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "OrElseGetGet",
      "RefasterAnyOf",
    })
    void skipRecipeGeneration(String recipeName) {
        Compilation compilation = compile("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertEquals(0, compilation.generatedSourceFiles().size(), "Should not generate recipe for " + recipeName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "Escapes",
      "Generics",
      "Matching",
      "MultipleDereferences",
      "ShouldAddImports",
      "ShouldSupportNestedClasses",
      "SimplifyTernary",
    })
    void nestedRecipes(String recipeName) {
        Compilation compilation = compile("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThat(compilation) // Recipes (plural)
          .generatedSourceFile("foo/" + recipeName + "Recipes")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("refaster/" + recipeName + "Recipes.java"));
    }

    private static Compilation compile(String resourceName) {
        return compile(resourceName, new RefasterTemplateProcessor());
    }

    static Compilation compile(String resourceName, TypeAwareProcessor processor) {
        // As per https://github.com/google/compile-testing/blob/v0.21.0/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        return javac()
          .withProcessors(processor)
          .withClasspath(Arrays.asList(
            fileForClass(BeforeTemplate.class),
            fileForClass(AfterTemplate.class),
            fileForClass(com.sun.tools.javac.tree.JCTree.class),
            fileForClass(org.openrewrite.Recipe.class),
            fileForClass(org.openrewrite.java.JavaTemplate.class),
            fileForClass(org.slf4j.Logger.class),
            fileForClass(Primitive.class)
          ))
          .compile(JavaFileObjects.forResource(resourceName));
    }

    // As per https://github.com/google/auto/blob/auto-value-1.10.2/factory/src/test/java/com/google/auto/factory/processor/AutoFactoryProcessorTest.java#L99
    private static File fileForClass(Class<?> c) {
        URL url = c.getProtectionDomain().getCodeSource().getLocation();
        assert url.getProtocol().equals("file") || url.getProtocol().equals("jrt") : "Unexpected URL: " + url;
        return new File(url.getPath());
    }
}
