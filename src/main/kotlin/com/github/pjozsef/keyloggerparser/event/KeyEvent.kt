package com.github.pjozsef.keyloggerparser.event

data class KeyEvent(val type: PressType, val char: String, val time: Long) {
    companion object {
        fun of(raw: String): KeyEvent {
            val split = raw.split(" ")
            return KeyEvent(PressType.of(split[0]), split[3], split[1].toLong())
        }
    }
}