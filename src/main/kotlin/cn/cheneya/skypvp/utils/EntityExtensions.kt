@file:Suppress("TooManyFunctions")

package cn.cheneya.skypvp.utils

import cn.cheneya.skypvp.api.kotlin.DirectionalInput
import cn.cheneya.skypvp.api.utils.InputAddition
import cn.cheneya.skypvp.skypvp.mc
import cn.cheneya.skypvp.utils.ClientUtil.player
import net.minecraft.client.input.Input
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.consume.UseAction
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.util.Hand
import net.minecraft.util.PlayerInput
import net.minecraft.util.math.*
import net.minecraft.world.World
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

val Entity.netherPosition: Vec3d
    get() = if (world.registryKey == World.NETHER) {
        Vec3d(x, y, z)
    } else {
        Vec3d(x / 8.0, y, z / 8.0)
    }

val ClientPlayerEntity.moving
    get() = input.movementForward != 0.0f || input.movementSideways != 0.0f

val Input.untransformed: PlayerInput
    get() = (this as InputAddition).`sky$getUntransformed`()

val Input.initial: PlayerInput
    get() = (this as InputAddition).`sky$getInitial`()

val Entity.exactPosition
    get() = Vec3d(x, y, z)

val Entity.blockVecPosition
    get() = Vec3i(blockX, blockY, blockZ)

val PlayerEntity.ping: Int
    get() = mc.networkHandler?.getPlayerListEntry(uuid)?.latency ?: 0


fun ClientPlayerEntity.getMovementDirectionOfInput(input: DirectionalInput): Float {
    return getMovementDirectionOfInput(this.yaw, input)
}

val ClientPlayerEntity.isBlockAction: Boolean
    get() = isUsingItem && activeItem.useAction == UseAction.BLOCK

fun Entity.lastRenderPos() = Vec3d(this.lastRenderX, this.lastRenderY, this.lastRenderZ)

val Hand.equipmentSlot: EquipmentSlot
    get() = when (this) {
        Hand.MAIN_HAND -> EquipmentSlot.MAINHAND
        Hand.OFF_HAND -> EquipmentSlot.OFFHAND

}

fun ClientPlayerEntity.canStep(height: Double = 1.0): Boolean {
    if (!horizontalCollision || isDescending || !isOnGround) {
        // If we are not colliding with anything, we are not meant to step
        return false
    }
    return true
}

fun LivingEntity.getActualHealth(fromScoreboard: Boolean = true): Float {
    if (fromScoreboard) {
        val health = getHealthFromScoreboard()

        if (health != null) {
            return health
        }
    }


    return health
}

private fun LivingEntity.getHealthFromScoreboard(): Float? {
    val objective = world.scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) ?: return null
    val score = objective.scoreboard.getScore(this, objective) ?: return null

    val displayName = objective.displayName

    if (score.score <= 0 || displayName?.string?.contains("❤") != true) {
        return null
    }

    return score.score.toFloat()
}

fun getMovementDirectionOfInput(facingYaw: Float, input: DirectionalInput): Float {
    var actualYaw = facingYaw
    var forward = 1f

    // Check if client-user tries to walk backwards (+180 to turn around)
    if (input.backwards) {
        actualYaw += 180f
        forward = -0.5f
    } else if (input.forwards) {
        forward = 0.5f
    }

    // Check which direction the client-user tries to walk sideways
    if (input.left) {
        actualYaw -= 90f * forward
    }
    if (input.right) {
        actualYaw += 90f * forward
    }

    return actualYaw
}

val PlayerEntity.sqrtSpeed: Double
    get() = velocity.sqrtSpeed

val Vec3d.sqrtSpeed: Double
    get() = sqrt(x * x + z * z)

fun Vec3d.withStrafe(
    speed: Double = sqrtSpeed,
    strength: Double = 1.0,
    input: DirectionalInput? = DirectionalInput(player.input),
    yaw: Float = player.getMovementDirectionOfInput(input ?: DirectionalInput(player.input)),
): Vec3d {
    if (input?.isMoving == false) {
        return Vec3d(0.0, y, 0.0)
    }

    val prevX = x * (1.0 - strength)
    val prevZ = z * (1.0 - strength)
    val useSpeed = speed * strength

    val angle = Math.toRadians(yaw.toDouble())
    val x = (-sin(angle) * useSpeed) + prevX
    val z = (cos(angle) * useSpeed) + prevZ
    return Vec3d(x, y, z)
}

