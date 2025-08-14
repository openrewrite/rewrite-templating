/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.java.template.processor;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import org.jspecify.annotations.Nullable;
import org.openrewrite.java.template.internal.ImportDetector;
import org.openrewrite.java.template.internal.JavacResolution;
import org.openrewrite.java.template.internal.TemplateCode;
import org.openrewrite.java.template.internal.UsedMethodDetector;

import javax.tools.Diagnostic;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.openrewrite.java.template.processor.RefasterTemplateProcessor.isAnyOfCall;

class TemplateDescriptor {
    private static final ClassValue<List<String>> LST_TYPE_MAP = new ClassValue<List<String>>() {
        @Override
        protected List<String> computeValue(Class<?> type) {
            if (JCTree.JCUnary.class.isAssignableFrom(type)) {
                return singletonList("J.Unary");
            }
            if (JCTree.JCBinary.class.isAssignableFrom(type)) {
                return singletonList("J.Binary");
            }
            if (JCTree.JCMethodInvocation.class.isAssignableFrom(type)) {
                return singletonList("J.MethodInvocation");
            }
            if (JCTree.JCFieldAccess.class.isAssignableFrom(type)) {
                return Arrays.asList("J.FieldAccess", "J.Identifier");
            }
            if (JCTree.JCConditional.class.isAssignableFrom(type)) {
                return singletonList("J.Ternary");
            }
            if (JCTree.JCNewClass.class.isAssignableFrom(type)) {
                return singletonList("J.NewClass");
            }
            if (JCTree.JCLambda.class.isAssignableFrom(type)) {
                return singletonList("J.Lambda");
            }
            if (JCTree.JCExpression.class.isAssignableFrom(type)) {
                // catch all for expressions
                return singletonList("Expression");
            }
            if (JCTree.JCStatement.class.isAssignableFrom(type)) {
                // catch all for statements
                return singletonList("Statement");
            }
            throw new IllegalArgumentException(type.toString());
        }
    };
    private static final Set<Tree.Kind> UNSUPPORTED_STATEMENTS = Stream.of(
            Tree.Kind.DO_WHILE_LOOP,
            Tree.Kind.ENHANCED_FOR_LOOP,
            Tree.Kind.FOR_LOOP,
            Tree.Kind.IF,
            Tree.Kind.SWITCH,
            Tree.Kind.WHILE_LOOP
    ).collect(toSet());

    private final JavacProcessingEnvironment processingEnv;
    private final JCTree.JCCompilationUnit cu;
    public final JCTree.JCClassDecl classDecl;
    public JCTree.JCMethodDecl method;

    public TemplateDescriptor(
            JavacProcessingEnvironment processingEnv,
            JCTree.JCCompilationUnit cu,
            JCTree.JCClassDecl classDecl,
            JCTree.JCMethodDecl method) {
        this.classDecl = classDecl;
        this.method = method;
        this.cu = cu;
        this.processingEnv = processingEnv;
    }

    public int getArity() {
        AtomicReference<JCTree.JCMethodInvocation> anyOfCall = new AtomicReference<>();
        new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                if (isAnyOfCall(jcMethodInvocation)) {
                    anyOfCall.set(jcMethodInvocation);
                    return;
                }
                super.visitApply(jcMethodInvocation);
            }
        }.scan(method);
        return Optional.ofNullable(anyOfCall.get()).map(call -> call.args.size()).orElse(1);
    }

    public Collection<String> getTypes() {
        if (getArity() == 1) {
            JCTree.JCExpression returnExpression = RefasterTemplateProcessor.getReturnExpression(method);
            Class<? extends JCTree> clazz = returnExpression != null ?
                    returnExpression.getClass() :
                    method.getBody().getStatements().last().getClass();
            return LST_TYPE_MAP.get(clazz);
        }
        Set<String> types = new HashSet<>();
        new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                if (isAnyOfCall(jcMethodInvocation)) {
                    for (JCTree.JCExpression argument : jcMethodInvocation.getArguments()) {
                        types.addAll(LST_TYPE_MAP.get(argument.getClass()));
                    }
                    return;
                }
                super.visitApply(jcMethodInvocation);
            }
        }.scan(method);
        return types;
    }

    public String toJavaTemplateBuilder(int pos) {
        JCTree tree = method.getBody().getStatements().get(0);
        if (tree instanceof JCTree.JCReturn) {
            tree = ((JCTree.JCReturn) tree).getExpression();
        }

        List<JCTree.JCTypeParameter> typeParameters = classDecl.typarams == null ? emptyList() : classDecl.typarams;
        return TemplateCode.process(
                tree,
                method.getReturnType().type,
                method.getParameters(),
                typeParameters,
                pos,
                method.restype.type instanceof Type.JCVoidType,
                true,
                true);
    }

    public boolean validate() {
        if (method.typarams != null && !method.typarams.isEmpty()) {
            RefasterTemplateProcessor.printNoteOnce(processingEnv, "Generic type parameters are only allowed at class level", classDecl.sym);
            return false;
        }
        for (JCTree.JCAnnotation annotation : RefasterTemplateProcessor.getTemplateAnnotations(method, RefasterTemplateProcessor.UNSUPPORTED_ANNOTATIONS::contains)) {
            RefasterTemplateProcessor.printNoteOnce(processingEnv, "@" + annotation.annotationType + " is currently not supported", classDecl.sym);
            return false;
        }
        for (JCTree.JCVariableDecl parameter : method.getParameters()) {
            List<? extends AnnotationTree> annotations = ((VariableTree) parameter).getModifiers().getAnnotations();
            for (JCTree.JCAnnotation annotation : RefasterTemplateProcessor.getTemplateAnnotations(annotations, RefasterTemplateProcessor.UNSUPPORTED_ANNOTATIONS::contains)) {
                RefasterTemplateProcessor.printNoteOnce(processingEnv, "@" + annotation.annotationType + " is currently not supported", classDecl.sym);
                return false;
            }
        }
        if (method.body.stats.isEmpty()) {
            return true; // Allow for easy removal of the input template body
        }
        if (method.body.stats.size() > 1) {
            RefasterTemplateProcessor.printNoteOnce(processingEnv, "Multiple statements are currently not supported", classDecl.sym);
            return false;
        }
        if (UNSUPPORTED_STATEMENTS.contains(method.body.stats.get(0).getKind())) {
            RefasterTemplateProcessor.printNoteOnce(processingEnv, method.body.stats.get(0).getKind() + " statements are currently not supported", classDecl.sym);
            return false;
        }
        return new TreeScanner() {
            boolean valid = true;
            int anyOfCount = 0;

            boolean validate(JCTree tree) {
                scan(tree);
                return valid;
            }

            @Override
            public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
                if ("com.google.errorprone.refaster.Refaster".equals(jcFieldAccess.selected.type.tsym.toString()) &&
                        "anyOf".equals(jcFieldAccess.name.toString())) {
                    // exception for `Refaster.anyOf()`
                    if (++anyOfCount > 1) {
                        RefasterTemplateProcessor.printNoteOnce(processingEnv, "Refaster.anyOf() can only be used once per template", classDecl.sym);
                        valid = false;
                    }
                    return;
                }
                super.visitSelect(jcFieldAccess);
            }

            @Override
            public void visitIdent(JCTree.JCIdent jcIdent) {
                if (valid &&
                        jcIdent.sym != null &&
                        jcIdent.sym.packge().getQualifiedName().contentEquals("com.google.errorprone.refaster")) {
                    RefasterTemplateProcessor.printNoteOnce(processingEnv, jcIdent.type.tsym.getQualifiedName() + " is currently not supported", classDecl.sym);
                    valid = false;
                }
            }
        }.validate(method.getBody());
    }

    public boolean resolve() {
        method = resolve(method);
        return method != null;
    }

    private JCTree.@Nullable JCMethodDecl resolve(JCTree.JCMethodDecl method) {
        JavacResolution res = new JavacResolution(processingEnv.getContext());
        try {
            classDecl.defs = classDecl.defs.prepend(method);
            JCTree.JCMethodDecl resolvedMethod = (JCTree.JCMethodDecl) requireNonNull(
                    res.resolveAll(processingEnv.getContext(), cu, singletonList(method)))
                    .get(method);
            classDecl.defs = classDecl.defs.tail;
            resolvedMethod.params = method.params;
            return resolvedMethod;
        } catch (Throwable t) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Had trouble type attributing the template method: " + method.name);
        }
        return null;
    }

    public List<Symbol.ClassSymbol> usedTypes(int i) {
        List<Symbol> imports;
        if (getArity() == 1) {
            imports = ImportDetector.imports(method);
        } else {
            Set<JCTree> skip = new HashSet<>();
            new TreeScanner() {
                @Override
                public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                    if (isAnyOfCall(jcMethodInvocation)) {
                        for (int j = 0; j < jcMethodInvocation.args.size(); j++) {
                            if (j != i) {
                                skip.add(jcMethodInvocation.args.get(j));
                            }
                        }
                        return;
                    }
                    super.visitApply(jcMethodInvocation);
                }
            }.scan(method);
            imports = ImportDetector.imports(method, t -> !skip.contains(t));
        }
        return imports.stream().filter(Symbol.ClassSymbol.class::isInstance).map(Symbol.ClassSymbol.class::cast).collect(toList());
    }

    public List<Symbol> usedMembers(int i) {
        List<Symbol> imports;
        if (getArity() == 1) {
            imports = ImportDetector.imports(method);
        } else {
            Set<JCTree> skip = new HashSet<>();
            new TreeScanner() {
                @Override
                public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                    if (isAnyOfCall(jcMethodInvocation)) {
                        for (int j = 0; j < jcMethodInvocation.args.size(); j++) {
                            if (j != i) {
                                skip.add(jcMethodInvocation.args.get(j));
                            }
                        }
                        return;
                    }
                    super.visitApply(jcMethodInvocation);
                }
            }.scan(method);
            imports = ImportDetector.imports(method, t -> !skip.contains(t));
        }
        return imports.stream().filter(sym -> sym instanceof Symbol.VarSymbol || sym instanceof Symbol.MethodSymbol).collect(toList());
    }

    public List<Symbol.MethodSymbol> usedMethods(int i) {
        if (getArity() == 1) {
            return UsedMethodDetector.usedMethods(method);
        }
        Set<JCTree> skip = new HashSet<>();
        new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                if (isAnyOfCall(jcMethodInvocation)) {
                    for (int j = 0; j < jcMethodInvocation.args.size(); j++) {
                        if (j != i) {
                            skip.add(jcMethodInvocation.args.get(j));
                        }
                    }
                    return;
                }
                super.visitApply(jcMethodInvocation);
            }
        }.scan(method);
        return UsedMethodDetector.usedMethods(method, t -> !skip.contains(t));
    }
}
