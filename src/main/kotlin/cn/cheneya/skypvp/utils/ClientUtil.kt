package cn.cheneya.skypvp.utils

import cn.cheneya.skypvp.api.utils.InputAddition
import cn.cheneya.skypvp.skypvp
import cn.cheneya.skypvp.skypvp.mc
import net.minecraft.client.MinecraftClient
import net.minecraft.client.input.Input
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.util.PlayerInput
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.apache.logging.log4j.Logger

object ClientUtil{
    val logger: Logger
        get() = skypvp.logger

    val inGame: Boolean
        get() = MinecraftClient.getInstance()?.let { mc ->
            mc.player != null && mc.world != null && mc.currentScreen == null
        } ?: false

    var isDestructed = false
    const val FIRST_PRIORITY: Short = 1000

    val network: ClientPlayNetworkHandler
        inline get() = mc.networkHandler!!
    val interaction: ClientPlayerInteractionManager
        inline get() = mc.interactionManager!!
    val player: ClientPlayerEntity
        inline get() = mc.player!!
    val Input.untransformed: PlayerInput
        get() = (this as InputAddition).`sky$getUntransformed`()

    fun shouldHideOffhand(
        player: net.minecraft.entity.player.PlayerEntity = this.player,
        offHandItem: net.minecraft.item.Item = player.offHandStack.item,
        mainHandItem: net.minecraft.item.Item = player.mainHandStack.item,
    ): Boolean {
        return true
    }

}

