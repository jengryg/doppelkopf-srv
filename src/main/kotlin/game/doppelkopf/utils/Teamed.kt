package game.doppelkopf.utils

import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.enums.Team

/**
 * [Teamed] is strictly typed wrapper for two instances of generic type [T] that are tagged with [re] and [ko]
 * properties.
 */
data class Teamed<out T>(
    val re: T,
    val ko: T
) {
    /**
     * @return the instance of [T] tagged with the [DefiniteTeam]
     */
    fun get(team: DefiniteTeam): T {
        return when (team) {
            DefiniteTeam.RE -> re
            DefiniteTeam.KO -> ko
        }
    }

    /**
     * @return the instance of [T] tagged with the [Team], [re] for [Team.RE] and [ko] for [Team.KO], null otherwise.
     */
    fun get(team: Team): T? {
        return when (team) {
            Team.RE -> re
            Team.KO -> ko
            else -> null
        }
    }

    /**
     * @return a [Teamed] containing the result of [block] applied to [re] and [ko]
     */
    fun <S> map(block: (T) -> S): Teamed<S> {
        return Teamed(
            re = block(re),
            ko = block(ko)
        )
    }

    /**
     * @return a [Teamed] containing the result of [block] applied to [DefiniteTeam.RE], [re]
     * and [DefiniteTeam.KO], [ko] where the first argument supplied to block is the [DefiniteTeam], the second argument
     * is from this [Teamed]
     */
    fun <S> mapTagged(block: (DefiniteTeam, T) -> S): Teamed<S> {
        return Teamed(
            re = block(DefiniteTeam.RE, re),
            ko = block(DefiniteTeam.KO, ko)
        )
    }

    /**
     * @return a [Teamed] containing the result of [block] applied to the teamed pairs where the first argument supplied
     * to block is from this [Teamed] and the second argument is from [other].
     */
    fun <S, U> map(other: Teamed<U>, block: (T, U) -> S): Teamed<S> {
        return Teamed(
            re = block(re, other.re),
            ko = block(ko, other.ko)
        )
    }

    /**
     * @return a [Teamed] containing the result of [block] applied to the teamed triples where the first argument supplied
     * to block is the [DefiniteTeam], the second argument is from this [Teamed] and the third argument is from [other].
     */
    fun <S, U> mapTagged(other: Teamed<U>, block: (DefiniteTeam, T, U) -> S): Teamed<S> {
        return Teamed(
            re = block(DefiniteTeam.RE, re, other.re),
            ko = block(DefiniteTeam.KO, ko, other.ko)
        )
    }

    companion object {
        /**
         * Takes [iterable] that is assumed to contain exactly one element where [selector] returns [Team.RE]
         * and exactly one element where [selector] returns [Team.KO] and sorts it into a [Teamed].
         *
         * @return the teamed result or null if it is not constructable from [iterable] and [selector]
         */
        fun <T> from(iterable: Iterable<T>, selector: (T) -> Team): Teamed<T>? {
            val re = iterable.singleOrNull { selector(it) == Team.RE }
            val ko = iterable.singleOrNull { selector(it) == Team.KO }

            re ?: return null
            ko ?: return null

            return Teamed(re, ko)
        }
    }
}