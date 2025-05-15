package game.doppelkopf.domain.scores

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.round.enums.DefiniteRoundWinner
import game.doppelkopf.utils.Teamed
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GameResultTableTest : BaseUnitTest() {

    @Nested
    inner class GamesWithoutTargetReductions {
        @Test
        fun `no calls, re 190, ko 50`() {
            val table = GameResultTable(
                Teamed(re = 190, ko = 50),
                Teamed(re = false, ko = false),
                Teamed(listOf(), listOf())
            )

            assertThat(table.targets.re).isEqualTo(121)
            assertThat(table.targets.ko).isEqualTo(120)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.lostScore.p60).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(3)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `no calls, re 28, ko 212`() {
            val table = GameResultTable(
                Teamed(re = 28, ko = 212),
                Teamed(re = false, ko = false),
                Teamed(listOf(), listOf())
            )

            assertThat(table.targets.re).isEqualTo(121)
            assertThat(table.targets.ko).isEqualTo(120)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.lostScore.p90).isEqualTo(Team.KO)
            assertThat(table.lostScore.p60).isEqualTo(Team.KO)
            assertThat(table.lostScore.p30).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(4)
        }

        @Test
        fun `no calls re 240, ko no tricks`() {
            val table = GameResultTable(
                Teamed(re = 240, ko = 0),
                Teamed(re = false, ko = true),
                Teamed(listOf(), listOf())
            )

            assertThat(table.targets.re).isEqualTo(121)
            assertThat(table.targets.ko).isEqualTo(120)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.lostScore.p60).isEqualTo(Team.RE)
            assertThat(table.lostScore.p30).isEqualTo(Team.RE)
            assertThat(table.lostScore.p00).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(5)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re called, re 130, ko 110`() {
            val table = GameResultTable(
                Teamed(re = 130, ko = 110),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120), listOf())
            )

            assertThat(table.targets.re).isEqualTo(121)
            assertThat(table.targets.ko).isEqualTo(120)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.re).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(3)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re called, re 222, ko 18`() {
            val table = GameResultTable(
                Teamed(re = 222, ko = 18),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120), listOf())
            )

            assertThat(table.targets.re).isEqualTo(121)
            assertThat(table.targets.ko).isEqualTo(120)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.re).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.lostScore.p60).isEqualTo(Team.RE)
            assertThat(table.lostScore.p30).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(6)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `ko called, re 80, ko 160`() {
            val table = GameResultTable(
                Teamed(re = 80, ko = 160),
                Teamed(re = false, ko = false),
                Teamed(listOf(), listOf(CallType.UNDER_120))
            )

            assertThat(table.targets.re).isEqualTo(120)
            assertThat(table.targets.ko).isEqualTo(121)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.lostScore.p90).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(4)
        }

        @Test
        fun `ko called, re 120, ko 120`() {
            val table = GameResultTable(
                Teamed(re = 120, ko = 120),
                Teamed(re = false, ko = false),
                Teamed(listOf(), listOf(CallType.UNDER_120))
            )

            assertThat(table.targets.re).isEqualTo(120)
            assertThat(table.targets.ko).isEqualTo(121)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basicCalls.ko).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(3)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re, ko called, re 120, ko 120`() {
            val table = GameResultTable(
                Teamed(re = 120, ko = 120),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120), listOf(CallType.UNDER_120))
            )

            assertThat(table.targets.re).isEqualTo(121)
            assertThat(table.targets.ko).isEqualTo(120)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)
            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(5)
        }

        @Test
        fun `re ko called, re 151, ko 89`() {
            val table = GameResultTable(
                Teamed(re = 151, ko = 89),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120), listOf(CallType.UNDER_120))
            )

            assertThat(table.targets.re).isEqualTo(121)
            assertThat(table.targets.ko).isEqualTo(120)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basicCalls.re).isEqualTo(Team.RE)
            assertThat(table.basicCalls.ko).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(6)
            assertThat(table.points.ko).isEqualTo(0)
        }
    }

    @Nested
    inner class GamesWithSuccessfulTargetReductions {
        @Test
        fun `re 90 called, re 160, ko 80`() {
            val table = GameResultTable(
                Teamed(re = 160, ko = 80),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120, CallType.UNDER_90), listOf())
            )

            assertThat(table.targets.re).isEqualTo(151)
            assertThat(table.targets.ko).isEqualTo(90)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.re).isEqualTo(Team.RE)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(5)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re 90 called, re 195, ko 45`() {
            val table = GameResultTable(
                Teamed(re = 195, ko = 45),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120, CallType.UNDER_90), listOf())
            )

            assertThat(table.targets.re).isEqualTo(151)
            assertThat(table.targets.ko).isEqualTo(90)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.re).isEqualTo(Team.RE)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.lostScore.p60).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(6)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re 90 called, re 240, ko no tricks`() {
            val table = GameResultTable(
                Teamed(re = 240, ko = 0),
                Teamed(re = false, ko = true),
                Teamed(listOf(CallType.UNDER_120, CallType.UNDER_90), listOf())
            )

            assertThat(table.targets.re).isEqualTo(151)
            assertThat(table.targets.ko).isEqualTo(90)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.re).isEqualTo(Team.RE)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.lostScore.p60).isEqualTo(Team.RE)
            assertThat(table.lostScore.p30).isEqualTo(Team.RE)
            assertThat(table.lostScore.p00).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(8)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re, ko 90 called, re 80, ko 160`() {
            val table = GameResultTable(
                Teamed(re = 80, ko = 160),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120), listOf(CallType.UNDER_120, CallType.UNDER_90))
            )

            assertThat(table.targets.re).isEqualTo(90)
            assertThat(table.targets.ko).isEqualTo(151)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)
            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.underCalls.ko.p90).isEqualTo(Team.KO)

            assertThat(table.lostScore.p90).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(7)
        }

        @Test
        fun `ko 90 60 called, re 28, ko 212`() {
            val table = GameResultTable(
                Teamed(re = 28, ko = 212),
                Teamed(re = false, ko = false),
                Teamed(listOf(), listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60))
            )

            assertThat(table.targets.re).isEqualTo(60)
            assertThat(table.targets.ko).isEqualTo(181)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.underCalls.ko.p90).isEqualTo(Team.KO)
            assertThat(table.underCalls.ko.p60).isEqualTo(Team.KO)

            assertThat(table.lostScore.p90).isEqualTo(Team.KO)
            assertThat(table.lostScore.p60).isEqualTo(Team.KO)
            assertThat(table.lostScore.p30).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(8)
        }

        @Test
        fun `re 90 60 30 00 called, re 240, ko no tricks`() {
            val table = GameResultTable(
                Teamed(re = 240, ko = 0),
                Teamed(re = false, ko = true),
                Teamed(
                    listOf(
                        CallType.UNDER_120,
                        CallType.UNDER_90,
                        CallType.UNDER_60,
                        CallType.UNDER_30,
                        CallType.NO_TRICKS
                    ), listOf()
                )
            )

            assertThat(table.targets.re).isEqualTo(240)
            assertThat(table.targets.ko).isEqualTo(0)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.re).isEqualTo(Team.RE)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.RE)
            assertThat(table.underCalls.re.p60).isEqualTo(Team.RE)
            assertThat(table.underCalls.re.p30).isEqualTo(Team.RE)
            assertThat(table.underCalls.re.p00).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.lostScore.p60).isEqualTo(Team.RE)
            assertThat(table.lostScore.p30).isEqualTo(Team.RE)
            assertThat(table.lostScore.p00).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(11)
            assertThat(table.points.ko).isEqualTo(0)
        }
    }

    @Nested
    inner class GamesWithFailedTargetReductions {
        @Test
        fun `re 90 called, re 130, ko 110`() {
            val table = GameResultTable(
                Teamed(re = 130, ko = 110),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120, CallType.UNDER_90), listOf())
            )

            assertThat(table.targets.re).isEqualTo(151)
            assertThat(table.targets.ko).isEqualTo(90)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(4)
        }

        @Test
        fun `re 90 60, ko called, re 170, ko 70`() {
            val table = GameResultTable(
                Teamed(re = 170, ko = 70),
                Teamed(re = false, ko = false),
                Teamed(listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60), listOf(CallType.UNDER_120))
            )

            assertThat(table.targets.re).isEqualTo(181)
            assertThat(table.targets.ko).isEqualTo(60)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)
            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p60).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(7)
        }

        @Test
        fun `re 90 60, ko called, re 150, ko 90`() {
            val table = GameResultTable(
                Teamed(re = 150, ko = 90),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60),
                    listOf(CallType.UNDER_120)
                )
            )

            assertThat(table.targets.re).isEqualTo(181)
            assertThat(table.targets.ko).isEqualTo(60)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)
            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p60).isEqualTo(Team.KO)

            assertThat(table.beating.ko.p60).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(8)
        }

        @Test
        fun `ko 90 60 30 called, re 111, ko 129`() {
            val table = GameResultTable(
                Teamed(re = 111, ko = 129),
                Teamed(re = false, ko = false),
                Teamed(listOf(), listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60, CallType.UNDER_30))
            )

            assertThat(table.targets.re).isEqualTo(30)
            assertThat(table.targets.ko).isEqualTo(211)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.ko).isEqualTo(Team.RE)

            assertThat(table.underCalls.ko.p90).isEqualTo(Team.RE)
            assertThat(table.underCalls.ko.p60).isEqualTo(Team.RE)
            assertThat(table.underCalls.ko.p30).isEqualTo(Team.RE)

            assertThat(table.beating.re.p60).isEqualTo(Team.RE)
            assertThat(table.beating.re.p30).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(8)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re 90 60 30 00, ko called, re 217, ko 23`() {
            val table = GameResultTable(
                Teamed(re = 217, ko = 23),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(
                        CallType.UNDER_120,
                        CallType.UNDER_90,
                        CallType.UNDER_60,
                        CallType.UNDER_30,
                        CallType.NO_TRICKS
                    ), listOf(CallType.UNDER_120)
                )
            )

            assertThat(table.targets.re).isEqualTo(240)
            assertThat(table.targets.ko).isEqualTo(0)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)
            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p60).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p30).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p00).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(9)
        }

        @Test
        fun `re 90 60 30 00, ko called, re 151, ko 89`() {
            val table = GameResultTable(
                Teamed(re = 151, ko = 89),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(
                        CallType.UNDER_120,
                        CallType.UNDER_90,
                        CallType.UNDER_60,
                        CallType.UNDER_30,
                        CallType.NO_TRICKS
                    ), listOf(CallType.UNDER_120)
                )
            )

            assertThat(table.targets.re).isEqualTo(240)
            assertThat(table.targets.ko).isEqualTo(0)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)
            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p60).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p30).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p00).isEqualTo(Team.KO)

            assertThat(table.beating.ko.p30).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p00).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(11)
        }

        @Test
        fun `ko 90 60 called, re 212, ko 28`() {
            val table = GameResultTable(
                Teamed(re = 212, ko = 28),
                Teamed(re = false, ko = false),
                Teamed(listOf(), listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60))
            )

            assertThat(table.targets.re).isEqualTo(60)
            assertThat(table.targets.ko).isEqualTo(181)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.RE)

            assertThat(table.basic.winning).isEqualTo(Team.RE)

            assertThat(table.basicCalls.ko).isEqualTo(Team.RE)

            assertThat(table.underCalls.ko.p90).isEqualTo(Team.RE)
            assertThat(table.underCalls.ko.p60).isEqualTo(Team.RE)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.lostScore.p60).isEqualTo(Team.RE)
            assertThat(table.lostScore.p30).isEqualTo(Team.RE)

            assertThat(table.beating.re.p90).isEqualTo(Team.RE)
            assertThat(table.beating.re.p60).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(10)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re 90 60, ko 90 60 called, re 39, ko 181`() {
            val table = GameResultTable(
                Teamed(re = 39, ko = 181),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60),
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60)
                )
            )

            assertThat(table.targets.re).isEqualTo(60)
            assertThat(table.targets.ko).isEqualTo(60)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.KO)

            assertThat(table.basic.winning).isEqualTo(Team.KO)

            assertThat(table.basicCalls.re).isEqualTo(Team.KO)
            assertThat(table.basicCalls.ko).isEqualTo(Team.KO)

            assertThat(table.underCalls.re.p90).isEqualTo(Team.KO)
            assertThat(table.underCalls.re.p60).isEqualTo(Team.KO)
            assertThat(table.underCalls.ko.p90).isEqualTo(Team.KO)
            assertThat(table.underCalls.ko.p60).isEqualTo(Team.KO)

            assertThat(table.lostScore.p90).isEqualTo(Team.KO)
            assertThat(table.lostScore.p60).isEqualTo(Team.KO)

            assertThat(table.beating.ko.p90).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p60).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(0)
            assertThat(table.points.ko).isEqualTo(13)
        }
    }

    @Nested
    inner class GamesWithNoWinner {
        @Test
        fun `re 90 60, ko 90 60 called, re 100, ko 140`() {
            val table = GameResultTable(
                Teamed(re = 100, ko = 140),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60),
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60)
                )
            )

            assertThat(table.targets.re).isEqualTo(60)
            assertThat(table.targets.ko).isEqualTo(60)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.DRAW)

            assertThat(table.beating.re.p60).isEqualTo(Team.RE)
            assertThat(table.beating.ko.p90).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p60).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(1)
            assertThat(table.points.ko).isEqualTo(2)
        }

        @Test
        fun `re 90 60, ko 90 60 called, re 170, ko 70`() {
            val table = GameResultTable(
                Teamed(re = 170, ko = 70),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60),
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60)
                )
            )

            assertThat(table.targets.re).isEqualTo(60)
            assertThat(table.targets.ko).isEqualTo(60)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.DRAW)

            assertThat(table.lostScore.p90).isEqualTo(Team.RE)
            assertThat(table.beating.re.p90).isEqualTo(Team.RE)
            assertThat(table.beating.re.p60).isEqualTo(Team.RE)

            assertThat(table.points.re).isEqualTo(3)
            assertThat(table.points.ko).isEqualTo(0)
        }

        @Test
        fun `re 90 60 30 0, ko 90 60 30 called, re 123, ko 117`() {
            val table = GameResultTable(
                Teamed(re = 123, ko = 117),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(
                        CallType.UNDER_120,
                        CallType.UNDER_90,
                        CallType.UNDER_60,
                        CallType.UNDER_30,
                        CallType.NO_TRICKS
                    ),
                    listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60, CallType.UNDER_30)
                )
            )

            assertThat(table.targets.re).isEqualTo(30)
            assertThat(table.targets.ko).isEqualTo(0)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.DRAW)

            assertThat(table.beating.re.p90).isEqualTo(Team.RE)
            assertThat(table.beating.re.p60).isEqualTo(Team.RE)
            assertThat(table.beating.re.p30).isEqualTo(Team.RE)
            assertThat(table.beating.ko.p60).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p30).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p00).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(3)
            assertThat(table.points.ko).isEqualTo(3)
        }

        @Test
        fun `re 90 60 30 00, ko 90 60 30 called, re 120, ko 120`() {
            val table = GameResultTable(
                Teamed(re = 120, ko = 120),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(
                        CallType.UNDER_120,
                        CallType.UNDER_90,
                        CallType.UNDER_60,
                        CallType.UNDER_30,
                        CallType.NO_TRICKS
                    ), listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60, CallType.UNDER_30)
                )
            )

            assertThat(table.targets.re).isEqualTo(30)
            assertThat(table.targets.ko).isEqualTo(0)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.DRAW)

            assertThat(table.beating.re.p90).isEqualTo(Team.RE)
            assertThat(table.beating.re.p60).isEqualTo(Team.RE)
            assertThat(table.beating.re.p30).isEqualTo(Team.RE)
            assertThat(table.beating.ko.p90).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p60).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p30).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p00).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(3)
            assertThat(table.points.ko).isEqualTo(4)
        }

        @Test
        fun `re 90 60 30 00, ko 90 60 30 called, re 80, ko 160`() {
            val table = GameResultTable(
                Teamed(re = 80, ko = 160),
                Teamed(re = false, ko = false),
                Teamed(
                    listOf(
                        CallType.UNDER_120,
                        CallType.UNDER_90,
                        CallType.UNDER_60,
                        CallType.UNDER_30,
                        CallType.NO_TRICKS
                    ), listOf(CallType.UNDER_120, CallType.UNDER_90, CallType.UNDER_60, CallType.UNDER_30)
                )
            )

            assertThat(table.targets.re).isEqualTo(30)
            assertThat(table.targets.ko).isEqualTo(0)

            assertThat(table.winner).isEqualTo(DefiniteRoundWinner.DRAW)

            assertThat(table.lostScore.p90).isEqualTo(Team.KO)

            assertThat(table.beating.re.p30).isEqualTo(Team.RE)
            assertThat(table.beating.ko.p90).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p60).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p30).isEqualTo(Team.KO)
            assertThat(table.beating.ko.p00).isEqualTo(Team.KO)

            assertThat(table.points.re).isEqualTo(1)
            assertThat(table.points.ko).isEqualTo(5)
        }
    }
}