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
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.singletonList;
import static org.openrewrite.java.template.internal.ClasspathJarNameDetector.classpathFor;

class ClasspathJarNameDetectorTest {

    @Test
    void detectsJarNamesFromImports() throws IOException {
        Set<String> jarNames = compileAndExtractJarNames("""
          import java.util.List;
          import java.util.ArrayList;
          class Test {
              List<String> list = new ArrayList<>();
          }
          """);

        // JDK classes should not be included in the jar names
        assertThat(jarNames).isEmpty();
    }

    @Test
    void detectJUnit() throws IOException {
        Set<String> jarNames = compileAndExtractJarNames("""
          import org.junit.jupiter.api.Test;
          import org.junit.jupiter.api.Assertions;
          class TestClass {
              @Test
              void testMethod() {
                  Assertions.assertEquals(1, 1);
              }
          }
          """);

        assertThat(jarNames).containsExactly("junit-jupiter-api");
    }

    @Test
    void detectJUnitAndOpenTest4J() throws IOException {
        Set<String> jarNames = compileAndExtractJarNames("""
          import org.junit.jupiter.api.Test;
          import org.junit.jupiter.api.Assertions;
          class TestClass {
              @Test
              void testMethod() {
                  Assertions.assertAll("This throws org.opentest4j.MultipleFailuresError");
              }
          }
          """);

        assertThat(jarNames).containsExactly("junit-jupiter-api", "opentest4j");
    }

    @Test
    void detectJUnitAndOpenTest4JFromStatement() throws IOException {
        JCCompilationUnit compilationUnit = compile("""
          import org.junit.jupiter.api.Assertions;
          class TestClass {
              void testMethod() {
                  Assertions.assertAll("heading");
              }
          }
          """);

        Set<String> jarNames = classpathFor(
          firstStatement(compilationUnit),
          ImportDetector.imports(compilationUnit));

        assertThat(jarNames).containsExactly("junit-jupiter-api", "opentest4j");
    }

    @Test
    void detectTransitiveDependencyThroughInheritance() throws IOException {
        JCCompilationUnit compilationUnit = compile("""
          import org.openrewrite.java.JavaVisitor;
          class TestClass {
              void testMethod() {
                  String language = new JavaVisitor<>().getLanguage();
              }
          }
          """);

        Set<String> jarNames = classpathFor(
          firstStatement(compilationUnit),
          ImportDetector.imports(compilationUnit));

        // JavaVisitor from rewrite-java extends TreeVisitor from rewrite-core, both are needed
        assertThat(jarNames).containsExactly("rewrite-java", "rewrite-core");
    }

    private static JCTree.JCStatement firstStatement(JCCompilationUnit compilationUnit) {
        // Just the first statement of the method body, not the complete compilation unit, just like the processor does
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) compilationUnit.getTypeDecls().getFirst();
        JCTree.JCMethodDecl methodDecl = classDecl.getMembers().stream()
          .filter(JCTree.JCMethodDecl.class::isInstance)
          .map(JCTree.JCMethodDecl.class::cast)
          .filter(member -> !member.sym.isConstructor())
          .findFirst()
          .orElseThrow();
        return methodDecl.body.getStatements().getFirst();
    }

    private Set<String> compileAndExtractJarNames(@Language("java") String source) throws IOException {
        JCCompilationUnit compilationUnit = compile(source);
        return classpathFor(
          compilationUnit.getTypeDecls().getFirst(),
          ImportDetector.imports(compilationUnit)
        );
    }

    private static JCCompilationUnit compile(@Language("java") String source) throws IOException {
        JavaCompiler compiler = JavacTool.create();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            JavaFileObject sourceFile = new SimpleJavaFileObject(
              URI.create("string:///Test.java"),
              JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source;
                }
            };

            JavacTask task = (JavacTask) compiler.getTask(
              null,
              fileManager,
              diagnostics,
              singletonList("-proc:none"),
              null,
              singletonList(sourceFile));

            @SuppressWarnings("unchecked")
            Iterable<? extends JCCompilationUnit> compilationUnits = (Iterable<? extends JCCompilationUnit>) task.parse();
            task.analyze();

            return compilationUnits.iterator().next();
        }
    }
}
