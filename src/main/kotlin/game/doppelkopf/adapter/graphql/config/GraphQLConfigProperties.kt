package game.doppelkopf.adapter.graphql.config

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.hibernate.validator.constraints.Range
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.graphql")
class GraphQLConfigProperties @ConstructorBinding constructor(
    /**
     * https://www.graphql-java.com/documentation/master/instrumentation#query-depth-instrumentation
     */
    @field:Range(min = 1, max = 255)
    val maxDepth: Int,

    /**
     * https://www.graphql-java.com/documentation/master/instrumentation#query-complexity-instrumentation
     */
    @field:Range(min = 1, max = 1_000_000)
    val maxComplexity: Int,
) : Logging {
    private val log = logger()

    init {
        log.atInfo()
            .setMessage { "GraphQLConfigProperties initialized." }
            .addKeyValue("maxDepth") { maxDepth }
            .addKeyValue("maxComplexity") { maxComplexity }
            .log()
    }
}