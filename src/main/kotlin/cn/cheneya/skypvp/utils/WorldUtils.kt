package cn.cheneya.skypvp.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object WorldUtils {
    fun getEntitiesInRange(range: Double): List<Entity> {
        val player = MinecraftClient.getInstance().player ?: return emptyList()
        val world = MinecraftClient.getInstance().world ?: return emptyList()

        val playerPos = player.pos
        val box = Box(
            playerPos.x - range,
            playerPos.y - range,
            playerPos.z - range,
            playerPos.x + range,
            playerPos.y + range,
            playerPos.z + range
        )

        return world.getOtherEntities(player, box) { true }
    }
}
