package foo;
import org.openrewrite.java.*;

public class ShouldAddClasspathRecipes$UnqualifiedRecipe$1_before {
    public static JavaTemplate.Builder getTemplate(JavaVisitor<?> visitor) {
        return JavaTemplate
                .builder("System.out.println(#{any(java.lang.String)})");
    }
}
