package cn.cheneya.skypvp.utils

import kotlin.random.Random

object MathUtils {
    fun getRandomNumber(min: Int, max: Int): Double {
        return Random.nextDouble(min.toDouble(), max.toDouble() + 1)
    }
}