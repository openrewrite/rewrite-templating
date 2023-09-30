package org.openrewrite.java.template.function;

public interface Expr4<R, P1, P2, P3, P4> {
    R accept(P1 p1, P2 p2, P3 p3, P4 p4) throws Exception;
}
