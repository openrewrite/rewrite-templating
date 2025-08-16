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
import com.sun.tools.javac.tree.TreeScanner;
import org.jspecify.annotations.Nullable;

import javax.tools.JavaFileObject;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClasspathJarNameDetector {

    /**
     * Locate types that are directly referred to by name in the
     * given tree and therefore need an import in the template.
     *
     * @return The list of imports to add.
     */
    public static Set<String> classpathFor(JCTree input, Collection<Symbol> imports) {
        Set<String> jarNames = new LinkedHashSet<String>() {
            @Override
            public boolean add(@Nullable String s) {
                return s != null && super.add(s);
            }
        };

        for (Symbol anImport : imports) {
            jarNames.add(jarNameFor(anImport));
        }

        new TreeScanner() {
            @Override
            public void scan(@Nullable JCTree tree) {
                if (tree == null) {
                    return;
                }

                // Collect type from tree.type
                if (tree.type != null) {
                    collectType(tree.type);
                }

                // Detect fully qualified classes
                if (tree instanceof JCFieldAccess &&
                        ((JCFieldAccess) tree).sym instanceof Symbol.ClassSymbol &&
                        Character.isUpperCase(((JCFieldAccess) tree).getIdentifier().toString().charAt(0))) {
                    jarNames.add(jarNameFor(((JCFieldAccess) tree).sym));
                }

                // Handle identifiers
                if (tree instanceof JCTree.JCIdent) {
                    JCTree.JCIdent ident = (JCTree.JCIdent) tree;
                    if (ident.sym instanceof Symbol.ClassSymbol) {
                        jarNames.add(jarNameFor(ident.sym));
                    }
                }

                // Handle new class expressions
                if (tree instanceof JCTree.JCNewClass) {
                    JCTree.JCNewClass newClass = (JCTree.JCNewClass) tree;
                    if (newClass.clazz.type != null) {
                        collectType(newClass.clazz.type);
                    }
                }

                // Handle expression statements (which might contain method invocations)
                if (tree instanceof JCTree.JCExpressionStatement) {
                    JCTree.JCExpressionStatement exprStmt = (JCTree.JCExpressionStatement) tree;
                    scan(exprStmt.expr);
                }

                // Handle method invocations
                if (tree instanceof JCTree.JCMethodInvocation) {
                    JCTree.JCMethodInvocation invocation = (JCTree.JCMethodInvocation) tree;

                    // Try to get the method symbol to access thrown exceptions
                    Symbol sym = null;
                    if (invocation.meth instanceof JCTree.JCIdent) {
                        sym = ((JCTree.JCIdent) invocation.meth).sym;
                    } else if (invocation.meth instanceof JCTree.JCFieldAccess) {
                        sym = ((JCTree.JCFieldAccess) invocation.meth).sym;
                    }

                    if (sym instanceof Symbol.MethodSymbol) {
                        Symbol.MethodSymbol methodSym = (Symbol.MethodSymbol) sym;
                        // Collect return type
                        collectType(methodSym.getReturnType());
                        // Collect parameter types
                        for (Symbol.VarSymbol param : methodSym.getParameters()) {
                            collectType(param.type);
                        }
                        // Collect exception types
                        for (Type thrownType : methodSym.getThrownTypes()) {
                            collectType(thrownType);
                        }
                    } else if (invocation.meth.type != null) {
                        // Fallback to the original approach if we can't get the method symbol
                        // Return type
                        collectType(invocation.meth.type.getReturnType());
                        // Parameter types
                        for (Type paramType : invocation.meth.type.getParameterTypes()) {
                            collectType(paramType);
                        }
                        // Exception types
                        for (Type thrownType : invocation.meth.type.getThrownTypes()) {
                            collectType(thrownType);
                        }
                    }
                }

                super.scan(tree);
            }

            private void collectType(@Nullable Type type) {
                if (type == null) {
                    return;
                }

                if (type.tsym instanceof Symbol.ClassSymbol) {
                    jarNames.add(jarNameFor(type.tsym));
                }

                // Handle generic types
                for (Type typeArg : type.getTypeArguments()) {
                    collectType(typeArg);
                }

                // Handle array types
                if (type instanceof Type.ArrayType) {
                    collectType(((Type.ArrayType) type).elemtype);
                }
            }
        }.scan(input);

        return jarNames;
    }


    private static @Nullable String jarNameFor(Symbol anImport) {
        Symbol.ClassSymbol enclClass = anImport instanceof Symbol.ClassSymbol ? (Symbol.ClassSymbol) anImport : anImport.enclClass();
        while (enclClass.enclClass() != null && enclClass.enclClass() != enclClass) {
            enclClass = enclClass.enclClass();
        }
        JavaFileObject classfile = enclClass.classfile;
        if (classfile != null) {
            String uriStr = classfile.toUri().toString();
            Matcher matcher = Pattern.compile("([^/]*)?\\.jar!/").matcher(uriStr);
            if (matcher.find()) {
                String jarName = matcher.group(1);
                return jarName.replaceAll("-\\d.*$", "");
            }
        }
        return null;
    }
}
