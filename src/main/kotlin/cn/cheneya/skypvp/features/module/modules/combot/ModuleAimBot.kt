package cn.cheneya.skypvp.features.module.modules.combot

import cn.cheneya.skypvp.api.math.Vector2f
import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.modules.misc.ModuleTarget
import cn.cheneya.skypvp.rotation.Rotation
import cn.cheneya.skypvp.rotation.RotationManager
import cn.cheneya.skypvp.rotation.RotationUtils
import net.minecraft.client.MinecraftClient
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ModuleAimBot : Module("AimBot", "自动瞄准", Category.COMBOT) {
    private val mc = MinecraftClient.getInstance()
    private val targetManager = ModuleTarget

    // 配置项
    private val fovLimit = float("FOV", 30.0f, 1.0f, 180.0f)
    private val minRotationSpeed = float("Min Rotation Speed", 1.0f, 0.1f, 10.0f)
    private val maxRotationSpeed = float("Max Rotation Speed", 5.0f, 0.1f, 20.0f)

    override fun onUpdate() {
        if (mc.player == null || !enabled) return

        val target = targetManager.getTarget()
        if (target != null) {
            val playerPos = mc.player!!.pos
            val targetPos = target.pos
            val rotation = RotationUtils.getRotationTo(playerPos, targetPos)
            
            // 检查目标是否在FOV范围内
            val currentYaw = mc.player!!.yaw
            val currentPitch = mc.player!!.pitch
            val yawDiff = RotationUtils.getAngleDifference(rotation.yaw, currentYaw)
            val pitchDiff = RotationUtils.getAngleDifference(rotation.pitch, currentPitch)
            
            if (abs(yawDiff) <= fovLimit.value && abs(pitchDiff) <= fovLimit.value) {
                // 应用旋转速度限制
                val distance = sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff).toFloat()
                val speed = min(maxRotationSpeed.value.toFloat(), max(minRotationSpeed.value.toFloat(), distance))
                val lerpedYaw = currentYaw + yawDiff * speed * 0.1f
                val lerpedPitch = currentPitch + pitchDiff * speed * 0.1f
                
                // w应用旋转到玩家实体
                MinecraftClient.getInstance().player?.apply {
                    yaw = lerpedYaw.toFloat()
                    pitch = lerpedPitch.toFloat()
                }
            }
        }
    }
}
