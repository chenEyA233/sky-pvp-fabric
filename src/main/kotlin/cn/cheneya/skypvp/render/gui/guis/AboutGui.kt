package cn.cheneya.skypvp.render.gui.guis

import cn.cheneya.skypvp.skypvp
import net.skypvpteam.skypvp.api.gui
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import java.awt.Color
import kotlin.math.sin

@gui("About gui")
class AboutGui(private val parent: Screen) : Screen(Text.literal("关于 SKY PVP Client")) {

    private var animationTicks = 0f

    override fun init() {
        super.init()

        // 返回按钮
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("返回")) {
                client?.setScreen(parent)
            }
                .dimensions(width / 2 - 100, height - 40, 200, 20)
                .build()
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        animationTicks += delta * 0.5f

        renderBackground(context, mouseX, mouseY, delta)

        // 标题
        val titleText = "关于 SKY PVP Client"
        val titleX = width / 2 - textRenderer.getWidth(titleText) / 2
        val titleY = 30
        context.drawText(textRenderer, titleText, titleX, titleY, Color(255, 255, 255, 255).getRGB(), true)

        // 版本信息
        val versionText = "版本: v" + skypvp.CLIENT_VERSION
        val versionX = width / 2 - textRenderer.getWidth(versionText) / 2
        val versionY = 60
        context.drawText(textRenderer, versionText, versionX, versionY, Color(255, 255, 255, 255).getRGB(), true)

        // 作者信息
        val authorText = "作者: " + skypvp.CLIENT_AUTHOR
        val authorX = width / 2 - textRenderer.getWidth(authorText) / 2
        val authorY = 80
        context.drawText(textRenderer, authorText, authorX, authorY, Color(255, 255, 255, 255).getRGB(), true)

        // 描述
        val descriptionLines = listOf(
            "SKYPVP 是一个Minecraft Fabric 1.21.4 模组，",
            "提供了强大的作弊功能和友好的界面。",
            "感谢您的使用和支持！"
        )

        for ((index, line) in descriptionLines.withIndex()) {
            val lineX = width / 2 - textRenderer.getWidth(line) / 2
            val lineY = 110 + index * 15
            context.drawText(textRenderer, line, lineX, lineY, 0xFFFFFFFF.toInt(), true)
        }

        super.render(context, mouseX, mouseY, delta)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0xFF000000.toInt())

        // 简单的动画背景
        val gridSize = 40
        val gridColor = 0x22FFFFFF

        for (y in 0 until height step gridSize) {
            val alpha = (0x33 * (1.0f + 0.5f * sin(y * 0.01f + animationTicks * 0.2f))).toInt()
            val lineColor = gridColor and 0xFFFFFF or (alpha shl 24)
            context.fill(0, y, width, y + 1, lineColor)
        }

        for (x in 0 until width step gridSize) {
            val alpha = (0x33 * (1.0f + 0.5f * sin(x * 0.01f + animationTicks * 0.2f))).toInt()
            val lineColor = gridColor and 0xFFFFFF or (alpha shl 24)
            context.fill(x, 0, x + 1, height, lineColor)
        }

        // 添加一些动态颜色效果
        for (i in 0 until 10) {
            val x = width / 2 + (100 * sin(animationTicks * 0.05f + i * 0.5f)).toInt()
            val y = height / 2 + (50 * sin(animationTicks * 0.03f + i * 0.4f)).toInt()
            val size = 5 + (3 * sin(animationTicks * 0.1f + i)).toInt()
            val color = Color.HSBtoRGB((animationTicks * 0.01f + i * 0.1f) % 1.0f, 0.7f, 0.7f) and 0x33FFFFFF

            context.fill(x - size, y - size, x + size, y + size, color)
        }
    }

    override fun shouldPause(): Boolean {
        return false
    }
}
