/*
 * Copyright 2026 the original author or authors.
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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.*;
import org.openrewrite.java.template.Primitive;
import org.openrewrite.java.template.function.*;
import org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor;
import org.openrewrite.java.tree.*;

import javax.annotation.Generated;
import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipes created for Refaster template {@code foo.NoGuavaRefaster}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class NoGuavaRefasterRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public NoGuavaRefasterRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster style Guava to Java migration recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipes that migrate from Guava to Java, using Refaster style templates for cases beyond what declarative recipes can cover.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new PreconditionsCheckNotNullToObjectsRequireNonNullRecipe(),
                new PreconditionsCheckNotNullWithMessageToObjectsRequireNonNullRecipe(),
                new PreconditionsCheckNotNullWithMessageToObjectsRequireNonNullMessageTypeObjectRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code NoGuavaRefaster.PreconditionsCheckNotNullToObjectsRequireNonNull}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class PreconditionsCheckNotNullToObjectsRequireNonNullRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public PreconditionsCheckNotNullToObjectsRequireNonNullRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "`Preconditions.checkNotNull` to `Objects.requireNonNull`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Migrate from Guava `Preconditions.checkNotNull` to Java 8 `java.util.Objects.requireNonNull`.";
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
                        before = JavaTemplate.builder("com.google.common.base.Preconditions.checkNotNull(#{object:any(java.lang.Object)})")
                                .bindType("java.lang.Object")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Preconditions");
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.Objects.requireNonNull(#{object:any(java.lang.Object)})")
                                    .bindType("java.lang.Object").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, STATIC_IMPORT_ALWAYS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("com.google.common.base.Preconditions", true),
                            new UsesMethod<>("com.google.common.base.Preconditions checkNotNull(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true)),
                            Preconditions.not(new UsesType<>("org.openrewrite.java.template.Semantics", true))
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code NoGuavaRefaster.PreconditionsCheckNotNullWithMessageToObjectsRequireNonNull}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class PreconditionsCheckNotNullWithMessageToObjectsRequireNonNullRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public PreconditionsCheckNotNullWithMessageToObjectsRequireNonNullRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "`Preconditions.checkNotNull` with `String` message to `Objects.requireNonNull`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Migrate from Guava `Preconditions.checkNotNull` to Java 8 `java.util.Objects.requireNonNull`.";
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
                        before = JavaTemplate.builder("com.google.common.base.Preconditions.checkNotNull(#{object:any(java.lang.Object)}, #{message:any(java.lang.String)})")
                                .bindType("java.lang.Object")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Preconditions");
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.Objects.requireNonNull(#{object:any(java.lang.Object)}, #{message:any(java.lang.String)})")
                                    .bindType("java.lang.Object").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, STATIC_IMPORT_ALWAYS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("com.google.common.base.Preconditions", true),
                            new UsesMethod<>("com.google.common.base.Preconditions checkNotNull(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true)),
                            Preconditions.not(new UsesType<>("org.openrewrite.java.template.Semantics", true))
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code NoGuavaRefaster.PreconditionsCheckNotNullWithMessageToObjectsRequireNonNullMessageTypeObject}.
     */
    @SuppressWarnings("all")
    @NullMarked
    public static class PreconditionsCheckNotNullWithMessageToObjectsRequireNonNullMessageTypeObjectRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public PreconditionsCheckNotNullWithMessageToObjectsRequireNonNullMessageTypeObjectRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "`Preconditions.checkNotNull` with `Object` message to `Objects.requireNonNull` with `String.valueOf`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Migrate from Guava `Preconditions.checkNotNull` to Java 8 `java.util.Objects.requireNonNull`.";
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
                        before = JavaTemplate.builder("com.google.common.base.Preconditions.checkNotNull(#{object:any(java.lang.Object)}, #{message:any(java.lang.Object)})")
                                .bindType("java.lang.Object")
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "guava-33"))
                                .build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.google.common.base.Preconditions");
                        if (after == null) {
                            after = JavaTemplate.builder("java.util.Objects.requireNonNull(#{object:any(java.lang.Object)}, String.valueOf(#{message:any(java.lang.Object)}))")
                                    .bindType("java.lang.Object").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, STATIC_IMPORT_ALWAYS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("com.google.common.base.Preconditions", true),
                            new UsesMethod<>("com.google.common.base.Preconditions checkNotNull(..)", true),
                            Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true)),
                            Preconditions.not(new UsesType<>("org.openrewrite.java.template.Semantics", true))
                    ),
                    javaVisitor
            );
        }
    }

}
