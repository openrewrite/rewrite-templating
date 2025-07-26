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
 * OpenRewrite recipes created for Refaster template {@code foo.Escapes}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class EscapesRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public EscapesRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`Escapes` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster template recipes for `foo.Escapes`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new ConstantsFormatRecipe(),
                new SplitRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Escapes.ConstantsFormat}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class ConstantsFormatRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public ConstantsFormatRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `Escapes.ConstantsFormat`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class ConstantsFormat {\n    \n    @BeforeTemplate\n    String before(String value) {\n        return String.format(\"\\\"%s\\\"\", Strings.nullToEmpty(value));\n    }\n    \n    @AfterTemplate\n    String after(String value) {\n        return Strings.lenientFormat(value);\n    }\n}\n```\n.";
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
                        before = JavaTemplate.builder("String.format(\"\\\"%s\\\"\", com.google.common.base.Strings.nullToEmpty(#{value:any(java.lang.String)}))")
                                .bindType("java.lang.String")
                                .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath())).build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("com.google.common.base.Strings.lenientFormat(#{value:any(java.lang.String)})")
                                    .bindType("java.lang.String")
                                    .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath())).build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                EmbeddingOption.SHORTEN_NAMES
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("com.google.common.base.Strings", true),
                            new UsesMethod<>("com.google.common.base.Strings nullToEmpty(..)", true),
                            new UsesMethod<>("java.lang.String format(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Escapes.Split}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class SplitRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public SplitRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `Escapes.Split`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class Split {\n    \n    @BeforeTemplate\n    String[] before(String s) {\n        return s.split(\"[^\\\\S]+\");\n    }\n    \n    @AfterTemplate\n    String[] after(String s) {\n        return s.split(\"\\\\s+\");\n    }\n}\n```\n.";
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
                        before = JavaTemplate.builder("#{s:any(java.lang.String)}.split(\"[^\\\\S]+\")")
                                .bindType("java.lang.String[]").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        if (after == null) {
                            after = JavaTemplate.builder("#{s:any(java.lang.String)}.split(\"\\\\s+\")")
                                    .bindType("java.lang.String[]").build();
                        }
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                EmbeddingOption.SHORTEN_NAMES
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String split(..)", true),
                    javaVisitor
            );
        }
    }

}
