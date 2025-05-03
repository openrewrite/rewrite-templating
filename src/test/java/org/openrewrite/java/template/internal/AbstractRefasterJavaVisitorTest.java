package org.openrewrite.java.template.internal;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;
import static org.openrewrite.test.RewriteTest.toRecipe;

class AbstractRefasterJavaVisitorTest implements RewriteTest {
    @Test
    void useStaticImports() {
        rewriteRun(
          spec -> spec.recipe(
            toRecipe(
              () -> new AbstractRefasterJavaVisitor() {
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
            )
          ),
          java(
            "import java.nio.file.Path;\n" +
              "\n" +
              "class A {\n" +
              "    boolean pathExists(Path path) {\n" +
              "        return path.toFile().exists();\n" +
              "    }\n" +
              "}",
            "import java.nio.file.Path;\n" +
              "\n" +
              "import static java.nio.file.Files.exists;\n" +
              "\n" +
              "class A {\n" +
              "    boolean pathExists(Path path) {\n" +
              "        return exists(path);\n" +
              "    }\n" +
              "}"
          )
        );
    }
}
