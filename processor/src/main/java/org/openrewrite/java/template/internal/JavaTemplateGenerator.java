package org.openrewrite.java.template.internal;

import org.openrewrite.Cursor;
import org.openrewrite.java.JavaTemplate;

public interface JavaTemplateGenerator {
    JavaTemplate build(Cursor cursor);

    Object[] getParameters(Cursor cursor);
}

