package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.skypvp
import cn.cheneya.skypvp.skypvp.mc
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.math.MathHelper
import org.joml.Quaternionf

/**
 * 防砍动画模块
 * 
 * 该模块提供剑格挡时的动画效果
 */
object ModuleBlockingSwing: Module("BlockingSwing", "防砍动画", Category.RENDER) {
    // 该模块现在作为开关使用，具体动画效果由ModuleAnimations控制

    val shouldHideOffhand = boolean("Should Hide Offhand",false)

    @JvmField
    var canSwing: Boolean =
        MinecraftClient.getInstance()?.let { mc ->
            enabled && mc.options.useKey.isPressed
        } ?: true


    /**
     * 判断是否应该隐藏副手物品
     * 
     * @param player 玩家实体
     * @param item 物品
     * @return 如果应该隐藏副手物品则返回true
     */
    fun shouldHideOffhand(player: net.minecraft.entity.player.PlayerEntity, item: net.minecraft.item.Item): Boolean {
        return enabled
    }
}