package cn.cheneya.skypvp.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

object RenderUtil {
    /**
     * 绘制圆角矩形
     */
    fun drawRoundedRect(matrices: MatrixStack, x: Float, y: Float, width: Float, height: Float, edgeRadius: Float, color: Int) {
        val matrix = matrices.peek().positionMatrix

        // 限制圆角半径
        var radius = edgeRadius
        if (radius < 0f) radius = 0f
        if (radius > width / 2f) radius = width / 2f
        if (radius > height / 2f) radius = height / 2f

        // 启用混合
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)

        // 绘制中心矩形
        drawRect(matrix, x + radius, y + radius, width - radius * 2f, height - radius * 2f, color)

        // 绘制上下边缘矩形
        drawRect(matrix, x + radius, y, width - radius * 2f, radius, color)
        drawRect(matrix, x + radius, y + height - radius, width - radius * 2f, radius, color)

        // 绘制左右边缘矩形
        drawRect(matrix, x, y + radius, radius, height - radius * 2f, color)
        drawRect(matrix, x + width - radius, y + radius, radius, height - radius * 2f, color)

        // 绘制四个圆角
        drawCorner(matrix, x + radius, y + radius, radius, 0, color) // 左上
        drawCorner(matrix, x + width - radius, y + radius, radius, 1, color) // 右上
        drawCorner(matrix, x + radius, y + height - radius, radius, 2, color) // 左下
        drawCorner(matrix, x + width - radius, y + height - radius, radius, 3, color) // 右下

        // 禁用混合
        RenderSystem.disableBlend()
    }

    /**
     * 绘制矩形
     */
    private fun drawRect(matrix: Matrix4f, x: Float, y: Float, width: Float, height: Float, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        buffer.vertex(matrix, x, y + height, 0f).color(red, green, blue, alpha)
        buffer.vertex(matrix, x + width, y + height, 0f).color(red, green, blue, alpha)
        buffer.vertex(matrix, x + width, y, 0f).color(red, green, blue, alpha)
        buffer.vertex(matrix, x, y, 0f).color(red, green, blue, alpha)

        BufferRenderer.drawWithGlobalProgram(buffer.end())
    }

    /**
     * 绘制圆角
     */
    private fun drawCorner(matrix: Matrix4f, centerX: Float, centerY: Float, radius: Float, corner: Int, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR)

        // 添加中心点
        buffer.vertex(matrix, centerX, centerY, 0f).color(red, green, blue, alpha)

        val vertices = minOf(maxOf(radius, 10f), 90f).toInt()

        // 绘制圆弧，使用与Nevan客户端相同的角度计算方式
        when (corner) {
            0 -> { // 左上
                for (i in 0..vertices) {
                    val angle = (Math.PI * 2) * (i + 180) / (vertices * 4)
                    val x = centerX + (Math.sin(angle) * radius).toFloat()
                    val y = centerY + (Math.cos(angle) * radius).toFloat()
                    buffer.vertex(matrix, x, y, 0f).color(red, green, blue, alpha)
                }
            }
            1 -> { // 右上
                for (i in 0..vertices) {
                    val angle = (Math.PI * 2) * (i + 90) / (vertices * 4)
                    val x = centerX + (Math.sin(angle) * radius).toFloat()
                    val y = centerY + (Math.cos(angle) * radius).toFloat()
                    buffer.vertex(matrix, x, y, 0f).color(red, green, blue, alpha)
                }
            }
            2 -> { // 左下
                for (i in 0..vertices) {
                    val angle = (Math.PI * 2) * (i + 270) / (vertices * 4)
                    val x = centerX + (Math.sin(angle) * radius).toFloat()
                    val y = centerY + (Math.cos(angle) * radius).toFloat()
                    buffer.vertex(matrix, x, y, 0f).color(red, green, blue, alpha)
                }
            }
            3 -> { // 右下
                for (i in 0..vertices) {
                    val angle = (Math.PI * 2) * i / (vertices * 4)
                    val x = centerX + (Math.sin(angle) * radius).toFloat()
                    val y = centerY + (Math.cos(angle) * radius).toFloat()
                    buffer.vertex(matrix, x, y, 0f).color(red, green, blue, alpha)
                }
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end())
    }

    /**
     * 填充矩形区域
     */
    fun fillBound(matrices: MatrixStack, left: Float, top: Float, width: Float, height: Float, color: Int) {
        drawRect(matrices.peek().positionMatrix, left, top, width, height, color)
    }
}