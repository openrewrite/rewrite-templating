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
        String result = new And(
          new And(new Rule("new UsesMethod<>(\"java.lang.String valueOf(..)\", true)")),
          new Or(
            new And(new Rule("new UsesMethod<>(\"java.util.HashMap <constructor>(..)\", true)"), new Rule("new UsesType<>(\"java.util.HashMap\", true)")),
            new Or(new Rule("new UsesType<>(\"java.util.LinkedHashMap\", true)"))
          ),
          new Rule("new UsesType<>(\"java.util.Map\", true)"),
          new Rule("new UsesType<>(\"java.util.List\", true)")
        ).toString();

        assertThat(result).isEqualTo(
          "Preconditions.and(\n" +
            "        new UsesType<>(\"java.util.List\", true),\n" +
            "        new UsesType<>(\"java.util.Map\", true),\n" +
            "        new UsesMethod<>(\"java.lang.String valueOf(..)\", true),\n" +
            "        Preconditions.or(\n" +
            "                new UsesType<>(\"java.util.LinkedHashMap\", true),\n" +
            "                Preconditions.and(\n" +
            "                        new UsesType<>(\"java.util.HashMap\", true),\n" +
            "                        new UsesMethod<>(\"java.util.HashMap <constructor>(..)\", true)\n" +
            "                )\n" +
            "        )\n" +
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
