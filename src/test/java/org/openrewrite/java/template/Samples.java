package org.openrewrite.java.template;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("ALL")
public class Samples {

    void templates() {
        new JavaVisitor<ExecutionContext>() {
            // old way (still available for complex cases where we string concatenate templates)
            JavaTemplate manual = JavaTemplate.builder(this::getCursor, "" +
                                                                        "if(#{any(com.desjardins.Something)}.equals(#{any(java.lang.String)})) {\n" +
                                                                        "} else {\n" +
                                                                        "}")
              .javaParser(() -> JavaParser.fromJavaVersion().classpath("desjardins").build())
              .build();

            // new way (for most cases)
            JavaTemplate auto = AutoTemplate
              .compile("ifElse", (String p1, String p2) -> {
                  if (p1.equals(p2)) {
                  } else {
                  }
              })
              .build(this);
        };

        new JavaVisitor<ExecutionContext>() {
            // current (still available for complex cases where we string concatenate templates)
            JavaTemplate manual = JavaTemplate.builder(this::getCursor,
                "#{any(org.slf4j.Logger)}.error(#{}, #{any(java.lang.Throwable)})")
              .javaParser(() -> JavaParser.fromJavaVersion().classpath("slf4j-api").build())
              .build();

            // new (use for most cases)
            JavaTemplate auto = AutoTemplate
              .compile("logError", (Logger log, @TemplateLiteral String msg, Throwable t) -> {
                  log.error(msg, t);
              })
              .build(this);
        };

        new JavaVisitor<ExecutionContext>() {
            // new (use for most cases)
            JavaTemplate auto = AutoTemplate
              .compile("logError", (Logger log, @TemplateLiteral String msg, Throwable t) -> {
                  log.error(msg, t);
              })
              .isolateClasspath("slf4j-api")
              .build(this);
        };
    }
}
