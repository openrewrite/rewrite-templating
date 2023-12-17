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
import org.openrewrite.java.*;

/**
 * OpenRewrite `after` template created for  `foo.ShouldAddClasspathRecipes$PrimitiveRecipe$1`.
 */
public class ShouldAddClasspathRecipes$PrimitiveRecipe$1_after {
    /**
     * Instantiates a new {@link ShouldAddClasspathRecipes$PrimitiveRecipe$1_after} instance.
     * @return the new instance
     */
    public ShouldAddClasspathRecipes$PrimitiveRecipe$1_after() {}

    /**
     * @return `JavaTemplate` to match or replace.
     */
    public static JavaTemplate.Builder getTemplate() {
        return JavaTemplate
                .builder("System.out.print(#{i:any(int)})");
    }
}
