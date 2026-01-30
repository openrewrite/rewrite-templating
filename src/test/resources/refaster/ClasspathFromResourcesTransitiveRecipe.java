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
 * OpenRewrite recipe created for Refaster template {@code ClasspathFromResourcesTransitive}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class ClasspathFromResourcesTransitiveRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public ClasspathFromResourcesTransitiveRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `ClasspathFromResourcesTransitive`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class ClasspathFromResourcesTransitive {\n    \n    @BeforeTemplate\n    String before(JavaVisitor visitor) {\n        return visitor.getLanguage();\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("#{visitor:any(org.openrewrite.java.JavaVisitor)}.getLanguage()")
                            .bindType("java.lang.String")
                            .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "rewrite-core-8", "rewrite-java-8"))
                            .build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    return SearchResult.found(elem);
                }
                return super.visitMethodInvocation(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("org.openrewrite.java.JavaVisitor", true),
                        new UsesMethod<>("org.openrewrite.java.JavaVisitor getLanguage(..)", true),
                        Preconditions.not(new UsesType<>("com.google.errorprone.refaster.annotation.BeforeTemplate", true))
                ),
                javaVisitor
        );
    }
}
