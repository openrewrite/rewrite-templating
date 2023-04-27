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
package org.openrewrite.java.template;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import org.openrewrite.java.template.internal.ImportDetector;
import org.openrewrite.java.template.internal.JavacResolution;
import org.openrewrite.java.template.internal.Permit;
import org.openrewrite.java.template.internal.permit.Parent;
import sun.misc.Unsafe;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.openrewrite.java.template.RefasterTemplateProcessor.AFTER_TEMPLATE;
import static org.openrewrite.java.template.RefasterTemplateProcessor.BEFORE_TEMPLATE;

/**
 * For steps to debug this annotation processor, see
 * <a href="https://medium.com/@joachim.beckers/debugging-an-annotation-processor-using-intellij-idea-in-2018-cde72758b78a">this blog post</a>.
 */
@SupportedAnnotationTypes({BEFORE_TEMPLATE, AFTER_TEMPLATE})
public class RefasterTemplateProcessor extends AbstractProcessor {
    public static final String BEFORE_TEMPLATE = "com.google.errorprone.refaster.annotation.BeforeTemplate";
    public static final String AFTER_TEMPLATE = "com.google.errorprone.refaster.annotation.AfterTemplate";
    static final String PRIMITIVE_ANNOTATION = "@Primitive";
    static final Map<String, String> PRIMITIVE_TYPE_MAP = new HashMap<>();

    static {
        PRIMITIVE_TYPE_MAP.put(boolean.class.getName(), Boolean.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(byte.class.getName(), Byte.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(char.class.getName(), Character.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(short.class.getName(), Short.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(int.class.getName(), Integer.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(long.class.getName(), Long.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(float.class.getName(), Float.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(double.class.getName(), Double.class.getSimpleName());
        PRIMITIVE_TYPE_MAP.put(void.class.getName(), Void.class.getSimpleName());
    }

    // for now excluding assignment expressions and prefix and postfix -- and ++
    static Set<Class<? extends JCTree>> EXPRESSION_STATEMENT_TYPES = Stream.of(
            JCTree.JCMethodInvocation.class,
            JCTree.JCNewClass.class).collect(Collectors.toSet());

    static ClassValue<String> LST_TYPE_MAP = new ClassValue<String>() {
        @Override
        protected String computeValue(Class<?> type) {
            if (JCTree.JCUnary.class.isAssignableFrom(type)) {
                return "Unary";
            } else if (JCTree.JCBinary.class.isAssignableFrom(type)) {
                return "Binary";
            } else if (JCTree.JCMethodInvocation.class.isAssignableFrom(type)) {
                return "MethodInvocation";
            } else if (JCTree.JCExpression.class.isAssignableFrom(type)) {
                return "Expression";
            } else if (JCTree.JCStatement.class.isAssignableFrom(type)) {
                return "Statement";
            }
            throw new IllegalArgumentException(type.toString());
        }
    };

    private ProcessingEnvironment processingEnv;
    private JavacProcessingEnvironment javacProcessingEnv;
    private Trees trees;

    /**
     * We just return the latest version of whatever JDK we run on. Stupid? Yeah, but it's either that
     * or warnings on all versions but 1.
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.javacProcessingEnv = getJavacProcessingEnvironment(processingEnv);
        if (javacProcessingEnv == null) {
            return;
        }
        trees = Trees.instance(javacProcessingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        TypeElement beforeTemplateType = processingEnv.getElementUtils().getTypeElement("com.google.errorprone.refaster.annotation.BeforeTemplate");
//        TypeElement afterTemplateType = processingEnv.getElementUtils().getTypeElement("com.google.errorprone.refaster.annotation.AfterTemplate");
//        roundEnv.getElementsAnnotatedWith(beforeTemplateType).forEach(e -> {
//            processingEnv.getMessager().printMessage(Kind.NOTE, "Found @BeforeTemplate: " + e);
//        });

        for (Element element : roundEnv.getRootElements()) {
            JCCompilationUnit jcCompilationUnit = toUnit(element);
            if (jcCompilationUnit != null) {
                maybeGenerateTemplateSources(jcCompilationUnit);
            }
        }

        return true;
    }

    void maybeGenerateTemplateSources(JCCompilationUnit cu) {
        Context context = javacProcessingEnv.getContext();

        new TreeScanner() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl tree) {
                super.visitClassDef(tree);
                TemplateDescriptor descriptor = getTemplateDescriptor(tree, context);
                if (descriptor != null) {
                    try {
                        descriptor.resolve(context, cu);
                    } catch (Throwable t) {
                        processingEnv.getMessager().printMessage(Kind.WARNING, "Had trouble type attributing the template.");
                        return;
                    }

                    TreeMaker treeMaker = TreeMaker.instance(context).forToplevel(cu);
                    List<JCTree> membersWithoutConstructor = tree.getMembers().stream()
                            .filter(m -> !(m instanceof JCTree.JCMethodDecl) || !((JCTree.JCMethodDecl) m).name.contentEquals("<init>"))
                            .collect(Collectors.toList());
                    JCTree.JCClassDecl copy = treeMaker.ClassDef(tree.mods, tree.name, tree.typarams, tree.extending, tree.implementing, com.sun.tools.javac.util.List.from(membersWithoutConstructor));

                    processingEnv.getMessager().printMessage(Kind.NOTE, "Generating template for " + descriptor.classDecl.getSimpleName());

                    String templateName = tree.sym.fullname.toString().substring(tree.sym.packge().fullname.length() + 1);
                    String templateFqn = tree.sym.fullname.toString() + "Recipe";
                    String templateCode = copy.toString().trim();
                    String displayName = cu.docComments.getComment(tree) != null ? cu.docComments.getComment(tree).getText().trim() : "Refaster template `" + templateName + '`';
                    if (displayName.endsWith(".")) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }

                    int paramCount = descriptor.afterTemplate.params.size();

                    Set<String> imports = new TreeSet<>();
                    imports.addAll(ImportDetector.imports(descriptor.beforeTemplates.get(0)));
                    imports.addAll(ImportDetector.imports(descriptor.afterTemplate));
                    imports.removeIf(i -> "java.lang".equals(i.substring(0, i.lastIndexOf('.'))));
                    imports.remove(BEFORE_TEMPLATE);
                    imports.remove(AFTER_TEMPLATE);

                    try {
                        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(templateFqn);
                        try (Writer out = builderFile.openWriter()) {
                            out.write("package " + tree.sym.packge().toString() + ";\n");
                            out.write("\n");
                            out.write("import org.openrewrite.ExecutionContext;\n");
                            out.write("import org.openrewrite.Recipe;\n");
                            out.write("import org.openrewrite.TreeVisitor;\n");
                            out.write("import org.openrewrite.java.JavaTemplate;\n");
                            out.write("import org.openrewrite.java.JavaVisitor;\n");
                            out.write("import org.openrewrite.java.template.Primitive;\n");
                            out.write("import org.openrewrite.java.tree.*;\n");
                            out.write("\n");
                            for (String anImport : imports) {
                                out.write("import " + anImport + ";\n");
                            }
                            out.write("\n");
                            out.write("public class " + templateFqn.substring(templateFqn.lastIndexOf('.') + 1) + " extends Recipe {\n");
                            out.write("\n");
                            out.write("    @Override\n");
                            out.write("    public String getDisplayName() {\n");
                            out.write("        return \"" + escape(displayName) + "\";\n");
                            out.write("    }\n");
                            out.write("\n");
                            out.write("    @Override\n");
                            out.write("    public String getDescription() {\n");
                            out.write("        return \"Recipe created for the following Refaster template:\\n```\\n" + escape(templateCode) + "\\n```.\";\n");
                            out.write("    }\n");
                            out.write("\n");
                            out.write("    @Override\n");
                            out.write("    protected TreeVisitor<?, ExecutionContext> getVisitor() {\n");
                            out.write("        return new JavaVisitor<ExecutionContext>() {\n");
                            out.write("            final JavaTemplate before0 = JavaTemplate.compile(this, \""
                                      + descriptor.beforeTemplates.get(0).getName().toString() + "\", "
                                      + toLambda(descriptor.beforeTemplates.get(0)) + ").build();\n");
                            out.write("            final JavaTemplate after = JavaTemplate.compile(this, \""
                                      + descriptor.afterTemplate.getName().toString() + "\", "
                                      + toLambda(descriptor.afterTemplate) + ").build();\n");
                            out.write("\n");

                            String lstType = LST_TYPE_MAP.get(getType(descriptor.beforeTemplates.get(0)));
                            if ("Statement".equals(lstType)) {
                                out.write("            @Override\n");
                                out.write("            public J visitStatement(Statement statement, ExecutionContext ctx) {\n");
                                out.write("                if (statement instanceof J.Block) {;\n");
                                out.write("                    // FIXME workaround\n");
                                out.write("                    return statement;\n");
                                out.write("                }\n");
                                out.write("                JavaTemplate.Matcher matcher = before0.matcher(statement);\n");
                                out.write("                if (matcher.find()) {\n");
                                out.write("                    return statement.withTemplate(after, statement.getCoordinates().replace(), " + parameters(descriptor) + ");\n");
                                out.write("                }\n");
                                out.write("                return super.visitStatement(statement, ctx);\n");
                                out.write("            }\n");
                            } else if ("Expression".equals(lstType)) {
                                out.write("            @Override\n");
                                out.write("            public J visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {\n");
                                out.write("                // FIXME workaround\n");
                                out.write("                return identifier;\n");
                                out.write("            }\n");
                                out.write("\n");
                                out.write("            @Override\n");
                                out.write("            public J visitExpression(Expression expression, ExecutionContext ctx) {\n");
                                out.write("                JavaTemplate.Matcher matcher = before0.matcher(expression);\n");
                                out.write("                if (matcher.find()) {\n");
                                out.write("                    return expression.withTemplate(after, expression.getCoordinates().replace(), " + parameters(descriptor) + ");\n");
                                out.write("                }\n");
                                out.write("                return super.visitExpression(expression, ctx);\n");
                                out.write("            }\n");
                            } else {
                                out.write("            @Override\n");
                                out.write("            public J visit" + lstType + "(J." + lstType + " elem, ExecutionContext ctx) {\n");
                                out.write("                JavaTemplate.Matcher matcher = before0.matcher(elem);\n");
                                out.write("                if (matcher.find()) {\n");
                                out.write("                    return elem.withTemplate(after, elem.getCoordinates().replace(), " + parameters(descriptor) + ");\n");
                                out.write("                }\n");
                                out.write("                return super.visit" + lstType + "(elem, ctx);\n");
                                out.write("            }\n");
                            }
                            out.write("        };\n");
                            out.write("    }\n");
                            out.write("}\n");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.scan(cu);
    }

    private static String lambdaCastType(Class<? extends JCTree> type, int paramCount) {
        boolean asFunction = JCTree.JCExpression.class.isAssignableFrom(type);
        StringJoiner joiner = new StringJoiner(", ", "<", ">");
        for (int i = 0; i < (asFunction ? paramCount + 1 : paramCount); i++) {
            joiner.add("?");
        }
        return "(JavaTemplate." + (asFunction ? 'F' : 'P') + paramCount + joiner + ") ";
    }

    private String escape(String string) {
        return string.replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String parameters(TemplateDescriptor descriptor) {
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < descriptor.afterTemplate.getParameters().size(); i++) {
            joiner.add("matcher.parameter(" + i + ")");
        }
        return joiner.toString();
    }

    private Class<? extends JCTree> getType(JCTree.JCMethodDecl method) {
        JCTree.JCStatement statement = method.getBody().getStatements().get(0);
        Class<? extends JCTree> type = statement.getClass();
        if (statement instanceof JCTree.JCReturn) {
            type = ((JCTree.JCReturn) statement).expr.getClass();
        } else if (statement instanceof JCTree.JCExpressionStatement) {
            type = ((JCTree.JCExpressionStatement) statement).expr.getClass();
        }
        return type;
    }

    private String toLambda(JCTree.JCMethodDecl method) {
        StringBuilder builder = new StringBuilder();

        Class<? extends JCTree> type = getType(method);
        if (EXPRESSION_STATEMENT_TYPES.contains(type)) {
            builder.append(lambdaCastType(type, method.params.size()));
        }

        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        for (JCTree.JCVariableDecl parameter : method.getParameters()) {
            String paramType = parameter.getType().type.tsym.getQualifiedName().toString();
            if (PRIMITIVE_TYPE_MAP.containsKey(paramType)) {
                paramType = PRIMITIVE_ANNOTATION + ' ' + PRIMITIVE_TYPE_MAP.get(paramType);
            } else if (paramType.startsWith("java.lang.")) {
                paramType = paramType.substring("java.lang.".length());
            }
            joiner.add(paramType + " " + parameter.getName());
        }
        builder.append(joiner);
        builder.append(" -> ");

        JCTree.JCStatement statement = method.getBody().getStatements().get(0);
        if (statement instanceof JCTree.JCReturn) {
            builder.append(((JCTree.JCReturn) statement).getExpression().toString());
        } else {
            String string = statement.toString();
            builder.append(string, 0, string.length() - 1);
        }
        return builder.toString();
    }

    private TemplateDescriptor getTemplateDescriptor(JCTree.JCClassDecl tree, Context context) {
        TemplateDescriptor result = new TemplateDescriptor(tree);
        for (JCTree member : tree.getMembers()) {
            if (member instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) member;
                List<JCTree.JCIdent> annotations = getTemplateAnnotations(method);
                if (annotations.stream().anyMatch(a -> a.sym.getQualifiedName().toString().equals(BEFORE_TEMPLATE))) {
                    result.beforeTemplate(method);
                } else if (annotations.stream().anyMatch(a -> a.sym.getQualifiedName().toString().equals(AFTER_TEMPLATE))) {
                    result.afterTemplate(method);
                }
            }
        }
        return result.validate();
    }

    static class TemplateDescriptor {
        private final JCTree.JCClassDecl classDecl;
        List<JCTree.JCMethodDecl> beforeTemplates;
        JCTree.JCMethodDecl afterTemplate;

        public TemplateDescriptor(JCTree.JCClassDecl classDecl) {
            this.classDecl = classDecl;
        }

        private TemplateDescriptor validate() {
            return beforeTemplates != null && afterTemplate != null ? this : null;
        }

        public void beforeTemplate(JCTree.JCMethodDecl method) {
            if (beforeTemplates == null) {
                beforeTemplates = new ArrayList<>();
            }
            beforeTemplates.add(method);
        }

        public void afterTemplate(JCTree.JCMethodDecl method) {
            afterTemplate = method;
        }

        public void resolve(Context context, JCCompilationUnit cu) {
            JavacResolution res = new JavacResolution(context);
            beforeTemplates.replaceAll(key -> {
                Map<JCTree, JCTree> resolved = res.resolveAll(context, cu, singletonList(key));
                return (JCTree.JCMethodDecl) resolved.get(key);
            });
            Map<JCTree, JCTree> resolved = res.resolveAll(context, cu, singletonList(afterTemplate));
            afterTemplate = (JCTree.JCMethodDecl) resolved.get(afterTemplate);
        }

        public boolean isExpression() {
            return afterTemplate.getReturnType().type.getKind() != TypeKind.VOID;
        }
    }

    private static List<JCTree.JCIdent> getTemplateAnnotations(JCTree.JCMethodDecl method) {
        return method.getModifiers().getAnnotations().stream()
                .filter(a -> a.getTag() == JCTree.Tag.ANNOTATION)
                .map(JCTree.JCAnnotation::getAnnotationType)
                .filter(a -> a.getKind() == Tree.Kind.IDENTIFIER)
                .map(JCTree.JCIdent.class::cast)
                .filter(i -> i.sym != null && i.sym.getQualifiedName().toString().startsWith("com.google.errorprone.refaster.annotation."))
                .collect(Collectors.toList());
    }

    private JCCompilationUnit toUnit(Element element) {
        TreePath path = null;
        if (trees != null) {
            try {
                path = trees.getPath(element);
            } catch (NullPointerException ignore) {
                // Happens if a package-info.java doesn't contain a package declaration.
                // We can safely ignore those, since they do not need any processing
            }
        }
        if (path == null) {
            return null;
        }

        return (JCCompilationUnit) path.getCompilationUnit();
    }

    /**
     * This class casts the given processing environment to a JavacProcessingEnvironment. In case of
     * gradle incremental compilation, the delegate ProcessingEnvironment of the gradle wrapper is returned.
     */
    public JavacProcessingEnvironment getJavacProcessingEnvironment(Object procEnv) {
        addOpens();
        if (procEnv instanceof JavacProcessingEnvironment) {
            return (JavacProcessingEnvironment) procEnv;
        }

        // try to find a "delegate" field in the object, and use this to try to obtain a JavacProcessingEnvironment
        for (Class<?> procEnvClass = procEnv.getClass(); procEnvClass != null; procEnvClass = procEnvClass.getSuperclass()) {
            Object delegate = tryGetDelegateField(procEnvClass, procEnv);
            if (delegate == null) {
                delegate = tryGetProxyDelegateToField(procEnv);
            }
            if (delegate == null) {
                delegate = tryGetProcessingEnvField(procEnvClass, procEnv);
            }

            if (delegate != null) {
                return getJavacProcessingEnvironment(delegate);
            }
            // delegate field was not found, try on superclass
        }

        processingEnv.getMessager().printMessage(Kind.WARNING, "Can't get the delegate of the gradle " +
                                                               "IncrementalProcessingEnvironment. " +
                                                               "OpenRewrite's template processor won't work.");
        return null;
    }

    private static void addOpens() {
        Class<?> cModule;
        try {
            cModule = Class.forName("java.lang.Module");
        } catch (ClassNotFoundException e) {
            return; //jdk8-; this is not needed.
        }

        Unsafe unsafe = getUnsafe();
        Object jdkCompilerModule = getJdkCompilerModule();
        Object ownModule = getOwnModule();
        String[] allPkgs = {
                "com.sun.tools.javac.code",
                "com.sun.tools.javac.comp",
                "com.sun.tools.javac.file",
                "com.sun.tools.javac.main",
                "com.sun.tools.javac.model",
                "com.sun.tools.javac.parser",
                "com.sun.tools.javac.processing",
                "com.sun.tools.javac.tree",
                "com.sun.tools.javac.util",
                "com.sun.tools.javac.jvm",
        };

        try {
            Method m = cModule.getDeclaredMethod("implAddOpens", String.class, cModule);
            long firstFieldOffset = getFirstFieldOffset(unsafe);
            unsafe.putBooleanVolatile(m, firstFieldOffset, true);
            for (String p : allPkgs) m.invoke(jdkCompilerModule, p, ownModule);
        } catch (Exception ignore) {
        }
    }

    private static long getFirstFieldOffset(Unsafe unsafe) {
        try {
            return unsafe.objectFieldOffset(Parent.class.getDeclaredField("first"));
        } catch (NoSuchFieldException e) {
            // can't happen.
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            // can't happen
            throw new RuntimeException(e);
        }
    }

    private static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getOwnModule() {
        try {
            Method m = Permit.getMethod(Class.class, "getModule");
            return m.invoke(RefasterTemplateProcessor.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getJdkCompilerModule() {
        // call public api: ModuleLayer.boot().findModule("jdk.compiler").get();
        // but use reflection because we don't want this code to crash on jdk1.7 and below.
        // In that case, none of this stuff was needed in the first place, so we just exit via
        // the catch block and do nothing.
        try {
            Class<?> cModuleLayer = Class.forName("java.lang.ModuleLayer");
            Method mBoot = cModuleLayer.getDeclaredMethod("boot");
            Object bootLayer = mBoot.invoke(null);
            Class<?> cOptional = Class.forName("java.util.Optional");
            Method mFindModule = cModuleLayer.getDeclaredMethod("findModule", String.class);
            Object oCompilerO = mFindModule.invoke(bootLayer, "jdk.compiler");
            return cOptional.getDeclaredMethod("get").invoke(oCompilerO);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gradle incremental processing
     */
    private Object tryGetDelegateField(Class<?> delegateClass, Object instance) {
        try {
            return Permit.getField(delegateClass, "delegate").get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Kotlin incremental processing
     */
    private Object tryGetProcessingEnvField(Class<?> delegateClass, Object instance) {
        try {
            return Permit.getField(delegateClass, "processingEnv").get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * IntelliJ >= 2020.3
     */
    private Object tryGetProxyDelegateToField(Object instance) {
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            return Permit.getField(handler.getClass(), "val$delegateTo").get(handler);
        } catch (Exception e) {
            return null;
        }
    }
}
