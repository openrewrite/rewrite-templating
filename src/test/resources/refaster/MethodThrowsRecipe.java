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
 * OpenRewrite recipe created for Refaster template {@code MethodThrows}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class MethodThrowsRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public MethodThrowsRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `MethodThrows`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\npublic class MethodThrows {\n    \n    @BeforeTemplate\n    void before(Path path) throws IOException {\n        Files.readAllLines(path, StandardCharsets.UTF_8);\n    }\n    \n    @AfterTemplate\n    void after(Path path) throws Exception {\n        Files.readAllLines(path);\n    }\n}\n```\n.";
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
                    before = JavaTemplate.builder("java.nio.file.Files.readAllLines(#{path:any(java.nio.file.Path)}, java.nio.charset.StandardCharsets.UTF_8);").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    maybeRemoveImport("java.nio.charset.StandardCharsets");
                    if (after == null) {
                        after = JavaTemplate.builder("java.nio.file.Files.readAllLines(#{path:any(java.nio.file.Path)});").build();
                    }
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
                        new UsesType<>("java.nio.charset.StandardCharsets", true),
                        new UsesType<>("java.nio.file.Files", true),
                        new UsesType<>("java.nio.file.Path", true),
                        new UsesMethod<>("java.nio.file.Files readAllLines(..)", true)
                ),
                javaVisitor
        );
    }
}
