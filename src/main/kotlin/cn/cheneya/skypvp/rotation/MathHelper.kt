package cn.cheneya.skypvp.rotation

import net.minecraft.util.math.MathHelper as MinecraftMathHelper

/**
 * 数学辅助工具类
 */
object MathHelper {
    /**
     * 将角度包装在-180到180度之间
     */
    fun wrapDegrees(degrees: Float): Float {
        return MinecraftMathHelper.wrapDegrees(degrees)
    }
    
    /**
     * 将值限制在指定范围内
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return MinecraftMathHelper.clamp(value, min, max)
    }
}