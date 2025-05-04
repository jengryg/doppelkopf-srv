package game.doppelkopf.domain.round.enums

/**
 * [RoundContract] defines the basic rules of the round to play.
 */
enum class RoundContract(
    val roundContractPublic: RoundContractPublic
) {
    UNDECIDED(RoundContractPublic.UNDECIDED),

    NORMAL(RoundContractPublic.NORMAL),

    // SILENT_MARRIAGE must be indicated as NORMAL to hide it.
    SILENT_MARRIAGE(RoundContractPublic.NORMAL),

    // MARRIAGE starts in unresolved state and turns to resolved or SOLO during the round.
    MARRIAGE_UNRESOLVED(RoundContractPublic.MARRIAGE),
    MARRIAGE_RESOLVED(RoundContractPublic.MARRIAGE),
    MARRIAGE_SOLO(RoundContractPublic.MARRIAGE_SOLO),

    SOLO(RoundContractPublic.SOLO),
}

/**
 * [RoundContractPublic] is used to communicate the basic rules of the round to the players.
 * It hides the [RoundContract.SILENT_MARRIAGE] variant, that must not be shown to other players.
 */
enum class RoundContractPublic {
    UNDECIDED,
    NORMAL,
    MARRIAGE,
    MARRIAGE_SOLO,
    SOLO
}