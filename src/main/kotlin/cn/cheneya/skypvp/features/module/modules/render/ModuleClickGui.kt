package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.ModuleManager
import cn.cheneya.skypvp.setting.BooleanSetting
import cn.cheneya.skypvp.setting.ModeSetting
import cn.cheneya.skypvp.setting.NumberSetting
import cn.cheneya.skypvp.setting.Setting
import cn.cheneya.skypvp.setting.SettingManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.max
import kotlin.math.min


object ModuleClickGui: Module("ClickGui","点击GUI", Category.RENDER) {

    init {
        this.key = GLFW.GLFW_KEY_RIGHT_SHIFT
    }

    override fun onEnable() {
        // 打开ClickGui界面
        MinecraftClient.getInstance().setScreen(ClickGuiScreen())
        // 自动关闭模块，避免重复触发
        enabled = false
    }

    // ClickGui界面实现
    open class ClickGuiScreen : Screen(Text.literal("ClickGui")) {

        private val categoryPanels = mutableListOf<CategoryPanel>()
        private var draggingPanel: CategoryPanel? = null
        private var dragOffsetX = 0
        private var dragOffsetY = 0

        init {
            // 初始化分类面板
            var startX = 10
            Category.entries.forEach { category ->
                categoryPanels.add(CategoryPanel(category, startX, 30))
                startX += 120
            }
        }
        
        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            // 绘制背景
            renderBackground(context, mouseX, mouseY, delta)
            
            // 保存当前渲染状态
            val matrices = context.matrices
            matrices.push()
            
            // 渲染所有分类面板
            categoryPanels.forEach { panel ->
                panel.render(context, mouseX, mouseY)
            }
            
            // 恢复渲染状态
            matrices.pop()
            
            // 调用父类的render方法，但不包括背景渲染
            super.render(context, mouseX, mouseY, delta)
        }
        
        // 覆盖renderBackground方法，绘制浅黑色半透明背景
        override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            // 绘制固定不透明背景
            val alpha = 80
            if (alpha > 0) {
                context.fill(0, 0, width, height, Color(0, 0, 0, alpha).getRGB())
            }
        }
        
        // 重写shouldPause方法，返回false使游戏不暂停
        override fun shouldPause(): Boolean {
            return false
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            // 处理面板拖动
            if (button == 0) {
                for (panel in categoryPanels) {
                    if (panel.isHeaderHovered(mouseX.toInt(), mouseY.toInt())) {
                        draggingPanel = panel
                        dragOffsetX = mouseX.toInt() - panel.x
                        dragOffsetY = mouseY.toInt() - panel.y
                        return true
                    }

                    // 处理模块点击
                    val clickedModule = panel.getModuleAt(mouseX.toInt(), mouseY.toInt())
                    if (clickedModule != null) {
                        clickedModule.toggle()
                        return true
                    }

                    // 处理设置点击
                    if (panel.expandedModule != null) {
                        val clickedSetting = panel.getSettingAt(mouseX.toInt(), mouseY.toInt())
                        if (clickedSetting != null) {
                            panel.handleSettingClick(clickedSetting, mouseX.toInt())
                            return true
                        }
                    }

                    // 处理模块展开/折叠
                    val moduleForExpand = panel.getModuleForExpand(mouseX.toInt(), mouseY.toInt())
                    if (moduleForExpand != null) {
                        if (panel.expandedModule == moduleForExpand) {
                            panel.expandedModule = null
                        } else {
                            panel.expandedModule = moduleForExpand
                        }
                        return true
                    }
                }
            }
            // 处理右键点击展开配置
            else if (button == 1) {
                for (panel in categoryPanels) {
                    val clickedModule = panel.getModuleAt(mouseX.toInt(), mouseY.toInt())
                    if (clickedModule != null) {
                        val settings = SettingManager.getSettingsByModule(clickedModule)
                        if (settings.isNotEmpty()) {
                            if (panel.expandedModule == clickedModule) {
                                panel.expandedModule = null
                            } else {
                                panel.expandedModule = clickedModule
                            }
                            return true
                        }
                    }
                }
            }

            return super.mouseClicked(mouseX, mouseY, button)
        }

        override fun mouseDragged(
            mouseX: Double,
            mouseY: Double,
            button: Int,
            deltaX: Double,
            deltaY: Double
        ): Boolean {
            // 处理面板拖动
            if (button == 0 && draggingPanel != null) {
                draggingPanel!!.x = mouseX.toInt() - dragOffsetX
                draggingPanel!!.y = mouseY.toInt() - dragOffsetY
                return true
            }

            // 处理滑块拖动
            for (panel in categoryPanels) {
                if (panel.handleSliderDrag(mouseX.toInt())) {
                    return true
                }
            }

            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        }

        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (button == 0) {
                draggingPanel = null

                // 停止滑块拖动
                categoryPanels.forEach { it.stopDraggingSlider() }
            }

            return super.mouseReleased(mouseX, mouseY, button)
        }

        override fun shouldCloseOnEsc(): Boolean {
            return true
        }

        inner class CategoryPanel(val category: Category, var x: Int, var y: Int) {
            private val width = 100
            private val headerHeight = 20
            var expandedModule: Module? = null
            private var draggingSlider: Setting<*>? = null

            fun render(context: DrawContext, mouseX: Int, mouseY: Int, opacity: Float = 1f) {
                // 绘制面板标题
                val headerColor = Color(30, 30, 30, (255 * opacity).toInt()).rgb
                val headerHoverColor = Color(40, 40, 40, (255 * opacity).toInt()).rgb
                val borderColor = Color(0, 120, 215, (255 * opacity).toInt()).rgb

                val isHeaderHovered = isHeaderHovered(mouseX, mouseY)
                context.fill(x, y, x + width, y + headerHeight, if (isHeaderHovered) headerHoverColor else headerColor)
                context.fill(x, y, x + width, y + 1, borderColor) // 顶部边框
                context.fill(x, y, x + 1, y + headerHeight, borderColor) // 左边框
                context.fill(x + width - 1, y, x + width, y + headerHeight, borderColor) // 右边框
                context.fill(x, y + headerHeight - 1, x + width, y + headerHeight, borderColor) // 底部边框

                // 绘制分类名称
                val categoryName = category.getName()
                val textX = x + width / 2 - textRenderer.getWidth(categoryName) / 2
                val textY = y + headerHeight / 2 - textRenderer.fontHeight / 2
                context.drawText(textRenderer, categoryName, textX, textY, Color(255, 255, 255, (255 * opacity).toInt()).rgb, true)

                // 获取该分类下的所有模块
                val modules = ModuleManager.modules.filter { it.category == category }

                // 计算面板内容高度
                var contentHeight = modules.size * 20
                if (expandedModule != null) {
                    val settings = SettingManager.getSettingsByModule(expandedModule!!)
                    contentHeight += settings.size * 20

                }

                // 绘制面板内容背景
                val contentColor = Color(20, 20, 20, (220 * opacity).toInt()).rgb
                context.fill(x, y + headerHeight, x + width, y + headerHeight + contentHeight, contentColor)
                context.fill(
                    x,
                    y + headerHeight + contentHeight,
                    x + width,
                    y + headerHeight + contentHeight + 1,
                    borderColor
                ) // 底部边框
                context.fill(x, y + headerHeight, x + 1, y + headerHeight + contentHeight, borderColor) // 左边框
                context.fill(
                    x + width - 1,
                    y + headerHeight,
                    x + width,
                    y + headerHeight + contentHeight,
                    borderColor
                ) // 右边框

                // 绘制模块
                var moduleY = y + headerHeight
                modules.forEach { module ->
                    val moduleColor = if (module.enabled) Color(0, 120, 215, (150 * opacity).toInt()).rgb else Color(40, 40, 40, (150 * opacity).toInt()).rgb
                    val moduleHoverColor =
                        if (module.enabled) Color(0, 140, 235, (150 * opacity).toInt()).rgb else Color(50, 50, 50, (150 * opacity).toInt()).rgb

                    val isModuleHovered =
                        mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY < moduleY + 20
                    context.fill(
                        x + 1,
                        moduleY,
                        x + width - 1,
                        moduleY + 20,
                        if (isModuleHovered) moduleHoverColor else moduleColor
                    )

                    // 绘制模块名称
                    val moduleName = module.getLocalizedName()
                    context.drawText(textRenderer, moduleName, x + 5, moduleY + 6, Color(255, 255, 255, (255 * opacity).toInt()).rgb, true)

                    // 绘制展开/折叠按钮
                    val settings = SettingManager.getSettingsByModule(module)
                    if (settings.isNotEmpty()) {
                        val expandChar = if (expandedModule == module) "-" else "+"
                        context.drawText(textRenderer, expandChar, x + width - 10, moduleY + 6, Color(255, 255, 255, (255 * opacity).toInt()).rgb, true)
                    }

                    moduleY += 20

                    // 如果模块展开，绘制设置
                    if (expandedModule == module) {
                        settings.forEach { setting ->
                            val settingColor = Color(60, 60, 60, (150 * opacity).toInt()).rgb
                            context.fill(x + 1, moduleY, x + width - 1, moduleY + 20, settingColor)

                            // 绘制设置名称
                            val settingName = setting.name
                            context.drawText(textRenderer, settingName, x + 5, moduleY + 6, Color(255, 255, 255, (255 * opacity).toInt()).rgb, true)

                            // 根据设置类型绘制控件
                            when (setting.value) {
                                is Boolean -> {
                                    val boolValue = setting.value as Boolean
                                    val toggleColor =
                                        if (boolValue) Color(0, 255, 0, (150 * opacity).toInt()).rgb else Color(255, 0, 0, (150 * opacity).toInt()).rgb
                                    context.fill(x + width - 25, moduleY + 5, x + width - 5, moduleY + 15, toggleColor)
                                    val toggleText = if (boolValue) "开" else "关"
                                    context.drawText(
                                        textRenderer,
                                        toggleText,
                                        x + width - 20,
                                        moduleY + 6,
                                        Color(255, 255, 255, (255 * opacity).toInt()).rgb,
                                        true
                                    )
                                }

                                is Int, is Double -> {
                                    // 处理NumberSetting
                                    if (setting is NumberSetting) {
                                        val numberValue = setting.value
                                        val minValue = setting.min
                                        val maxValue = setting.max
                                        
                                        // 绘制数值（保留一位小数）
                                        val displayValue = if (numberValue % 1.0 == 0.0) {
                                            numberValue.toInt().toString()
                                        } else {
                                            String.format("%.1f", numberValue)
                                        }
                                        
                                        context.drawText(
                                            textRenderer,
                                            displayValue,
                                            x + width - 25,
                                            moduleY + 6,
                                            Color(255, 255, 255, (255 * opacity).toInt()).rgb,
                                            true
                                        )

                                        // 绘制滑块背景
                                        val sliderWidth = width - 30
                                        val sliderX = x + 15
                                        val sliderY = moduleY + 15
                                        context.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 4, Color(60, 60, 60, (200 * opacity).toInt()).rgb)

                                        // 计算滑块位置
                                        val percentage = (numberValue - minValue) / (maxValue - minValue)
                                        val sliderPos = sliderX + percentage * sliderWidth
                                        
                                        // 绘制滑块
                                        context.fill(sliderX, sliderY, sliderPos.toInt(), sliderY + 4, Color(0, 120, 215, (255 * opacity).toInt()).rgb)
                                        context.fill(sliderPos.toInt() - 2, sliderY - 2, sliderPos.toInt() + 2, sliderY + 6, Color(200, 200, 200, (255 * opacity).toInt()).rgb)
                                    }
                                    // 处理非NumberSetting的Int值
                                    else if (setting.value is Int) {
                                        val intValue = setting.value as Int
                                        context.drawText(
                                            textRenderer,
                                            intValue.toString(),
                                            x + width - 25,
                                            moduleY + 6,
                                            Color.WHITE.rgb,
                                            true
                                        )
                                    }
                                }

                                is String -> {
                                    val stringValue = setting.value as String
                                    val valueWidth = min(textRenderer.getWidth(stringValue), 50)
                                    context.drawText(
                                        textRenderer,
                                        stringValue,
                                        x + width - 5 - valueWidth,
                                        moduleY + 6,
                                        Color(255, 255, 255, (255 * opacity).toInt()).rgb,
                                        true
                                    )
                                }

                                is Enum<*> -> {
                                    val enumValue = setting.value as Enum<*>
                                    val valueWidth = min(textRenderer.getWidth(enumValue.name), 50)
                                    context.drawText(
                                        textRenderer,
                                        enumValue.name,
                                        x + width - 5 - valueWidth,
                                        moduleY + 6,
                                        Color(255, 255, 255, (255 * opacity).toInt()).rgb,
                                        true
                                    )
                                }
                            }

                            moduleY += 20
                        }
                    }
                }
            }

            fun isHeaderHovered(mouseX: Int, mouseY: Int): Boolean {
                return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + headerHeight
            }

            fun getModuleAt(mouseX: Int, mouseY: Int): Module? {
                if (mouseX < x || mouseX > x + width) return null // 扩大点击区域
                val modules = ModuleManager.modules.filter { it.category == category }
                var moduleY = y + headerHeight

                for (module in modules) {
                    // 检查是否点击在模块区域（排除展开/折叠按钮区域）
                    if (mouseY >= moduleY && mouseY < moduleY + 20) {
                        // 如果点击在展开/折叠按钮区域，则不触发模块切换
                        if (mouseX >= x + width - 20 && mouseX <= x + width) {
                            val settings = SettingManager.getSettingsByModule(module)
                            if (settings.isNotEmpty()) {
                                return null // 在展开/折叠按钮区域不触发模块切换
                            }
                        }
                        // 确保点击在模块名称区域
                        if (mouseX >= x && mouseX <= x + width - 20) {
                            return module // 在模块区域点击，返回该模块
                        }
                    }

                    moduleY += 20

                    // 如果模块展开，跳过设置区域
                    if (expandedModule == module) {
                        val settings = SettingManager.getSettingsByModule(module)
                        moduleY += settings.size * 20
                    }
                }

                return null
            }

            fun getModuleForExpand(mouseX: Int, mouseY: Int): Module? {
                // 只在右侧20像素区域内检测展开/折叠按钮点击
                if (mouseX < x + width - 20 || mouseX > x + width) return null

                val modules = ModuleManager.modules.filter { it.category == category }
                var moduleY = y + headerHeight

                for (module in modules) {
                    // 检查是否点击在展开/折叠按钮区域
                    if (mouseY >= moduleY && mouseY < moduleY + 20) {
                        val settings = SettingManager.getSettingsByModule(module)
                        if (settings.isNotEmpty()) {
                            return module
                        }
                    }

                    moduleY += 20

                    // 如果模块展开，跳过设置区域
                    if (expandedModule == module) {
                        val settings = SettingManager.getSettingsByModule(module)
                        moduleY += settings.size * 20
                    }
                }

                return null
            }

            fun getSettingAt(mouseX: Int, mouseY: Int): Setting<*>? {
                if (expandedModule == null) return null
                if (mouseX < x || mouseX > x + width) return null

                val modules = ModuleManager.modules.filter { it.category == category }
                var moduleY = y + headerHeight

                // 找到展开的模块的位置
                for (module in modules) {
                    moduleY += 20

                    if (module == expandedModule) {
                        val settings = SettingManager.getSettingsByModule(module)
                        for (setting in settings) {
                            // 检查鼠标是否在设置区域内
                            if (mouseY >= moduleY && mouseY < moduleY + 20) {
                                // 对于数值类型的设置，检查是否点击在滑块区域
                                if (setting.value is Int || setting.value is Float || setting.value is Double || setting is NumberSetting) {
                                    val sliderY = moduleY + 15
                                    // 如果点击在滑块区域
                                    if (mouseY >= sliderY - 2 && mouseY <= sliderY + 6) {
                                        val sliderX = x + 15
                                        val sliderWidth = width - 30
                                        // 确保点击在滑块的水平范围内
                                        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth) {
                                            return setting
                                        }
                                    }
                                }
                                return setting
                            }

                            // 更新下一个设置的Y坐标
                            moduleY += 20
                        }
                        break
                    }

                    // 如果其他模块展开，跳过设置区域
                    if (expandedModule == module) {
                        val settings = SettingManager.getSettingsByModule(module)
                        moduleY += settings.size * 20
                    }
                }
                return null
            }

            fun handleSettingClick(setting: Setting<*>, mouseX: Int) {
                when (setting) {
                    is BooleanSetting -> {
                        // 切换布尔值
                        setting.value = !setting.value
                    }

                    is NumberSetting -> {
                        // 开始拖动滑块
                        draggingSlider = setting
                        updateSliderValue(setting, mouseX)
                    }

                    is ModeSetting -> {
                        setting.nextMode()
                    }
                }
            }

            fun handleSliderDrag(mouseX: Int): Boolean {
                if (draggingSlider != null && draggingSlider is NumberSetting) {
                    updateSliderValue(draggingSlider as NumberSetting, mouseX)
                    return true
                }
                return false
            }

            private fun updateSliderValue(setting: NumberSetting, mouseX: Int) {
                val sliderWidth = width - 30
                val sliderX = x + 15  // 修正为与render方法中相同的滑块X坐标

                // 确保鼠标X坐标在滑块范围内
                val clampedMouseX = max(sliderX, min(sliderX + sliderWidth, mouseX))

                // 计算百分比
                val percentage = (clampedMouseX - sliderX).toFloat() / sliderWidth
                val clampedPercentage = percentage.coerceIn(0f, 1f)

                // 计算新值（始终为Double类型）
                val minValue = setting.min
                val maxValue = setting.max
                val newValue = minValue + clampedPercentage * (maxValue - minValue)
                
                // 设置新值
                setting.value = newValue
            }

            fun stopDraggingSlider() {
                draggingSlider = null
            }
        }
    }
}