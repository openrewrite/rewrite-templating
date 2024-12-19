package org.openrewrite.java.template.internal;

public class StringUtils {
    public static String indent(String text, int indent) {
        String whitespace = String.format("%" + indent + "s", " ");
        return whitespace + text.replaceAll("\\R", "\n" + whitespace);
    }
}
