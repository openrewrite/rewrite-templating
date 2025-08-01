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
import jakarta.annotation.Generated;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.java.template.processor.RefasterTemplateProcessor;
import org.openrewrite.java.template.processor.TypeAwareProcessor;

import javax.annotation.security.RolesAllowed;
import javax.tools.JavaFileObject;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RefasterTemplateProcessorTest {
    @ParameterizedTest
    @ValueSource(strings = {
      "Arrays",
      "CharacterEscapeAnnotation",
      "ComplexGenerics",
      "FindListAdd",
      "MatchOrder",
      "MethodThrows",
      "MultimapGet",
      "NestedPreconditions",
      "NewBufferedWriter",
      "OrElseGetGet",
      "ParameterOrder",
      "SimplifyBooleans",
      "StringIsEmptyPredicate",
      "TwoVisitMethods",
      "UseStringIsEmpty"
    })
    void generateRecipe(String recipeName) {
        Compilation compilation = compileResource("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThatGeneratedSourceFileMatchesResource(compilation,
          "foo/" + recipeName + "Recipe",
          "refaster/" + recipeName + "Recipe.java");
    }

    @Test
    void generateRecipeInDefaultPackage() {
        Compilation compilation = compileResource("refaster/UnnamedPackage.java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThatGeneratedSourceFileMatchesResource(compilation, "UnnamedPackageRecipe", "refaster/UnnamedPackageRecipe.java");
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "InvalidRecipe",
    })
    void skipRecipeGeneration(String recipeName) {
        Compilation compilation = compileResource("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertEquals(0, compilation.generatedSourceFiles().size(), "Should not generate recipe for " + recipeName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "EmptyAfterMethod",
      "Escapes",
      "Generics",
      "Lambdas",
      "Matching",
      "MultipleDereferences",
      "Parameters",
      "PicnicRules",
      "PreconditionsVerifier",
      "RefasterAnyOf",
      "ShouldAddImports",
      "ShouldSupportNestedClasses",
      "SimplifyTernary",
      "SuppressedWarningsAsTags"
    })
    void nestedRecipes(String recipeName) {
        Compilation compilation = compileResource("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThatGeneratedSourceFileMatchesResource(compilation,
          "foo/" + recipeName + "Recipes",
          "refaster/" + recipeName + "Recipes.java");
    }

    @Test
    void missingArguments() {
        Compilation compilation = compileResource("refaster/MissingArguments.java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteContaining("@AfterTemplate defines arguments that are not present in all @BeforeTemplate methods");
        assertEquals(0, compilation.generatedSourceFiles().size(), "Must not generate recipe for missing arguments");
    }

    @Test
    void extraArguments() {
        Compilation compilation = compileResource("refaster/ExtraArguments.java");
        assertThat(compilation).succeeded();
        assertEquals(1, compilation.generatedSourceFiles().size(), "Must generate recipe for discarded arguments");
    }

    @Test
    void annotatedUnusedArgument() {
        Compilation compilation = compileResource("refaster/AnnotatedUnusedArgument.java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteContaining("Ignoring annotation org.openrewrite.java.template.Matches on unused parameter b");
        assertThat(compilation).hadNoteContaining("Ignoring annotation org.openrewrite.java.template.NotMatches on unused parameter c");
        assertEquals(1, compilation.generatedSourceFiles().size(), "Should warn but generate recipe for discarded arguments");
        assertThatGeneratedSourceFileMatchesResource(compilation, "foo/AnnotatedUnusedArgumentRecipe", "refaster/AnnotatedUnusedArgumentRecipe.java");
    }

    @Test
    void jakartaGeneratedAnnotationOverride() throws Exception {
        // As per https://github.com/google/compile-testing/blob/v0.21.0/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        Compilation compilation = compile(
          JavaFileObjects.forResource("refaster/UseStringIsEmpty.java"),
          new RefasterTemplateProcessor(),
          "-Arewrite.generatedAnnotation=jakarta.annotation.Generated");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);

        // Replace import in reference output file and compare with what's generated
        Path path = Paths.get(requireNonNull(getClass().getResource("/refaster/UseStringIsEmptyRecipe.java")).toURI());
        String source = new String(Files.readAllBytes(path))
          .replace("javax.annotation.Generated", "jakarta.annotation.Generated");
        JavaFileObject expectedSource = JavaFileObjects.forSourceString("refaster.UseStringIsEmptyRecipe", source);
        assertThat(compilation)
          .generatedSourceFile("foo/UseStringIsEmptyRecipe")
          .hasSourceEquivalentTo(expectedSource);
    }

    private static Compilation compileResource(String resourceName) {
        return compileResource(resourceName, new RefasterTemplateProcessor());
    }

    static Compilation compileResource(String resourceName, TypeAwareProcessor processor) {
        // As per https://github.com/google/compile-testing/blob/v0.21.0/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        return compile(JavaFileObjects.forResource(resourceName), processor);
    }

    @SuppressWarnings("unused") // use when text blocks are available
    static Compilation compileSource(String fqn, @Language("java") String source) {
        return compile(JavaFileObjects.forSourceString(fqn, source), new RefasterTemplateProcessor());
    }

    @SuppressWarnings("unused") // use when text blocks are available
    static Compilation compileSource(String fqn, @Language("java") String source, TypeAwareProcessor processor) {
        return compile(JavaFileObjects.forSourceString(fqn, source), processor);
    }

    static Compilation compile(JavaFileObject javaFileObject, TypeAwareProcessor processor, Object... options) {
        return javac()
          .withProcessors(processor)
          .withClasspath(Arrays.asList(
            fileForClass(BeforeTemplate.class),
            fileForClass(AfterTemplate.class),
            fileForClass(com.google.common.collect.ImmutableMap.class),
            fileForClass(org.openrewrite.Recipe.class),
            fileForClass(org.openrewrite.java.JavaTemplate.class),
            fileForClass(org.slf4j.Logger.class),
            fileForClass(Primitive.class),
            fileForClass(NullMarked.class),
            fileForClass(Generated.class), // jakarta.annotation.Generated
            fileForClass(RolesAllowed.class) // javax.annotation.Generated
          ))
          .withOptions(options)
          .compile(javaFileObject);
    }

    // As per https://github.com/google/auto/blob/auto-value-1.10.2/factory/src/test/java/com/google/auto/factory/processor/AutoFactoryProcessorTest.java#L99

    private static File fileForClass(Class<?> c) {
        URL url = c.getProtectionDomain().getCodeSource().getLocation();
        assert url.getProtocol().equals("file") || url.getProtocol().equals("jrt") : "Unexpected URL: " + url;
        return new File(url.getPath());
    }

    private static void assertThatGeneratedSourceFileMatchesResource(Compilation compilation, String qualifiedName, String resourceName) {
        JavaFileObject expectedSource = JavaFileObjects.forResource(resourceName);

        // XXX Enable the following lines to overwrite the expected output files
//        try (java.io.Reader in = compilation.generatedSourceFile(qualifiedName).get().openReader(true);
//             java.io.Writer out = new java.io.FileWriter("src/test/resources/" + resourceName)) {
//            char[] buffer = new char[1024];
//            int len;
//            while ((len = in.read(buffer)) >= 0) {
//                out.write(buffer, 0, len);
//            }
//            org.junit.jupiter.api.Assertions.fail("File was overwritten; check `git diff` instead!");
//        } catch (java.io.IOException e) {
//            throw new RuntimeException(e);
//        }

        assertThat(compilation)
          .generatedSourceFile(qualifiedName)
          .hasSourceEquivalentTo(expectedSource);
    }

}
