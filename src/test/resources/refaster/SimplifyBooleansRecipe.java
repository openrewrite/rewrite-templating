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
 * OpenRewrite recipe created for Refaster template {@code SimplifyBooleans}.
 */
@SuppressWarnings("all")
@NullMarked
@Generated("org.openrewrite.java.template.processor.RefasterTemplateProcessor")
public class SimplifyBooleansRecipe extends Recipe {

    /**
     * Instantiates a new instance.
     */
    public SimplifyBooleansRecipe() {}

    @Override
    public String getDisplayName() {
        return "Refaster template `SimplifyBooleans`";
    }

    @Override
    public String getDescription() {
        return "Recipe created for the following Refaster template:\n```java\npublic class SimplifyBooleans {\n    \n    @BeforeTemplate()\n    String before(String s, String s1, String s2) {\n        return s.replaceAll(s1, s2);\n    }\n    \n    @AfterTemplate()\n    String after(String s, String s1, String s2) {\n        return s != null ? s.replaceAll(s1, s2) : s;\n    }\n}\n```\n.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        JavaVisitor<ExecutionContext> javaVisitor = new AbstractRefasterJavaVisitor() {

            @Override
            public J visitMethodInvocation(J.MethodInvocation elem, ExecutionContext ctx) {
                JavaTemplate.Matcher matcher;
                if ((matcher = JavaTemplate
                        .builder("#{s:any(java.lang.String)}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)})")
                        .build().matcher(getCursor())).find()) {
                    return embed(
                            JavaTemplate
                                    .builder("#{s:any(java.lang.String)} != null ? #{s}.replaceAll(#{s1:any(java.lang.String)}, #{s2:any(java.lang.String)}) : #{s}")
                                    .build().apply(getCursor(), elem.getCoordinates().replace(), matcher.parameter(0), matcher.parameter(1), matcher.parameter(2)),
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
