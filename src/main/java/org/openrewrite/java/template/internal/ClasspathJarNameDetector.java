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
            public void scan(JCTree tree) {
                // Detect fully qualified classes
                if (tree instanceof JCFieldAccess &&
                        ((JCFieldAccess) tree).sym instanceof Symbol.ClassSymbol &&
                        Character.isUpperCase(((JCFieldAccess) tree).getIdentifier().toString().charAt(0))) {
                    jarNames.add(jarNameFor(((JCFieldAccess) tree).sym));
                }
                
                // Detect method invocations and their types
                if (tree instanceof JCTree.JCMethodInvocation) {
                    JCTree.JCMethodInvocation invocation = (JCTree.JCMethodInvocation) tree;
                    if (invocation.meth instanceof JCTree.JCFieldAccess) {
                        JCTree.JCFieldAccess methodAccess = (JCTree.JCFieldAccess) invocation.meth;
                        if (methodAccess.sym instanceof Symbol.MethodSymbol) {
                            Symbol.MethodSymbol methodSym = (Symbol.MethodSymbol) methodAccess.sym;
                            
                            // Add jar for the method's owner class
                            jarNames.add(jarNameFor(methodSym.owner));
                            
                            // Add jar for the return type
                            if (methodSym.getReturnType() != null) {
                                addTypeAndTransitiveDependencies(methodSym.getReturnType(), jarNames);
                            }
                            
                            // Add jars for exception types
                            for (Type thrownType : methodSym.getThrownTypes()) {
                                addTypeAndTransitiveDependencies(thrownType, jarNames);
                            }
                        }
                    }
                }
                
                // Detect identifiers that reference classes
                if (tree instanceof JCTree.JCIdent) {
                    JCTree.JCIdent ident = (JCTree.JCIdent) tree;
                    if (ident.sym instanceof Symbol.ClassSymbol) {
                        Symbol.ClassSymbol classSym = (Symbol.ClassSymbol) ident.sym;
                        jarNames.add(jarNameFor(classSym));
                        
                        // Add transitive dependencies through inheritance
                        addTypeAndTransitiveDependencies(classSym.type, jarNames);
                    }
                }
                
                super.scan(tree);
            }
            
            private void addTypeAndTransitiveDependencies(Type type, Set<String> jarNames) {
                if (type == null) return;
                
                if (type.tsym instanceof Symbol.ClassSymbol) {
                    Symbol.ClassSymbol classSym = (Symbol.ClassSymbol) type.tsym;
                    jarNames.add(jarNameFor(classSym));
                    
                    // Check superclass
                    Type superType = classSym.getSuperclass();
                    if (superType != null && superType.tsym != null) {
                        jarNames.add(jarNameFor(superType.tsym));
                    }
                    
                    // Check interfaces
                    for (Type iface : classSym.getInterfaces()) {
                        if (iface.tsym != null) {
                            jarNames.add(jarNameFor(iface.tsym));
                        }
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
