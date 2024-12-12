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
 * OpenRewrite recipes created for Refaster template {@code foo.PreConditionsVerifier}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class PreConditionsVerifierRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public PreConditionsVerifierRecipes() {}

    @Override
    public String getDisplayName() {
        return "A refaster template to test when a `UsesType`and Preconditions.or should or should not be applied to the Preconditions check";
    }

    @Override
    public String getDescription() {
        return "Refaster template recipes for `foo.PreConditionsVerifier`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe(),
                new NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe(),
                new NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe(),
                new UsesTypeMapWhenBeforeTemplateContainsMapRecipe(),
                new UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreConditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrStringRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreConditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveOrString {\n    \n    @BeforeTemplate()\n    void before(double actual, int ignore) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate()\n    void beforeTwo(String actual, String ignore) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("System.out.println(#{actual:any(double)});")
                        .build();
                final JavaTemplate beforeTwo = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.lang.String)});")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
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
                    if ((matcher = beforeTwo.matcher(getCursor())).find()) {
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
                    new UsesMethod<>("java.io.PrintStream println(..)", true),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreConditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherTypeRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreConditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsPrimitiveAndAnotherType {\n    \n    @BeforeTemplate()\n    void before(int actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate()\n    void before(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("System.out.println(#{actual:any(int)});")
                        .build();
                final JavaTemplate before0 = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.util.Map<?,?>)});")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
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
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
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
                            new UsesType<>("java.util.Map", true),
                            new UsesMethod<>("java.io.PrintStream println(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreConditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherTypeRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreConditionsVerifier.NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class NoUsesTypeWhenBeforeTemplateContainsStringAndAnotherType {\n    \n    @BeforeTemplate()\n    void before(String actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate()\n    void before(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.lang.String)});")
                        .build();
                final JavaTemplate before0 = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.util.Map<?,?>)});")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
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
                    if ((matcher = before0.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
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
                            new UsesType<>("java.util.Map", true),
                            new UsesMethod<>("java.io.PrintStream println(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreConditionsVerifier.UsesTypeMapWhenBeforeTemplateContainsMap}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsesTypeMapWhenBeforeTemplateContainsMapRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeMapWhenBeforeTemplateContainsMapRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreConditionsVerifier.UsesTypeMapWhenBeforeTemplateContainsMap`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeMapWhenBeforeTemplateContainsMap {\n    \n    @BeforeTemplate()\n    void withGeneric(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate()\n    void withGenericTwo(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate()\n    void withoutGeneric(Map actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate withGeneric = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.util.Map<?,?>)});")
                        .build();
                final JavaTemplate withGenericTwo = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.util.Map<?,?>)});")
                        .build();
                final JavaTemplate withoutGeneric = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.util.Map)});")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = withGenericTwo.matcher(getCursor())).find()) {
                        return embed(
                                withoutGeneric.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = withGeneric.matcher(getCursor())).find()) {
                        return embed(
                                withoutGeneric.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
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
                            new UsesType<>("java.util.Map", true),
                            new UsesMethod<>("java.io.PrintStream println(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code PreConditionsVerifier.UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public UsesTypeMapOrListWhenBeforeTemplateContainsMapAndListRecipe() {}

        @Override
        public String getDisplayName() {
            return "Refaster template `PreConditionsVerifier.UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList`";
        }

        @Override
        public String getDescription() {
            return "Recipe created for the following Refaster template:\n```java\npublic static class UsesTypeMapOrListWhenBeforeTemplateContainsMapAndList {\n    \n    @BeforeTemplate()\n    void before(List<?> actual) {\n        System.out.println(actual);\n    }\n    \n    @BeforeTemplate()\n    void beforeTwo(Map<?, ?> actual) {\n        System.out.println(actual);\n    }\n    \n    @AfterTemplate()\n    void after(Object actual) {\n        System.out.println(\"Changed: \" + actual);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                final JavaTemplate before = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.util.List<?>)});")
                        .build();
                final JavaTemplate beforeTwo = JavaTemplate
                        .builder("System.out.println(#{actual:any(java.util.Map<?,?>)});")
                        .build();
                final JavaTemplate after = JavaTemplate
                        .builder("System.out.println(\"Changed: \" + #{actual:any(java.lang.Object)});")
                        .build();

                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = before.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.List");
                        return embed(
                                after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
                                getCursor(),
                                ctx,
                                SHORTEN_NAMES
                        );
                    }
                    if ((matcher = beforeTwo.matcher(getCursor())).find()) {
                        maybeRemoveImport("java.util.Map");
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
                            new UsesMethod<>("java.io.PrintStream println(..)", true),
                            Preconditions.or(
                                    new UsesType<>("java.util.List", true),
                                    new UsesType<>("java.util.Map", true)
                            )
                    ),
                    javaVisitor
            );
        }
    }

}
