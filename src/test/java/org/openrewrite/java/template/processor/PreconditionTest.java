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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

class PreconditionTest {
    @Test
    void toStringWithInden() {
        String result = new Precondition.Or(
          setOf(
            new Precondition.And(
              setOf(
                new Precondition.Rule("A"),
                new Precondition.Rule("B"),
                new Precondition.Rule("C")),
              4
            ),
            new Precondition.And(
              setOf(
                new Precondition.Rule("X"),
                new Precondition.Rule("Y"),
                new Precondition.Rule("Z")),
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
    void sameRulesArePrunedAutomatically() {
        Set<Precondition> result = setOf(new Precondition.Rule("A"), new Precondition.Rule("A"));

        assertThat(result).isEqualTo(setOf(new Precondition.Rule("A")));
    }

    @Test
    void sameRulesArePrunedAutomaticallyInAnOr() {
        Precondition x = new Precondition.Or(
          setOf(new Precondition.Rule("A"), new Precondition.Rule("A")),
          4
        );

        assertThat(x).isEqualTo(new Precondition.Or(
          setOf(new Precondition.Rule("A")),
          4
        ));
    }

    @Test
    void pruneOrWithAnds() {
        Precondition result = new Precondition.Or(
          setOf(
            new Precondition.And(
              setOf(new Precondition.Rule("A"), new Precondition.Rule("B")),
              4
            ),
            new Precondition.And(
              setOf(new Precondition.Rule("A"), new Precondition.Rule("B"), new Precondition.Rule("C")),
              4
            )
          ), 4).prune();

        assertThat(result).isEqualTo(new Precondition.And(
          setOf(new Precondition.Rule("A"), new Precondition.Rule("B")),
          4
        ));
    }

    @Test
    void pruneOrWithAndAndRule() {
        Precondition result = new Precondition.Or(
          setOf(
            new Precondition.And(
              setOf(new Precondition.Rule("A"), new Precondition.Rule("B")),
              4
            ),
             new Precondition.Rule("B")
          ), 4).prune();

        assertThat(result).isEqualTo(new Precondition.Rule("B"));
    }

    @SafeVarargs
    private final <T> Set<T> setOf(T... rules) {
        return new HashSet<>(Arrays.asList(rules));
    }
}
