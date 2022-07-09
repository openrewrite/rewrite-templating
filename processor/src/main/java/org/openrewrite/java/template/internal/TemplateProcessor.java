package org.openrewrite.java.template.internal;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.internal.JavaTypeCache;
import org.openrewrite.java.isolated.ReloadableJava11ParserVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.util.Collections.emptyList;

// https://medium.com/@joachim.beckers/debugging-an-annotation-processor-using-intellij-idea-in-2018-cde72758b78a
@SupportedAnnotationTypes("org.openrewrite.java.template.EnableTemplating")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TemplateProcessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnv;
    private JavacProcessingEnvironment javacProcessingEnv;
    //    private JavacFiler javacFiler;
    private Trees trees;

    // map to rewrite AST -> print -> replace parameters with variables

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.processingEnv = processingEnv;
        this.javacProcessingEnv = getJavacProcessingEnvironment(processingEnv);
        if (javacProcessingEnv == null) {
            return;
        }

//        this.javacFiler = getJavacFiler(procEnv.getFiler());

        trees = Trees.instance(javacProcessingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            JCCompilationUnit jcCompilationUnit = toUnit(element);
            if (jcCompilationUnit != null) {
                try (InputStream is = jcCompilationUnit.getSourceFile().openInputStream()) {
                    Path sourcePath;
                    try {
                        sourcePath = Paths.get(jcCompilationUnit.getSourceFile().toUri());
                    } catch (IllegalArgumentException e) {
                        sourcePath = Paths.get(jcCompilationUnit.getSourceFile().toUri().toString());
                    }

                    ReloadableJava11ParserVisitor parser = new ReloadableJava11ParserVisitor(
                            sourcePath,
                            null,
                            new EncodingDetectingInputStream(is),
                            emptyList(),
                            new JavaTypeCache(),
                            new InMemoryExecutionContext(),
                            javacProcessingEnv.getContext()
                    );

                    J.CompilationUnit cu = (J.CompilationUnit) parser.scan(jcCompilationUnit, Space.EMPTY);
                    maybeGenerateTemplateSources(cu, element);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    void maybeGenerateTemplateSources(J.CompilationUnit cu, Element element) {
        new JavaIsoVisitor<Integer>() {
            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, Integer integer) {
                for (J.Annotation ann : method.getAllAnnotations()) {
                    if (TypeUtils.isOfClassType(ann.getType(), "org.openrewrite.java.template.After")) {
                        J.ClassDeclaration classDeclaration = getCursor().firstEnclosingOrThrow(J.ClassDeclaration.class);

                        // FIXME collect up @Before and generate semantic equality comparison

                        JavaType.FullyQualified classType = classDeclaration.getType();
                        if (classType != null) {
                            String generatedName = classType.getPackageName() + ".Generated" + classType.getClassName();
                            try {
                                JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(generatedName, element);
                                try (Writer writer = sourceFile.openWriter()) {
                                    writer.write("" +
                                            "package " + classType.getPackageName() + ";\n" +
                                            "class Generated" + classType.getClassName() + "{\n"
                                            + "}");
                                }
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                        break;
                    }
                }
                return super.visitMethodDeclaration(method, integer);
            }
        }.visit(cu, 0);
    }

    @Nullable
    private JCCompilationUnit toUnit(Element element) {
        TreePath path = null;
        if (trees != null) {
            try {
                path = trees.getPath(element);
            } catch (NullPointerException ignore) {
                // Happens if a package-info.java dowsn't conatin a package declaration.
                // https://github.com/rzwitserloot/lombok/issues/2184
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
            if (delegate == null) delegate = tryGetProxyDelegateToField(procEnvClass, procEnv);
            if (delegate == null) delegate = tryGetProcessingEnvField(procEnvClass, procEnv);

            if (delegate != null) return getJavacProcessingEnvironment(delegate);
            // delegate field was not found, try on superclass
        }

        processingEnv.getMessager().printMessage(Kind.WARNING,
                "Can't get the delegate of the gradle IncrementalProcessingEnvironment. OpenRewrite's template processor won't work.");
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
     * Kotlin incremental processing
     */
    @Nullable
    private Object tryGetFilerField(Class<?> delegateClass, Object instance) {
        try {
            return Permit.getField(delegateClass, "filer").get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * IntelliJ >= 2020.3
     */
    @Nullable
    private Object tryGetProxyDelegateToField(Class<?> delegateClass, Object instance) {
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            return Permit.getField(handler.getClass(), "val$delegateTo").get(handler);
        } catch (Exception e) {
            return null;
        }
    }
}
