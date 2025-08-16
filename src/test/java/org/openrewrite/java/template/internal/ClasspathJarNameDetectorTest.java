/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.java.template.internal;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class ClasspathJarNameDetectorTest {

    @Test
    void detectsJarNamesFromImports() throws IOException {
        String source =
          "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "\n" +
            "class Test {\n" +
            "    List<String> list = new ArrayList<>();\n" +
            "}";

        Set<String> jarNames = compileAndExtractJarNames(source);

        // JDK classes should not be included in the jar names
        assertThat(jarNames).isEmpty();
    }

    @Test
    void detectJUnit() throws IOException {
        String source =
          "import org.junit.jupiter.api.Test;\n" +
            "import org.junit.jupiter.api.Assertions;\n" +
            "\n" +
            "class TestClass {\n" +
            "    @Test\n" +
            "    void testMethod() {\n" +
            "        Assertions.assertEquals(1, 1);\n" +
            "    }\n" +
            "}";

        Set<String> jarNames = compileAndExtractJarNames(source);

        assertThat(jarNames).containsExactly("junit-jupiter-api");
    }

    @Test
    void detectJUnitAndOpenTest4J() throws IOException {
        String source =
          "import org.junit.jupiter.api.Test;\n" +
            "import org.junit.jupiter.api.Assertions;\n" +
            "\n" +
            "class TestClass {\n" +
            "    @Test\n" +
            "    void testMethod() {\n" +
            "        Assertions.assertAll(\"This throws org.opentest4j.MultipleFailuresError\");\n" +
            "    }\n" +
            "}";

        Set<String> jarNames = compileAndExtractJarNames(source);

        assertThat(jarNames).containsExactly("junit-jupiter-api", "opentest4j");
    }

    private Set<String> compileAndExtractJarNames(String source) throws IOException {
        JCCompilationUnit compilationUnit = compile(source);
        List<Symbol> imports = extractImports(compilationUnit);
        return ClasspathJarNameDetector.classpathFor(
          compilationUnit.getTypeDecls().get(0),
          imports
        );
    }

    private static JCCompilationUnit compile(String source) throws IOException {
        JavaCompiler compiler = JavacTool.create();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {

            JavaFileObject sourceFile = new SimpleJavaFileObject(
              URI.create("string:///Test.java"),
              JavaFileObject.Kind.SOURCE
            ) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source;
                }
            };

            JavacTask task = (JavacTask) compiler.getTask(
              null,
              fileManager,
              diagnostics,
              Collections.singletonList("-proc:none"),
              null,
              Collections.singletonList(sourceFile)
            );

            @SuppressWarnings("unchecked")
            Iterable<? extends JCCompilationUnit> compilationUnits = (Iterable<? extends JCCompilationUnit>) task.parse();
            task.analyze();

            return compilationUnits.iterator().next();
        }
    }

    private static List<Symbol> extractImports(JCCompilationUnit compilationUnit) {
        List<Symbol> imports = new ArrayList<>();
        if (compilationUnit.getImports() != null) {
            for (JCTree.JCImport imp : compilationUnit.getImports()) {
                if (imp.qualid instanceof JCTree.JCFieldAccess) {
                    JCTree.JCFieldAccess fa = (JCTree.JCFieldAccess) imp.qualid;
                    if (fa.sym != null) {
                        imports.add(fa.sym);
                    }
                }
            }
        }
        return imports;
    }
}
