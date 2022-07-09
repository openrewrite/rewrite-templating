package org.openrewrite.java.template.internal

import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.jupiter.api.Test

class TemplateProcessorTest {
    @Test
    fun processor() {
        Compiler.javac()
            .withProcessors(TemplateProcessor())
            .compile(
                JavaFileObjects.forSourceString(
                    "StringIsEmpty",
                    //language=java
                    """
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
    }
}
