package org.openrewrite.java.template.internal

import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TemplateProcessorTest {

    @Test
    fun processor() {
        val gen = Compiler.javac()
                .withProcessors(TemplateProcessor())
                .withOptions("-verbose")
                .compile(
                        JavaFileObjects.forSourceString(
                                "org.openrewrite.sample.Test",
                                //language=java
                                """
                                    package org.openrewrite.sample;
                                    
                                    import org.openrewrite.ExecutionContext;
                                    import org.openrewrite.java.*;
                                    import org.openrewrite.java.template.*;
                                    
                                    @EnableTemplating
                                    class Test extends JavaVisitor<ExecutionContext> {
                                        String s = "hello".trim();
                                    
                                        JavaTemplate auto = AutoTemplate.forPattern(this, "log")
                                                .build((Throwable t) -> t.printStackTrace());
                                    }
                                """
                        )
                )
                .generatedSourceFile("org.openrewrite.sample.Test_log")
                .orElseThrow()
                .openInputStream()
                .bufferedReader()
                .readText()

        assertThat(gen).isEqualTo("")
    }
}
