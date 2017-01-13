package com.github.pjozsef.keyloggerparser.measurement

import com.github.pjozsef.keyloggerparser.event.KeyEvent
import com.github.pjozsef.keyloggerparser.event.PressType

data class FlightTime(val type: PressType, val prevChar: String, val nextChar: String, val duration: Long){
    companion object{
        fun of(preceedingEvent: KeyEvent, succeedingEvent: KeyEvent): FlightTime{
            require(preceedingEvent.type == succeedingEvent.type){ "Type mismatch: ${preceedingEvent.type} - ${succeedingEvent.type}"}
            return FlightTime(preceedingEvent.type, preceedingEvent.char, succeedingEvent.char, succeedingEvent.time - preceedingEvent.time)
        }
    }
}