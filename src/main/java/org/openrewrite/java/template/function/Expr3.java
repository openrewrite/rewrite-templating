package org.openrewrite.java.template.function;

public interface Expr3<R, P1, P2, P3> {
    R accept(P1 p1, P2 p2, P3 p3) throws Exception;
}
