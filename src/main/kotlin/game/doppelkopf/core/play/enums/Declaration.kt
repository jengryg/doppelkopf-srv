package game.doppelkopf.core.play.enums

/**
 * [Declaration] is made by the players at the beginning of the round in the auction phase.
 */
enum class Declaration(val declarationPublic: DeclarationPublic) {
    NOTHING(DeclarationPublic.NOTHING),
    HEALTHY(DeclarationPublic.HEALTHY),
    SILENT_MARRIAGE(DeclarationPublic.HEALTHY),
    RESERVATION(DeclarationPublic.RESERVATION),
}

/**
 * [DeclarationPublic] is used to communicate the choice of the players to each other.
 * It hides the [Declaration.SILENT_MARRIAGE] option, that must not be shown to other players.
 */
enum class DeclarationPublic {
    NOTHING,
    HEALTHY,
    RESERVATION,
}

/**
 * [DeclarationOption] is used to provide the choice to the player making it.
 * It prevents [Declaration.NOTHING] option, that is only for internal state management.
 */
enum class DeclarationOption(
    val internal: Declaration
) {
    HEALTHY(Declaration.HEALTHY),
    SILENT_MARRIAGE(Declaration.SILENT_MARRIAGE),
    RESERVATION(Declaration.RESERVATION),
}