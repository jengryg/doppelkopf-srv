package game.doppelkopf.adapter.graphql.core.hand.dto

import game.doppelkopf.domain.hand.enums.DeclarationOption
import java.util.*

data class DeclareInput(
    val handId: UUID,
    val declaration: DeclarationOption
)
