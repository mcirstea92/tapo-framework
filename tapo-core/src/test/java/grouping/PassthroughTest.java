package grouping;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for secure pass through device related tests.
 * Need be added to the test classes that use the SecurePassThroughDeviceClient.
 * Will run with the -Ppassthrough-tests maven profile: e.g mvn test -Ppassthrough-tests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Tag("grouping.PassthroughTest")
public @interface PassthroughTest {
}

