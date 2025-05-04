package game.doppelkopf.domain.user.enums

/**
 * We use a basic authority model where each user can only have exactly one authority.
 * The available authorities are the default ones of spring security `ADMIN`, `USER`.
 * [NONE] will be translated to no authorities set on the user.
 */
enum class Authority(val authority: String) {
    ADMIN("ADMIN"),
    USER("USER"),
    NONE("")
}