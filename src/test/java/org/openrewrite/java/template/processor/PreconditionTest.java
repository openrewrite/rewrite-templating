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
          new And(
            new Or(new Rule("A"), new Rule("B")),
            new Or(new Rule("C"), new Rule("D"))
          ),
          new And(new Rule("X"), new Rule("Y"), new Rule("Z"))
        ).toString();

        assertThat(result).isEqualTo("Preconditions.or(\n" +
          "    Preconditions.and(\n" +
          "        Preconditions.or(\n" +
          "            A,\n" +
          "            B\n" +
          "        ),\n" +
          "        Preconditions.or(\n" +
          "            C,\n" +
          "            D\n" +
          "        )\n" +
          "    ),\n" +
          "    Preconditions.and(\n" +
          "        X,\n" +
          "        Y,\n" +
          "        Z\n" +
          "    )\n" +
          ")");
    }

    @Test
    void ruleFitsInRule() {
        assertThat(new Rule("A").fitsInto(new Rule("A"))).isTrue();
    }

    @Test
    void orFitsInOr() {
        boolean result = new Or(new Rule("A"), new Rule("B"))
          .fitsInto(new Or(new Rule("B"), new Rule("A")));

        assertThat(result).isTrue();
    }

    @Test
    void ardFitsNotInAndWithDifferentRules() {
        boolean result = new Or(new Rule("A"), new Rule("C"))
          .fitsInto(new Or(new Rule("B"), new Rule("A")));

        assertThat(result).isFalse();
    }

    @Test
    void andFitsInAnd() {
        boolean result = new And(new Rule("A"))
          .fitsInto(new And(new Rule("B"), new Rule("A")));

        assertThat(result).isTrue();
    }

    @Test
    void andFitsNotInAndWithDifferentRules() {
        boolean result = new And(new Rule("A"), new Rule("C"))
          .fitsInto(new And(new Rule("B"), new Rule("A")));

        assertThat(result).isFalse();
    }

    @Test
    void sameRulesArePrunedAutomatically() {
        Set<Precondition> result = setOf(new Rule("A"), new Rule("A"));

        assertThat(result).isEqualTo(setOf(new Rule("A")));
    }

    @Test
    void sameRulesArePrunedAutomaticallyInAnOr() {
        Precondition result = new Or(new Rule("A"), new Rule("A"));

        assertThat(result).isEqualTo(new Or(new Rule("A")));
    }

    @Test
    void pruneOrWithAnds() {
        Precondition result = new Or(
          new And(new Rule("A"), new Rule("B")),
          new And(new Rule("A"), new Rule("B"), new Rule("C"))
        ).prune();

        assertThat(result).isEqualTo(new And(new Rule("A"), new Rule("B")));
    }

    @Test
    void pruneOrWithAndAndRule() {
        Precondition result = new Or(
          new And(new Rule("A"), new Rule("B")),
          new Rule("B")
        ).prune();

        assertThat(result).isEqualTo(new Rule("B"));
    }

    @Test
    void pruneOrWithTypeChange() {
        Precondition result = new Or(
          new And(new Rule("A"), new Rule("B"), new Rule("C")),
          new And(new Rule("D"), new Rule("B"), new Rule("E"))
        ).prune();

        assertThat(result).isEqualTo(
          new And(
            new Rule("B"),
            new Or(
              new And(new Rule("A"), new Rule("C")),
              new And(new Rule("D"), new Rule("E"))
            )
          )
        );
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... rules) {
        return new HashSet<>(Arrays.asList(rules));
    }
}
