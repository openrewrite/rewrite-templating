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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.test.RewriteTest;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.SHORTEN_NAMES;
import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.STATIC_IMPORT_ALWAYS;
import static org.openrewrite.test.RewriteTest.toRecipe;

class AbstractRefasterJavaVisitorTest implements RewriteTest {
    @DocumentExample
    @Test
    void useStaticImports() {
        rewriteRun(
          spec -> spec.recipe(toRecipe(FilesExistsVisitor::new)),
          java(
            """
              import java.nio.file.Path;

              class A {
                  boolean pathExists(Path path) {
                      return path.toFile().exists();
                  }
              }
              """,
            """
              import java.nio.file.Path;

              import static java.nio.file.Files.exists;

              class A {
                  boolean pathExists(Path path) {
                      return exists(path);
                  }
              }
              """
          )
        );
    }

    @Test
    void useStaticImportsForNestedFactory() {
        rewriteRun(
          spec -> spec.recipe(toRecipe(AssertThatIsEqualToVisitor::new)),
          java(
            """
              import org.assertj.core.api.Assertions;

              class A {
                  void test(int actual, int expected) {
                      Assertions.assertThat(actual).isSameAs(expected);
                  }
              }
              """,
            """
              import static org.assertj.core.api.Assertions.assertThat;

              class A {
                  void test(int actual, int expected) {
                      assertThat(actual).isEqualTo(expected);
                  }
              }
              """
          )
        );
    }

    @Test
    void referenceComparisonOfTypeVariableSkipsPrimitives() {
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new AbstractRefasterJavaVisitor() {
              private final JavaTemplate before$0 = JavaTemplate
                .builder("org.assertj.core.api.Assertions.assertThat(#{actual:any(T)} == #{expected:any(T)}).isTrue()")
                .genericTypes("T")
                .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                .build();
              private final JavaTemplate before$1 = JavaTemplate
                .builder("org.assertj.core.api.Assertions.assertThat(#{actual:any(T)} != #{expected:any(T)}).isFalse()")
                .genericTypes("T")
                .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                .build();
              private final JavaTemplate after = JavaTemplate
                .builder("org.assertj.core.api.Assertions.assertThat(#{actual:any(T)}).isSameAs(#{expected:any(T)})")
                .genericTypes("T")
                .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                .build();

              @Override
              public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                  for (JavaTemplate before : new JavaTemplate[]{before$0, before$1}) {
                      JavaTemplate.Matcher matcher = before.matcher(getCursor());
                      if (matcher.find()) {
                          if (anyPrimitive(matcher.parameter(0), matcher.parameter(1))) {
                              return super.visitMethodInvocation(elem, ctx);
                          }
                          return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES
                          );
                      }
                  }
                  return super.visitMethodInvocation(elem, ctx);
              }
          })),
          java(
            """
              import org.assertj.core.api.Assertions;

              class Primitives {
                  void test(byte b, int i, int j, long l, Integer boxed) {
                      Assertions.assertThat(b == 1).isTrue();
                      Assertions.assertThat(i == 5L).isTrue();
                      Assertions.assertThat(boxed == i).isTrue();
                      Assertions.assertThat(i == j).isTrue();
                      Assertions.assertThat(l == 5L).isTrue();
                      Assertions.assertThat(i != 5L).isFalse();
                      Assertions.assertThat(i != j).isFalse();
                  }
              }
              """
          ),
          java(
            """
              import org.assertj.core.api.Assertions;

              class References {
                  void test(Object o1, Object o2) {
                      Assertions.assertThat(o1 == o2).isTrue();
                      Assertions.assertThat(o1 != o2).isFalse();
                  }
              }
              """,
            """
              import org.assertj.core.api.Assertions;

              class References {
                  void test(Object o1, Object o2) {
                      Assertions.assertThat(o1).isSameAs(o2);
                      Assertions.assertThat(o1).isSameAs(o2);
                  }
              }
              """
          )
        );
    }

    @Test
    void referenceComparisonGuardCoversEveryOperand() {
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new AbstractRefasterJavaVisitor() {
              private final JavaTemplate before = JavaTemplate
                .builder("#{a:any(T)} == #{b:any(T)} && #{b} != #{c:any(T)}")
                .genericTypes("T")
                .build();

              @Override
              public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                  JavaTemplate.Matcher matcher = before.matcher(getCursor());
                  if (matcher.find()) {
                      if (anyPrimitive(matcher.parameter(0), matcher.parameter(1), matcher.parameter(2))) {
                          return super.visitBinary(elem, ctx);
                      }
                      return SearchResult.found(elem);
                  }
                  return super.visitBinary(elem, ctx);
              }
          })),
          java(
            """
              class A {
                  boolean test(Object o1, Object o2, Object o3, int i) {
                      return (o1 == o2 && o2 != o3) || (o1 == o2 && o2 != i);
                  }
              }
              """,
            """
              class A {
                  boolean test(Object o1, Object o2, Object o3, int i) {
                      return (/*~~>*/o1 == o2 && o2 != o3) || (o1 == o2 && o2 != i);
                  }
              }
              """
          )
        );
    }

    @Test
    void unknownTypesAreNotPrimitive() {
        assertThat(AbstractRefasterJavaVisitor.anyPrimitive(
          identifier(JavaType.Unknown.getInstance()), identifier(null), identifier(JavaType.Primitive.String), identifier(JavaType.Primitive.Null)))
          .isFalse();
        assertThat(AbstractRefasterJavaVisitor.anyPrimitive(identifier(null), identifier(JavaType.Primitive.Int)))
          .isTrue();
    }

    private static J.Identifier identifier(@Nullable JavaType type) {
        return new J.Identifier(Tree.randomId(), Space.EMPTY, Markers.EMPTY, emptyList(), "x", type, null);
    }

    private static class FilesExistsVisitor extends AbstractRefasterJavaVisitor {
        private final JavaTemplate template = JavaTemplate
          .builder("#{path:any(java.nio.file.Path)}.toFile().exists()")
          .build();

        @Override
        public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
            JavaTemplate.Matcher matcher;
            if ((matcher = template.matcher(getCursor())).find()) {
                return embed(
                  JavaTemplate.apply(
                    "java.nio.file.Files.exists(#{path:any(java.nio.file.Path)})",
                    getCursor(),
                    elem.getCoordinates().replace(),
                    matcher.parameter(0)
                  ),
                  getCursor(),
                  ctx,
                  SHORTEN_NAMES, STATIC_IMPORT_ALWAYS
                );
            }
            return super.visitMethodInvocation(elem, ctx);
        }
    }

    private static class AssertThatIsEqualToVisitor extends AbstractRefasterJavaVisitor {
        private final JavaTemplate before = JavaTemplate
          .builder("org.assertj.core.api.Assertions.assertThat(#{actual:any(int)}).isSameAs(#{expected:any(int)})")
          .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
          .build();
        private final JavaTemplate after = JavaTemplate
          .builder("org.assertj.core.api.Assertions.assertThat(#{actual:any(int)}).isEqualTo(#{expected:any(int)})")
          .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
          .build();

        @Override
        public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
            JavaTemplate.Matcher matcher;
            if ((matcher = before.matcher(getCursor())).find()) {
                return embed(
                  after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                  getCursor(),
                  ctx,
                  SHORTEN_NAMES, STATIC_IMPORT_ALWAYS
                );
            }
            return super.visitMethodInvocation(elem, ctx);
        }
    }
}
