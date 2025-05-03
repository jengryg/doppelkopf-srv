package game.doppelkopf.core.common.enums

enum class RoundOperation {
    /**
     * This operation can be used to trigger the evaluation of the declarations.
     */
    DECLARE_EVALUATION,

    /**
     * This operation can be used to trigger the evaluation of the biddings.
     */
    BID_EVALUATION,

    /**
     * This operation can be used to trigger the marriage resolver.
     */
    MARRIAGE_RESOLVER
}