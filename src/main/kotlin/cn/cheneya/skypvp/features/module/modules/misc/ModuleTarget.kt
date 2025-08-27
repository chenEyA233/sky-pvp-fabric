package cn.cheneya.skypvp.features.module.modules.misc

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.skypvp.logger
import cn.cheneya.skypvp.utils.ChatUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity

object ModuleTarget: Module("Target","目标", Category.MISC) {
    // 目标类型设置
    val Player = boolean("Player", true)
    val Mobs = boolean("Mobs", false)
    val Animals = boolean("Animals", false)

    // 获取当前目标实体
    fun getTarget(): net.minecraft.entity.Entity? {
        val world = net.minecraft.client.MinecraftClient.getInstance().world ?: return null
        val player = net.minecraft.client.MinecraftClient.getInstance().player ?: return null

        return world.entities.filter { entity ->
            entity != player && entity.isAlive && (
                (entity is PlayerEntity && Player.value) ||
                (entity is MobEntity && Mobs.value) ||
                (entity is AnimalEntity && Animals.value)
            )
        }.minByOrNull { entity ->
            player.squaredDistanceTo(entity)
        }
    }

    // 模块启用时调用
    override fun onEnable() {
        ChatUtil.silence("你看你妈呢!")
        logger.info("Heypixel is best!")
        enabled = false
    }

    override fun onDisable() {
        ChatUtil.silence("你好，我是贝利亚!")
        logger.info(":)")
    }
}
