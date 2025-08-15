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
package org.openrewrite.java.template.internal;

import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Value
@SuppressWarnings("unused")
public class PatternBuilder {
    String name;

    public JavaTemplate.Builder build(JavaVisitor<?> owner) {
        try {
            Class<?> templateClass = Class.forName(owner.getClass().getName() + "_" + name, true,
                    owner.getClass().getClassLoader());
            Method getTemplate = templateClass.getDeclaredMethod("getTemplate");
            return (JavaTemplate.Builder) getTemplate.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaTemplate.Builder build(ExecutionContext ctx, JavaVisitor<?> owner) {
        try {
            Class<?> templateClass = Class.forName(owner.getClass().getName() + "_" + name, true,
                    owner.getClass().getClassLoader());
            Method getTemplate = templateClass.getDeclaredMethod("getTemplate", ExecutionContext.class);
            return (JavaTemplate.Builder) getTemplate.invoke(null, ctx);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
