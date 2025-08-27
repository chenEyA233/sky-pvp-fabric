package cn.cheneya.skypvp.features.module.modules.movement

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.skypvp.mc
import org.lwjgl.glfw.GLFW

/**
 * 卡住模块，用于配合 KillAura 的移动修复
 * 参考自 Loratadine 客户端
 */
object ModuleStuck : Module("Stuck", "卡住", Category.MOVEMENT) {
    init {
        key = GLFW.GLFW_KEY_N
    }
    
    private val stuckTicks = integer("Stuck Ticks", 5, 1, 20, 1)
    private var ticks = 0
    
    override fun onUpdate() {
        val player = mc.player ?: return
        
        // 增加计数器
        ticks++
        
        // 如果达到设定的卡住时间，则关闭模块
        if (ticks >= stuckTicks.value) {
            ticks = 0
            enabled = false
        }
    }
    
    override fun onEnable() {
        ticks = 0
    }
    
    override fun onDisable() {
        ticks = 0
    }
}