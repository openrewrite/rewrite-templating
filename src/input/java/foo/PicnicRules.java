/*
 * Copyright 2024 the original author or authors.
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
package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Picnic rules for foo, showing how JavaDoc is converted to Markdown.
 */
@OnlineDocumentation
public class PicnicRules {
    /**
     * A single line used as description.
     */
    public static class FirstRule {
        @BeforeTemplate
        String before(String s, String s1, String s2) {
            return s.replaceAll(s1, s2);
        }

        @AfterTemplate
        String after(String s, String s1, String s2) {
            return s != null ? s.replaceAll(s1, s2) : s;
        }
    }

    /**
     * A continuation line,
     * used as a description.
     */
    public static class SecondRule {
        @BeforeTemplate
        String before(String s, String s1, String s2) {
            return s.replaceAll(s1, s2);
        }

        @AfterTemplate
        String after(String s, String s1, String s2) {
            return s != null ? s.replaceAll(s1, s2) : s;
        }
    }

    /**
     * A first line as displayName.
     *
     * A second line as description.
     */
    public static class ThirdRule {
        @BeforeTemplate
        String before(String s, String s1, String s2) {
            return s.replaceAll(s1, s2);
        }

        @AfterTemplate
        String after(String s, String s1, String s2) {
            return s != null ? s.replaceAll(s1, s2) : s;
        }
    }

    /**
     * A continuation line,
     * used as a description.
     *
     * A second line
     * as description.
     */
    public static class FourthRule {
        @BeforeTemplate
        String before(String s, String s1, String s2) {
            return s.replaceAll(s1, s2);
        }

        @AfterTemplate
        String after(String s, String s1, String s2) {
            return s != null ? s.replaceAll(s1, s2) : s;
        }
    }

    // No JavaDoc
    public static class FifthRule {
        @BeforeTemplate
        String before(String s, String s1, String s2) {
            return s.replaceAll(s1, s2);
        }

        @AfterTemplate
        String after(String s, String s1, String s2) {
            return s != null ? s.replaceAll(s1, s2) : s;
        }
    }
}
