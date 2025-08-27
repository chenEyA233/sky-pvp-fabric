package cn.cheneya.skypvp.render.ui

import cn.cheneya.skypvp.render.RenderUtil
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.client.font.TextRenderer.TextLayerType

class ProgressBarUi(
    private val textRenderer: TextRenderer,
    private val vertexConsumers: VertexConsumerProvider,
    private val name: Text,
    private val x: Float,
    private val y: Float,
    private val width: Float = 100f,
    private val height: Float = 8f,
    private val edgeRadius: Float = 2f,
    private val bgColor: Int = 0x80000000.toInt(),
    private val progressColor: Int = 0xFF00FF00.toInt(),
    private val textColor: Int = 0xFFFFFFFF.toInt()
) {
    private var minValue: Float = 0f
    private var maxValue: Float = 100f
    private var currentValue: Float = 0f

    fun setValues(min: Float, max: Float, current: Float) {
        minValue = min
        maxValue = max
        currentValue = current.coerceIn(min, max)
    }

    fun render(matrices: MatrixStack) {
        val matrix = matrices.peek().positionMatrix
        
        // 渲染名称
        textRenderer.draw(
            name,
            x,
            y,
            textColor,
            false,
            matrix,
            vertexConsumers,
            TextLayerType.NORMAL,
            0,
            0xFFFFFF
        )

        // 计算进度条位置
        val barY = y + textRenderer.fontHeight + 2f

        // 渲染进度条背景
        RenderUtil.drawRoundedRect(
            matrices,
            x,
            barY,
            width,
            height,
            edgeRadius,
            bgColor
        )

        // 计算进度百分比
        val progress = if (maxValue > minValue) {
            (currentValue - minValue) / (maxValue - minValue)
        } else {
            0f
        }

        // 渲染进度条填充
        val progressWidth = (width - edgeRadius * 2) * progress
        if (progressWidth > 0) {
            RenderUtil.fillBound(
                matrices,
                x + edgeRadius,
                barY + edgeRadius,
                progressWidth,
                height - edgeRadius * 2,
                progressColor
            )
        }

        // 渲染数值文本
        val valueText = Text.literal("${currentValue.toInt()}/${maxValue.toInt()}")
        val valueX = x + width - textRenderer.getWidth(valueText) - 2f
        textRenderer.draw(
            valueText,
            valueX,
            barY + (height - textRenderer.fontHeight) / 2f,
            textColor,
            false,
            matrix,
            vertexConsumers,
            TextLayerType.NORMAL,
            0,
            0xFFFFFF
        )
    }
}
