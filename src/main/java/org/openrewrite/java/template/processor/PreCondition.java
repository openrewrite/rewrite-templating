package org.openrewrite.java.template.processor;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public abstract class PreCondition {

    abstract boolean fitsInto(PreCondition p);

    public PreCondition prune() {
        return this;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class Rule extends PreCondition {
        String rule;

        @Override
        boolean fitsInto(PreCondition p) {
            if (p instanceof Rule) {
                return ((Rule) p).rule.equals(rule);
            } else {
                return p.fitsInto(this);
            }
        }

        @Override
        public String toString() {
            return rule;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class Or extends PreCondition {
        Set<PreCondition> preConditions;
        int indent;

        @Override
        boolean fitsInto(PreCondition p) {
            throw new NotImplementedException();
        }

        @Override
        public PreCondition prune() {
            for (PreCondition p : preConditions) {
                int matches = 0;
                for (PreCondition p2 : preConditions) {
                    if (p == p2 || p.fitsInto(p2)) {
                        matches++;
                    }
                    if (matches == preConditions.size()) {
                        return p;
                    }
                }
            }

            return this;
        }

        @Override
        public String toString() {
            return joinPreconditions(preConditions, "or", indent);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class And extends PreCondition {
        Set<PreCondition> preConditions;
        int indent;

        @Override
        boolean fitsInto(PreCondition p) {
            if (p instanceof Rule) {
                return preConditions.contains(p);
            } else if (p instanceof Or) {
                throw new NotImplementedException();
            } else if (p instanceof And) {
                if (preConditions.size() > ((And) p).preConditions.size()) {
                    return false;
                }
                return preConditions.stream().allMatch(it -> it.fitsInto(p));
            }
            throw new IllegalArgumentException("Type is not supported: " + p.getClass());
        }

        @Override
        public String toString() {
            return joinPreconditions(preConditions, "and", indent);
        }
    }

    private static String joinPreconditions(Collection<PreCondition> rules, String op, int indent) {
        if (rules.isEmpty()) {
            return "";
        } else if (rules.size() == 1) {
            return rules.iterator().next().toString();
        }
        char[] indentChars = new char[indent];
        Arrays.fill(indentChars, ' ');
        String indentStr = new String(indentChars);
        Set<String> preconditions = rules.stream().map(Object::toString).collect(toSet());
        return "Preconditions." + op + "(\n" + indentStr + String.join(",\n" + indentStr, preconditions) + "\n" + indentStr.substring(0, indent - 4) + ')';
    }
}
