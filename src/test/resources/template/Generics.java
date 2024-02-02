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
package template;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.template.Semantics;

public class Generics {
    JavaIsoVisitor visitor = new JavaIsoVisitor<ExecutionContext>() {
        final JavaTemplate before = Semantics.expression(this, "before", (java.util.List<java.lang.String> l) -> l.iterator().next()).build();
        final JavaTemplate after = Semantics.expression(this, "after", (java.util.List<java.lang.String> l) -> l.get(0)).build();
    };
}
