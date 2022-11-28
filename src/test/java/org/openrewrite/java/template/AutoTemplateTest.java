package org.openrewrite.java.template;

import org.intellij.lang.annotations.Language;
import org.joor.CompileOptions;
import org.joor.Reflect;
import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.template.internal.TemplateProcessor;
import org.openrewrite.test.RewriteTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class AutoTemplateTest implements RewriteTest {

    @Language("java")
    private static final String template = "" +
                                           "package org.openrewrite.java.template;\n" +
                                           "\n" +
                                           "import org.openrewrite.ExecutionContext;\n" +
                                           "import org.openrewrite.*;\n" +
                                           "import org.openrewrite.java.*;\n" +
                                           "import org.openrewrite.java.tree.*;\n" +
                                           "import org.openrewrite.java.template.*;\n" +
                                           "import java.util.Comparator;\n" +
                                           "\n" +
                                           "class Test extends JavaVisitor<ExecutionContext> {\n" +
                                           "    @Override\n" +
                                           "    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {\n" +
                                           "        if (method.getBody() != null && method.getBody().getStatements().isEmpty()) {\n" +
                                           "            return method.withTemplate(\n" +
                                           "                    auto(),\n" +
                                           "                    method.getBody().getCoordinates().addStatement(Comparator.comparing(J::getId)),\n" +
                                           "                    ((J.VariableDeclarations) method.getParameters().get(0)).getVariables().get(0).getName()\n" +
                                           "            );\n" +
                                           "        }\n" +
                                           "        return method;\n" +
                                           "    }\n" +
                                           "\n" +
                                           "    public JavaTemplate auto() {\n" +
                                           "        return AutoTemplate\n" +
                                           "            .compile(\"print\", (Throwable t) -> t.printStackTrace())\n" +
                                           "            .build(this);\n" +
                                           "    }" +
                                           "}";

    @Test
    void annotationProcessor() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        TemplateProcessor tp = new TemplateProcessor(template);

        Object test = Reflect.compile(
          "org.openrewrite.java.template.Test",
          template,
          new CompileOptions().processors(tp)
        ).create().get();

        Constructor<?> noArgVisitorCtor = test.getClass().getDeclaredConstructor();

        //noinspection unchecked
        JavaVisitor<ExecutionContext> visitor = (JavaVisitor<ExecutionContext>) noArgVisitorCtor.newInstance();

        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> visitor)),
          //language=java
          java(
            "class ClassWithThrowableMethodArg {\n" +
            "    void test(Throwable t) {\n" +
            "    }\n" +
            "}\n",
            "class ClassWithThrowableMethodArg {\n" +
            "    void test(Throwable t) {\n" +
            "        t.printStackTrace();\n" +
            "    }\n" +
            "}\n"
          )
        );
    }
}
