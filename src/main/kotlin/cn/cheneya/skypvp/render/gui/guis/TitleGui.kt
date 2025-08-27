package cn.cheneya.skypvp.render.gui.guis

import cn.cheneya.skypvp.features.module.modules.render.ModuleClickGui
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.OptionsScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.skypvpteam.skypvp.api.gui
import java.awt.Color
import java.io.File

@gui("Title gui")
open class TitleGui : Screen(Text.literal("SKYPVP Client")) {
    override fun init() {
        super.init()
        
        val buttonHeight = 20
        val spacing = 4
        val buttonWidth = 133
        val leftMargin = 10
        
        // 关于按钮 - 放在左上角
        val smallButtonWidth = 60
        val smallButtonHeight = 20
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("关于")) {
                client?.setScreen(AboutGui(this))
            }
                .dimensions(leftMargin, 10, smallButtonWidth, smallButtonHeight)
                .build()
        )

        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("Click Gui")) {
                client?.setScreen(ModuleClickGui.ClickGuiScreen())
            }
                .dimensions(leftMargin, 10 + smallButtonHeight + spacing, smallButtonWidth, smallButtonHeight)
                .build()
        )
        
        // 计算主按钮的起始Y位置，在Click Gui按钮下方留出一些空间
        val startY = 10 + (smallButtonHeight + spacing) * 2 + spacing * 3

        // 单人游戏
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("menu.singleplayer")) {
                client?.setScreen(SelectWorldScreen(this))
            }
                .dimensions(leftMargin, startY, buttonWidth, buttonHeight)
                .build()
        )

        // 多人游戏
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("menu.multiplayer")) {
                client?.setScreen(MultiplayerScreen(this))
            }
                .dimensions(leftMargin, startY + buttonHeight + spacing, buttonWidth, buttonHeight)
                .build()
        )

        // 选项
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("menu.options")) {
                client?.setScreen(OptionsScreen(this, client?.options))
            }
                .dimensions(leftMargin, startY + (buttonHeight + spacing) * 2, buttonWidth, buttonHeight)
                .build()
        )

        // 退出
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("menu.quit")) {
                client?.scheduleStop()
            }
                .dimensions(leftMargin, startY + (buttonHeight + spacing) * 3, buttonWidth, buttonHeight)
                .build()
        )

    }


    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0xFF000000.toInt())

        // 设置渲染状态
        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        
        // 确保在渲染其他元素前设置正确的渲染状态
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        
        // 绘制版权信息
        val copyrightText = "SKY PVP Client | Copyright (C) 2024 - 2025 SKY PVP Team! | user:" + LoginGui.currentUsername
        val copyrightX = width / 2 - textRenderer.getWidth(copyrightText) / 2
        val copyrightY = height - 10
        context.drawText(client?.textRenderer, copyrightText, copyrightX, copyrightY, Color(255, 255, 255, 255).getRGB(), true)
        
        // 恢复渲染状态
        RenderSystem.enableDepthTest()
        RenderSystem.disableBlend()
    }

}