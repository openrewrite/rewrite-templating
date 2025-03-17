package foo;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;

class CollectionIsEmpty<T> {
    @BeforeTemplate
    boolean before(Collection<T> collection) {
        return Refaster.anyOf(
                collection.size() == 0,
                collection.size() <= 0,
                collection.size() < 1,
                collection.stream().findAny().isEmpty(),
                collection.stream().findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(Collection<T> collection) {
        return collection.isEmpty();
    }
}
