package foo;
import org.openrewrite.java.*;

public class ParameterReuseRecipe$1_before {
    public static JavaTemplate.Builder getTemplate() {
        return JavaTemplate
                .builder("#{s:any(java.lang.String)}.equals(#{s})");
    }
}
