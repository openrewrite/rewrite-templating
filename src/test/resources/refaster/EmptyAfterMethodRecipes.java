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
 * OpenRewrite recipes created for Refaster template {@code foo.EmptyAfterMethod}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class EmptyAfterMethodRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public EmptyAfterMethodRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`EmptyAfterMethod` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster template recipes for `foo.EmptyAfterMethod`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new StringLengthZeroRecipe(),
                new MethodInvocationRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code EmptyAfterMethod.StringLengthZero}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class StringLengthZeroRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public StringLengthZeroRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `EmptyAfterMethod.StringLengthZero`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\nclass StringLengthZero {\n    \n    @BeforeTemplate\n    boolean before(String s) {\n        return s.length() == 0;\n    }\n    \n    @AfterTemplate\n    void after(String s) {\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;

                @Override
                public J visitBinary(J.Binary elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("#{s:any(java.lang.String)}.length() == 0").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return null;
                    }
                    return super.visitBinary(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String length(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code EmptyAfterMethod.MethodInvocation}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class MethodInvocationRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public MethodInvocationRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `EmptyAfterMethod.MethodInvocation`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\nclass MethodInvocation {\n    \n    @BeforeTemplate\n    void before(String s) {\n        System.out.println(s);\n    }\n    \n    @AfterTemplate\n    void after(String s) {\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                JavaTemplate before;

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if (before == null) {
                        before = JavaTemplate.builder("System.out.println(#{s:any(java.lang.String)});").build();
                    }
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return null;
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.io.PrintStream println(..)", true),
                    javaVisitor
            );
        }
    }

}
