<p align="center">
  <a href="https://docs.openrewrite.org">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-dark.svg">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg">
      <img alt="OpenRewrite Logo" src="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg" width='600px'>
    </picture>
  </a>
</p>

<div align="center">
  <h1>rewrite-templating</h1>
</div>

<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://github.com/openrewrite/rewrite-templating/actions/workflows/ci.yml/badge.svg)](https://github.com/openrewrite/rewrite-templating/actions/workflows/ci.yml)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite-templating.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite/rewrite-templating.svg)](https://mvnrepository.com/artifact/org.openrewrite/rewrite-templating)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.openrewrite.org/scans)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)
</div>

### What is this?

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

## Options
Annotation processors can take options to customize their behavior. Options are passed to the annotation processor via the `-A` flag.

### Change the `@Generated` annotation
By default, the annotation processor will add a `@javax.annotation.Generated` annotation to the generated classes, compatible with Java 8.
On newer Java version you'd perhaps want to pass in the following option:
```
-Arewrite.generatedAnnotation=jakarta.annotation.Generated
```

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.
