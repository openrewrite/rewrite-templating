package org.openrewrite.java.template.processor;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class PreConditionTest {
    @Test
    void toStringWithInden() {
        String result = new PreCondition.Or(
          setOf(
            new PreCondition.And(
              setOf(
                new PreCondition.Rule("A"),
                new PreCondition.Rule("B"),
                new PreCondition.Rule("C")),
              4
            ),
            new PreCondition.And(
              setOf(
                new PreCondition.Rule("X"),
                new PreCondition.Rule("Y"),
                new PreCondition.Rule("Z")),
              4
            )
          ), 4).toString();

        assertThat(result).isEqualTo("Preconditions.or(\n" +
          "    Preconditions.and(\n" +
          "    X,\n" +
          "    Y,\n" +
          "    Z\n" +
          "),\n" +
          "    Preconditions.and(\n" +
          "    A,\n" +
          "    B,\n" +
          "    C\n" +
          ")\n" +
          ")");
    }

    @Test
    void sameRulesArePrunedAutomatically() {
        Set<PreCondition> result = setOf(new PreCondition.Rule("A"), new PreCondition.Rule("A"));

        assertThat(result).isEqualTo(setOf(new PreCondition.Rule("A")));
    }

    @Test
    void sameRulesArePrunedAutomaticallyInAnOr() {
        PreCondition x = new PreCondition.Or(
          setOf(new PreCondition.Rule("A"), new PreCondition.Rule("A")),
          4
        );

        assertThat(x).isEqualTo(new PreCondition.Or(
          setOf(new PreCondition.Rule("A")),
          4
        ));
    }

    @Test
    void pruneOrWithAnds() {
        PreCondition result = new PreCondition.Or(
          setOf(
            new PreCondition.And(
              setOf(new PreCondition.Rule("A"), new PreCondition.Rule("B")),
              4
            ),
            new PreCondition.And(
              setOf(new PreCondition.Rule("A"), new PreCondition.Rule("B"), new PreCondition.Rule("C")),
              4
            )
          ), 4).prune();

        assertThat(result).isEqualTo(new PreCondition.And(
          setOf(new PreCondition.Rule("A"), new PreCondition.Rule("B")),
          4
        ));
    }

    @Test
    void pruneOrWithAndAndRule() {
        PreCondition result = new PreCondition.Or(
          setOf(
            new PreCondition.And(
              setOf(new PreCondition.Rule("A"), new PreCondition.Rule("B")),
              4
            ),
             new PreCondition.Rule("B")
          ), 4).prune();

        assertThat(result).isEqualTo(new PreCondition.Rule("B"));
    }

    @SafeVarargs
    private final <T> Set<T> setOf(T... rules) {
        return new HashSet<>(Arrays.asList(rules));
    }
}
