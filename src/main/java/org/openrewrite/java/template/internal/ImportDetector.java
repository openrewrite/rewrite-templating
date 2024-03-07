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
package org.openrewrite.java.template.internal;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.TreeScanner;

import javax.lang.model.element.ElementKind;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ImportDetector {
    /**
     * Locate types that are directly referred to by name in the
     * given method declaration and therefore need an import in the template.
     * <p>
     * Excludes exceptions thrown by the method.
     *
     * @return The list of imports to add.
     */
    public static List<Symbol> imports(JCTree.JCMethodDecl methodDecl) {
        return imports(methodDecl, t -> true);
    }

    public static List<Symbol> imports(JCTree.JCMethodDecl methodDecl, Predicate<JCTree> scopePredicate) {
        ImportScanner importScanner = new ImportScanner(scopePredicate);
        importScanner.scan(methodDecl.getBody());
        importScanner.scan(methodDecl.getReturnType());
        for (JCTree.JCVariableDecl param : methodDecl.getParameters()) {
            importScanner.scan(param);
        }
        for (JCTree.JCTypeParameter param : methodDecl.getTypeParameters()) {
            importScanner.scan(param);
        }
        return new ArrayList<>(importScanner.imports);
    }

    /**
     * Locate types that are directly referred to by name in the
     * given tree and therefore need an import in the template.
     *
     * @return The list of imports to add.
     */
    public static List<Symbol> imports(JCTree tree) {
        ImportScanner importScanner = new ImportScanner(t -> true);
        importScanner.scan(tree);
        return new ArrayList<>(importScanner.imports);
    }

    private static class ImportScanner extends TreeScanner {
        final Set<Symbol> imports = new LinkedHashSet<>();
        private final Predicate<JCTree> scopePredicate;

        public ImportScanner(Predicate<JCTree> scopePredicate) {
            this.scopePredicate = scopePredicate;
        }

        @Override
        public void scan(JCTree tree) {
            if (!scopePredicate.test(tree)) {
                return;
            }

            JCTree maybeFieldAccess = tree;
            if (maybeFieldAccess instanceof JCFieldAccess &&
                ((JCFieldAccess) maybeFieldAccess).sym instanceof Symbol.ClassSymbol &&
                Character.isUpperCase(((JCFieldAccess) maybeFieldAccess).getIdentifier().toString().charAt(0))) {
                while (maybeFieldAccess instanceof JCFieldAccess) {
                    maybeFieldAccess = ((JCFieldAccess) maybeFieldAccess).getExpression();
                    if (maybeFieldAccess instanceof JCIdent &&
                        Character.isUpperCase(((JCIdent) maybeFieldAccess).getName().toString().charAt(0))) {
                        // this might be a fully qualified type name, so we don't want to add an import for it
                        // and returning will skip the nested identifier which represents just the class simple name
                        return;
                    }
                }
            }

            if (tree instanceof JCTree.JCAnnotation) {
                // completely skip annotations for now
                return;
            }

            if (tree instanceof JCIdent) {
                if (tree.type == null || !(tree.type.tsym instanceof Symbol.ClassSymbol)) {
                    return;
                }
                if (((JCIdent) tree).sym.getKind() == ElementKind.CLASS || ((JCIdent) tree).sym.getKind() == ElementKind.INTERFACE) {
                    imports.add(tree.type.tsym);
                } else if (((JCIdent) tree).sym.getKind() == ElementKind.FIELD) {
                    imports.add(((JCIdent) tree).sym);
                } else if (((JCIdent) tree).sym.getKind() == ElementKind.METHOD) {
                    imports.add(((JCIdent) tree).sym);
                } else if (((JCIdent) tree).sym.getKind() == ElementKind.ENUM_CONSTANT) {
                    imports.add(((JCIdent) tree).sym);
                }
            } else if (tree instanceof JCFieldAccess && ((JCFieldAccess) tree).sym instanceof Symbol.VarSymbol
                       && ((JCFieldAccess) tree).selected instanceof JCIdent
                       && ((JCIdent) ((JCFieldAccess) tree).selected).sym instanceof Symbol.ClassSymbol) {
                imports.add(((JCIdent) ((JCFieldAccess) tree).selected).sym);
            } else if (tree instanceof JCFieldAccess && ((JCFieldAccess) tree).sym instanceof Symbol.MethodSymbol
                       && ((JCFieldAccess) tree).selected instanceof JCIdent
                       && ((JCIdent) ((JCFieldAccess) tree).selected).sym instanceof Symbol.ClassSymbol) {
                imports.add(((JCIdent) ((JCFieldAccess) tree).selected).sym);
            } else if (tree instanceof JCFieldAccess && ((JCFieldAccess) tree).sym instanceof Symbol.ClassSymbol
                       && ((JCFieldAccess) tree).selected instanceof JCIdent
                       && ((JCIdent) ((JCFieldAccess) tree).selected).sym instanceof Symbol.ClassSymbol
                       && !(((JCIdent) ((JCFieldAccess) tree).selected).sym.type instanceof Type.ErrorType)) {
                imports.add(((JCIdent) ((JCFieldAccess) tree).selected).sym);
            } else if (tree instanceof JCTree.JCFieldAccess &&
                ((JCTree.JCFieldAccess) tree).sym instanceof Symbol.ClassSymbol) {
                if (tree.toString().equals(((JCTree.JCFieldAccess) tree).sym.toString())) {
                    imports.add(((JCTree.JCFieldAccess) tree).sym);
                }
            }

            super.scan(tree);
        }
    }
}
