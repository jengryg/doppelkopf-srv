package game.doppelkopf.adapter.api

import game.doppelkopf.CommonConfig
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(
    private val apiMetaInformation: ApiMetaInformation,
    private val commonConfig: CommonConfig,
) {
    @Bean
    fun openApiCustomizer(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            openApi.info(
                Info()
                    .title("${apiMetaInformation.title} [${commonConfig.stage}]")
                    .description(apiMetaInformation.description ?: "")
                    .version(commonConfig.version)
            )

            apiMetaInformation.run {
                if (!contact.name.isNullOrBlank() || !contact.url.isNullOrBlank() || !contact.email.isNullOrBlank()) {
                    openApi.info.contact(
                        Contact().name(contact.name).url(contact.url).email(contact.email)
                    )
                }

                if (!license.name.isNullOrBlank() || !license.url.isNullOrBlank()) {
                    openApi.info.license(
                        License().name(license.name).url(license.url)
                    )
                }

                if (!termsOfService.isNullOrBlank()) {
                    openApi.info.termsOfService(termsOfService)
                }
            }
        }
    }
}