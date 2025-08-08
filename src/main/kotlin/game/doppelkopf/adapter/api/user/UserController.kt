package game.doppelkopf.adapter.api.user

import game.doppelkopf.adapter.api.user.dto.PublicUserInfoResponse
import game.doppelkopf.adapter.api.user.dto.UserRegisterRequest
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.adapter.persistence.model.user.UserPersistence
import game.doppelkopf.adapter.persistence.model.user.UserRepository
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.user.enums.Authority
import game.doppelkopf.errors.ApplicationRuntimeException
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userPersistence: UserPersistence
) {
    @Operation(
        summary = "Create a new user.",
        description = "This endpoint can be used to create a new user."
    )
    @PostMapping("")
    fun register(
        @RequestBody @Valid registerDto: UserRegisterRequest
    ): ResponseEntity<PublicUserInfoResponse> {
        if (registerDto.password != registerDto.passwordConfirm) {
            throw InvalidActionException("Passwords do not match.")
        }

        if (userRepository.findByUsername(registerDto.username) != null) {
            throw ApplicationRuntimeException(
                status = HttpStatus.CONFLICT,
            ).apply {
                setTitle("Username already exists.")
                setDetail("There is already an account with the username ${registerDto.username}.")
            }
        }

        val user = UserEntity(
            username = registerDto.username,
            password = passwordEncoder.encode(registerDto.password),
            authority = Authority.USER,
        ).let {
            userRepository.save(it)
        }

        return PublicUserInfoResponse(user).let {
            ResponseEntity.created(
                UriComponentsBuilder.newInstance().path("/v1/users/{id}").build(it.id)
            ).body(it)
        }
    }

    @Operation(
        summary = "Show user.",
        description = "Shows the public information about the user with the specified id."
    )
    @GetMapping("/{id}")
    fun show(
        @PathVariable id: UUID
    ): ResponseEntity<PublicUserInfoResponse> {
        return ResponseEntity.ok(
            PublicUserInfoResponse(userPersistence.load(id))
        )
    }
}

