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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.TreeScanner;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class UsedMethodDetector {

    public static List<Symbol.MethodSymbol> usedMethods(JCTree input) {
        return usedMethods(input, t -> true);
    }

    public static List<Symbol.MethodSymbol> usedMethods(JCTree input, Predicate<JCTree> scopePredicate) {
        Set<Symbol.MethodSymbol> imports = new LinkedHashSet<>();

        new TreeScanner() {
            @Override
            public void scan(JCTree tree) {
                if (tree instanceof JCTree.JCAnnotation || !scopePredicate.test(tree)) {
                    // completely skip annotations for now
                    return;
                }

                if (tree instanceof JCIdent && ((JCIdent) tree).sym instanceof Symbol.MethodSymbol) {
                    imports.add(((Symbol.MethodSymbol) ((JCIdent) tree).sym));
                } else if (tree instanceof JCFieldAccess && ((JCFieldAccess) tree).sym instanceof Symbol.MethodSymbol) {
                    imports.add(((Symbol.MethodSymbol) ((JCFieldAccess) tree).sym));
                } else if (tree instanceof JCTree.JCNewClass && ((JCTree.JCNewClass) tree).constructor instanceof Symbol.MethodSymbol) {
                    imports.add(((Symbol.MethodSymbol) ((JCTree.JCNewClass) tree).constructor));
                }

                super.scan(tree);
            }
        }.scan(input);

        return new ArrayList<>(imports);
    }
}
