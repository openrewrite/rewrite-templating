/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.template;

import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.PatternBuilder;

@SuppressWarnings("unused")
public class Semantics {
    private Semantics() {
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr0<?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr1<?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr2<?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr3<?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr4<?, ?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr5<?, ?, ?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr6<?, ?, ?, ?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr7<?, ?, ?, ?, ?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr8<?, ?, ?, ?, ?, ?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr9<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder expression(JavaVisitor<?> owner, String name, Expr10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> f) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat0 p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat1<?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat2<?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat3<?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat4<?, ?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat5<?, ?, ?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat6<?, ?, ?, ?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat7<?, ?, ?, ?, ?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat8<?, ?, ?, ?, ?, ?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat9<?, ?, ?, ?, ?, ?, ?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }

    public static JavaTemplate.Builder statement(JavaVisitor<?> owner, String name, Stat10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> p) {
        return new PatternBuilder(name).build(owner);
    }
}
