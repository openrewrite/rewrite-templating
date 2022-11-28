package org.openrewrite.java.template.internal;

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import org.openrewrite.Cursor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.template.AutoTemplate;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;

// maybe don't need EnableTemplating?
// see https://chariotsolutions.com/blog/post/changing-java-8-handling-nulls-ast/

// https://medium.com/@joachim.beckers/debugging-an-annotation-processor-using-intellij-idea-in-2018-cde72758b78a
@SupportedAnnotationTypes("org.openrewrite.java.template.EnableTemplating")
public class TemplateProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnv;
    private JavacProcessingEnvironment javacProcessingEnv;
    private Trees trees;

    @Nullable
    private final String javaFileContent;

    public TemplateProcessor(@Nullable String javaFileContent) {
        this.javaFileContent = javaFileContent;
    }

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
        JavacResolution res = new JavacResolution(context);

        new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation tree) {
                JCTree.JCExpression jcSelect = tree.getMethodSelect();
                String name = jcSelect instanceof JCTree.JCFieldAccess ?
                        ((JCTree.JCFieldAccess) jcSelect).name.toString() :
                        ((JCTree.JCIdent) jcSelect).getName().toString();

                if (name.equals("compile") && tree.getArguments().size() == 2) {
                    JCTree.JCMethodInvocation resolvedMethod = (JCTree.JCMethodInvocation) res.resolveAll(context, cu, singletonList(tree))
                            .get(tree);

                    if (resolvedMethod.type.tsym instanceof Symbol.ClassSymbol &&
                        AutoTemplate.Builder.class.getName().replace('$', '.')
                                .equals(((Symbol.ClassSymbol) resolvedMethod.type.tsym).fullname.toString()) &&
                        tree.getArguments().get(1) instanceof JCTree.JCLambda) {

                        JCTree.JCLambda template = (JCTree.JCLambda) tree.getArguments().get(1);
                        Map<JCTree, JCTree> parameterResolution = res.resolveAll(context, cu, template.getParameters());
                        List<JCTree.JCVariableDecl> parameters = new ArrayList<>(template.getParameters().size());
                        for (VariableTree p : template.getParameters()) {
                            parameters.add((JCTree.JCVariableDecl) parameterResolution.get(p));
                        }
                        JCTree.JCLambda resolvedTemplate = (JCTree.JCLambda) parameterResolution.get(template);

                        Map<Integer, JCTree.JCVariableDecl> parameterPositions = new HashMap<>();
                        new TreeScanner() {
                            @Override
                            public void visitIdent(JCTree.JCIdent ident) {
                                for (JCTree.JCVariableDecl parameter : parameters) {
                                    if (parameter.sym == ident.sym) {
                                        parameterPositions.put(ident.getStartPosition(), parameter);
                                    }
                                }
                            }
                        }.scan(resolvedTemplate.getBody());

                        JCTree.JCClassDecl classDecl = cursor(cu, template)
                                .getPathAsStream()
                                .filter(JCTree.JCClassDecl.class::isInstance)
                                .map(JCTree.JCClassDecl.class::cast)
                                .reduce((next, acc) -> next)
                                .orElseThrow();

                        try (InputStream inputStream = javaFileContent == null ?
                                cu.getSourceFile().openInputStream() : new ByteArrayInputStream(javaFileContent.getBytes())) {
                            inputStream.skip(template.getBody().getStartPosition());
                            byte[] templateSourceBytes = new byte[template.getBody().getEndPosition(cu.endPositions) - template.getBody().getStartPosition()];
                            inputStream.read(templateSourceBytes);
                            String templateSource = new String(templateSourceBytes);

                            for (Map.Entry<Integer, JCTree.JCVariableDecl> paramPos : parameterPositions.entrySet()) {
                                templateSource = templateSource.substring(0, paramPos.getKey() - template.getBody().getStartPosition()) +
                                                 "#{any(" + paramPos.getValue().type.toString() + ")}" +
                                                 templateSource.substring((paramPos.getKey() - template.getBody().getStartPosition()) +
                                                                          paramPos.getValue().name.length());
                            }

                            JCTree.JCLiteral templateName = (JCTree.JCLiteral) tree.getArguments().get(0);
                            if(templateName.value == null) {
                                processingEnv.getMessager().printMessage(Kind.WARNING, "Can't compile a template with a null name.");
                                return;
                            }

                            String templateClassName =  classDecl.sym.fullname.toString()+ "_" + templateName.getValue().toString();
                            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(templateClassName);
                            try (Writer out = builderFile.openWriter()) {
                                out.write("package " + classDecl.sym.packge().toString() + ";\n");
                                out.write("import org.openrewrite.java.*;\n");
                                out.write("public class " + classDecl.sym.getSimpleName().toString() + "_" + templateName.getValue() + " {\n");
                                out.write("    public static JavaTemplate getTemplate(JavaVisitor<?> visitor) {\n");
                                out.write("        return JavaTemplate\n");
                                out.write("                .builder(visitor::getCursor, \"" + templateSource + "\")\n");
                                out.write("                .build();\n");
                                out.write("    }\n");
                                out.write("}\n");
                                out.flush();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                super.visitApply(tree);
            }
        }.scan(cu);
    }

    private Cursor cursor(JCCompilationUnit cu, Tree t) {
        AtomicReference<Cursor> matching = new AtomicReference<>();
        new TreePathScanner<Cursor, Cursor>() {
            @Override
            public Cursor scan(Tree tree, Cursor parent) {
                Cursor cursor = new Cursor(parent, tree);
                if(tree == t) {
                    matching.set(cursor);
                    return cursor;
                }
                return super.scan(tree, cursor);
            }
        }.scan(cu, null);
        return matching.get();
    }

    @Nullable
    private JCCompilationUnit toUnit(Element element) {
        TreePath path = null;
        if (trees != null) {
            try {
                path = trees.getPath(element);
            } catch (NullPointerException ignore) {
                // Happens if a package-info.java dowsn't contain a package declaration.
                // We can safely ignore those, since they do not need any processing
            }
        }
        if (path == null) return null;

        return (JCCompilationUnit) path.getCompilationUnit();
    }

    /**
     * This class casts the given processing environment to a JavacProcessingEnvironment. In case of
     * gradle incremental compilation, the delegate ProcessingEnvironment of the gradle wrapper is returned.
     */
    @Nullable
    public JavacProcessingEnvironment getJavacProcessingEnvironment(Object procEnv) {
        if (procEnv instanceof JavacProcessingEnvironment) return (JavacProcessingEnvironment) procEnv;

        // try to find a "delegate" field in the object, and use this to try to obtain a JavacProcessingEnvironment
        for (Class<?> procEnvClass = procEnv.getClass(); procEnvClass != null; procEnvClass = procEnvClass.getSuperclass()) {
            Object delegate = tryGetDelegateField(procEnvClass, procEnv);
            if (delegate == null) delegate = tryGetProxyDelegateToField(procEnv);
            if (delegate == null) delegate = tryGetProcessingEnvField(procEnvClass, procEnv);

            if (delegate != null) return getJavacProcessingEnvironment(delegate);
            // delegate field was not found, try on superclass
        }

        processingEnv.getMessager().printMessage(Kind.WARNING, "Can't get the delegate of the gradle " +
                                                               "IncrementalProcessingEnvironment. " +
                                                               "OpenRewrite's template processor won't work.");
        return null;
    }

    /**
     * Gradle incremental processing
     */
    @Nullable
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
    @Nullable
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
    @Nullable
    private Object tryGetProxyDelegateToField(Object instance) {
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            return Permit.getField(handler.getClass(), "val$delegateTo").get(handler);
        } catch (Exception e) {
            return null;
        }
    }
}
