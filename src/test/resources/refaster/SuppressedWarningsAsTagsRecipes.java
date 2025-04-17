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
 * OpenRewrite recipes created for Refaster template {@code foo.SuppressedWarningsAsTags}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class SuppressedWarningsAsTagsRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public SuppressedWarningsAsTagsRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`SuppressedWarningsAsTags` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster template recipes for `foo.SuppressedWarningsAsTags`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new FirstRuleRecipe(),
                new SecondRuleRecipe(),
                new ThirdRuleRecipe(),
                new FourthRuleRecipe(),
                new FifthRuleRecipe(),
                new SixthRuleRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SuppressedWarningsAsTags.FirstRule}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class FirstRuleRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public FirstRuleRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `SuppressedWarningsAsTags.FirstRule`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\n@SuppressWarnings(value = \"java:S1234\")\npublic static class FirstRule {\n    \n    @BeforeTemplate()\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S1234");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String replaceAll(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SuppressedWarningsAsTags.SecondRule}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class SecondRuleRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public SecondRuleRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `SuppressedWarningsAsTags.SecondRule`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\n@SuppressWarnings(value = {\"java:S1234\"})\npublic static class SecondRule {\n    \n    @BeforeTemplate()\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S1234");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String replaceAll(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SuppressedWarningsAsTags.ThirdRule}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class ThirdRuleRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public ThirdRuleRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `SuppressedWarningsAsTags.ThirdRule`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\n@SuppressWarnings(value = \"java:S1234\")\npublic static class ThirdRule {\n    \n    @BeforeTemplate()\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S1234");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String replaceAll(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SuppressedWarningsAsTags.FourthRule}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class FourthRuleRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public FourthRuleRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `SuppressedWarningsAsTags.FourthRule`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\n@SuppressWarnings(value = {\"java:S1234\"})\npublic static class FourthRule {\n    \n    @BeforeTemplate()\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S1234");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String replaceAll(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SuppressedWarningsAsTags.FifthRule}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class FifthRuleRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public FifthRuleRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `SuppressedWarningsAsTags.FifthRule`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\n@SuppressWarnings(value = {\"java:S1234\", \"java:S5678\", \"unused\"})\npublic static class FifthRule {\n    \n    @BeforeTemplate()\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
        }

        @Override
        public Set<String> getTags() {
            return new HashSet<>(Arrays.asList("RSPEC-S1234", "RSPEC-S5678"));
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String replaceAll(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code SuppressedWarningsAsTags.SixthRule}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class SixthRuleRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public SixthRuleRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `SuppressedWarningsAsTags.SixthRule`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class SixthRule {\n    \n    @BeforeTemplate()\n    @SuppressWarnings(value = \"java:S1234\")\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
        }

        @Override
        public Set<String> getTags() {
            return Collections.singleton("RSPEC-S1234");
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                        );
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    new UsesMethod<>("java.lang.String replaceAll(..)", true),
                    javaVisitor
            );
        }
    }

}
