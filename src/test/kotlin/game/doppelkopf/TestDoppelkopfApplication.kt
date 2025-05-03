package game.doppelkopf

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<DoppelkopfApplication>()
        .with(TestcontainersConfiguration::class)
        .run(*args)
}