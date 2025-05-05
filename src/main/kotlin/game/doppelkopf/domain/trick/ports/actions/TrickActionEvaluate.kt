package game.doppelkopf.domain.trick.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Evaluate the trick given by [trickId].
 */
class TrickActionEvaluate(
    override val user: UserDetails,
    override val trickId: UUID
) : ITrickAction