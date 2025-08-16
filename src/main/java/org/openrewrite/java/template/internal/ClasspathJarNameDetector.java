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
                System.out.println("Detected classpath jar: " + jarName + " for symbol: " + sym);
                jarNames.add(jarName);
            }
        };

        imports.forEach(addJarNameForSymbol);

        new TreeScanner() {
            @Override
            public void scan(@Nullable JCTree tree) {
                // Collect type from tree.type for all nodes
                if (tree != null && tree.type != null) {
                    collectType(tree.type);
                }
                super.scan(tree);
            }

            @Override
            public void visitIdent(JCTree.JCIdent ident) {
                // Handle simple class references (e.g., String, List)
                if (ident.sym instanceof Symbol.ClassSymbol) {
                    addJarNameForSymbol.accept(ident.sym);
                }
                super.visitIdent(ident);
            }

            @Override
            public void visitSelect(JCFieldAccess fieldAccess) {
                // Handle fully qualified class references (e.g., java.util.List)
                if (fieldAccess.sym instanceof Symbol.ClassSymbol &&
                        Character.isUpperCase(fieldAccess.getIdentifier().toString().charAt(0))) {
                    addJarNameForSymbol.accept(fieldAccess.sym);
                }
                super.visitSelect(fieldAccess);
            }

            @Override
            public void visitNewClass(JCTree.JCNewClass newClass) {
                // Handle new expressions (e.g., new ArrayList<>())
                if (newClass.clazz.type != null) {
                    collectType(newClass.clazz.type);
                }

                // Also collect types from the constructor if available
                if (newClass.constructor instanceof Symbol.MethodSymbol) {
                    collectMethodTypes((Symbol.MethodSymbol) newClass.constructor);
                }

                super.visitNewClass(newClass);
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

            @Override
            public void visitTypeApply(JCTree.JCTypeApply typeApply) {
                // Handle generic type applications (e.g., List<String>)
                if (typeApply.clazz.type != null) {
                    collectType(typeApply.clazz.type);
                }

                for (JCTree.JCExpression typeArg : typeApply.arguments) {
                    if (typeArg.type != null) {
                        collectType(typeArg.type);
                    }
                }

                super.visitTypeApply(typeApply);
            }

            @Override
            public void visitTypeCast(JCTree.JCTypeCast cast) {
                // Handle type casts (e.g., (String) obj)
                if (cast.clazz.type != null) {
                    collectType(cast.clazz.type);
                }
                super.visitTypeCast(cast);
            }

            @Override
            public void visitTypeTest(JCTree.JCInstanceOf instanceOf) {
                // Handle instanceof checks
                if (instanceOf.clazz.type != null) {
                    collectType(instanceOf.clazz.type);
                }
                super.visitTypeTest(instanceOf);
            }

            @Override
            public void visitTypeArray(JCTree.JCArrayTypeTree arrayType) {
                // Handle array type declarations
                if (arrayType.type != null) {
                    collectType(arrayType.type);
                }
                super.visitTypeArray(arrayType);
            }

            @Override
            public void visitVarDef(JCTree.JCVariableDecl varDecl) {
                // Handle variable declarations
                if (varDecl.vartype != null && varDecl.vartype.type != null) {
                    collectType(varDecl.vartype.type);
                }
                super.visitVarDef(varDecl);
            }

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
                // Handle method declarations
                if (methodDecl.restype != null && methodDecl.restype.type != null) {
                    collectType(methodDecl.restype.type);
                }

                // Process thrown exceptions
                for (JCTree.JCExpression thrown : methodDecl.thrown) {
                    if (thrown.type != null) {
                        collectType(thrown.type);
                    }
                }

                super.visitMethodDef(methodDecl);
            }

            @Override
            public void visitAnnotation(JCTree.JCAnnotation annotation) {
                // Handle annotations
                if (annotation.annotationType.type != null) {
                    collectType(annotation.annotationType.type);
                }
                super.visitAnnotation(annotation);
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
                if (type == null) {
                    return;
                }

                // Collect the main type
                if (type.tsym instanceof Symbol.ClassSymbol) {
                    addJarNameForSymbol.accept(type.tsym);
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
            // Try first pattern for standard jar URLs (jar:file:/path/to/file.jar!/...)
            Matcher matcher = Pattern.compile("([^/]*)?\\.jar!/").matcher(uriStr);
            if (matcher.find()) {
                String jarName = matcher.group(1);
                return jarName.replaceAll("-\\d.*$", "");
            }
            // Try second pattern for ZipFileIndexFileObject format (/path/to/file.jar(...)
            matcher = Pattern.compile("/([^/]*)\\.jar\\(").matcher(uriStr);
            if (matcher.find()) {
                String jarName = matcher.group(1);
                return jarName.replaceAll("-\\d.*$", "");
            }
        }
        return null;
    }
}
