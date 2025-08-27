package cn.cheneya.skypvp.features.module.modules.movement

import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.Category
import net.minecraft.client.MinecraftClient
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import org.lwjgl.glfw.GLFW

object ModuleFly : Module(
    name = "Fly",
    displayName = "飞行",
    category = Category.MOVEMENT
) {
    // 速度设置 (0.1 - 25)
    private val speedSetting = number("Speed", 1.0, 0.1, 25.0, 0.1)

    
    // Minecraft客户端实例
    private val mc: MinecraftClient = MinecraftClient.getInstance()
    
    // 使用单一的事件监听器
    init {
        // 在初始化时注册永久的tick事件
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            // 只有在模块启用时才执行逻辑
            if (enabled) {
                onTick()
            }
        }
    }


    
    private fun onTick() {
        val player = mc.player ?: return

        val speed = speedSetting.value

        player.abilities.allowFlying = true
        player.abilities.flying = true

        val window = mc.window.handle
        when (GLFW.GLFW_PRESS) {
            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) -> {
                player.setVelocity(player.velocity.x, speed / 10.0, player.velocity.z)
            }
            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) -> {
                player.setVelocity(player.velocity.x, -speed / 10.0, player.velocity.z)
            }
            else -> {
                player.setVelocity(player.velocity.x, 0.0, player.velocity.z)
            }
        }

        player.abilities.flySpeed = (speed / 20f).toFloat()
    }

    override fun onDisable() {
        val player = mc.player ?: return
        player.abilities.flying = false

        if (!player.isCreative) {
            player.abilities.allowFlying = false
        }

        player.abilities.flySpeed = 0.05f

        player.setVelocity(0.0, 0.0, 0.0)
        player.networkHandler.sendPacket(
            PlayerMoveC2SPacket.OnGroundOnly(true, false)
        )

    }
}
