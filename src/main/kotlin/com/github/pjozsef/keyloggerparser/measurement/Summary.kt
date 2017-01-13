package com.github.pjozsef.keyloggerparser.measurement

import com.github.pjozsef.keyloggerparser.event.KeyEvent
import com.github.pjozsef.keyloggerparser.event.PressType
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.descriptive.StatisticalSummary
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues
import rx.Observable
import rx.Subscriber
import java.util.*

data class Summary(
        val dwellStatistics: DescriptiveStatistics,
        val dwellDistribution: NormalDistribution,
        val downStatistics: DescriptiveStatistics,
        val downDistribution: NormalDistribution,
        val upStatistics: DescriptiveStatistics,
        val upDistribution: NormalDistribution,
        val extraInfo: Info
) {
    data class Info(
            val originalDwellTimeCount: Int,
            val filteredDwellTimeCount: Int,
            val originalDownFlightTimeCount: Int,
            val filteredDownFlightTimeCount: Int,
            val originalUpFlightTimeCount: Int,
            val filteredUpFlightTimeCount: Int
    )

    companion object {
        fun of(input: String, dwellFilterTime: Int = 250, flightFilterTime: Int = 400): Observable<Summary> {
            val lines = Observable.just(input).flatMapIterable { it.split("\n") }.filter { it.isNotBlank() }
            val keyEvents = lines
                    .map { KeyEvent.of(it) }
                    .distinctUntilChanged { prev, current -> prev.char == current.char && prev.type == current.type }
            val filteredEvents = keyEvents//.filter { it.char.matches(Regex("[a-z]|(space)")) }
            val sortedEvents = sortedKeyEventsObservable(filteredEvents)
            val dwellTimes = sortedEvents
                    .buffer(2)
                    .map { list -> DwellTime.of(list[0], list[1]) }
            val dwellTimesFiltered = dwellTimes.filter { it.duration < dwellFilterTime }
                    .map { it.duration.toDouble() }

            val dwellTimesStat = dwellTimesFiltered
                    .toList()
                    .map { DescriptiveStatistics(it.toDoubleArray()) }

            val downs = sortedEvents.filter { it.type == PressType.DOWN }
            val ups = sortedEvents.filter { it.type == PressType.UP }

            val downsFiltered = downs
                    .buffer(2)
                    .filter { it.size == 2 }
                    .map { list -> FlightTime.of(list[0], list[1]) }
                    .map { it.duration.toDouble() }
                    .filter { 0 < it && it < flightFilterTime }

            val downFlightTimesStat = downsFiltered
                    .toList()
                    .map { DescriptiveStatistics(it.toDoubleArray()) }

            val upsFiltered = ups
                    .buffer(2)
                    .filter { it.size == 2 }
                    .map { list -> FlightTime.of(list[0], list[1]) }
                    .map { it.duration.toDouble() }
                    .filter { 0 < it && it < flightFilterTime }

            val upFlightTimesStat = upsFiltered
                    .toList()
                    .map { DescriptiveStatistics(it.toDoubleArray()) }

            val info = Observable.zip(
                    dwellTimes.count(),
                    dwellTimesFiltered.count(),
                    downs.count(),
                    downsFiltered.count(),
                    ups.count(),
                    upsFiltered.count(),
                    ::Info)

            return Observable.zip(
                    dwellTimesStat,
                    downFlightTimesStat,
                    upFlightTimesStat,
                    info){dwellStat, downStat, upStat, info ->
                Summary(
                        dwellStat,
                        dwellStat.distribution(),
                        downStat,
                        downStat.distribution(),
                        upStat,
                        upStat.distribution(),
                        info
                )
            }
        }

        private fun StatisticalSummary.distribution() = NormalDistribution(this.mean, this.standardDeviation)

        private fun sortedKeyEventsObservable(keyEvents: Observable<KeyEvent>): Observable<KeyEvent> {
            val obs = Observable.create(object : Observable.OnSubscribe<KeyEvent> {
                private val map: MutableMap<String, KeyEvent> = HashMap()
                private var warningsUp = 0
                private var warningsDown = 0

                override fun call(subscriber: Subscriber<in KeyEvent>) {
                    keyEvents.subscribe(
                            { keyEvent ->
                                try {
                                    when (keyEvent.type) {
                                        PressType.DOWN -> {
                                            handleDownEvent(keyEvent)
                                        }
                                        PressType.UP -> {
                                            handleUpEvent(keyEvent, subscriber)
                                        }
                                    }
                                } catch (ex: Exception) {
                                    subscriber.onError(ex)
                                }
                            },
                            { error -> subscriber.onError(error) },
                            {
                                /*println("Number of multiple down events: $warningsDown")
                                println("Number of multiple up events: $warningsUp")
                                warningsDown = 0
                                warningsUp = 0*/
                                subscriber.onCompleted()
                            }
                    )
                }

                private fun handleDownEvent(downEvent: KeyEvent) {
                    map[downEvent.char]?.let {
                        warningsDown++
                    }
                    map[downEvent.char] = downEvent
                }

                private fun handleUpEvent(upEvent: KeyEvent, subscriber: Subscriber<in KeyEvent>) {
                    map[upEvent.char]?.let { downEvent ->
                        map.remove(downEvent.char)
                        subscriber.onNext(downEvent)
                        subscriber.onNext(upEvent)
                    } ?: let {
                        warningsUp++
                    }
                }

            })
            return obs
        }
    }
}