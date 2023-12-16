## Rewrite Templating
Support before/after templating as seen in Google Refaster.

[![ci](https://github.com/openrewrite/rewrite-templating/actions/workflows/ci.yml/badge.svg)](https://github.com/openrewrite/rewrite-templating/actions/workflows/ci.yml)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite-templating.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.openrewrite.org/scans)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)

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

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.
