package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.render.RenderUtil
import cn.cheneya.skypvp.utils.ChatUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.awt.Color
import java.util.concurrent.ConcurrentLinkedQueue

object ModuleNotification : Module("Notification", "提示", Category.RENDER,true) {

    fun isEnabled(): Boolean = enabled

    private val mode = enumChoice("Mode", Mode_enum.Text)
    private val sound = boolean("Sound",true)
    private val sound_mode = enumChoice("Sound Mode", sound_mode_enum.RICE)

    private enum class Mode_enum {
        Text,
        Normal;

        override fun toString(): String {
            return name
        }
    }

    private enum class sound_mode_enum{
        RICE,
        XYLITOL,
        SILENCE;

        override fun toString(): String {
            return name
        }
    }

    // Normal通知系统
    private enum class NotificationLevel(val color: Int) {
        SUCCESS(Color(23, 150, 38, 120).rgb),
        INFO(Color(23, 22, 38, 120).rgb),
        WARNING(Color(218, 182, 4, 120).rgb),
        ERROR(Color(160, 42, 43, 120).rgb)
    }

    // 平滑动画计时器
    private class SmoothAnimationTimer(var target: Float = 0f, var value: Float = 0f) {
        private val speed = 0.3f
        
        fun update(smooth: Boolean = true): Float {
            if (smooth) {
                value += (target - value) * speed
            } else {
                value = target
            }
            return value
        }
        
        fun isAnimationDone(threshold: Boolean = false): Boolean {
            return if (threshold) {
                Math.abs(target - value) < 0.1f
            } else {
                target == value
            }
        }
    }

    private class Notification(
        val level: NotificationLevel,
        val message: String,
        val maxAge: Long,
        val createTime: Long = System.currentTimeMillis()
    ) {
        // 动画计时器
        val widthTimer = SmoothAnimationTimer(0f)
        val heightTimer = SmoothAnimationTimer(0f)
        
        fun getWidth(): Float {
            val textRenderer = MinecraftClient.getInstance().textRenderer
            return textRenderer.getWidth(message) * 1.0f + 20f
        }
        
        fun getHeight(): Float {
            return 16f
        }
        
        fun renderShader(matrices: MatrixStack, x: Float, y: Float) {
            val cornerRadius = 8f // 增加圆角半径，与render方法保持一致
            RenderUtil.drawRoundedRect(matrices, x + 2f, y + 4f, getWidth(), getHeight(), cornerRadius, level.color)
        }
        fun render(context: DrawContext, x: Float, y: Float) {
            val matrices = context.matrices
            val textRenderer = MinecraftClient.getInstance().textRenderer
            val height = getHeight() // 使用getHeight方法获取高度
            val cornerRadius = 8f // 增加圆角半径
            
            // 直接绘制圆角矩形背景，不使用模板缓冲区
            RenderUtil.drawRoundedRect(matrices, x + 2f, y + 4f, getWidth(), height, cornerRadius, level.color)
            
            // 绘制文本
            val scale = 1.0f // 缩放比例
            matrices.push()
            matrices.scale(scale, scale, 1f)
            context.drawTextWithShadow(
                textRenderer,
                message,
                ((x + 10f) / scale).toInt(), // 增加左边距
                ((y + height/2 - 4f) / scale).toInt(), // 垂直居中
                0xFFFFFFFF.toInt()
            )
            matrices.pop()
        }
    }

    // 通知队列
    private val notifications = ConcurrentLinkedQueue<Notification>()

    // 提示持续时间(毫秒)
    private var lastToggleTime = 0L
    private var showingPrompt = false
    private var promptText = ""
    private var promptState = false

    
    // 当其他模块状态变化时调用
    fun onModuleToggle(module: Module) {
        if (!enabled) return

        // 更新提示状态
        promptText = module.getLocalizedName()
        promptState = module.enabled
        lastToggleTime = System.currentTimeMillis()
        showingPrompt = true

        // 根据模式决定是显示文本提示还是渲染HUD
        when (mode.value.toString()) {
            Mode_enum.Text.toString() -> {
                showTextPrompt()
            }
            Mode_enum.Normal.toString() -> {
                // 添加到Normal通知队列
                val level = if (module.enabled) NotificationLevel.SUCCESS else NotificationLevel.INFO
                val stateText = if (module.enabled) "已开启" else "已关闭"
                val message = "${module.getLocalizedName()} $stateText"
                notifications.add(Notification(level, message, 3000L))
            }
        }
    }


    fun showTextPrompt() {
        // 只有在TEXT模式下才发送消息
        if (mode.value.toString() != Mode_enum.Text.toString()) return

        val player = MinecraftClient.getInstance().player
        if (player != null && promptText.isNotEmpty()) {
            val stateText = if (promptState) "已开启" else "已关闭"
            ChatUtil.info("${promptText} $stateText")
        }
    }

    /**
     * 处理覆盖消息
     */
    fun onOverlayMessage(message: Text, tinted: Boolean): Boolean {
        if (!enabled) return false
        
        // 根据模式设置处理消息
        when (mode.value) {
            Mode_enum.Text.toString() -> {
                return true
            }
            Mode_enum.Normal.toString() -> {
                return false
            }
            else -> return false
        }
    }

    // 渲染阴影效果
    fun renderShadow(matrices: MatrixStack) {
        if (!enabled || mode.value.toString() != Mode_enum.Normal.toString()) return
        
        val mc = MinecraftClient.getInstance()
        val screenWidth = mc.window.scaledWidth
        val screenHeight = mc.window.scaledHeight
        
        // 渲染通知
        var yOffset = 20f // 底部边距
        
        // 创建通知列表的副本，以便按照从旧到新的顺序渲染
        val notificationsList = notifications.toList().reversed()
        
        for (notification in notificationsList) {
            val widthTimer = notification.widthTimer
            val heightTimer = notification.heightTimer
            
            // 只有当高度大于0时才渲染阴影
            if (heightTimer.value > 1f) {
                // 计算位置（右下角，确保完全在屏幕内）
                val width = notification.getWidth()
                val x = screenWidth - widthTimer.value - 20f // 右边距20像素
                val y = screenHeight - yOffset - heightTimer.value
                
                // 渲染阴影
                notification.renderShader(matrices, x, y)
                
                // 更新下一个通知的位置
                yOffset += heightTimer.value + 5f // 添加5像素的间距
            }
        }
    }
    
    fun renderHUDNormal(context: DrawContext) {
        if (!enabled || mode.value != Mode_enum.Normal.toString()) return
        
        val mc = MinecraftClient.getInstance()
        val screenWidth = mc.window.scaledWidth
        val screenHeight = mc.window.scaledHeight
        
        // 渲染通知
        var yOffset = 20f // 底部边距
        val currentTime = System.currentTimeMillis()
        
        // 创建要移除的通知列表
        val toRemove = mutableListOf<Notification>()
        
        // 创建通知列表的副本，以便按照从旧到新的顺序渲染
        val notificationsList = notifications.toList().reversed()
        
        for (notification in notificationsList) {
            // 保存当前矩阵状态
            context.matrices.push()
            
            val width = notification.getWidth()
            val height = notification.getHeight()
            
            val widthTimer = notification.widthTimer
            val heightTimer = notification.heightTimer
            
            // 计算生命周期
            val lifeTime = currentTime - notification.createTime
            
            if (lifeTime > notification.maxAge) {
                // 通知过期，开始退出动画
                widthTimer.target = 0f
                heightTimer.target = 0f
                
                // 如果动画完成，标记为移除
                if (widthTimer.isAnimationDone(true) && heightTimer.isAnimationDone(true)) {
                    toRemove.add(notification)
                    // 跳过渲染这个通知
                    context.matrices.pop()
                    continue
                }
            } else {
                // 通知显示中，设置目标值
                widthTimer.target = width
                heightTimer.target = height
            }
            
            // 更新动画
            widthTimer.update()
            heightTimer.update()
            
            // 只有当高度大于0时才渲染通知
            if (heightTimer.value > 1f) {
                // 计算位置（右下角，确保完全在屏幕内）
                val x = screenWidth - widthTimer.value - 20f // 右边距20像素
                val y = screenHeight - yOffset - heightTimer.value
                
                // 渲染通知
                notification.render(context, x, y)
                
                // 更新下一个通知的位置
                yOffset += heightTimer.value + 5f // 添加5像素的间距
            }
            
            // 恢复矩阵状态
            context.matrices.pop()
        }
        
        // 在遍历结束后移除标记的通知
        if (toRemove.isNotEmpty()) {
            notifications.removeAll(toRemove)
        }
    }
}
