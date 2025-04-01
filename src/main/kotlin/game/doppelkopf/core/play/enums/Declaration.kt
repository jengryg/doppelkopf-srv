package game.doppelkopf.core.play.enums

/**
 * [Declaration] defines the options players have to declare their hand.
 * The [publicDeclaration] represents the corresponding public representation of the choice, to hide [SILENT_MARRIAGE]
 * options from the other players.
 */
enum class Declaration(val publicDeclaration: PublicDeclaration) {
    NOTHING(PublicDeclaration.NOTHING),
    HEALTHY(PublicDeclaration.HEALTHY),
    SILENT_MARRIAGE(PublicDeclaration.HEALTHY),
    RESERVATION(PublicDeclaration.RESERVATION),
}

/**
 * [PublicDeclaration] is used to communicate the choice of the players to each other.
 * It hides the [Declaration.SILENT_MARRIAGE] option, that must not be shown to other players.
 */
enum class PublicDeclaration {
    NOTHING,
    HEALTHY,
    RESERVATION,
}