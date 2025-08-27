package cn.cheneya.skypvp.features.module.modules.player

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.block.AirBlock
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.network.ClientPlayerEntity
import org.lwjgl.glfw.GLFW

object ModuleEagle: Module("Eagle", "自动蹲起搭", Category.PLAYER) {
    private val mc: MinecraftClient = MinecraftClient.getInstance()
    private var tickCallback: ClientTickEvents.EndTick? = null
    
    init {
        // 设置Z键为切换键
        this.key = GLFW.GLFW_KEY_Z
    }
    
    override fun onEnable() {
        tickCallback = ClientTickEvents.EndTick { client: MinecraftClient ->
            if (enabled) onTick()
        }.also {
            ClientTickEvents.END_CLIENT_TICK.register(it)
        }
    }
    
    override fun onDisable() {
        // 释放Shift键
        mc.options.sneakKey.isPressed = false
        // Fabric会自动处理回调的清理
        tickCallback = null
    }
    
    private fun onTick() {
        val player = mc.player ?: return
        if (mc.currentScreen != null) return
        
        // 检测玩家下方是否为空气方块
        if (getBlockUnderPlayer(player) is AirBlock) {
            if (player.isOnGround) {
                mc.options.sneakKey.isPressed = true
            }
        } else if (player.isOnGround) {
            mc.options.sneakKey.isPressed = false
        }
    }
    
    // 获取玩家正下方的方块
    private fun getBlockUnderPlayer(player: ClientPlayerEntity): Block? {
        return getBlock(BlockPos(player.blockPos).down())
    }
    
    // 获取指定位置的方块
    private fun getBlock(pos: BlockPos): Block? {
        return mc.world?.getBlockState(pos)?.block
    }
}