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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.Matches;
import org.openrewrite.java.template.MethodInvocationMatcher;
import org.openrewrite.java.template.NotMatches;

public class AnnotatedUnusedArgument {
    @BeforeTemplate
    public int before1(int a, @Matches(MethodInvocationMatcher.class) int b) {
        return a;
    }

    @BeforeTemplate
    public int before2(int a, @NotMatches(MethodInvocationMatcher.class) int c) {
        return a;
    }

    @AfterTemplate
    public int after(int a) {
        return a;
    }
}
