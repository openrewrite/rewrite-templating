/*
 * Copyright 2024 the original author or authors.
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
        return "`Escapes` Refaster recipes";
    }

    @Override
    public String getDescription() {
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
            return "Refaster template `Escapes.ConstantsFormat`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class ConstantsFormat {\n    \n    @BeforeTemplate()\n    String before(String value) {\n        return String.format(\"\\\"%s\\\"\", Convert.quote(value));\n    }\n    \n    @AfterTemplate()\n    String after(String value) {\n        return Constants.format(value);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("String.format(\"\\\"%s\\\"\", com.sun.tools.javac.util.Convert.quote(#{value:any(java.lang.String)}))")
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("com.sun.tools.javac.util.Constants.format(#{value:any(java.lang.String)})")
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("com.sun.tools.javac.util.Convert");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                            new UsesType<>("com.sun.tools.javac.util.Convert", true),
                            new UsesMethod<>("com.sun.tools.javac.util.Convert quote(..)", true),
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
            return "Refaster template `Escapes.Split`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class Split {\n    \n    @BeforeTemplate()\n    String[] before(String s) {\n        return s.split(\"[^\\\\S]+\");\n    }\n    \n    @AfterTemplate()\n    String[] after(String s) {\n        return s.split(\"\\\\s+\");\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.split(\"[^\\\\S]+\")")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.split(\"\\\\s+\")")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
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
