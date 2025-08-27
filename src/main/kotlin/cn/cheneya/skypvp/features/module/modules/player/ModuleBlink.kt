package cn.cheneya.skypvp.features.module.modules.player

import cn.cheneya.skypvp.api.kotlin.PacketQueueManager
import cn.cheneya.skypvp.event.*
import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.skypvp.mc
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import org.lwjgl.glfw.GLFW
import java.util.*

object ModuleBlink : Module("Blink", "瞬移", Category.PLAYER) {
    private var dummyPlayer: OtherClientPlayerEntity? = null
    private val dummy = boolean("Dummy",true)
    
    init {
        key = GLFW.GLFW_KEY_K
    }

    override fun onEnable() {
        if (dummy.value) {
            mc.player?.let { player ->
                mc.world?.let { world ->
                    val clone = OtherClientPlayerEntity(world, player.gameProfile)
                    clone.copyPositionAndRotation(player)
                    clone.uuid = UUID.randomUUID()
                    world.addEntity(clone)
                    dummyPlayer = clone
                }
            }
        }
    }

    override fun onDisable() {
        // 清空数据包队列
        PacketQueueManager.cancel()
        
        // 移除假人实体
        dummyPlayer?.let { dummy ->
            mc.world?.removeEntity(dummy.id, Entity.RemovalReason.DISCARDED)
        }
        dummyPlayer = null
    }

    fun teleport() {
        PacketQueueManager.flush { true }

        mc.player?.let { player ->
            dummyPlayer?.copyPositionAndRotation(player)
        }
    }
}
