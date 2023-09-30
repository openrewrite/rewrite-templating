package org.openrewrite.java.template.function;

public interface Expr2<R, P1, P2> {
    R accept(P1 p1, P2 p2) throws Exception;
}
