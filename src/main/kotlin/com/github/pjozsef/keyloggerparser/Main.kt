package com.github.pjozsef.keyloggerparser

import com.github.pjozsef.keyloggerparser.event.KeyEvent
import com.github.pjozsef.keyloggerparser.measurement.Summary
import rx.Observable
import java.nio.file.Files
import java.nio.file.Paths


fun main(args: Array<String>) {
    val start = System.currentTimeMillis()
    val DWELL_TIME_LIMIT = 250
    val FLIGHT_TIME_LIMIT = 400

    val content = String(Files.readAllBytes(Paths.get(args[0])))
    Summary.of(content, DWELL_TIME_LIMIT, FLIGHT_TIME_LIMIT).subscribe {
        println(it.dwellStatistics.mean)
        println(it.dwellStatistics.standardDeviation)
        val stat = it.dwellStatistics
        val distribution = it.dwellDistribution
        println(distribution.cumulativeProbability(stat.mean+stat.standardDeviation))
        println((distribution.cumulativeProbability(86.72129926339217))*200)
        println((1-distribution.cumulativeProbability(106.72129926339217))*200)
        println((1-distribution.cumulativeProbability(120.0))*200)
        println((1-distribution.cumulativeProbability(150.0))*200)

        println(it)
        //println(it.dwellDistribution.)
    }
    println("Finished within ${(System.currentTimeMillis()-start)/1000} seconds")
}

private fun printFilteredDistinct(filteredEvents: Observable<KeyEvent>) {
    println("Filtered characters:")
    filteredEvents.map { it.char }.distinct().subscribe(::println, Throwable::printStackTrace)
    println()
}

private fun printDistinct(keyEvents: Observable<KeyEvent>) {
    println("Distinct characters:")
    keyEvents
            .map { it.char }
            .distinct()
            .subscribe(
                    ::println,
                    Throwable::printStackTrace)
}

val input = """down 1471380337237 110 n
down 1471380337286 101 e
up 1471380337323 110 n
up 1471380337382 101 e
down 1471380337435 116 t
up 1471380337507 116 t
down 1471380337715 119 w
up 1471380337800 119 w
down 1471380337842 111 o
down 1471380337932 114 r
up 1471380337950 111 o
up 1471380338009 114 r
down 1471380338092 107 k
up 1471380338211 107 k
down 1471380338368 0 Return
up 1471380338461 0 Return
down 1471380339953 0 Alt_L
down 1471380339964 0 Control_L
down 1471380340128 0 Right
up 1471380340252 0 Right
down 1471380340407 0 Down
up 1471380340519 0 Down
up 1471380340847 0 Control_L
up 1471380340856 0 Alt_L
down 1471380341708 0 Alt_L
down 1471380341723 0 Control_L
down 1471380341835 0 Up
up 1471380341916 0 Up
down 1471380342209 0 Left
up 1471380342307 0 Left
down 1471380342836 0 Right
up 1471380342946 0 Right
down 1471380344052 0 Left
up 1471380344123 0 Left
down 1471380344230 0 Down
up 1471380344360 0 Down
up 1471380344441 0 Alt_L
up 1471380344505 0 Control_L
down 1471380345140 0 Alt_L
down 1471380345170 0 Control_L
down 1471380345296 0 Up
up 1471380345388 0 Up
up 1471380345420 0 Alt_L
up 1471380345457 0 Control_L
down 1471380345711 0 Alt_L
down 1471380345836 0 Tab
up 1471380345945 0 Tab
down 1471380346520 0 Tab
up 1471380346613 0 Tab
down 1471380347218 0 Left
up 1471380347286 0 Left
up 1471380348834 0 Alt_L
down 1471380364709 0 Control_L
down 1471380364720 0 Alt_L
down 1471380364929 0 P_Multiply
up 1471380365026 0 P_Multiply
up 1471380365080 0 Alt_L
up 1471380365098 0 Control_L
down 1471380392472 121 y
up 1471380392604 121 y
down 1471380392856 111 o
down 1471380392930 117 u
up 1471380393007 111 o
up 1471380393029 117 u
down 1471380393250 116 t
up 1471380393331 116 t
down 1471380393388 117 u
up 1471380393447 117 u
down 1471380393484 98 b
up 1471380393564 98 b
down 1471380393654 101 e
up 1471380393739 101 e
down 1471380393810 46 period
up 1471380393919 46 period
down 1471380393951 99 c
up 1471380394041 99 c
down 1471380394074 111 o
down 1471380394147 109 m
up 1471380394220 111 o
up 1471380394254 109 m
down 1471380394415 0 Return
up 1471380394521 0 Return"""