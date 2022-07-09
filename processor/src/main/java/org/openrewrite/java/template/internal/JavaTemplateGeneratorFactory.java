package org.openrewrite.java.template.internal;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

public class JavaTemplateGeneratorFactory extends JavaIsoVisitor<ExecutionContext> {

    @Override
    public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
        return super.visitNewClass(newClass, ctx);
    }

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
        return super.visitClassDeclaration(classDecl, executionContext);
    }
}

class SampleGeneratedTemplate implements JavaTemplateGenerator {
    @Override
    public JavaTemplate build(Cursor cursor) {
        return JavaTemplate.builder(() -> cursor, "#{any(java.lang.String)}.isEmpty()")
                .build();
    }

    @Override
    public Object[] getParameters(Cursor cursor) {
        return new Object[] { cursor.getValue() };
    }
}
