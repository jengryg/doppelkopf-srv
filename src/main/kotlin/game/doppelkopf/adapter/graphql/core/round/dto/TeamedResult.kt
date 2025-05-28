package game.doppelkopf.adapter.graphql.core.round.dto

data class TeamedResult(
    val re: RoundResult,
    val ko: RoundResult,
)