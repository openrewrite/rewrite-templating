package tech.picnic.errorprone.refaster.annotation;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.util.Comparator;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Map.Entry.comparingByKey;

public class Test<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> one() {
        return Refaster.anyOf(comparing(Map.Entry::getKey), comparingByKey(naturalOrder()));
    }
}
