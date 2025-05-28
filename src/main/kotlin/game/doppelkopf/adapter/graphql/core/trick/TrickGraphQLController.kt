package game.doppelkopf.adapter.graphql.core.trick

import game.doppelkopf.adapter.graphql.core.trick.dto.Trick
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.security.UserDetails
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class TrickGraphQLController(
    private val trickPersistence: TrickPersistence,
) {
    @QueryMapping
    fun trick(
        @Argument id: UUID,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): Trick {
        return Trick(trickPersistence.load(id), userDetails.entity)
    }
}