package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.ModuleManager
import cn.cheneya.skypvp.skypvp
import cn.cheneya.skypvp.utils.ClientUtil.player
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import java.awt.Color

object ModuleHUD : Module("HUD","渲染",Category.RENDER,true) {
    val mc = MinecraftClient.getInstance()!!
    
    var showFPS = boolean("Show FPS", true)
    var showClientInfo = boolean("Show client info", true)
    var showModules = boolean("Show module list", true)
    var showTextBackground = boolean("Show text background", false)
    val mode = enumChoice("Mode", Mode.SKYPVPTEXT)
    private var lastPlayerNameUpdateTime = 0L
    private var cachedPlayerName = "Unknown"

    val playername: String
        get() {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPlayerNameUpdateTime > 500) {
                cachedPlayerName = player.getName().getString() ?: "Unknown"
                lastPlayerNameUpdateTime = currentTime
            }
            return cachedPlayerName
        }

    enum class Mode {
        SKYPVPTEXT,
        OPAI,
        EXHIBITION;

        override fun toString(): String {
            return name
        }
    }

    fun renderMode(context: DrawContext){
        when (mode.value) {
            Mode.SKYPVPTEXT.toString() -> {
                renderHUDSKYPVPTEXT(context)
            }
            Mode.EXHIBITION.toString() -> {
                renderHUDEXHIBITION(context)
            }
            Mode.OPAI.toString() -> {
                renderHUDOPAI(context)
            }
        }
    }
    
    fun renderHUDEXHIBITION(context: DrawContext) {
        if (!enabled) return
        val window = mc.window
        val textRenderer = mc.textRenderer
        val matrices = context.matrices
        matrices.push()

        // Exhibition风格的HUD渲染
        // 顶部信息栏 - 使用更现代的设计
        if (showClientInfo.value || showFPS.value) {
            val fpsText = if (showFPS.value) {
                "[" + mc.fpsDebugString.split(" ")[0] + " FPS]"
            } else {
                ""
            }

            val clientInfoText = if (showClientInfo.value) {
                "[§cS§rKYPVP]  [$playername]"
            } else {
                ""
            }

            // 组合文本
            val displayText = when {
                showFPS.value && showClientInfo.value -> "$clientInfoText  $fpsText"
                showFPS.value -> fpsText
                else -> clientInfoText
            }

            // 计算背景宽度
            val textWidth = textRenderer.getWidth(displayText) + 10
            
            if (showTextBackground.value) {
                // 渐变背景
                context.fill(
                    2, 2,
                    2 + textWidth, 14,
                    Color(20, 20, 20, 180).rgb
                )
                context.fill(
                    2, 2,
                    2 + textWidth, 3,
                    Color(255, 0, 0, 0).rgb
                )
            }

            // 绘制文本
            context.drawText(
                textRenderer,
                Text.literal(displayText),
                5, 8,
                Color(255, 255, 255).rgb,
                false
            )
        }

        // 模块列表 - Exhibition风格
        if (showModules.value) {
            val enabledModules = ModuleManager.getEnabledModules()
                .sortedByDescending { textRenderer.getWidth(it.getLocalizedName()) }
                .map { it.getLocalizedName() }

            if (enabledModules.isNotEmpty()) {
                val maxWidth = enabledModules.maxOf { textRenderer.getWidth(it) } + 10
                val startX = window.scaledWidth - maxWidth - 2
                var yPos = 5

                if (showTextBackground.value) {
                    // Exhibition风格的模块列表背景
                    context.fill(
                        startX - 2, 2,
                        window.scaledWidth - 2, 2 + enabledModules.size * 10 + 2,
                        Color(20, 20, 20, 180).rgb
                    )
                    context.fill(
                        window.scaledWidth - 3, 2,
                        window.scaledWidth - 2, 2 + enabledModules.size * 10 + 2,
                        Color(255, 0, 0, 255).rgb
                    )
                }

                for (module in enabledModules) {
                    context.drawText(
                        textRenderer,
                        Text.literal(module),
                        window.scaledWidth - textRenderer.getWidth(module) - 5, yPos,
                        Color(255, 255, 255).rgb,
                        false
                    )
                    yPos += 10
                }
            }
        }

        // 先渲染阴影
        ModuleNotification.renderShadow(matrices)
        // 再渲染通知
        ModuleNotification.renderHUDNormal(context)
        matrices.pop()
    }

    fun renderHUDOPAI(context: DrawContext) {
        if (!enabled) return
        val matrices = context.matrices
        matrices.push()

        // 先渲染阴影
        ModuleNotification.renderShadow(matrices)
        // 再渲染通知
        ModuleNotification.renderHUDNormal(context)
        matrices.pop()
    }

    var backgroundColor = Color(0, 0, 0, 100)
    var textColor = Color(255, 255, 255)

    fun renderHUDSKYPVPTEXT(context: DrawContext) {
        if (!enabled) return
        val window = mc.window
        val textRenderer = mc.textRenderer
        val matrices = context.matrices
        matrices.push()

        // Combine FPS and client info in one line
        if (showClientInfo.value || showFPS.value) {
            val fpsText = if (showFPS.value) {
                mc.fpsDebugString.split(" ")[0] + " FPS"
            } else {
                ""
            }

            val clientInfoText = if (showClientInfo.value) {
                "§cS§rKY PVP" + " " + skypvp.CLIENT_VERSION + " | " + playername
            } else {
                ""
            }

            // Combine texts based on what's enabled
            val displayText = when {
                showFPS.value && showClientInfo.value -> "$clientInfoText | $fpsText"
                showFPS.value -> fpsText
                else -> clientInfoText
            }

            // Calculate background width based on text length
            val textWidth = textRenderer.getWidth(displayText) + 10

            // Draw background if enabled
            if (showTextBackground.value) {
                context.fill(
                    2, 2,
                    2 + textWidth, 12,
                    backgroundColor.rgb
                )
            }

            // Draw combined text
            context.drawText(
                textRenderer,
                Text.literal(displayText),
                5, 5,
                textColor.rgb,
                false
            )
        }

        if (showModules.value) {
            val enabledModules = ModuleManager.getEnabledModules()
                .sortedByDescending { textRenderer.getWidth(it.getLocalizedName()) }
                .map { it.getLocalizedName() }

            if (enabledModules.isNotEmpty()) {
                val maxWidth = enabledModules.maxOf { textRenderer.getWidth(it) } + 10
                val startX = window.scaledWidth - maxWidth - 2
                var yPos = 5

                if (showTextBackground.value) {
                    context.fill(
                        startX, 2,
                        window.scaledWidth - 2, 2 + enabledModules.size * 10,
                        backgroundColor.rgb
                    )
                }

                for (module in enabledModules) {
                    context.drawText(
                        textRenderer,
                        Text.literal(module),
                        window.scaledWidth - textRenderer.getWidth(module) - 2, yPos,
                        textColor.rgb,
                        false
                    )
                    yPos += 10
                }
            }
        }

        // 先渲染阴影
        ModuleNotification.renderShadow(matrices)
        ModuleNotification.renderHUDNormal(context)
        matrices.pop()
    }
}
