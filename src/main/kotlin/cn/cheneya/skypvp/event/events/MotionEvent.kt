package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.event.CancellableEvent

/**
 * 运动事件，用于处理玩家旋转
 * 参考自 Loratadine 客户端
 */
@Nameable("motion")
class MotionEvent(
    val post: Boolean,
    private var yaw: Float,
    private var pitch: Float
) : CancellableEvent() {
    
    /**
     * 获取偏航角
     */
    fun getYaw(): Float {
        return yaw
    }
    
    /**
     * 设置偏航角
     */
    fun setYaw(yaw: Float) {
        this.yaw = yaw
    }
    
    /**
     * 获取俯仰角
     */
    fun getPitch(): Float {
        return pitch
    }
    
    /**
     * 设置俯仰角
     */
    fun setPitch(pitch: Float) {
        this.pitch = pitch
    }
}