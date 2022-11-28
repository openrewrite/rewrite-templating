package org.openrewrite.java.template;

import lombok.Value;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class AutoTemplate {

    public static Builder compile(String name, F1<?> f) {
        return new Builder(name);
    }

    public static Builder compile(String name, F2<?, ?> f) {
        return new Builder(name);
    }

    public static Builder compile(String name, F3<?, ?, ?> f) {
        return new Builder(name);
    }

    public static Builder compile(String name, F4<?, ?, ?, ?> f) {
        return new Builder(name);
    }

    public static Builder compile(String name, F5<?, ?, ?, ?, ?> f) {
        return new Builder(name);
    }

    public static Builder compile(String name, F6<?, ?, ?, ?, ?, ?> f) {
        return new Builder(name);
    }

    @Value
    @SuppressWarnings("unused")
    public static class Builder {
        String name;

        public Builder isolateClasspath(String... isolatedClasspathEntries) {
            // TODO argument is used at annotation-processing time while generating the template
            return this;
        }

        public JavaTemplate build(JavaVisitor<?> owner) {
            try {
                Class<?> templateClass = Class.forName(owner.getClass().getName() + "_" + name);
                Method getTemplate = templateClass.getDeclaredMethod("getTemplate", JavaVisitor.class);
                return (JavaTemplate) getTemplate.invoke(null, owner);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface F1<P1> {
        void accept(P1 p) throws Exception;
    }

    public interface F2<P1, P2> {
        void accept(P1 p, P2 p2) throws Exception;
    }

    public interface F3<P1, P2, P3> {
        void accept(P1 p, P2 p2, P3 p3) throws Exception;
    }

    public interface F4<P1, P2, P3, P4> {
        void accept(P1 p, P2 p2, P3 p3, P4 p4) throws Exception;
    }

    public interface F5<P1, P2, P3, P4, P5> {
        void accept(P1 p, P2 p2, P3 p3, P4 p4, P5 p5) throws Exception;
    }

    public interface F6<P1, P2, P3, P4, P5, P6> {
        void accept(P1 p, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) throws Exception;
    }
}
