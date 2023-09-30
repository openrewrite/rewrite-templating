package org.openrewrite.java.template.function;

public interface Stat2<P1, P2> {
    void accept(P1 p1, P2 p2) throws Exception;
}
