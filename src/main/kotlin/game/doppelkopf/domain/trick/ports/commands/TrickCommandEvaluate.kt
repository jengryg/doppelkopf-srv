package game.doppelkopf.domain.trick.ports.commands

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Evaluate the trick given by [trickId].
 */
class TrickCommandEvaluate(
    override val user: UserDetails,
    override val trickId: UUID
) : ITrickCommand