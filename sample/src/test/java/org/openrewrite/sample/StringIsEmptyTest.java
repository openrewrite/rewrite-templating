package org.openrewrite.sample;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@SuppressWarnings("ALL")
public class StringIsEmptyTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new StringIsEmpty());
    }

    void lengthEquals0() {
//        rewriteRun(
//            java(
//                //language=java
//                """
//                    class Test {{
//                        "test".length() == 0;
//                    }}
//                """,
//                //language=java
//                """
//                    class Test {{
//                        "test".isEmpty();
//                    }}
//                """
//            )
//        );
    }
}
