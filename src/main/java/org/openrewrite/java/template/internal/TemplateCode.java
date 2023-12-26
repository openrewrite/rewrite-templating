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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.Pretty;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TemplateCode {

    public static <T extends JCTree> String process(T tree, List<JCTree.JCVariableDecl> parameters) {
        StringWriter writer = new StringWriter();
        TemplateCodePrinter printer = new TemplateCodePrinter(writer, parameters);
        try {
            printer.printExpr(tree);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class TemplateCodePrinter extends Pretty {

        private static final String PRIMITIVE_ANNOTATION = "org.openrewrite.java.template.Primitive";
        private final List<JCTree.JCVariableDecl> declaredParameters;
        private final Set<JCTree.JCVariableDecl> seenParameters = new HashSet<>();

        public TemplateCodePrinter(Writer writer, List<JCTree.JCVariableDecl> declaredParameters) {
            super(writer, true);
            this.declaredParameters = declaredParameters;
        }

        @Override
        public void visitIdent(JCIdent jcIdent) {
            try {
                Symbol sym = jcIdent.sym;
                Optional<JCTree.JCVariableDecl> param = declaredParameters.stream().filter(p -> p.sym == sym).findFirst();
                if (param.isPresent()) {
                    print("#{" + sym.name);
                    if (seenParameters.add(param.get())) {
                        String type = param.get().type.toString();
                        if (param.get().getModifiers().getAnnotations().stream().anyMatch(a -> a.attribute.type.tsym.getQualifiedName().toString().equals(PRIMITIVE_ANNOTATION))) {
                            type = getUnboxedPrimitive(type);
                        }
                        print(":any(" + type + ")");
                    }
                    print("}");
                } else {
                    print(sym);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        void print(Symbol sym) throws IOException {
            if (sym instanceof Symbol.ClassSymbol) {
                print(sym.packge().fullname.contentEquals("java.lang") ? sym.name.toString() : sym.getQualifiedName().toString());
            } else if (sym instanceof Symbol.MethodSymbol || sym instanceof Symbol.VarSymbol) {
                print(sym.owner);
                print('.');
                print(sym.name);
            } else if (sym instanceof Symbol.PackageSymbol) {
                print(sym.getQualifiedName().toString());
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
}
