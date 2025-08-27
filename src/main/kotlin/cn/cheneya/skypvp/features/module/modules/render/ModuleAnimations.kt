package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis

/**
 * 动画模块
 *
 * 该模块影响物品动画，允许用户自定义动画效果。
 */
object ModuleAnimations: Module("Animations", "手部动画管理", Category.RENDER) {

    // 主手设置
    val mainHandItemScale = float("Main Hand ItemScale", 0f, -5f, 5f)
    val mainHandX = float("Main Hand X", 0f, -5f, 5f)
    val mainHandY = float("Main Hand Y", 0f, -5f, 5f)
    val mainHandPositiveX = float("Main Hand Positive Rotation x", 0f, -50f, 50f)
    val mainHandPositiveY = float("Main Hand Positive Rotation Y", 0f, -50f, 50f)
    val mainHandPositiveZ = float("Main Hand Positive Rotation Z", 0f, -50f, 50f)

    // 副手设置
    val offHandItemScale = float("Off Hand Item Scale", 0f, -5f, 5f)
    val offHandX = float("Off Hand X", 0f, -1f, 1f)
    val offHandY = float("Off Hand Y", 0f, -1f, 1f)
    val offHandPositiveX = float("Off Hand Positive Rotation X", 0f, -50f, 50f)
    val offHandPositiveY = float("Off Hand Positive Rotation Y", 0f, -50f, 50f)
    val offHandPositiveZ = float("Off Hand Positive Rotation Z", 0f, -50f, 50f)

    // 挥动速度
    val swingDuration = integer("Swing Duration", 6, 1, 20)
    val ignoreBlocking = boolean("Ignore Blocking", true)

    // 格挡动画选择
    val blockingAnimation = enumChoice("Blocking Animation", BlockStyle.MC1_7)

    enum class BlockStyle{
        MC1_7,
        Pushdown
    }

    /**
     * 1.7风格的动画
     */
    fun applyOneSevenAnimation(matrices: MatrixStack, arm: Arm, equipProgress: Float, swingProgress: Float) {
        val translateY = 0.1f
        val swingProgressScale = 0.9f

        matrices.translate(if (arm == Arm.RIGHT) -0.1f else 0.1f, translateY, 0.0f)
        applySwingOffset(matrices, arm, swingProgress * swingProgressScale)
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f))
        matrices.multiply(
            (if (arm == Arm.RIGHT) RotationAxis.POSITIVE_Y else RotationAxis.NEGATIVE_Y)
                .rotationDegrees(13.365f)
        )
        matrices.multiply(
            (if (arm == Arm.RIGHT) RotationAxis.POSITIVE_Z else RotationAxis.NEGATIVE_Z)
                .rotationDegrees(78.05f)
        )
    }

    /**
     * 下压风格的动画
     */
    fun applyPushdownAnimation(matrices: MatrixStack, arm: Arm, equipProgress: Float, swingProgress: Float) {
        matrices.translate(if (arm == Arm.RIGHT) -0.1f else 0.1f, 0.1f, 0.0f)

        val g = MathHelper.sin(MathHelper.sqrt(swingProgress) * Math.PI.toFloat())
        matrices.multiply(
            RotationAxis.POSITIVE_Z.rotationDegrees(
                (if (arm == Arm.RIGHT) 1 else -1) * g * 10.0f
            )
        )
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -35.0f))

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f))
        matrices.multiply(
            (if (arm == Arm.RIGHT) RotationAxis.POSITIVE_Y else RotationAxis.NEGATIVE_Y)
                .rotationDegrees(13.365f)
        )
        matrices.multiply(
            (if (arm == Arm.RIGHT) RotationAxis.POSITIVE_Z else RotationAxis.NEGATIVE_Z)
                .rotationDegrees(78.05f)
        )
    }

    /**
     * 应用挥动偏移
     */
    private fun applySwingOffset(matrices: MatrixStack, arm: Arm, swingProgress: Float) {
        val armSide = if (arm == Arm.RIGHT) 1 else -1
        val f = MathHelper.sin(swingProgress * swingProgress * Math.PI.toFloat())
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide.toFloat() * (45.0f + f * -20.0f)))
        val g = MathHelper.sin(MathHelper.sqrt(swingProgress) * Math.PI.toFloat())
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armSide.toFloat() * g * -20.0f))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f))
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide.toFloat() * -45.0f))
    }

    /**
     * 应用变换
     */
    fun applyTransformations(matrices: MatrixStack, translateX: Float, translateY: Float, translateZ: Float, rotateX: Float, rotateY: Float, rotateZ: Float) {
        matrices.translate(translateX, translateY, translateZ)
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateX))
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateY))
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotateZ))
    }
}