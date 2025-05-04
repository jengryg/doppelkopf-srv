package game.doppelkopf.domain.hand.ports.commands

import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Declare the [declaration] on the hand given by [handId] where [user] must be the player of the hand.
 */
class HandCommandDeclare(
    override val user: UserDetails,
    override val handId: UUID,
    val declaration: DeclarationOption
) : IHandCommand {
    override fun getSlug() = "Hand:Declare"
}