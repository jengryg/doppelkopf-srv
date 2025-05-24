@file:Suppress("CanUnescapeDollarLiteral")

package game.doppelkopf

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import jakarta.validation.constraints.Pattern
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.common")
class CommonConfig @ConstructorBinding constructor(
    /**
     * The stage identifier.
     */
    @field:Pattern(regexp = "^[0-9A-Za-z]+\$")
    val stage: String,

    /**
     * The version of the project.
     */
    @field:Pattern(regexp = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$")
    val version: String? = null,
) : Logging {
    private val log = logger()

    init {
        log.atInfo()
            .setMessage("CommonConfig initialized.")
            .addKeyValue("stage") { stage }
            .addKeyValue("version") { version }
            .log()

    }
}