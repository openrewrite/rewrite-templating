/*
 * Copyright 2024 the original author or authors.
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
package org.openrewrite.java.template.processor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static org.openrewrite.java.template.internal.StringUtils.indent;

public abstract class Precondition {
    private static final Comparator<String> BY_USES_TYPE_METHOD_FIRST = Comparator
            .comparing((String s) -> {
                if (s.startsWith("new UsesType")) return 0;
                if (s.startsWith("new UsesMethod")) return 1;
                return 2;
            })
            .thenComparing(Comparator.naturalOrder());

    abstract boolean fitsInto(Precondition p);

    public Precondition prune() {
        return this;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Rule extends Precondition {
        String rule;

        @Override
        boolean fitsInto(Precondition p) {
            if (p instanceof Rule) {
                return this.equals(p);
            } else if (p instanceof Or) {
                return ((Or) p).preconditions.stream().anyMatch(this::fitsInto);
            } else if (p instanceof And) {
                return ((And) p).preconditions.stream().anyMatch(this::fitsInto);
            }
            return false; // unreachable code
        }

        @Override
        public String toString() {
            return rule;
        }
    }

    @Value
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Or extends Precondition {
        Set<Precondition> preconditions;

        public Or(Precondition... preconditions) {
            this.preconditions = new HashSet<>(Arrays.asList(preconditions));
        }

        @Override
        boolean fitsInto(Precondition p) {
            if (p instanceof Or) {
                return this.equals(p);
            }
            return false;
        }

        @Override
        public Precondition prune() {
            Precondition pruned = takeElementIfItFitsInAllOtherElements();
            return pruned == null ? extractCommonElements() : pruned;
        }

        /**
         * If element fits in all others, take element as precondition. Eg:
         * <pre>
         * or(
         *    and(new UsesType<>("Map"), new UsesMethod<>("PrintStream println(..)")),
         *    new UsesMethod<>("PrintStream println(..)")
         * )
         * </pre>
         * <p>
         * will return:
         * <pre>
         * new UsesMethod<>("PrintStream println()")
         * </pre>
         */
        private Precondition takeElementIfItFitsInAllOtherElements() {
            for (Precondition p : preconditions) {
                int matches = 0;
                for (Precondition p2 : preconditions) {
                    if (p == p2 || p.fitsInto(p2)) {
                        matches++;
                    }
                    if (matches == preconditions.size()) {
                        return p;
                    }
                }
            }
            return null;
        }

        /**
         * If child element of an element exist as child element in all others, move child element up. Eg:
         * <pre>
         * or(
         *    and(new UsesType<>("Map"), new UsesType<>("HashMap"), new UsesMethod<>("PrintStream println(..)")),
         *    and(new UsesType<>("List"), new UsesType<>("ArrayList"), new UsesMethod<>("PrintStream println(..)"))
         * )
         * </pre>
         * <p>
         * will return:
         * <pre>
         * and(
         *    new UsesMethod<>("PrintStream println()"),
         *    or(
         *      and(new UsesType<>("Map"), new UsesType<>("HashMap")),
         *      and(new UsesType<>("List"), new UsesType<>("ArrayList")),
         *    )
         * )
         * </pre>
         */
        private Precondition extractCommonElements() {
            boolean first = true;
            Set<Precondition> commons = new HashSet<>();
            for (Precondition p : preconditions) {
                if (!(p instanceof And)) {
                    return this;
                }
                if (first) {
                    commons.addAll(((And) p).preconditions);
                    first = false;
                } else {
                    commons.retainAll(((And) p).preconditions);
                }
            }

            if (!commons.isEmpty()) {
                preconditions.forEach(it -> ((And) it).preconditions.removeAll(commons));
                commons.add(new Or(preconditions));
                return new And(commons);
            }

            return this;
        }

        @Override
        public String toString() {
            return joinPreconditions(preconditions, "or");
        }
    }

    @Value
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class And extends Precondition {
        Set<Precondition> preconditions;

        public And(Precondition... preconditions) {
            this.preconditions = new HashSet<>(Arrays.asList(preconditions));
        }

        @Override
        boolean fitsInto(Precondition p) {
            if (p instanceof And) {
                if (preconditions.size() > ((And) p).preconditions.size()) {
                    return false;
                }
                return preconditions.stream().allMatch(it -> it.fitsInto(p));
            }
            return false;
        }

        @Override
        public String toString() {
            return joinPreconditions(preconditions, "and");
        }
    }

    private static String joinPreconditions(Collection<Precondition> rules, String op) {
        if (rules.isEmpty()) {
            return "";
        } else if (rules.size() == 1) {
            return rules.iterator().next().toString();
        }
        String preconditions = rules.stream().map(Object::toString).sorted(BY_USES_TYPE_METHOD_FIRST).collect(joining(",\n"));
        return "Preconditions." + op + "(\n" +
                indent(preconditions, 4) + "\n" +
                ")";
    }
}
