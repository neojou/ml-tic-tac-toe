package com.neojou

class BoardStatus() {
    private val b: IntArray = IntArray(9) { 0 }

    constructor(init: IntArray) : this() {
        require(init.size == 9) { "BoardStatus init array size must be 9" }
        init.copyInto(b)
        require(b.all { it in 0..2 }) { "Cell value must be 0, 1, or 2" }
    }

    fun set(pos: Int, value: Int) {
        require(pos in 0..8) { "pos must be in 0..8" }
        require(value in 0..2) { "value must be 0, 1, or 2" }
        b[pos] = value
    }

    operator fun get(pos: Int): Int {
        require(pos in 0..8) { "pos must be in 0..8" }
        return b[pos]
    }

    fun copyArray(): IntArray = b.copyOf()
}

