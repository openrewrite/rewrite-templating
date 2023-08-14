package foo;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.slf4j.LoggerFactory;

public class ShouldAddClasspath {

    @BeforeTemplate
    void before(String message) {
        System.out.println(message);
    }

    @AfterTemplate
    void after(String message) {
        LoggerFactory.getLogger("ROOT").info(message);
    }

}
