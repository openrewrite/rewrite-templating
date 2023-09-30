## Rewrite Templating

Support before/after templating as seen in Google Refaster.

### Input

Allows you to defined one or more `@BeforeTemplate` annotated methods and a single `@AfterTemplate` method.

```java
package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

public class UseStringIsEmpty {
    @BeforeTemplate
    boolean before(String s) {
        return s.length() > 0;
    }

    @AfterTemplate
    boolean after(String s) {
        return !s.isEmpty();
    }
}
```

### Output

This results in a recipe that can be used to transform code that matches the `@BeforeTemplate` to the `@AfterTemplate`.
