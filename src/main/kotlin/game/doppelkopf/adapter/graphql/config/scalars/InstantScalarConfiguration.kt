package game.doppelkopf.adapter.graphql.config.scalars

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.*
import graphql.schema.idl.RuntimeWiring
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import java.time.Instant
import java.util.*


/**
 * Custom graphql type `Instant` for [Instant].
 */
@Configuration
class InstantScalarConfiguration {
    @Bean
    fun instantScalar(): GraphQLScalarType {
        return GraphQLScalarType.newScalar()
            .name("Instant")
            .description("Instant in time.")
            .coercing(InstantCoercing())
            .build()
    }

    @Bean
    fun runtimeWiringConfigurerForInstanceScalar(): RuntimeWiringConfigurer {
        return RuntimeWiringConfigurer { builder: RuntimeWiring.Builder? -> builder!!.scalar(instantScalar()) }
    }

    class InstantCoercing : Coercing<Instant, String> {
        override fun serialize(
            dataFetcherResult: Any,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): String {
            when (dataFetcherResult) {
                is Instant -> return runCatching { serializer(dataFetcherResult) }.getOrElse {
                    throw CoercingSerializeException("Could not serialize Instant.", it)
                }

                else -> throw CoercingSerializeException("Given dataFetcherResult is not an Instant type.")
            }
        }

        override fun parseValue(
            input: Any,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): Instant {
            when (input) {
                is String -> return runCatching { parser(input) }.getOrElse {
                    throw CoercingParseValueException("Could not parse input $input to Instant.", it)
                }

                else -> throw CoercingParseValueException("Given input is not a String type.")
            }
        }

        override fun parseLiteral(
            input: Value<*>,
            variables: CoercedVariables,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): Instant {
            when (input) {
                is StringValue -> return runCatching { parser(input.value) }.getOrElse {
                    throw CoercingParseLiteralException("Could not parse input ${input.value} to Instant.", it)
                }

                else -> throw CoercingParseLiteralException("Given input is not a StringValue type.")
            }
        }

        override fun valueToLiteral(
            input: Any,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): Value<*> {
            return StringValue(input.toString())
        }

        private fun serializer(instant: Instant): String {
            return instant.toString()
        }

        private fun parser(input: String): Instant {
            return Instant.parse(input)
        }
    }
}