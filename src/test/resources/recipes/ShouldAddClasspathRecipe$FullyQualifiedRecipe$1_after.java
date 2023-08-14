package foo;
import org.openrewrite.java.*;

public class ShouldAddClasspathRecipes$FullyQualifiedRecipe$1_after {
    public static JavaTemplate.Builder getTemplate(JavaVisitor<?> visitor) {
        return JavaTemplate
                .builder("org.slf4j.LoggerFactory.getLogger(\"ROOT\").info(#{any(java.lang.String)}))")
                .javaParser(JavaParser.fromJavaVersion().classpath("slf4j-api"));
    }
}
