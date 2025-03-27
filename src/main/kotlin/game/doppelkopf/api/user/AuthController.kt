package game.doppelkopf.api.user

import game.doppelkopf.api.user.dto.PublicUserInfoDto
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.security.UserDetails
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController : Logging {
    private val log = logger()

    @Operation(
        summary = "Endpoint for the login page url of spring.",
        description = "This endpoint can be used to check the current login status of the user."
    )
    @GetMapping("/status")
    fun status(@AuthenticationPrincipal userDetails: UserDetails?): ResponseEntity<PublicUserInfoDto> {
        log.atDebug()
            .setMessage("Current user login status obtained.")
            .addKeyValue("userDetails") { userDetails?.user }
            .log()

        if (userDetails != null) {
            return ResponseEntity.ok(
                PublicUserInfoDto(
                    id = userDetails.user.id,
                    name = userDetails.user.username
                )
            )
        } else {
            return ResponseEntity.noContent().build()
        }
    }
}