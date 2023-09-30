package foo;
import org.openrewrite.java.*;

public class ShouldAddClasspathRecipes$UnqualifiedRecipe$1_after {
    public static JavaTemplate.Builder getTemplate() {
        return JavaTemplate
                .builder("org.slf4j.LoggerFactory.getLogger(#{any(java.lang.String)})")
                .javaParser(JavaParser.fromJavaVersion().classpath("slf4j-api"));
    }
}
