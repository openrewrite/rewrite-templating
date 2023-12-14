package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.sun.tools.javac.util.Constants;
import com.sun.tools.javac.util.Convert;

class ConstantsFormat {
    @BeforeTemplate
    String before(String value) {
        return String.format("\"%s\"", Convert.quote(value));
    }

    @AfterTemplate
    String after(String value) {
        return Constants.format(value);
    }
}
