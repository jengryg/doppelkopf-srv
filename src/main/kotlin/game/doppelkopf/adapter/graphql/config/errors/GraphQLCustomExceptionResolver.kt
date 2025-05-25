package game.doppelkopf.adapter.graphql.config.errors

import game.doppelkopf.errors.ApplicationRuntimeException
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/**
 * [GraphQLCustomExceptionResolver] can be used to handle exceptions we use in this application.
 * It is executed before the [GraphQLFallbackExceptionResolver] that catches all unhandled exceptions.
 *
 * You can define your own handlers in any [DataFetcherExceptionResolverAdapter] implementation that has
 * [Ordered.HIGHEST_PRECEDENCE] or in this class here.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class GraphQLCustomExceptionResolver : DataFetcherExceptionResolverAdapter(), Logging {
    private val log = logger()

    /**
     * If [ex] was not handled by [resolveToMultipleErrors], spring calls this method to resolve the exception to one
     * [GraphQLError].
     */
    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        return when (ex) {
            is ApplicationRuntimeException -> handleApplicationRuntimeException(ex, env)
            // pass the exception to lower precedence resolvers
            else -> null
        }
    }

    /**
     * First, spring calls this method to resolve the exception to multiple [GraphQLError].
     * If this method returns null, it will try [resolveToSingleError] next.
     */
    override fun resolveToMultipleErrors(ex: Throwable, env: DataFetchingEnvironment): List<GraphQLError>? {
        // pass the exception to the resolveToSingleError method
        return super.resolveToMultipleErrors(ex, env)
    }

    private fun handleApplicationRuntimeException(
        ex: ApplicationRuntimeException,
        env: DataFetchingEnvironment
    ): GraphQLError {
        log.atDebug()
            .setMessage { "Handled ApplicationRuntimeException." }
            .setCause(ex)
            .log()

        return GraphqlErrorBuilder
            .newError(env)
            .errorType(ErrorType.ExecutionAborted)
            .message(ex.body.title ?: HttpStatus.valueOf(ex.statusCode.value()).name)
            .extensions(
                mapOf(
                    "type" to ex.body.type,
                    "title" to ex.body.title,
                    "detail" to ex.body.detail,
                    "instance" to ex.body.instance,
                    "properties" to ex.body.properties,
                )
            )
            .build()
    }
}