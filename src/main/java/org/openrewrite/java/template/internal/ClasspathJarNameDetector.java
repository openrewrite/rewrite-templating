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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
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
        Set<String> jarNames = new LinkedHashSet<>();
        Consumer<Symbol> addJarNameForSymbol = sym -> {
            String jarName = jarNameFor(sym);
            if (jarName != null) {
                jarNames.add(jarName);
            }
        };

        imports.forEach(addJarNameForSymbol);

        new TreeScanner() {
            // Track visited types to avoid infinite recursion
            private final Set<Type> visitedTypes = new HashSet<>();

            @Override
            public void scan(@Nullable JCTree tree) {
                // Collect type from tree.type for all nodes
                if (tree != null && tree.type != null) {
                    collectType(tree.type);
                }
                super.scan(tree);
            }

            @Override
            public void visitApply(JCTree.JCMethodInvocation invocation) {
                // Handle method invocations
                Symbol sym = null;
                if (invocation.meth instanceof JCTree.JCIdent) {
                    sym = ((JCTree.JCIdent) invocation.meth).sym;
                } else if (invocation.meth instanceof JCFieldAccess) {
                    sym = ((JCFieldAccess) invocation.meth).sym;
                }

                if (sym instanceof Symbol.MethodSymbol) {
                    Symbol.MethodSymbol ms = (Symbol.MethodSymbol) sym;
                    collectMethodTypes(ms);
                }
                // Also check invocation.type which contains method type info
                if (invocation.type != null) {
                    collectType(invocation.type);
                }

                // Process the method expression itself for any type info
                if (invocation.meth != null && invocation.meth.type != null) {
                    Type methodType = invocation.meth.type;
                    if (methodType.getReturnType() != null) {
                        collectType(methodType.getReturnType());
                    }
                    for (Type paramType : methodType.getParameterTypes()) {
                        collectType(paramType);
                    }
                    for (Type thrownType : methodType.getThrownTypes()) {
                        collectType(thrownType);
                    }
                }

                super.visitApply(invocation);
            }

            private void collectMethodTypes(Symbol.MethodSymbol methodSym) {
                // Collect return type
                collectType(methodSym.getReturnType());

                // Collect parameter types
                for (Symbol.VarSymbol param : methodSym.getParameters()) {
                    collectType(param.type);
                }

                // Collect exception types (important for transitive dependencies)
                for (Type thrownType : methodSym.getThrownTypes()) {
                    collectType(thrownType);
                }
            }

            private void collectType(@Nullable Type type) {
                if (type == null || !visitedTypes.add(type)) {
                    return;
                }

                // Collect the main type
                if (type.tsym instanceof Symbol.ClassSymbol) {
                    Symbol.ClassSymbol classSym = (Symbol.ClassSymbol) type.tsym;
                    addJarNameForSymbol.accept(classSym);

                    // Collect superclass and interfaces for transitive dependencies
                    Type superClass = classSym.getSuperclass();
                    if (superClass != null && superClass.tsym != null) {
                        collectType(superClass);
                    }
                    for (Type iface : classSym.getInterfaces()) {
                        collectType(iface);
                    }
                }

                // Handle generic type arguments (e.g., List<String>)
                for (Type typeArg : type.getTypeArguments()) {
                    collectType(typeArg);
                }

                // Handle array component types
                if (type instanceof Type.ArrayType) {
                    collectType(((Type.ArrayType) type).elemtype);
                }

                // Handle wildcard bounds if present
                if (type instanceof Type.WildcardType) {
                    Type.WildcardType wildcard = (Type.WildcardType) type;
                    if (wildcard.type != null) {
                        collectType(wildcard.type);
                    }
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
