package game.doppelkopf.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.meta.api")
class ApiMetaInformation @ConstructorBinding constructor(
    /**
     * REQUIRED. The title of the application.
     */
    val title: String,

    /**
     * A description of the API. CommonMark syntax, see https://spec.commonmark.org/, MAY be used for rich text representation.
     */
    val description: String? = null,

    /**
     * The contact information for the exposed API.
     */
    @DefaultValue
    val contact: OpenApiContact,

    /**
     * The license information for the exposed API.
     */
    @DefaultValue
    val license: OpenApiLicence,

    /**
     * A URL to the Terms of Service for the API. MUST be in the format of a URL or null.
     */
    val termsOfService: String? = null,
) {
    class OpenApiContact(
        /**
         * The identifying name of the contact person/organization.
         */
        val name: String? = null,

        /**
         * The URL pointing to the contact information. MUST be in the format of a URL.
         */
        val email: String? = null,

        /**
         * The email address of the contact person/organization. MUST be in the format of an email address.
         */
        val url: String? = null,
    )

    class OpenApiLicence(
        /**
         * REQUIRED. The license name used for the API.
         */
        val name: String? = null,

        /**
         * A URL to the license used for the API. MUST be in the format of a URL.
         */
        val url: String? = null,
    )
}