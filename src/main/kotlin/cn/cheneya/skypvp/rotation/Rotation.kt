package cn.cheneya.skypvp.rotation

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

/**
 * 旋转API，用于控制玩家视角旋转
 */
class Rotation {
    var yaw: Float = 0.0f
    var pitch: Float = 0.0f
    var distanceSq: Double = 0.0
    var task: Runnable? = null
    var postTask: Runnable? = null

    constructor() {
        this.yaw = 0.0f
        this.pitch = 0.0f
    }

    constructor(yaw: Float, pitch: Float) {
        this.yaw = yaw
        this.pitch = pitch
    }

    constructor(vec: Vec2f) {
        this.yaw = vec.x
        this.pitch = vec.y
    }

    constructor(from: Vec3d, to: Vec3d) {
        val diff = to.subtract(from)
        this.yaw = MathHelper.wrapDegrees((Math.toDegrees(atan2(diff.z, diff.x)).toFloat() - 90.0f))
        this.pitch = MathHelper.wrapDegrees((-Math.toDegrees(atan2(diff.y, sqrt(diff.x * diff.x + diff.z * diff.z))).toFloat()))
    }

    fun toVec2f(): Vec2f {
        return Vec2f(this.yaw, this.pitch)
    }

    fun subtract(other: Rotation): Rotation {
        return Rotation(this.yaw - other.yaw, this.pitch - other.pitch)
    }

    fun invert(): Rotation {
        return Rotation(-this.yaw, -this.pitch)
    }

    fun onApply(task: Runnable): Rotation {
        this.task = task
        return this
    }

    fun onPost(task: Runnable): Rotation {
        this.postTask = task
        return this
    }

    fun apply() {
        MinecraftClient.getInstance().player?.let { player ->
            player.yaw = this.yaw
            player.pitch = this.pitch
        }
    }

    fun toPlayer(player: net.minecraft.entity.player.PlayerEntity) {
        if (!this.yaw.isNaN() && !this.pitch.isNaN()) {
            fixedSensitivity(MinecraftClient.getInstance().options.mouseSensitivity)
            player.yaw = this.yaw
            player.pitch = this.pitch
        }
    }

    fun fixedSensitivity(sensitivityOption: net.minecraft.client.option.SimpleOption<Double>) {
        val sensitivity = sensitivityOption.value.toFloat()
        val f = sensitivity * 0.6f + 0.2f
        val gcd = f * f * f * 1.2f
        this.yaw = this.yaw - this.yaw % gcd
        this.pitch = this.pitch - this.pitch % gcd
    }

    companion object {
        fun updateRotation(current: Float, calc: Float, maxDelta: Float): Float {
            var f = MathHelper.wrapDegrees(calc - current)
            if (f > maxDelta) {
                f = maxDelta
            }
            if (f < -maxDelta) {
                f = -maxDelta
            }
            return current + f
        }
    }

    fun getAngleTo(other: Rotation): Double {
        val yaw1 = MathHelper.wrapDegrees(this.yaw)
        val yaw2 = MathHelper.wrapDegrees(other.yaw)
        val diffYaw = MathHelper.wrapDegrees(yaw1 - yaw2)
        val pitch1 = MathHelper.wrapDegrees(this.pitch)
        val pitch2 = MathHelper.wrapDegrees(other.pitch)
        val diffPitch = MathHelper.wrapDegrees(pitch1 - pitch2)
        return sqrt((diffYaw * diffYaw + diffPitch * diffPitch).toDouble())
    }

    fun rotateToYaw(yawSpeed: Float, currentYaw: Float, calcYaw: Float): Float {
        val yaw = updateRotation(currentYaw, calcYaw, yawSpeed + Random.nextFloat() * 15.0f)
        val diffYaw = MathHelper.wrapDegrees(calcYaw - currentYaw).toDouble()
        var result = yaw
        
        if (-yawSpeed > diffYaw || diffYaw > yawSpeed) {
            MinecraftClient.getInstance().player?.let { player ->
                result += (Random.nextFloat() * 1.0f + 1.0f) * sin(player.pitch * Math.PI.toFloat()).toFloat()
            }
        }

        if (result == currentYaw) {
            return currentYaw
        } else {
            val mouseSensitivityOption = MinecraftClient.getInstance().options.mouseSensitivity
            val mouseSensitivityValue = mouseSensitivityOption.value
            val mouseSensitivityFloat = if (mouseSensitivityValue == 0.5) {
                0.47887325f
            } else {
                mouseSensitivityValue.toFloat()
            }

            val f1 = mouseSensitivityFloat * 0.6f + 0.2f
            val f2 = f1 * f1 * f1 * 8.0f
            val deltaX = ((6.667 * result - 6.666666666666667 * currentYaw) / f2).toInt()
            val f3 = deltaX * f2
            return (currentYaw + f3 * 0.15f)
        }
    }

    fun rotateToYaw(yawSpeed: Float, currentRots: FloatArray, calcYaw: Float): Float {
        val yaw = updateRotation(currentRots[0], calcYaw, yawSpeed + Random.nextFloat() * 15.0f)
        var result = yaw
        
        if (yaw != calcYaw) {
            result += (Random.nextFloat() * 1.0f + 1.0f) * sin(currentRots[1] * Math.PI.toFloat()).toFloat()
        }

        if (result == currentRots[0]) {
            return currentRots[0]
        } else {
            val mouseSensitivityOption = MinecraftClient.getInstance().options.mouseSensitivity
            result += ThreadLocalRandom.current().nextGaussian().toFloat() * 0.2f
            
            val mouseSensitivityValue = mouseSensitivityOption.value
            val mouseSensitivityFloat = if (mouseSensitivityValue == 0.5) {
                0.47887325f
            } else {
                mouseSensitivityValue.toFloat()
            }

            val f1 = mouseSensitivityFloat * 0.6f + 0.2f
            val f2 = f1 * f1 * f1 * 8.0f
            val deltaX = ((6.667 * result - 6.6666667 * currentRots[0]) / f2).toInt()
            val f3 = deltaX * f2
            return (currentRots[0] + f3 * 0.15f)
        }
    }

    fun rotateToPitch(pitchSpeed: Float, currentPitch: Float, calcPitch: Float): Float {
        val pitch = updateRotation(currentPitch, calcPitch, pitchSpeed + Random.nextFloat() * 15.0f)
        var result = pitch
        
        if (pitch != calcPitch) {
            MinecraftClient.getInstance().player?.let { player ->
                result += (Random.nextFloat() * 1.0f + 1.0f) * sin(player.yaw * Math.PI.toFloat()).toFloat()
            }
        }

        val mouseSensitivityOption = MinecraftClient.getInstance().options.mouseSensitivity
        val mouseSensitivityValue = mouseSensitivityOption.value
        val mouseSensitivityFloat = if (mouseSensitivityValue == 0.5) {
            0.47887325f
        } else {
            mouseSensitivityValue.toFloat()
        }

        val f1 = mouseSensitivityFloat * 0.6f + 0.2f
        val f2 = f1 * f1 * f1 * 8.0f
        val deltaY = ((6.667 * result - 6.666667 * currentPitch) / f2).toInt() * -1
        val f3 = deltaY * f2
        val f4 = (currentPitch - f3 * 0.15f)
        return MathHelper.clamp(f4, -90.0f, 90.0f)
    }

    fun rotateToPitch(pitchSpeed: Float, currentRots: FloatArray, calcPitch: Float): Float {
        val pitch = updateRotation(currentRots[1], calcPitch, pitchSpeed + Random.nextFloat() * 15.0f)
        var result = pitch
        
        if (pitch != calcPitch) {
            result += (Random.nextFloat() * 1.0f + 1.0f) * sin(currentRots[0] * Math.PI.toFloat()).toFloat()
        }

        val mouseSensitivityOption = MinecraftClient.getInstance().options.mouseSensitivity
        val mouseSensitivityValue = mouseSensitivityOption.value
        val mouseSensitivityFloat = if (mouseSensitivityValue == 0.5) {
            0.47887325f
        } else {
            mouseSensitivityValue.toFloat()
        }

        val f1 = mouseSensitivityFloat * 0.6f + 0.2f
        val f2 = f1 * f1 * f1 * 8.0f
        val deltaY = ((6.667 * result - 6.666667 * currentRots[1]) / f2).toInt() * -1
        val f3 = deltaY * f2
        val f4 = (currentRots[1] - f3 * 0.15f)
        return MathHelper.clamp(f4, -90.0f, 90.0f)
    }

    fun set(yaw: Float, pitch: Float) {
        this.yaw = yaw
        this.pitch = pitch
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rotation) return false

        if (yaw != other.yaw) return false
        if (pitch != other.pitch) return false
        if (distanceSq != other.distanceSq) return false
        if (task != other.task) return false
        if (postTask != other.postTask) return false

        return true
    }

    override fun hashCode(): Int {
        var result = yaw.hashCode()
        result = 31 * result + pitch.hashCode()
        result = 31 * result + distanceSq.hashCode()
        result = 31 * result + (task?.hashCode() ?: 0)
        result = 31 * result + (postTask?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Rotation(yaw=$yaw, pitch=$pitch, distanceSq=$distanceSq, task=$task, postTask=$postTask)"
    }
}

/**
 * 2D向量，用于表示旋转角度
 */
class Vec2f(val x: Float, val y: Float) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vec2f) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun toString(): String {
        return "Vec2f(x=$x, y=$y)"
    }
}