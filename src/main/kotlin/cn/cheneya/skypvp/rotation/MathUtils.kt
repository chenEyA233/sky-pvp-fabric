package cn.cheneya.skypvp.rotation

import kotlin.random.Random

/**
 * 数学工具类
 */
object MathUtils {
    /**
     * 获取指定范围内的随机双精度浮点数
     */
    fun getRandomDoubleInRange(min: Double, max: Double): Double {
        return min + (max - min) * Random.nextDouble()
    }
}