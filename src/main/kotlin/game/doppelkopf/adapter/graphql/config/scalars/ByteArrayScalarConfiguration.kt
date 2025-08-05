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
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Configuration
class ByteArrayScalarConfiguration {
    @Bean
    fun byteArrayScalar(): GraphQLScalarType {
        return GraphQLScalarType.newScalar()
            .name("ByteArray")
            .description("Array of bytes.")
            .coercing(ByteArrayCoercing())
            .build()
    }

    @Bean
    fun runtimeWiringConfigurerForByteArrayScalar(): RuntimeWiringConfigurer {
        return RuntimeWiringConfigurer { builder: RuntimeWiring.Builder? -> builder!!.scalar(byteArrayScalar()) }
    }

    class ByteArrayCoercing : Coercing<ByteArray, String> {
        override fun serialize(
            dataFetcherResult: Any,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): String {
            when (dataFetcherResult) {
                is ByteArray -> return runCatching { serializer(dataFetcherResult) }.getOrElse {
                    throw CoercingSerializeException("Could not serialize ByteArray.", it)
                }

                else -> throw CoercingSerializeException("Given dataFetcherResult is not an ByteArray type.")
            }
        }

        override fun parseValue(
            input: Any,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): ByteArray {
            when (input) {
                is String -> return runCatching { parser(input) }.getOrElse {
                    throw CoercingParseValueException("Could not parse input $input to ByteArray.", it)
                }

                else -> throw CoercingParseValueException("Given input is not an String type.")
            }
        }

        override fun parseLiteral(
            input: Value<*>,
            variables: CoercedVariables,
            graphQLContext: GraphQLContext,
            locale: Locale
        ): ByteArray {
            when (input) {
                is StringValue -> return runCatching { parser(input.value) }.getOrElse {
                    throw CoercingParseLiteralException("Could not parse input ${input.value} to ByteArray.", it)
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

        @OptIn(ExperimentalEncodingApi::class)
        private fun serializer(bytes: ByteArray): String {
            return Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT).encode(bytes)
        }

        @OptIn(ExperimentalEncodingApi::class)
        private fun parser(input: String): ByteArray {
            return Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT).decode(input)
        }
    }
}