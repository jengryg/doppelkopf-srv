package game.doppelkopf.instrumentation.micrometer

import io.micrometer.core.instrument.Tags

/**
 * Extension function to [Tags] that returns a new [Tags] instance by merging this collection and the tags returned by
 * [IMicrometerTags.tags]. In case of duplicated keys, the value from [other] overwrites the one from [this].
 *
 * @param other the [IMicrometerTags] to add to [this]
 * @return a new [Tags] instance containing the merge result
 *
 * @see [Tags]
 */
fun Tags.and(other: IMicrometerTags): Tags {
    return this.and(other.tags())
}

/**
 * Extension function to [Tags] that returns a new [Tags] instance by merging [this] collection with the flattened
 * collections of the given iterable of [IMicrometerTags]. In case of duplicated keys, the value from the
 * [other] overwrites the one from [this]. Duplicates inside [other] resolve to the last value being used.
 *
 * @param other the iterable of [IMicrometerTags] to add to [this]
 * @return a new [Tags] instance containing the merge result
 *
 * @see [Tags]
 */
fun Tags.and(other: Iterable<IMicrometerTags?>): Tags {
    return this.and(other.mapNotNull { it?.tags() }.flatten())
}