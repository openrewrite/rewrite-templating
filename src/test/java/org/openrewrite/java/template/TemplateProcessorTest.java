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

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.java.template.processor.TemplateProcessor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.openrewrite.java.template.RefasterTemplateProcessorTest.*;

class TemplateProcessorTest {
    @ParameterizedTest
    @ValueSource(strings = {
      "FullyQualified",
      "FullyQualifiedField",
      "Primitive",
      "Unqualified",
    })
    void qualification(String qualifier) {
        // As per https://github.com/google/compile-testing/blob/v0.21.0/src/main/java/com/google/testing/compile/package-info.java#L53-L55
        Compilation compilation = compileResource("template/ShouldAddClasspathRecipes.java", new TemplateProcessor());
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("foo/ShouldAddClasspathRecipes$" + qualifier + "Recipe$1_before")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/ShouldAddClasspathRecipe$" + qualifier + "Recipe$1_before.java"));
        assertThat(compilation)
          .generatedSourceFile("foo/ShouldAddClasspathRecipes$" + qualifier + "Recipe$1_after")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/ShouldAddClasspathRecipe$" + qualifier + "Recipe$1_after.java"));
    }

    @Test
    void parameterReuse() {
        Compilation compilation = compileResource("template/ParameterReuseRecipe.java", new TemplateProcessor());
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("foo/ParameterReuseRecipe$1_before")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/ParameterReuseRecipe$1_before.java"));
    }

    @Test
    void parserClasspath() {
        Compilation compilation = compileResource("template/LoggerRecipe.java", new TemplateProcessor());
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("template/LoggerRecipe$1_logger")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/LoggerRecipe$1_logger.java"));
        assertThat(compilation)
          .generatedSourceFile("template/LoggerRecipe$1_info")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/LoggerRecipe$1_info.java"));
    }

    @Test
    void generics() {
        Compilation compilation = compileResource("template/Generics.java", new TemplateProcessor());
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("template/Generics$1_before")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/Generics$1_before.java"));
        assertThat(compilation)
          .generatedSourceFile("template/Generics$1_after")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/Generics$1_after.java"));
    }

    @Test
    void throwNew() {
        Compilation compilation = compileResource("template/ThrowNewRecipe.java", new TemplateProcessor());
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("template/ThrowNewRecipe$1_template")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/ThrowNewRecipe$1_template.java"));
    }

    @Test
    void unnamedPackage() {
        Compilation compilation = compileResource("template/UnnamedPackage.java", new TemplateProcessor());
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("UnnamedPackage$1_message")
          .hasSourceEquivalentTo(JavaFileObjects.forResource("template/UnnamedPackage$1_message.java"));
    }

    @Test
    void inline() {
        Compilation compilation = compileSource("foo.SomeRecipe",
          "package foo;" +
          "import org.openrewrite.ExecutionContext;" +
          "import org.openrewrite.java.JavaVisitor;" +
          "import org.openrewrite.java.JavaTemplate;" +
          "import org.openrewrite.java.template.Semantics;" +
          "public class SomeRecipe {" +
          "    JavaVisitor visitor = new JavaVisitor<ExecutionContext>() {" +
          "        JavaTemplate.Builder before = Semantics.statement(this, \"before\", (String s) -> System.out.println(s));" +
          "    };" +
          "}",
          new TemplateProcessor());
        assertThat(compilation).succeeded();
        assertThat(compilation)
          .generatedSourceFile("foo/SomeRecipe$1_before")
          .hasSourceEquivalentTo(JavaFileObjects.forSourceString("/SOURCE_OUTPUT/foo/SomeRecipe$1_before",
            //language=java
            "package foo;\n" +
            "import org.openrewrite.java.*;\n" +
            "\n" +
            "/**\n" +
            " * OpenRewrite `before` template created for {@code foo.SomeRecipe$1}.\n" +
            " */\n" +
            "@SuppressWarnings(\"all\")\n" +
            "public class SomeRecipe$1_before {\n" +
            "    /**\n" +
            "     * Instantiates a new instance.\n" +
            "     */\n" +
            "    public SomeRecipe$1_before() {}\n" +
            "\n" +
            "    /**\n" +
            "     * Get the {@code JavaTemplate.Builder} to match or replace.\n" +
            "     * @return the JavaTemplate builder.\n" +
            "     */\n" +
            "    public static JavaTemplate.Builder getTemplate() {\n" +
            "        return JavaTemplate\n" +
            "                .builder(\"System.out.println(#{s:any(java.lang.String)})\");\n" +
            "    }\n" +
            "}"));
    }
}
