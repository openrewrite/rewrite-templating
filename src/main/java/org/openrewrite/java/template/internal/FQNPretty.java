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
package org.openrewrite.java.template.internal;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

public class FQNPretty extends Pretty {
    private FQNPretty(Writer writer) {
        super(writer, false);
    }

    public static String toString(JCTree tree) {
        StringWriter writer = new StringWriter();
        try {
            new FQNPretty(writer).printExpr(tree);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return writer.toString();
    }

    @Override
    public void visitIdent(JCTree.JCIdent jcIdent) {
        try {
            if (jcIdent.sym.getQualifiedName().toString().startsWith("java.lang.")) {
                print(jcIdent.name);
            } else {
                print(jcIdent.sym.getQualifiedName());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
