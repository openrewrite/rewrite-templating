package org.openrewrite.java.template.processor;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public class PreCondition {

    @RequiredArgsConstructor
    public static class Rule extends PreCondition {
        final String rule;

        @Override
        public String toString() {
            return rule;
        }
    }

    @RequiredArgsConstructor
    public static class Or extends PreCondition {
        final Set<PreCondition> rules;
        final int indent;

        @Override
        public String toString() {
            return joinPreconditions(rules, "or", indent);
        }
    }

    @RequiredArgsConstructor
    public static class And extends PreCondition {
        final Set<PreCondition> rules;
        final int indent;

        @Override
        public String toString() {
            return joinPreconditions(rules, "and", indent);
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
