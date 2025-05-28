package game.doppelkopf.adapter.graphql.config

import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.instrumentation.Instrumentation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLConfig(
    private val qglConfig: GraphQLConfigProperties
) {
    @Bean
    fun queryDepthInstrumentation(): Instrumentation {
        return ChainedInstrumentation(
            listOf(
                // https://www.graphql-java.com/documentation/master/instrumentation#query-depth-instrumentation
                MaxQueryDepthInstrumentation(qglConfig.maxDepth),
                // https://www.graphql-java.com/documentation/master/instrumentation#query-complexity-instrumentation
                MaxQueryComplexityInstrumentation(qglConfig.maxComplexity)
            )
        )
    }
}