/*
 * Copyright 2025 the original author or authors.
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
package foo;

import org.jspecify.annotations.NullMarked;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import javax.annotation.Generated;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipe created for Refaster template {@code ComplexGenerics}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class ComplexGenericsRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public ComplexGenericsRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `ComplexGenerics`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\nclass ComplexGenerics<S extends Serializable & Comparable<? super S>, T extends S, U extends T> {\n    \n    @BeforeTemplate\n    boolean before(Stream<S> stream, List<U> list, Collector<S, ?, ? extends List<T>> collector) {\n        return stream.collect(collector).containsAll(list);\n    }\n    \n    @AfterTemplate\n    boolean after(Stream<S> stream, List<U> list, Collector<S, ?, ? extends Iterable<T>> collector) {\n        return stream.collect(collector).equals(list);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            JavaTemplate after;

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("#{stream:any(java.util.stream.Stream<S>)}.collect(#{collector:any(java.util.stream.Collector<S, ?, ? extends java.util.List<T>>)}).containsAll(#{list:any(java.util.List<U>)})")
                            .genericTypes("S extends java.io.Serializable & java.lang.Comparable<? super S>", "T extends S", "U extends T").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    if (after == null) {
                        after = JavaTemplate.builder("#{stream:any(java.util.stream.Stream<S>)}.collect(#{collector:any(java.util.stream.Collector<S, ?, ? extends java.lang.Iterable<T>>)}).equals(#{list:any(java.util.List<U>)})")
                                .genericTypes("S extends java.io.Serializable & java.lang.Comparable<? super S>", "T extends S", "U extends T").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                            getCursor(),
                            ctx,
                            EmbeddingOption.SHORTEN_NAMES, EmbeddingOption.SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.util.List", true),
                        new UsesType<>("java.util.stream.Collector", true),
                        new UsesType<>("java.util.stream.Stream", true),
                        new UsesMethod<>("java.util.List containsAll(..)", true),
                        new UsesMethod<>("java.util.stream.Stream collect(..)", true)
                ),
                javaVisitor
        );
    }
}
