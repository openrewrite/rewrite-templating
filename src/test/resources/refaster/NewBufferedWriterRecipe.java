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
 * OpenRewrite recipe created for Refaster template {@code NewBufferedWriter}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class NewBufferedWriterRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public NewBufferedWriterRecipe() {}

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Refaster template `NewBufferedWriter`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Recipe created for the following Refaster template:\n```java\nclass NewBufferedWriter {\n    \n    @BeforeTemplate\n    BufferedWriter before(String f, Boolean b) throws IOException {\n        return new BufferedWriter(new java.io.FileWriter(f, b));\n    }\n    \n    @AfterTemplate\n    BufferedWriter after(String f, Boolean b) throws IOException {\n        return java.nio.file.Files.newBufferedWriter(new java.io.File(f).toPath(), b ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE);\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {
            JavaTemplate before;
            JavaTemplate after;

            @Override
            public J visitNewClass(J.NewClass elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if (before == null) {
                    before = JavaTemplate.builder("new java.io.BufferedWriter(new java.io.FileWriter(#{f:any(java.lang.String)}, #{b:any(java.lang.Boolean)}))")
                            .expressionType("java.io.BufferedWriter").build();
                }
                if ((matcher = before.matcher(getCursor())).find()) {
                    maybeRemoveImport("java.io.FileWriter");
                    if (after == null) {
                        after = JavaTemplate.builder("java.nio.file.Files.newBufferedWriter(new java.io.File(#{f:any(java.lang.String)}).toPath(), #{b:any(java.lang.Boolean)} ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE)")
                                .expressionType("java.io.BufferedWriter").build();
                    }
                    return embed(
                            after.apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1)),
                            getCursor(),
                            ctx,
                            SHORTEN_NAMES, SIMPLIFY_BOOLEANS
                    );
                }
                return super.visitNewClass(elem, ctx);
            }

        };
        return Preconditions.check(
                Preconditions.and(
                        new UsesType<>("java.io.BufferedWriter", true),
                        new UsesType<>("java.io.FileWriter", true),
                        new UsesMethod<>("java.io.BufferedWriter <init>(..)", true),
                        new UsesMethod<>("java.io.FileWriter <init>(..)", true)
                ),
                javaVisitor
        );
    }
}
