package com.github.pjozsef.keyloggerparser.event


enum class PressType {
    DOWN, UP;
    companion object{
        fun of(type: String) = when(type){
            "down", "DOWN" -> DOWN
            "up", "UP" -> UP
            else -> error("Unrecognized input: $type")
        }
    }
}