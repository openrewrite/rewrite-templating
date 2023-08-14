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
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.TreeScanner;

import javax.tools.JavaFileObject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClasspathJarNameDetector {

    /**
     * Locate types that are directly referred to by name in the
     * given tree and therefore need an import in the template.
     *
     * @return The list of imports to add.
     */
    public static String classpathFor(JCTree input, List<Symbol> imports) {
        Set<String> jarNames = new LinkedHashSet<>();

        for (Symbol anImport : imports) {
            jarNames.add(jarNameFor(anImport));
        }

        new TreeScanner() {
            @Override
            public void scan(JCTree tree) {
                JCTree maybeFieldAccess = tree;
                if (maybeFieldAccess instanceof JCFieldAccess &&
                    ((JCFieldAccess) maybeFieldAccess).sym instanceof Symbol.ClassSymbol &&
                    Character.isUpperCase(((JCFieldAccess) maybeFieldAccess).getIdentifier().toString().charAt(0))) {
                    while (maybeFieldAccess instanceof JCFieldAccess) {
                        maybeFieldAccess = ((JCFieldAccess) maybeFieldAccess).getExpression();
                        if (maybeFieldAccess instanceof JCIdent &&
                            Character.isUpperCase(((JCIdent) maybeFieldAccess).getName().toString().charAt(0))) {
                            // this might be a fully qualified type name, so we don't want to add an import for it
                            // and returning will skip the nested identifier which represents just the class simple name
                            ;//return;
                        }
                    }
                }

                super.scan(tree);
            }
        }.scan(input);

        return jarNames.stream()
                .filter(Objects::nonNull)
                .map(jarName -> '"' + jarName + '"')
                .collect(Collectors.joining(", "));
    }


    private static String jarNameFor(Symbol anImport) {
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
