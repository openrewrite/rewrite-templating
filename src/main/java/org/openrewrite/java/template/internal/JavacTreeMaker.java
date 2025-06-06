/*
 * Copyright 2022 the original author or authors.
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

/*
 * Copyright (C) 2013-2020 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.openrewrite.java.template.internal;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("SameParameterValue")
public class JavacTreeMaker {
    private final TreeMaker tm;

    public JavacTreeMaker(TreeMaker tm) {
        this.tm = tm;
    }

    public TreeMaker getUnderlyingTreeMaker() {
        return tm;
    }

    private static class SchroedingerType {
        final Object value;

        private SchroedingerType(Object value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return value == null ? -1 : value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SchroedingerType) {
                Object other = ((SchroedingerType) obj).value;
                return Objects.equals(value, other);
            }
            return false;
        }

        static Object getFieldCached(ConcurrentMap<String, Object> cache, String className, String fieldName) {
            Object value = cache.get(fieldName);
            if (value != null) {
                return value;
            }
            try {
                value = Permit.getField(Class.forName(className), fieldName).get(null);
            } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
                //noinspection DataFlowIssue
                throw Javac.sneakyThrow(e);
            }

            cache.putIfAbsent(fieldName, value);
            return value;
        }

        private static final Field NOSUCHFIELDEX_MARKER;

        static {
            try {
                NOSUCHFIELDEX_MARKER = Permit.getField(SchroedingerType.class, "NOSUCHFIELDEX_MARKER");
            } catch (NoSuchFieldException e) {
                //noinspection DataFlowIssue
                throw Javac.sneakyThrow(e);
            }
        }

        static Object getFieldCached(ConcurrentMap<Class<?>, Field> cache, Object ref, String fieldName) throws NoSuchFieldException {
            Class<?> c = ref.getClass();
            Field field = cache.get(c);
            if (field == null) {
                try {
                    field = Permit.getField(c, fieldName);
                } catch (NoSuchFieldException e) {
                    cache.putIfAbsent(c, NOSUCHFIELDEX_MARKER);
                    //noinspection DataFlowIssue
                    throw Javac.sneakyThrow(e);
                }
                Permit.setAccessible(field);
                Field old = cache.putIfAbsent(c, field);
                if (old != null) {
                    field = old;
                }
            }

            if (field == NOSUCHFIELDEX_MARKER) {
                throw new NoSuchFieldException(fieldName);
            }
            try {
                return field.get(ref);
            } catch (IllegalAccessException e) {
                //noinspection DataFlowIssue
                throw Javac.sneakyThrow(e);
            }
        }
    }

    public static final class TypeTag extends SchroedingerType {
        private static final ConcurrentMap<String, Object> TYPE_TAG_CACHE = new ConcurrentHashMap<>();
        private static final ConcurrentMap<Class<?>, Field> FIELD_CACHE = new ConcurrentHashMap<>();
        private static final Method TYPE_TYPETAG_METHOD;

        static {
            Method m = null;
            try {
                m = Permit.getMethod(Type.class, "getTag");
            } catch (NoSuchMethodException ignored) {
            }
            TYPE_TYPETAG_METHOD = m;
        }

        private TypeTag(Object value) {
            super(value);
        }

        public static TypeTag typeTag(JCTree o) {
            try {
                return new TypeTag(getFieldCached(FIELD_CACHE, o, "typetag"));
            } catch (NoSuchFieldException e) {
                //noinspection DataFlowIssue
                throw Javac.sneakyThrow(e);
            }
        }

        public static TypeTag typeTag(@Nullable Type t) {
            if (t == null) {
                return Javac.CTC_VOID;
            }
            try {
                return new TypeTag(getFieldCached(FIELD_CACHE, t, "tag"));
            } catch (NoSuchFieldException e) {
                if (TYPE_TYPETAG_METHOD == null) {
                    throw new IllegalStateException("Type " + t.getClass() + " has neither 'tag' nor getTag()");
                }
                try {
                    return new TypeTag(TYPE_TYPETAG_METHOD.invoke(t));
                } catch (IllegalAccessException ex) {
                    throw Javac.sneakyThrow(ex);
                } catch (InvocationTargetException ex) {
                    throw Javac.sneakyThrow(ex.getCause());
                }
            }
        }

        public static TypeTag typeTag(String identifier) {
            return new TypeTag(getFieldCached(TYPE_TAG_CACHE, "com.sun.tools.javac.code.TypeTag", identifier));
        }

        @SuppressWarnings("DataFlowIssue")
        public static @Nullable TypeTag typeTagPermissive(String identifier) {
            try {
                return typeTag(identifier);
            } catch (Exception e) {
                //noinspection ConstantValue
                if (e instanceof NoSuchFieldException) return null;
                throw Javac.sneakyThrow(e);
            }
        }
    }

}
