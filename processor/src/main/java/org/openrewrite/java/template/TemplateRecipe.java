package org.openrewrite.java.template;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

@EnableTemplating
public abstract class TemplateRecipe extends Recipe implements Template {
    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TemplateMatcher(this).replaceAll();
    }
}
