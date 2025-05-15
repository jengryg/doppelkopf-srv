package game.doppelkopf.domain.call.enums

import game.doppelkopf.utils.Teamed

enum class CallType(
    /**
     * A public string representation for this call based on the team.
     */
    val publicIdentifiers: Teamed<String>,

    /**
     * The higher this number is, the more restrictive is the call.
     */
    val orderIndex: Int,

    /**
     * The call can only be made if equal or fewer cards were played by the player.
     */
    val cardLimit: Int,

    /**
     * The point value of this call.
     */
    val points: Int,

    /**
     * If this represents a [CallTypeTargetReducing], this property is set to the corresponding value, otherwise null.
     */
    val reducingType: CallTypeTargetReducing?
) {
    // team declaring calls, these are worth 2 points
    UNDER_120(
        Teamed("RE", "KO"),
        0,
        1,
        2,
        null
    ),

    // target lowering calls, these are worth 1 point
    UNDER_90(
        Teamed("RE 90", "KO 90"),
        1,
        2,
        1,
        CallTypeTargetReducing.UNDER_90
    ),
    UNDER_60(
        Teamed("RE 60", "KO 60"),
        2,
        3,
        1,
        CallTypeTargetReducing.UNDER_60
    ),
    UNDER_30(
        Teamed("RE 30", "KO 30"),
        3,
        4,
        1,
        CallTypeTargetReducing.UNDER_30
    ),
    NO_TRICKS(
        Teamed("RE BLANK", "KO BLANK"),
        4,
        5,
        1,
        CallTypeTargetReducing.NO_TRICKS
    );

    fun getPrevious(): CallType? {
        return when (this) {
            UNDER_120 -> null
            UNDER_90 -> UNDER_120
            UNDER_60 -> UNDER_90
            UNDER_30 -> UNDER_60
            NO_TRICKS -> UNDER_30
        }
    }
}

enum class CallTypeTargetReducing(
    val callType: CallType,
    val reduceTo: Int,
    val increaseTo: Int,
) {
    UNDER_90(CallType.UNDER_90, 90, 151),
    UNDER_60(CallType.UNDER_60, 60, 181),
    UNDER_30(CallType.UNDER_30, 30, 211),
    NO_TRICKS(CallType.NO_TRICKS, 0, 240),
}