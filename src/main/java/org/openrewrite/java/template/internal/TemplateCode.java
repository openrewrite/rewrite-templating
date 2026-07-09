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

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeInfo;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.*;

import static java.util.stream.Collectors.joining;

public class TemplateCode {

    /** The rendered {@code JavaTemplate.builder(...)} code, plus whether it preserved any line break from the source. */
    public static final class Result {
        public final String template;
        /**
         * Whether the rendered template contains at least one preserved line break (the author placed a newline
         * before a {@code .} in a fluent chain). A generated recipe only needs to
         * auto-format its replacement when this holds; a single-line template never needs reformatting, so the
         * (per-match) formatting pass can be skipped for it.
         */
        public final boolean multiline;

        Result(String template, boolean multiline) {
            this.template = template;
            this.multiline = multiline;
        }
    }

    public static <T extends JCTree> Result process(
            T tree,
            @Nullable Type returnType,
            List<JCTree.JCVariableDecl> parameters,
            List<JCTree.JCTypeParameter> typeParameters,
            int pos,
            boolean asStatement,
            boolean fullyQualified,
            boolean classpathFromResources,
            @Nullable CharSequence source) {
        StringWriter writer = new StringWriter();
        TemplateCodePrinter printer = new TemplateCodePrinter(writer, parameters, pos, fullyQualified, source);
        try {
            if (asStatement) {
                printer.printStat(tree);
            } else {
                printer.printExpr(tree);
            }
            StringBuilder builder = new StringBuilder("JavaTemplate.builder(\"")
                    .append(writer.toString()
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replaceAll("\\R", "\\\\n"))
                    .append("\")");
            if (returnType != null && !returnType.isPrimitiveOrVoid()) {
                builder.append("\n        .bindType(\"").append(templateTypeString(returnType)).append("\")");
            }
            if (!typeParameters.isEmpty()) {
                builder.append("\n        .genericTypes(").append(typeParameters.stream().map(tp -> '"' + genericTypeString(tp) + '"').collect(joining(", "))).append(")");
            }
            if (!printer.imports.isEmpty()) {
                builder.append("\n        .imports(").append(printer.imports.stream().map(i -> '"' + i + '"').collect(joining(", "))).append(")");
            }
            if (!printer.staticImports.isEmpty()) {
                builder.append("\n        .staticImports(").append(printer.staticImports.stream().map(i -> '"' + i + '"').collect(joining(", "))).append(")");
            }
            ClasspathJarNameDetector classpathJarNameDetector = new ClasspathJarNameDetector();
            parameters.forEach(classpathJarNameDetector::classpathFor);
            Set<String> jarNames = classpathJarNameDetector.classpathFor(tree);
            if (!jarNames.isEmpty()) {
                builder.append("\n        .javaParser(JavaParser.fromJavaVersion()");
                if (classpathFromResources) {
                    String joinedJarNames = jarNames.stream().collect(joining("\", \"", "\"", "\""));
                    builder.append(".classpathFromResources(ctx, ").append(joinedJarNames).append("))\n        ");
                } else {
                    builder.append(".classpath(JavaParser.runtimeClasspath()))\n        ");
                }
            }
            return new Result(builder.toString(), printer.emittedNewline);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class TemplateCodePrinter extends Pretty {

        private static final String PRIMITIVE_ANNOTATION = "org.openrewrite.java.template.Primitive";
        private static final String REPEATED_ANNOTATION = "com.google.errorprone.refaster.annotation.Repeated";
        private final List<JCTree.JCVariableDecl> declaredParameters;
        private final int pos;
        private final boolean fullyQualified;
        private final @Nullable CharSequence source;
        private final Set<JCTree.JCVariableDecl> seenParameters = new HashSet<>();
        private final TreeSet<String> imports = new TreeSet<>();
        private final TreeSet<String> staticImports = new TreeSet<>();
        private boolean emittedNewline;

        public TemplateCodePrinter(Writer writer, List<JCTree.JCVariableDecl> declaredParameters, int pos, boolean fullyQualified,
                                   @Nullable CharSequence source) {
            super(writer, true);
            this.declaredParameters = declaredParameters;
            this.pos = pos;
            this.fullyQualified = fullyQualified;
            this.source = source;
        }

        @Override
        public void visitMethodDef(JCTree.JCMethodDecl tree) {
            if ((Flags.GENERATEDCONSTR & tree.getModifiers().flags) == 0L) {
                super.visitMethodDef(tree);
            }
        }

        @Override
        public void visitIdent(JCIdent jcIdent) {
            try {
                Symbol sym = jcIdent.sym;
                Optional<JCTree.JCVariableDecl> param = declaredParameters.stream().filter(p -> p.sym == sym).findFirst();
                if (param.isPresent()) {
                    print("#{" + sym.name);
                    if (seenParameters.add(param.get())) {
                        Type type = param.get().sym.type;
                        String typeString;
                        boolean isPrimitive = param.get().getModifiers().getAnnotations().stream()
                                .anyMatch(a -> PRIMITIVE_ANNOTATION.equals(a.attribute.type.tsym.getQualifiedName().toString()));
                        if (isPrimitive) {
                            typeString = getUnboxedPrimitive(type.toString());
                        } else {
                            typeString = templateTypeString(type);
                        }
                        // Use anyArray for @Repeated parameters
                        boolean isRepeated = param.get().getModifiers().getAnnotations().stream()
                                .anyMatch(a -> REPEATED_ANNOTATION.equals(a.attribute.type.tsym.getQualifiedName().toString()));
                        print(isRepeated ? ":anyArray(" + typeString + ")" : ":any(" + typeString + ")");
                    }
                    print("}");
                } else if (sym != null) {
                    print(sym);
                } else {
                    print(jcIdent.name);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void visitApply(JCTree.JCMethodInvocation tree) {
            Symbol sym = TreeInfo.symbol(tree.meth);
            boolean refaster = sym.owner.getQualifiedName().contentEquals("com.google.errorprone.refaster.Refaster");
            if (refaster && sym.getSimpleName().contentEquals("anyOf")) {
                tree.args.get(pos).accept(this);
            } else if (refaster && sym.getSimpleName().contentEquals("asVarargs")) {
                // asVarargs() unwraps to just the parameter reference
                tree.args.get(0).accept(this);
            } else {
                // The default printer still routes the method selector through visitSelect, so a line break
                // the author placed before a `.` in a fluent chain is preserved.
                super.visitApply(tree);
            }
        }

        @Override
        public void visitSelect(JCTree.JCFieldAccess tree) {
            try {
                printExpr(tree.selected, TreeInfo.postfixPrec);
                // Preserve a line break the author placed before the `.` in a fluent chain; AUTO_FORMAT re-indents.
                // For a field access, tree.pos is the position of the `.` itself.
                printNewlineIfSourceHadOne(tree.pos);
                print("." + tree.name);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Emit a preserved line break if the author placed one before {@code pos} (scanning backwards over
         * whitespace only, since end positions are not reliably recorded for the resolved template trees).
         * Records that a newline was emitted so {@link Result#multiline} reflects the render.
         */
        private void printNewlineIfSourceHadOne(int pos) throws IOException {
            if (source == null || pos < 0 || pos > source.length()) {
                return;
            }
            for (int i = pos - 1; i >= 0; i--) {
                char c = source.charAt(i);
                if (c == '\n' || c == '\r') {
                    print("\n");
                    emittedNewline = true;
                    return;
                }
                if (!Character.isWhitespace(c)) {
                    return;
                }
            }
        }

        void print(Symbol sym) throws IOException {
            if (sym instanceof Symbol.ClassSymbol) {
                if (fullyQualified) {
                    print(sym.packge().fullname.contentEquals("java.lang") ? sym.name : sym.getQualifiedName());
                } else {
                    print(sym.name);
                    if (!sym.packge().fullname.contentEquals("java.lang")) {
                        imports.add(sym.getQualifiedName().toString());
                    }
                }
            } else if (sym instanceof Symbol.VarSymbol) {
                if (fullyQualified) {
                    if (sym.owner instanceof Symbol.ClassSymbol) {
                        print(sym.owner);
                        print('.');
                    }
                    print(sym.name);
                } else {
                    print(sym.name);
                    if (!sym.packge().fullname.contentEquals("java.lang")) {
                        staticImports.add(sym.owner.getQualifiedName() + "." + sym.name);
                    }
                }
            } else if (sym instanceof Symbol.MethodSymbol) {
                if (fullyQualified) {
                    print(sym.owner);
                    print('.');
                    print(sym.name);
                } else {
                    print(sym.name);
                    if (!sym.packge().fullname.contentEquals("java.lang")) {
                        staticImports.add(sym.owner.getQualifiedName() + "." + sym.name);
                    }
                }
            } else if (sym instanceof Symbol.PackageSymbol) {
                print(sym.getQualifiedName());
            } else if (sym instanceof Symbol.TypeVariableSymbol) {
                print(sym.name);
            }
        }

        private String getUnboxedPrimitive(String paramType) {
            switch (paramType) {
                case "java.lang.Boolean":
                    return "boolean";
                case "java.lang.Byte":
                    return "byte";
                case "java.lang.Character":
                    return "char";
                case "java.lang.Double":
                    return "double";
                case "java.lang.Float":
                    return "float";
                case "java.lang.Integer":
                    return "int";
                case "java.lang.Long":
                    return "long";
                case "java.lang.Short":
                    return "short";
                case "java.lang.Void":
                    return "void";
            }
            return paramType;
        }
    }

    private static String genericTypeString(JCTree.JCTypeParameter tp) {
        String name = tp.name.toString();
        if (tp.getBounds() != null && !tp.getBounds().isEmpty()) {
            String bounds = tp.getBounds().stream()
                    .map(e -> e.type)
                    .map(TemplateCode::templateTypeString)
                    .collect(joining(" & "));
            return name + " extends " + bounds;
        }
        return name;
    }

    private static String templateTypeString(Type type) {
        if (type instanceof Type.ArrayType) {
            Type elemtype = ((Type.ArrayType) type).elemtype;
            return templateTypeString(elemtype) + "[]";
        }
        if (type instanceof Type.WildcardType) {
            Type.WildcardType wildcardType = (Type.WildcardType) type;
            if (wildcardType.kind == BoundKind.EXTENDS) {
                return "? extends " + templateTypeString(wildcardType.type);
            }
            if (wildcardType.kind == BoundKind.SUPER) {
                return "? super " + templateTypeString(wildcardType.type);
            }
            return "?";
        }
        if (type.isParameterized()) {
            return type.tsym.getQualifiedName().toString() + '<' + type.allparams().stream().map(TemplateCode::templateTypeString).collect(joining(", ")) + '>';
        }
        return type.tsym.getQualifiedName().toString();
    }
}
