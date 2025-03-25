package game.doppelkopf

/**
 * All junit [org.junit.jupiter.api.Tag] definitions used in this service should be defined as constants here.
 */
object TestTags {
    /**
     * This tag is applied in [BaseSpringBootTest] and should be set on all tests that use the
     * [org.springframework.boot.test.context.SpringBootTest] annotation to define a spring context.
     */
    const val SPRING_INTEGRATION = "SpringIntegration"
}