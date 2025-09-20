/*
 * Copyright (C) 2011-2021 The Project Lombok Authors.
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

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import org.jspecify.annotations.Nullable;

import javax.tools.DiagnosticListener;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * During resolution, the resolver will emit resolution errors, but without appropriate file names and line numbers. If these resolution errors stick around
 * then they will be generated AGAIN, this time with proper names and line numbers, at the end. Therefore, we want to suppress the logger.
 */
public final class CompilerMessageSuppressor {

    private final Log log;
    private static final WriterField errWriterField, warnWriterField, noticeWriterField;
    private static final @Nullable Field dumpOnErrorField, promptOnErrorField, diagnosticListenerField;
    private static final @Nullable Field deferDiagnosticsField, deferredDiagnosticsField, diagnosticHandlerField;
    private static final ConcurrentMap<Class<?>, Field> handlerDeferredFields = new ConcurrentHashMap<>();
    private static final @Nullable Field NULL_FIELD;
    private @Nullable Boolean dumpOnError, promptOnError;
    private @Nullable DiagnosticListener<?> contextDiagnosticListener, logDiagnosticListener;
    private final Context context;

    private static final ThreadLocal<Queue<?>> queueCache = new ThreadLocal<>();

    enum Writers {
        ERROR("errWriter", "ERROR"),
        WARNING("warnWriter", "WARNING"),
        NOTICE("noticeWriter", "NOTICE");

        final String fieldName;
        final String keyName;

        Writers(String fieldName, String keyName) {
            this.fieldName = fieldName;
            this.keyName = keyName;
        }
    }

    static {
        errWriterField = createWriterField(Writers.ERROR);
        warnWriterField = createWriterField(Writers.WARNING);
        noticeWriterField = createWriterField(Writers.NOTICE);
        dumpOnErrorField = getDeclaredField(Log.class, "dumpOnError");
        promptOnErrorField = getDeclaredField(Log.class, "promptOnError");
        diagnosticListenerField = getDeclaredField(Log.class, "diagListener");
        deferDiagnosticsField = getDeclaredField(Log.class, "deferDiagnostics");
        deferredDiagnosticsField = getDeclaredField(Log.class, "deferredDiagnostics");

        // javac8
        diagnosticHandlerField = getDeclaredField(Log.class, "diagnosticHandler");

        NULL_FIELD = getDeclaredField(JavacResolution.class, "NULL_FIELD");
    }

    static @Nullable Field getDeclaredField(Class<?> c, String fieldName) {
        try {
            return Permit.getField(c, fieldName);
        } catch (Throwable t) {
            return null;
        }
    }

    public CompilerMessageSuppressor(Context context) {
        this.log = Log.instance(context);
        this.context = context;
    }

    public void disableLoggers() {
        contextDiagnosticListener = context.get(DiagnosticListener.class);
        context.put(DiagnosticListener.class, (DiagnosticListener<?>) null);

        errWriterField.pauze(log);
        warnWriterField.pauze(log);
        noticeWriterField.pauze(log);

        if (deferDiagnosticsField != null) {
            try {
                if (Boolean.TRUE.equals(deferDiagnosticsField.get(log))) {
                    queueCache.set((Queue<?>) deferredDiagnosticsField.get(log));
                    Queue<?> empty = new LinkedList<>();
                    deferredDiagnosticsField.set(log, empty);
                }
            } catch (Exception e) {
            }
        }

        if (diagnosticHandlerField != null) {
            try {
                Object handler = diagnosticHandlerField.get(log);
                Field field = getDeferredField(handler);
                if (field != null) {
                    queueCache.set((Queue<?>) field.get(handler));
                    Queue<?> empty = new LinkedList<>();
                    field.set(handler, empty);
                }
            } catch (Exception e) {
            }
        }

        if (dumpOnErrorField != null) {
            try {
                dumpOnError = (Boolean) dumpOnErrorField.get(log);
                dumpOnErrorField.set(log, false);
            } catch (Exception e) {
            }
        }

        if (promptOnErrorField != null) {
            try {
                promptOnError = (Boolean) promptOnErrorField.get(log);
                promptOnErrorField.set(log, false);
            } catch (Exception e) {
            }
        }

        if (diagnosticListenerField != null) {
            try {
                logDiagnosticListener = (DiagnosticListener<?>) diagnosticListenerField.get(log);
                diagnosticListenerField.set(log, null);
            } catch (Exception e) {
            }
        }
    }

    private static @Nullable Field getDeferredField(Object handler) {
        Class<? extends Object> key = handler.getClass();
        Field field = handlerDeferredFields.get(key);
        if (field != null) {
            return field == NULL_FIELD ? null : field;
        }
        Field value = getDeclaredField(key, "deferred");
        handlerDeferredFields.put(key, value == null ? NULL_FIELD : value);
        return getDeferredField(handler);
    }

    public void enableLoggers() {
        if (contextDiagnosticListener != null) {
            context.put(DiagnosticListener.class, contextDiagnosticListener);
            contextDiagnosticListener = null;
        }

        errWriterField.resume(log);
        warnWriterField.resume(log);
        noticeWriterField.resume(log);

        if (dumpOnError != null) {
            try {
                dumpOnErrorField.set(log, dumpOnError);
                dumpOnError = null;
            } catch (Exception e) {
            }
        }

        if (promptOnError != null) {
            try {
                promptOnErrorField.set(log, promptOnError);
                promptOnError = null;
            } catch (Exception e) {
            }
        }

        if (logDiagnosticListener != null) {
            try {
                diagnosticListenerField.set(log, logDiagnosticListener);
                logDiagnosticListener = null;
            } catch (Exception e) {
            }
        }

        if (diagnosticHandlerField != null && queueCache.get() != null) {
            try {
                Object handler = diagnosticHandlerField.get(log);
                Field field = getDeferredField(handler);
                if (field != null) {
                    field.set(handler, queueCache.get());
                    queueCache.set(null);
                }
            } catch (Exception e) {
            }
        }

        if (deferDiagnosticsField != null && queueCache.get() != null) {
            try {
                deferredDiagnosticsField.set(log, queueCache.get());
                queueCache.set(null);
            } catch (Exception e) {
            }
        }
    }

    private static WriterField createWriterField(Writers w) {
        // jdk9
        try {
            Field writers = getDeclaredField(Log.class, "writer");
            if (writers != null) {
                Class<?> kindsClass = Class.forName("com.sun.tools.javac.util.Log$WriterKind");
                for (Object enumConstant : kindsClass.getEnumConstants()) {
                    if (enumConstant.toString().equals(w.keyName)) {
                        return new Java9WriterField(writers, enumConstant);
                    }
                }
                return WriterField.NONE;
            }
        } catch (Exception e) {
        }

        // jdk8
        Field writerField = getDeclaredField(Log.class, w.fieldName);
        if (writerField != null) {
            return new Java8WriterField(writerField);
        }

        // other jdk
        return WriterField.NONE;
    }

    interface WriterField {
        PrintWriter NO_WRITER = new PrintWriter(new OutputStream() {
            @Override
            public void write(int b) {
                // Do nothing on purpose
            }
        });

        WriterField NONE = new WriterField() {
            @Override
            public void pauze(Log log) {
                // do nothing
            }

            @Override
            public void resume(Log log) {
                // no nothing
            }
        };

        void pauze(Log log);

        void resume(Log log);
    }

    static class Java8WriterField implements WriterField {
        private final Field field;
        private @Nullable PrintWriter writer;

        public Java8WriterField(Field field) {
            this.field = field;
        }

        @Override
        public void pauze(Log log) {
            try {
                writer = (PrintWriter) field.get(log);
                field.set(log, NO_WRITER);
            } catch (Exception e) {
            }
        }

        @Override
        public void resume(Log log) {
            if (writer != null) {
                try {
                    field.set(log, writer);
                } catch (Exception e) {
                }
            }
            writer = null;
        }
    }


    static class Java9WriterField implements WriterField {
        private final Field field;
        private final Object key;
        private @Nullable PrintWriter writer;

        public Java9WriterField(Field field, Object key) {
            this.field = field;
            this.key = key;
        }

        @Override
        public void pauze(Log log) {
            try {
                @SuppressWarnings("unchecked") Map<Object, PrintWriter> map = (Map<Object, PrintWriter>) field.get(log);
                writer = map.get(key);
                map.put(key, NO_WRITER);
            } catch (Exception e) {
            }
        }

        @Override
        public void resume(Log log) {
            if (writer != null) {
                try {
                    @SuppressWarnings("unchecked") Map<Object, PrintWriter> map = (Map<Object, PrintWriter>) field.get(log);
                    map.put(key, writer);
                } catch (Exception e) {
                }
            }
            writer = null;
        }
    }
}
