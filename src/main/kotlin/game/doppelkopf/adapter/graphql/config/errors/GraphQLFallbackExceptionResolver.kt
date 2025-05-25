package game.doppelkopf.adapter.graphql.config.errors

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
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Final [DataFetcherExceptionResolverAdapter] that is executed by spring for this application to protect against
 * information leakage due to generic exception to error conversions.
 *
 * [Ordered.HIGHEST_PRECEDENCE] + 2 resolver provides the fallback to handle all unhandled exceptions explicitly.
 * You can define your own handlers in any [DataFetcherExceptionResolverAdapter] implementation that has
 * [Ordered.HIGHEST_PRECEDENCE] or add another fallback in the [GraphQLCustomExceptionResolver] that has
 * [Ordered.HIGHEST_PRECEDENCE] +1.
 *
 * Note: [Ordered] uses precedence scores where the highest one is the smallest singled integer and the lowest is the
 * largest signed integer, thus increasing the value reduces the precedence. [Int.MIN_VALUE] is the first handler.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
class GraphQLFallbackExceptionResolver : DataFetcherExceptionResolverAdapter(), Logging {
    private val log = logger()

    /**
     * If all other resolvers can not handle the [Throwable], we resolve it to a generic error without additional
     * information exposed to the user. Best practices.
     *
     * This method is not allowed to return `null`, since that would pass the error to other resolvers that may expose
     * information.
     */
    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError {
        log.atError()
            .setMessage { "Exception handled by general exception resolver." }
            .setCause(ex)
            .log()
        // This is logged at ERROR level since we do not expect to have unhandled exceptions in this application.

        return GraphqlErrorBuilder
            .newError(env)
            .message(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase)
            .errorType(ErrorType.ExecutionAborted)
            .build()
    }

    override fun resolveToMultipleErrors(ex: Throwable, env: DataFetchingEnvironment): List<GraphQLError>? {
        return when (ex) {
            // handle validation errors originating from the @Valid annotation
            is MethodArgumentNotValidException -> ex.bindingResult.allErrors.map { error ->
                when (error) {
                    is FieldError -> resolveFieldError(error, env)
                    else -> resolveObjectError(error, env)
                }
            }
            // pass the exception to the resolveToSingleError method
            else -> null
        }
    }

    private fun resolveFieldError(error: FieldError, env: DataFetchingEnvironment): GraphQLError {
        return GraphqlErrorBuilder
            .newError(env)
            .message("Validation failed for field '${error.field}': ${error.defaultMessage}")
            .errorType(ErrorType.ValidationError)
            .extensions(
                mapOf(
                    "field" to error.field,
                    "rejectedValue" to error.rejectedValue,
                    "message" to error.defaultMessage
                )
            ).build()
    }

    private fun resolveObjectError(error: ObjectError, env: DataFetchingEnvironment): GraphQLError {
        return GraphqlErrorBuilder
            .newError(env)
            .message("Validation error: ${error.defaultMessage}")
            .errorType(ErrorType.ValidationError)
            .build()
    }
}