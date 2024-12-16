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

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.*;

import static java.util.stream.Collectors.toCollection;

@RequiredArgsConstructor
public abstract class Precondition {
    private static final Comparator<String> BY_USES_TYPE_FIRST = Comparator
            .comparing((String s) -> !s.startsWith("new UsesType"))
            .thenComparing(Comparator.naturalOrder());

    abstract boolean fitsInto(Precondition p);

    public Precondition prune() {
        return this;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
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
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class Or extends Precondition {
        Set<Precondition> preconditions;
        int indent;

        @Override
        boolean fitsInto(Precondition p) {
            if (p instanceof Or) {
                return this.equals(p);
            }
            return false;
        }

        @Override
        public Precondition prune() {
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

            return this;
        }

        @Override
        public String toString() {
            return joinPreconditions(preconditions, "or", indent);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class And extends Precondition {
        Set<Precondition> preconditions;
        int indent;

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
            return joinPreconditions(preconditions, "and", indent);
        }
    }

    private static String joinPreconditions(Collection<Precondition> rules, String op, int indent) {
        if (rules.isEmpty()) {
            return "";
        } else if (rules.size() == 1) {
            return rules.iterator().next().toString();
        }
        String whitespace = String.format("%" + indent + "s", " ");
        Set<String> preconditions = rules.stream().map(Object::toString).sorted(BY_USES_TYPE_FIRST).collect(toCollection(LinkedHashSet::new));
        return "Preconditions." + op + "(\n"
                + whitespace + String.join(",\n" + whitespace, preconditions) + "\n"
                + whitespace.substring(0, indent - 4) + ')';
    }
}
