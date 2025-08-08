@file:Suppress("CanUnescapeDollarLiteral")

package game.doppelkopf.adapter.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(
    description = "User data required to register."
)
class UserRegisterRequest(
    @field:Schema(
        description = "The name of the user to create."
    )
    @field:NotBlank
    @field:Size(min = 3, max = 16)
    val username: String,

    @field:Schema(
        description = "The password of the user to register."
    )
    @field:NotBlank
    @field:Size(min = 8, max = 256)
    val password: String,

    @field:Schema(
        description = "The password of the user to register repeated."
    )
    @field:NotBlank
    @field:Size(min = 8, max = 256)
    val passwordConfirm: String,
)