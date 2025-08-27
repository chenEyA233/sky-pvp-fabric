package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.render.font.Fonts
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Color
import kotlin.UninitializedPropertyAccessException

object ModuleNewClickGui: Module("NewClickGui","新点击Gui", Category.RENDER) {

    val mode = enumChoice("Mode",Mode.New)

    enum class Mode{
        New,
    }

    override fun onEnable() {
        MinecraftClient.getInstance().setScreen(ClickGui())
        enabled = false
    }

    open class ClickGui: Screen(Text.literal("ClickGui")){

        override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.fill(0, 0, width, height, Color(0, 0, 0, 80).rgb)
        }

        override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            // 计算屏幕中间位置和矩形大小（占整个屏幕7/10宽度和高度）
            val rectWidth = width * 0.7f
            val rectHeight = height * 0.7f
            val rectX = (width - rectWidth) / 2
            val rectY = (height - rectHeight) / 2

            // 绘制毛玻璃效果的圆角矩形
            val matrixStack = context!!.matrices

            
            // 在矩形右上角渲染文字
            val text = "SKY PVP Client Gui"
            val textScale = 1.2
            val textColor = Color(255, 255, 255, 220) // 白色，稍微透明
            
            try {
                // 尝试获取字体渲染器，如果未初始化则跳过渲染文字
                val fontRenderer = Fonts.harmony
                
                // 计算文字位置（右上角，留出一定边距）
                val textWidth = fontRenderer.getWidth(text, textScale)
                val textX = rectX + rectWidth - textWidth - 15 // 右边距15像素
                val textY = rectY + 15 // 上边距15像素
                
                // 渲染文字（带阴影效果）
                fontRenderer.render(matrixStack, text, textX.toDouble(), textY.toDouble(), textColor, true, textScale)
            } catch (e: UninitializedPropertyAccessException) {
                // 字体未初始化，使用Minecraft原生字体渲染
                val textX = rectX + rectWidth - 120 // 估算位置
                val textY = rectY + 15
                context.drawText(client!!.textRenderer, text, textX.toInt(), textY.toInt(), textColor.rgb, true)
            }
            
            super.render(context, mouseX, mouseY, delta)
        }

        override fun shouldPause(): Boolean {
            return false
        }


    }
}