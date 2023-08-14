package foo;
import org.openrewrite.java.*;

public class ShouldAddClasspathRecipes$UnqualifiedRecipe$1_after {
    public static JavaTemplate.Builder getTemplate(JavaVisitor<?> visitor) {
        return JavaTemplate
                .builder("LoggerFactory.getLogger(\"ROOT\").inf#{any(java.lang.String)}ge)")
                .javaParser(JavaParser.fromJavaVersion().classpath("slf4j-api"))
                .imports("org.slf4j.LoggerFactory");
    }
}
