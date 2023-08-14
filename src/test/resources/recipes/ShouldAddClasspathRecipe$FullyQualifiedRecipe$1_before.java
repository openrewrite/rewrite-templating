package foo;
import org.openrewrite.java.*;

public class ShouldAddClasspathRecipes$FullyQualifiedRecipe$1_before {
    public static JavaTemplate.Builder getTemplate(JavaVisitor<?> visitor) {
        return JavaTemplate
                .builder("System.out.println(#{any(java.lang.String)})");
    }
}
