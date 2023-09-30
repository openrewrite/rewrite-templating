package org.openrewrite.java.template.function;

public interface Stat3<P1, P2, P3> {
    void accept(P1 p1, P2 p2, P3 p3) throws Exception;
}
