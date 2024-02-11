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
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.java.template.processor.RefasterTemplateProcessor;
import org.openrewrite.java.template.processor.TypeAwareProcessor;

import javax.tools.JavaFileObject;
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
      "CharacterEscapeAnnotation",
      "MethodThrows",
      "NestedPreconditions",
      "ParameterReuse",
      "UseStringIsEmpty",
      "SimplifyBooleans",
    })
    void generateRecipe(String recipeName) {
        Compilation compilation = compileResource("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThat(compilation)
          .generatedSourceFile("foo/" + recipeName + "Recipe")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("refaster/" + recipeName + "Recipe.java"));
    }

    @Test
    void generateRecipeInDefaultPackage() {
        Compilation compilation = compileResource("refaster/UnnamedPackage.java");
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
        Compilation compilation = compileResource("refaster/" + recipeName + ".java");
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
        Compilation compilation = compileResource("refaster/" + recipeName + ".java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(0);
        assertThat(compilation) // Recipes (plural)
          .generatedSourceFile("foo/" + recipeName + "Recipes")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("refaster/" + recipeName + "Recipes.java"));
    }

    @Test
    void stringIsEmptyPredicate() {
        Compilation compilation = compileResource("refaster/StringIsEmptyPredicate.java");
        assertThat(compilation).succeeded();
        assertThat(compilation).hadNoteCount(1);
        assertThat(compilation).hadNoteContaining("Lambdas are currently not supported");
        assertEquals(0, compilation.generatedSourceFiles().size(), "Not yet supported");
    }

    @Test
    void inline() {
        Compilation compilation = compileSource("foo.UseStringIsEmpty",
          "package foo;\n" +
          "\n" +
          "import com.google.errorprone.refaster.annotation.AfterTemplate;\n" +
          "import com.google.errorprone.refaster.annotation.BeforeTemplate;\n" +
          "\n" +
          "public class UseStringIsEmpty {\n" +
          "    @BeforeTemplate\n" +
          "    boolean before(String s) {\n" +
          "        return s.length() > 0;\n" +
          "    }\n" +
          "\n" +
          "    @AfterTemplate\n" +
          "    boolean after(String s) {\n" +
          "        return !(s.isEmpty());\n" +
          "    }\n" +
          "}\n");
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("foo/UseStringIsEmptyRecipe")
          .hasSourceEquivalentTo(JavaFileObjects.forSourceString("/SOURCE_OUTPUT/foo/UseStringIsEmptyRecipe",
            //language=java
            "package foo;\n" +
            "\n" +
            "import org.openrewrite.ExecutionContext;\n" +
            "import org.openrewrite.Preconditions;\n" +
            "import org.openrewrite.Recipe;\n" +
            "import org.openrewrite.TreeVisitor;\n" +
            "import org.openrewrite.internal.lang.NonNullApi;\n" +
            "import org.openrewrite.java.JavaParser;\n" +
            "import org.openrewrite.java.JavaTemplate;\n" +
            "import org.openrewrite.java.JavaVisitor;\n" +
            "import org.openrewrite.java.search.*;\n" +
            "import org.openrewrite.java.template.Primitive;\n" +
            "import org.openrewrite.java.template.Semantics;\n" +
            "import org.openrewrite.java.template.function.*;\n" +
            "import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;\n" +
            "import org.openrewrite.java.tree.*;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * OpenRewrite recipe created for Refaster template {@code UseStringIsEmpty}.\n" +
            " */\n" +
            "@SuppressWarnings(\"all\")\n" +
            "@NonNullApi\n" +
            "public class UseStringIsEmptyRecipe extends Recipe {\n" +
            "\n" +
            "    /**\n" +
            "     * Instantiates a new instance.\n" +
            "     */\n" +
            "    public UseStringIsEmptyRecipe() {}\n" +
            "\n" +
            "    @Override\n" +
            "    public String getDisplayName() {\n" +
            "        return \"Refaster template `UseStringIsEmpty`\";\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public String getDescription() {\n" +
            "        return \"Recipe created for the following Refaster template:\\n```java\\npublic class UseStringIsEmpty {\\n    \\n    @BeforeTemplate()\\n    boolean before(String s) {\\n        return s.length() > 0;\\n    }\\n    \\n    @AfterTemplate()\\n    boolean after(String s) {\\n        return !(s.isEmpty());\\n    }\\n}\\n```\\n.\";\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public TreeVisitor<?, ExecutionContext> getVisitor() {\n" +
            "        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {\n" +
            "            final JavaTemplate before = JavaTemplate\n" +
            "                    .builder(\"#{s:any(java.lang.String)}.length() > 0\")\n" +
            "                    .build();\n" +
            "            final JavaTemplate after = JavaTemplate\n" +
            "                    .builder(\"!(#{s:any(java.lang.String)}.isEmpty())\")\n" +
            "                    .build();\n" +
            "\n" +
            "            @Override\n" +
            "            public J visitBinary(J.Binary elem, ExecutionContext ctx) {\n" +
            "                JavaTemplate.Matcher matcher;\n" +
            "                if ((matcher = before.matcher(getCursor())).find()) {\n" +
            "                    return embed(\n" +
            "                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),\n" +
            "                            getCursor(),\n" +
            "                            ctx,\n" +
            "                            REMOVE_PARENS, SHORTEN_NAMES, SIMPLIFY_BOOLEANS\n" +
            "                    );\n" +
            "                }\n" +
            "                return super.visitBinary(elem, ctx);\n" +
            "            }\n" +
            "\n" +
            "        };\n" +
            "        return Preconditions.check(\n" +
            "                new UsesMethod<>(\"java.lang.String length(..)\"),\n" +
            "                javaVisitor\n" +
            "        );\n" +
            "    }\n" +
            "}"));
    }

    private static Compilation compileResource(String resourceName) {
        return compileResource(resourceName, new RefasterTemplateProcessor());
    }

    static Compilation compileResource(String resourceName, TypeAwareProcessor processor) {
        // As per https://github.com/google/compile-testing/blob/v0.21.0/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        return compile(JavaFileObjects.forResource(resourceName), processor);
    }

    static Compilation compileSource(String fqn, @Language("java") String source) {
        return compile(JavaFileObjects.forSourceString(fqn, source), new RefasterTemplateProcessor());
    }

    static Compilation compileSource(String fqn, @Language("java") String source, TypeAwareProcessor processor) {
        return compile(JavaFileObjects.forSourceString(fqn, source), processor);
    }

    static Compilation compile(JavaFileObject javaFileObject, TypeAwareProcessor processor) {
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
          .compile(javaFileObject);
    }

    // As per https://github.com/google/auto/blob/auto-value-1.10.2/factory/src/test/java/com/google/auto/factory/processor/AutoFactoryProcessorTest.java#L99
    private static File fileForClass(Class<?> c) {
        URL url = c.getProtectionDomain().getCodeSource().getLocation();
        assert url.getProtocol().equals("file") || url.getProtocol().equals("jrt") : "Unexpected URL: " + url;
        return new File(url.getPath());
    }
}
