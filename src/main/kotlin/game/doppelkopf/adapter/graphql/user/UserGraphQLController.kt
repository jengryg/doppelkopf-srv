package game.doppelkopf.adapter.graphql.user

import game.doppelkopf.adapter.graphql.user.dto.PrivateUser
import game.doppelkopf.adapter.graphql.user.dto.PublicUser
import game.doppelkopf.adapter.persistence.model.user.UserPersistence
import game.doppelkopf.security.UserDetails
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class UserGraphQLController(
    private val userPersistence: UserPersistence
) {
    @QueryMapping
    fun privateUser(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): PrivateUser {
        return PrivateUser(userDetails.entity)
    }

    @QueryMapping
    fun publicUser(
        @Argument id: UUID
    ): PublicUser {
        return userPersistence.load(id).let { PublicUser(it) }
    }
}