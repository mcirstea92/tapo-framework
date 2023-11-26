package grouping;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for cloud related tests.
 * Need be added to the test classes that use the TapoCloudClient.
 * Will run with the -Pcloud-test maven profile: e.g mvn test -Pcloud-test
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Tag("grouping.CloudTest")
public @interface CloudTest {
}
