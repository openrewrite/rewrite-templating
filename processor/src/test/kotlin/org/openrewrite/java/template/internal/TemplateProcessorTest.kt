package org.openrewrite.java.template.internal

import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openrewrite.internal.StringUtils

class TemplateProcessorTest {

    @Test
    fun processor() {
        val gen = Compiler.javac()
            .withProcessors(TemplateProcessor())
            .withOptions("-verbose")
            .compile(
                JavaFileObjects.forSourceString(
                    "StringIsEmpty",
                    //language=java
                    """
                        package org.openrewrite.sample;
                        import org.openrewrite.java.template.*;
                            
                        @EnableTemplating
                        class StringIsEmpty2 {
                            Template t = new Template() {
                                @Before
                                boolean equalsEmptyString(String string) {
                                    return string.equals("");
                                }
                    
                                @Before
                                boolean lengthEquals0(String string) {
                                    return string.length() == 0;
                                }
                    
                                @After
                                boolean optimizedMethod(String string) {
                                    return string.isEmpty();
                                }
                            };
                        }
                    """
                )
            )
            .generatedSourceFile("org.openrewrite.sample.GeneratedStringIsEmpty2_1")
            .orElseThrow()
            .openInputStream()
            .bufferedReader()
            .readText()

        assertThat(gen).isEqualTo("")
    }
}
