package grouping;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for cloud related tests.
 * Need be added to the test classes that use the KlapDeviceClient.
 * Will run with the -Pklap-tests maven profile: e.g mvn test -Pklap-tests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Tag("grouping.KlapTest")
public @interface KlapTest {
}
