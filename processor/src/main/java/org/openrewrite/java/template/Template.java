package org.openrewrite.java.template;

public interface Template {
    static <P> P removedField(Object select, String name) {
        //noinspection ConstantConditions
        return (P) null;
    }

    static <P> P removedConstant(Class<?> target, String name) {
        //noinspection ConstantConditions
        return (P) null;
    }
}
