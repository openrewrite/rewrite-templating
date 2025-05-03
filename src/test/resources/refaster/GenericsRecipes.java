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
import org.openrewrite.marker.SearchResult;

import javax.annotation.Generated;
import java.util.*;

import static org.openrewrite.java.template.internal.AbstractRefasterJavaVisitor.EmbeddingOption.*;

/**
 * OpenRewrite recipes created for Refaster template {@code foo.Generics}.
 */
@SuppressWarnings("all")
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class GenericsRecipes extends Recipe {
    /**
     * Instantiates a new instance.
     */
    public GenericsRecipes() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "`Generics` Refaster recipes";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Refaster template recipes for `foo.Generics`.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
                new FirstElementRecipe(),
                new EmptyCollectionsRecipe(),
                new WilcardsRecipe()
        );
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Generics.FirstElement}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class FirstElementRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public FirstElementRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `Generics.FirstElement`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class FirstElement {\n    \n    @BeforeTemplate()\n    String before(List<String> l) {\n        return l.iterator().next();\n    }\n    \n    @AfterTemplate()\n    String after(List<String> l) {\n        return l.get(0);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = JavaTemplate
                        .builder("#{l:any(java.util.List<java.lang.String>)}.iterator().next()").build()
                        .matcher(getCursor())).find()) {
                        return embed(
                                JavaTemplate
                        .builder("#{l:any(java.util.List<java.lang.String>)}.get(0)").build()
                                .apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0)),
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
                        new UsesType<>("java.util.List", true),
                        new UsesMethod<>("java.util.Iterator next(..)", true),
                        new UsesMethod<>("java.util.List iterator(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Generics.EmptyCollections}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class EmptyCollectionsRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public EmptyCollectionsRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `Generics.EmptyCollections`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class EmptyCollections<K, T> {\n    \n    @BeforeTemplate()\n    List<T> emptyList() {\n        return Collections.emptyList();\n    }\n    \n    @BeforeTemplate()\n    Collection<T> emptyMap() {\n        return Collections.<K, T>emptyMap().values();\n    }\n    \n    @BeforeTemplate()\n    List<T> newList() {\n        return new ArrayList<>();\n    }\n    \n    @BeforeTemplate()\n    Map<K, T> newMap() {\n        return new HashMap<>();\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = JavaTemplate
                        .builder("java.util.Collections.emptyList()")
                        .genericTypes("K", "T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    if ((matcher = JavaTemplate
                        .builder("java.util.Collections.<K, T>emptyMap().values()")
                        .genericTypes("K", "T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

                @Override
                public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = JavaTemplate
                        .builder("new java.util.ArrayList<>()")
                        .genericTypes("K", "T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    if ((matcher = JavaTemplate
                        .builder("new java.util.HashMap<>()")
                        .genericTypes("K", "T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    return super.visitNewClass(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.or(
                        Preconditions.and(
                            new UsesType<>("java.util.ArrayList", true),
                            new UsesType<>("java.util.List", true),
                            new UsesMethod<>("java.util.ArrayList <constructor>(..)", true)
                        ),
                        Preconditions.and(
                            new UsesType<>("java.util.Collection", true),
                            new UsesType<>("java.util.Collections", true),
                            new UsesMethod<>("java.util.Collections emptyMap(..)", true),
                            new UsesMethod<>("java.util.Map values(..)", true)
                        ),
                        Preconditions.and(
                            new UsesType<>("java.util.Collections", true),
                            new UsesType<>("java.util.List", true),
                            new UsesMethod<>("java.util.Collections emptyList(..)", true)
                        ),
                        Preconditions.and(
                            new UsesType<>("java.util.HashMap", true),
                            new UsesType<>("java.util.Map", true),
                            new UsesMethod<>("java.util.HashMap <constructor>(..)", true)
                        )
                    ),
                    javaVisitor
            );
        }
    }

    /**
     * OpenRewrite recipe created for Refaster template {@code Generics.Wilcards}.
     */
    @SuppressWarnings("all")
    @NullMarked
    @Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
    public static class WilcardsRecipe extends Recipe {

        /**
         * Instantiates a new instance.
         */
        public WilcardsRecipe() {}

        @Override
        public String getDisplayName() {
            //language=markdown
            return "Refaster template `Generics.Wilcards`";
        }

        @Override
        public String getDescription() {
            //language=markdown
            return "Recipe created for the following Refaster template:\n```java\npublic static class Wilcards<T> {\n    \n    @BeforeTemplate()\n    Comparator<?> wilcard1(Comparator<?> cmp) {\n        return cmp.thenComparingInt(null);\n    }\n    \n    @BeforeTemplate()\n    Comparator<? extends Number> wilcard2(Comparator<? extends Number> cmp) {\n        return cmp.thenComparingInt(null);\n    }\n    \n    @BeforeTemplate()\n    Comparator<T> wilcard3(Comparator<T> cmp) {\n        return cmp.thenComparingInt(null);\n    }\n    \n    @BeforeTemplate()\n    Comparator<? extends T> wilcard4(Comparator<? extends T> cmp) {\n        return cmp.thenComparingInt(null);\n    }\n}\n```\n.";
        }

        @Override
        public TreeVisitor<?, ExecutionContext> getVisitor() {
            JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
                @Override
                public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                    JavaTemplate.Matcher matcher;
                    if ((matcher = JavaTemplate
                        .builder("#{cmp:any(java.util.Comparator<?>)}.thenComparingInt(null)")
                        .genericTypes("T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    if ((matcher = JavaTemplate
                        .builder("#{cmp:any(java.util.Comparator<? extends java.lang.Number>)}.thenComparingInt(null)")
                        .genericTypes("T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    if ((matcher = JavaTemplate
                        .builder("#{cmp:any(java.util.Comparator<T>)}.thenComparingInt(null)")
                        .genericTypes("T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    if ((matcher = JavaTemplate
                        .builder("#{cmp:any(java.util.Comparator<? extends T>)}.thenComparingInt(null)")
                        .genericTypes("T").build()
                        .matcher(getCursor())).find()) {
                        return SearchResult.found(elem);
                    }
                    return super.visitMethodInvocation(elem, ctx);
                }

            };
            return Preconditions.check(
                    Preconditions.and(
                        new UsesType<>("java.util.Comparator", true),
                        new UsesMethod<>("java.util.Comparator thenComparingInt(..)", true)
                    ),
                    javaVisitor
            );
        }
    }

}
