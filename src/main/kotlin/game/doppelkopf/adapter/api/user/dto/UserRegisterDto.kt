@file:Suppress("CanUnescapeDollarLiteral")

package game.doppelkopf.adapter.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

@Schema(
    description = "User data required to register."
)
class UserRegisterDto(
    @Schema(
        description = "The name of the user to create."
    )
    @field:NotBlank
    @field:Length(min = 3, max = 16)
    val username: String,

    @Schema(
        description = "The password of the user to register."
    )
    @field:NotBlank
    @field:Length(min = 8, max = 256)
    val password: String,

    @Schema(
        description = "The password of the user to register repeated."
    )
    @field:NotBlank
    @field:Length(min = 8, max = 256)
    val passwordConfirm: String,
)