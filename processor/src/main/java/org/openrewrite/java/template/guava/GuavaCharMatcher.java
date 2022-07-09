package org.openrewrite.java.template.guava;

import com.google.common.base.CharMatcher;
import org.openrewrite.java.template.After;
import org.openrewrite.java.template.Before;
import org.openrewrite.java.template.TemplateRecipe;

import static org.openrewrite.java.template.Template.removedConstant;

class GuavaCharMatcher extends TemplateRecipe {
    @Override
    public String getDisplayName() {
        return "Fix Guava `CharMatcher` deprecations.";
    }

    static class JavaLetterOrDigit {
        @Before
        CharMatcher before() {
            // a constant that USED to exist, but has since been removed and would
            // no longer compile
            return removedConstant(CharMatcher.class, "JAVA_LETTER_OR_DIGIT");
        }

        @Before
        CharMatcher staticMethod() {
            return CharMatcher.javaLetterOrDigit();
        }

        @After
        CharMatcher forPredicate() {
            return CharMatcher.forPredicate(Character::isLetterOrDigit);
        }
    }

    static class Whitespace {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "WHITESPACE");
        }

        @After
        CharMatcher after() {
            return CharMatcher.whitespace();
        }
    }

    static class BreakingWhitespace {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "BREAKING_WHITESPACE");
        }

        @After
        CharMatcher after() {
            return CharMatcher.breakingWhitespace();
        }
    }

    static class Ascii {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "ASCII");
        }

        @After
        CharMatcher after() {
            return CharMatcher.ascii();
        }
    }

    static class Digit {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "DIGIT");
        }

        @After
        CharMatcher after() {
            return CharMatcher.digit();
        }
    }

    static class JavaDigit {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "JAVA_DIGIT");
        }

        @After
        CharMatcher after() {
            return CharMatcher.javaDigit();
        }
    }

    static class JavaUpperCase {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "JAVA_UPPER_CASE");
        }

        @After
        CharMatcher after() {
            return CharMatcher.javaUpperCase();
        }
    }

    static class JavaLowerCase {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "JAVA_LOWER_CASE");
        }

        @After
        CharMatcher after() {
            return CharMatcher.javaLowerCase();
        }
    }

    static class JavaIsoControl {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "JAVA_ISO_CONTROL");
        }

        @After
        CharMatcher after() {
            return CharMatcher.javaIsoControl();
        }
    }

    static class Invisible {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "INVISIBLE");
        }

        @After
        CharMatcher after() {
            return CharMatcher.invisible();
        }
    }

    static class SingleWidth {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "SINGLE_WIDTH");
        }

        @After
        CharMatcher after() {
            return CharMatcher.singleWidth();
        }
    }

    static class Any {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "ANY");
        }

        @After
        CharMatcher after() {
            return CharMatcher.any();
        }
    }

    static class None {
        @Before
        CharMatcher before() {
            return removedConstant(CharMatcher.class, "NONE");
        }

        @After
        CharMatcher after() {
            return CharMatcher.none();
        }
    }
}
