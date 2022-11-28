/*
 * Copyright (C) 2011-2020 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.openrewrite.java.template.internal;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import org.openrewrite.Cursor;

import javax.tools.JavaFileObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("ConstantConditions")
public class JavacResolution {
    private final Attr attr;
    private final TreeMirrorMaker mirrorMaker;
    private final Log log;

    public JavacResolution(Context context) {
        this.attr = Attr.instance(context);
        this.mirrorMaker = new TreeMirrorMaker(new JavacTreeMaker(TreeMaker.instance(context)));
        this.log = Log.instance(context);
    }

    public Map<JCTree, JCTree> resolveAll(Context context, JCCompilationUnit cu, List<? extends Tree> trees) {
        AtomicReference<Map<JCTree, JCTree>> resolved = new AtomicReference<>();

        new TreeScanner() {
            Cursor cursor = null;

            @Override
            public void scan(JCTree tree) {
                cursor = new Cursor(cursor, tree);
                for (Tree t : trees) {
                    if (t == tree) {
                        EnvFinder finder = new EnvFinder(context);
                        Iterator<Object> path = cursor.getPath();
                        List<JCTree> reversePath = new ArrayList<>();
                        while (path.hasNext()) {
                            reversePath.add(0, (JCTree) path.next());
                        }
                        for (JCTree p : reversePath) {
                            p.accept(finder);
                        }

                        JCTree copy = mirrorMaker.copy(finder.copyAt());
                        JavaFileObject oldFileObject = log.useSource(cu.getSourceFile());
                        try {
                            memberEnterAndAttribute(copy, finder.get(), context);
                            resolved.set(mirrorMaker.getOriginalToCopyMap());
                        } finally {
                            log.useSource(oldFileObject);
                        }
                        return;
                    }
                }
                super.scan(tree);
                cursor = cursor.getParent();
            }
        }.scan(cu);

        return resolved.get();
    }

    private static Field memberEnterDotEnv;

    private static Field getMemberEnterDotEnv() {
        if (memberEnterDotEnv != null) return memberEnterDotEnv;
        try {
            return memberEnterDotEnv = Permit.getField(MemberEnter.class, "env");
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Env<AttrContext> getEnvOfMemberEnter(MemberEnter memberEnter) {
        Field f = getMemberEnterDotEnv();
        try {
            return (Env<AttrContext>) f.get(memberEnter);
        } catch (Exception e) {
            return null;
        }
    }

    private static void setEnvOfMemberEnter(MemberEnter memberEnter, Env<AttrContext> env) {
        Field f = getMemberEnterDotEnv();
        try {
            f.set(memberEnter, env);
        } catch (Exception ignored) {
        }
    }

    private void memberEnterAndAttribute(JCTree copy, Env<AttrContext> env, Context context) {
        MemberEnter memberEnter = MemberEnter.instance(context);
        Env<AttrContext> oldEnv = getEnvOfMemberEnter(memberEnter);
        setEnvOfMemberEnter(memberEnter, env);
        try {
            copy.accept(memberEnter);
        } catch (Exception ignored) {
            // intentionally ignored; usually even if this step fails, val will work (but not for val in method local inner classes and anonymous inner classes).
        } finally {
            setEnvOfMemberEnter(memberEnter, oldEnv);
        }
        attrib(copy, env);
    }

    private void attrib(JCTree tree, Env<AttrContext> env) {
        if (env.enclClass.type == null) try {
            env.enclClass.type = Type.noType;
        } catch (Throwable ignore) {
            // This addresses issue #1553 which involves JDK9; if it doesn't exist, we probably don't need to set it.
        }
        if (tree instanceof JCBlock) attr.attribStat(tree, env);
        else if (tree instanceof JCMethodDecl) attr.attribStat(((JCMethodDecl) tree).body, env);
        else if (tree instanceof JCVariableDecl) attr.attribStat(tree, env);
        else throw new IllegalStateException("Called with something that isn't a block, method decl, or variable decl");
    }

    /*
     * We need to dig down to the level of the method or field declaration or (static) initializer block, then attribute that entire method/field/block using
     * the appropriate environment. So, we start from the top and walk down the node tree until we hit that method/field/block and stop there, recording both
     * the environment object (`env`) and the exact tree node (`copyAt`) at which to begin the attr process.
     */
    private static final class EnvFinder extends JCTree.Visitor {
        private Env<AttrContext> env = null;
        private final Enter enter;
        private final MemberEnter memberEnter;
        private JCTree copyAt = null;

        EnvFinder(Context context) {
            this.enter = Enter.instance(context);
            this.memberEnter = MemberEnter.instance(context);
        }

        Env<AttrContext> get() {
            return env;
        }

        JCTree copyAt() {
            return copyAt;
        }

        @Override
        public void visitTopLevel(JCCompilationUnit tree) {
            if (copyAt != null) return;
            env = enter.getTopLevelEnv(tree);
        }

        @Override
        public void visitClassDef(JCClassDecl tree) {
            if (copyAt != null) return;
            if (tree.sym != null) env = enter.getClassEnv(tree.sym);
        }

        @Override
        public void visitMethodDef(JCMethodDecl tree) {
            if (copyAt != null) return;
            env = memberEnter.getMethodEnv(tree, env);
            copyAt = tree;
        }

        public void visitVarDef(JCVariableDecl tree) {
            if (copyAt != null) return;
            env = memberEnter.getInitEnv(tree, env);
            copyAt = tree;
        }

        @Override
        public void visitBlock(JCBlock tree) {
            if (copyAt != null) return;
            copyAt = tree;
        }

        @Override
        public void visitTree(JCTree that) {
        }
    }
}
