/*
 * Copyright 2023 the original author or authors.
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

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.template.Semantics;

public class ParameterReuseRecipe {
    JavaIsoVisitor visitor = new JavaIsoVisitor<ExecutionContext>() {
        JavaTemplate.Builder before = Semantics.expression(this, "before", (String s) -> s.equals(s));
        JavaTemplate.Builder after = Semantics.expression(this, "after", (String s) -> true);
    };
}
