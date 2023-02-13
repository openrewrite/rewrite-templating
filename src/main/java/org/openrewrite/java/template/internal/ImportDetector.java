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

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class ImportDetector {

    /**
     * Locate types that are directly referred to by name in the
     * lambda body and therefore need an import in the template.
     * @return The list of imports to add.
     */
    public static List<String> imports(JCTree.JCLambda lambda) {
        List<String> imports = new ArrayList<>();
        new TreeScanner() {
            @Override
            public void visitIdent(JCTree.JCIdent tree) {
//                if(tree.sym.type. tree.getName().toString())
            }
        }.scan(lambda);
        return emptyList();
    }
}
