package grouping;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for general tests. Can be applied to any type of tests
 * Will run with the -Pgeneral-tests maven profile: e.g mvn test -Pgeneral-tests
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Tag("grouping.GeneralTest")
public @interface GeneralTest {
}
