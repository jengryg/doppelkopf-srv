package game.doppelkopf.core.common.enums

/**
 * [RoundContract] defines the basic rules of the round to play.
 */
enum class RoundContract(
    val roundContractPublic: RoundContractPublic
) {
    UNDECIDED(RoundContractPublic.UNDECIDED),

    NORMAL(RoundContractPublic.NORMAL),
    SILENT_MARRIAGE(RoundContractPublic.NORMAL),

    WEDDING(RoundContractPublic.WEDDING),

    SOLO(RoundContractPublic.SOLO),
}

/**
 * [RoundContractPublic] is used to communicate the basic rules of the round to the players.
 * It hides the [RoundContract.SILENT_MARRIAGE] variant, that must not be shown to other players.
 */
enum class RoundContractPublic {
    UNDECIDED,
    NORMAL,
    WEDDING,
    SOLO
}