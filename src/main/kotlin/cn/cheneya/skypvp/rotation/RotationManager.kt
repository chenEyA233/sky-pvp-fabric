package cn.cheneya.skypvp.rotation

import cn.cheneya.skypvp.api.math.Vector2f
import cn.cheneya.skypvp.event.EventListener
import cn.cheneya.skypvp.event.events.*
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * 旋转管理器，用于处理玩家视角旋转
 */
object RotationManager : EventListener {
    private val log: Logger = LogManager.getLogger(RotationManager::class.java)
    private val mc = MinecraftClient.getInstance()
    
    // 旋转角度
    var rotations: Vector2f? = null
    var lastRotations: Vector2f? = null
    var animationRotation: Vector2f? = null
    var lastAnimationRotation: Vector2f? = null
    
    // 是否激活
    var active = false
    
    override val running: Boolean
        get() = true
    
    override fun parent() = null
    

    
    /**
     * 处理重生事件
     */
    fun onDisconnect(e: DisconnectEvent) {
        lastRotations = null
        rotations = null
    }
    
    /**
     * 更新全局偏航角
     */
    fun onGameTick(e: GameTickEvent) {
        if (mc.player != null) {
            // 这里需要根据项目实际情况添加模块管理器和相关模块的检查
            // 由于原代码中引用了很多模块，这里简化处理
            
            active = false
            
            // 在实际项目中，这里应该检查各种模块的状态并设置相应的旋转角度
            // 例如：
            // if (autoMLG.isEnabled && autoMLG.rotation) {
            //     setRotations(Vector2f(mc.player!!.yaw, 90.0f))
            //     active = true
            // } else if (...) {
            //     ...
            // }
        }
    }
    
    /**
     * 处理运动事件
     */
    fun onMotion(e: MotionEvent) {
        if (!e.post) {
            if (rotations == null || lastRotations == null) {
                mc.player?.let {
                    rotations = Vector2f(it.yaw, it.pitch)
                    lastRotations = rotations
                }
            }
            
            lastAnimationRotation = animationRotation
            
            rotations?.let { rot ->
                val yaw = rot.x
                val pitch = rot.y
                
                if (!yaw.isNaN() && !pitch.isNaN() && active) {
                    e.setYaw(yaw)
                    e.setPitch(pitch)
                }
            }
            
            // 在实际项目中，这里应该检查脚手架模块的状态
            // 例如：
            // if (scaffold.isEnabled && scaffold.mode == "Normal" && scaffold.snap && scaffold.hideSnap) {
            //     animationRotation = scaffold.correctRotation
            // } else {
            //     animationRotation = Vector2f(e.getYaw(), e.getPitch())
            // }
            
            animationRotation = Vector2f(e.getYaw(), e.getPitch())
            lastRotations = Vector2f(e.getYaw(), e.getPitch())
        }
    }
    
    /**
     * 处理移动输入事件
     */
    fun onMovementInput(event: MovementInputEvent) {
        if (active && rotations != null) {
            val yaw = rotations!!.x
            // 在实际项目中，这里应该调用MoveUtils.fixMovement方法
            // 例如：MoveUtils.fixMovement(event, yaw)
        }
    }
    
    /**
     * 处理平移事件
     */
    fun onVelocityStrafe(event: PlayerVelocityStrafe) {
        if (active && rotations != null) {
            // 使用玩家速度平移事件替代原来的StrafeEvent
            // event.yaw = rotations!!.x
        }
    }
    
    /**
     * 处理跳跃事件
     */
    fun onPlayerJump(event: PlayerJumpEvent) {
        if (active && rotations != null) {
            // 使用玩家跳跃事件替代原来的JumpEvent
            // event.yaw = rotations!!.x
        }
    }
}