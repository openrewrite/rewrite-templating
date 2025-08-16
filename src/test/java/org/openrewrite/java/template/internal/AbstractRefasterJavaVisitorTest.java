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
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.test.RewriteTest;

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
}
