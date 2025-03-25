package game.doppelkopf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Configure the spring boot application to scan for components using the [DoppelkopfPackageMarker] for type safety and
 * better IDE checking.
 */
@SpringBootApplication(
    scanBasePackageClasses = [
        DoppelkopfPackageMarker::class
    ]
)
@ConfigurationPropertiesScan(
    basePackageClasses = [
        DoppelkopfPackageMarker::class
    ]
)
class DoppelkopfApplication

fun main(args: Array<String>) {
    runApplication<DoppelkopfApplication>(*args)
}
