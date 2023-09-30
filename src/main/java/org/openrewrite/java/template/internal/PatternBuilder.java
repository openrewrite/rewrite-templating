package org.openrewrite.java.template.internal;

import lombok.Value;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Value
@SuppressWarnings("unused")
public class PatternBuilder {
    String name;

    public JavaTemplate.Builder build(JavaVisitor<?> owner) {
        try {
            Class<?> templateClass = Class.forName(owner.getClass().getName() + "_" + name, true,
                    owner.getClass().getClassLoader());
            Method getTemplate = templateClass.getDeclaredMethod("getTemplate", JavaVisitor.class);
            return (JavaTemplate.Builder) getTemplate.invoke(null, owner);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
