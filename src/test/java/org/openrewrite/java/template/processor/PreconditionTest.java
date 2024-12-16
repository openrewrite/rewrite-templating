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

import org.junit.jupiter.api.Test;
import org.openrewrite.java.template.processor.Precondition.And;
import org.openrewrite.java.template.processor.Precondition.Or;
import org.openrewrite.java.template.processor.Precondition.Rule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class PreconditionTest {
    @Test
    void toStringWithInden() {
        String result = new Or(
          setOf(
            new And(
              setOf(
                new Rule("A"),
                new Rule("B"),
                new Rule("C")),
              4
            ),
            new And(
              setOf(
                new Rule("X"),
                new Rule("Y"),
                new Rule("Z")),
              4
            )
          ), 4).toString();

        assertThat(result).isEqualTo("Preconditions.or(\n" +
          "    Preconditions.and(\n" +
          "    A,\n" +
          "    B,\n" +
          "    C\n" +
          "),\n" +
          "    Preconditions.and(\n" +
          "    X,\n" +
          "    Y,\n" +
          "    Z\n" +
          ")\n" +
          ")");
    }

    @Test
    void ruleFitsInRule() {
        assertThat(new Rule("A").fitsInto(new Rule("A"))).isTrue();
    }

    @Test
    void orFitsInOr() {
        boolean result = new Or(
          setOf(new Rule("A"), new Rule("B")),
          4
        ).fitsInto(new Or(
          setOf(new Rule("B"), new Rule("A")),
          4
        ));

        assertThat(result).isTrue();
    }

    @Test
    void ardFitsNotInAndWithDifferentRules() {
        boolean result = new Or(
          setOf(new Rule("A"), new Rule("C")),
          4
        ).fitsInto(new Or(
          setOf(new Rule("B"), new Rule("A")),
          4
        ));

        assertThat(result).isFalse();
    }

    @Test
    void andFitsInAnd() {
        boolean result = new And(
          setOf(new Rule("A")),
          4
        ).fitsInto(new And(
          setOf(new Rule("B"), new Rule("A")),
          4
        ));

        assertThat(result).isTrue();
    }

    @Test
    void andFitsNotInAndWithDifferentRules() {
        boolean result = new And(
          setOf(new Rule("A"), new Rule("C")),
          4
        ).fitsInto(new And(
          setOf(new Rule("B"), new Rule("A")),
          4
        ));

        assertThat(result).isFalse();
    }

    @Test
    void sameRulesArePrunedAutomatically() {
        Set<Precondition> result = setOf(new Rule("A"), new Rule("A"));

        assertThat(result).isEqualTo(setOf(new Rule("A")));
    }

    @Test
    void sameRulesArePrunedAutomaticallyInAnOr() {
        Precondition result = new Or(
          setOf(new Rule("A"), new Rule("A")),
          4
        );

        assertThat(result).isEqualTo(new Or(
          setOf(new Rule("A")),
          4
        ));
    }

    @Test
    void pruneOrWithAnds() {
        Precondition result = new Or(
          setOf(
            new And(
              setOf(new Rule("A"), new Rule("B")),
              4
            ),
            new And(
              setOf(new Rule("A"), new Rule("B"), new Rule("C")),
              4
            )
          ), 4).prune();

        assertThat(result).isEqualTo(new And(
          setOf(new Rule("A"), new Rule("B")),
          4
        ));
    }

    @Test
    void pruneOrWithAndAndRule() {
        Precondition result = new Or(
          setOf(
            new And(
              setOf(new Rule("A"), new Rule("B")),
              4
            ),
            new Rule("B")
          ), 4).prune();

        assertThat(result).isEqualTo(new Rule("B"));
    }

    @Test
    void pruneOrWithTypeChange() {
        Precondition result = new Or(
          setOf(
            new And(
              setOf(new Rule("A"), new Rule("B"), new Rule("C")),
              4
            ),
            new And(
              setOf(new Rule("D"), new Rule("B"), new Rule("E")),
              4
            )
          ), 4).prune();

        assertThat(result).isEqualTo(new And(
          setOf(
            new Rule("B"),
            new Or(
              setOf(
                new And(setOf(new Rule("A"), new Rule("C")), 4),
                new And(setOf(new Rule("D"), new Rule("E")), 4)
              ), 4
            )
          ), 4));
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... rules) {
        return new HashSet<>(Arrays.asList(rules));
    }
}
