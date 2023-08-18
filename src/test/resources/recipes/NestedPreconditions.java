package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

import java.util.*;

public class NestedPreconditions {
    @BeforeTemplate
    Map hashMap(int size) {
        return new HashMap(size);
    }

    @BeforeTemplate
    Map linkedHashMap(int size) {
        return new LinkedHashMap(size);
    }

    @AfterTemplate
    Map hashtable(int size) {
        return new Hashtable(size);
    }
}
