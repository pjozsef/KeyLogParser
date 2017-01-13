package com.github.pjozsef.keyloggerparser.measurement

import com.github.pjozsef.keyloggerparser.event.KeyEvent
import com.github.pjozsef.keyloggerparser.event.PressType


data class DwellTime(val char: String, val duration: Long){
    companion object {
        fun of(down: KeyEvent, up: KeyEvent): DwellTime {
            require(down.type == PressType.DOWN){ "The first parameter is not of type 'down'" }
            require(up.type == PressType.UP){ "The first parameter is not of type 'up'" }
            require(down.char == up.char) { "Character mismatch: ${down.char} - ${up.char}" }
            return DwellTime(down.char, up.time-down.time)
        }
    }
}