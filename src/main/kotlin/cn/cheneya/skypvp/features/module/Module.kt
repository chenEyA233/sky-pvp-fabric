package cn.cheneya.skypvp.features.module

import cn.cheneya.skypvp.api.utils.minecraft.GetLang
import cn.cheneya.skypvp.config.ConfigManager
import cn.cheneya.skypvp.event.EventListener
import cn.cheneya.skypvp.event.events.MotionEvent
import cn.cheneya.skypvp.setting.BooleanSetting
import cn.cheneya.skypvp.setting.ModeSetting
import cn.cheneya.skypvp.setting.NumberSetting
import cn.cheneya.skypvp.setting.SettingManager
import cn.cheneya.skypvp.utils.ClientUtil

/**
 * 模块基类
 * @param name 模块英文名称
 * @param displayName 模块中文名称
 * @param category 模块类别
 */
open class Module(
    open val name: String,
    open val displayName: String,
    open val category: Category,
    open var enabled: Boolean = false
) : EventListener {

    override val running: Boolean
        get() = enabled


    val ENName: String = name
    val CNName: String = displayName

    
    /**
     * 获取当前语言的模块名称
     */
    fun getLocalizedName(): String {
        return if (GetLang.isChinese()) CNName else ENName
    }


    // 添加boolean类型设置（开关）
    protected fun boolean(name: String, defaultValue: Boolean): BooleanSetting {
        val setting = BooleanSetting(name, this, defaultValue)
        SettingManager.registerSetting(setting)
        return setting
    }

    // 添加enum类型设置（多选项）
    protected inline fun <reified T : Enum<T>> enumChoice(name: String, defaultValue: T): ModeSetting {
        val modes = enumValues<T>().map { it.name }
        val setting = ModeSetting(name, this, defaultValue.name, modes)
        SettingManager.registerSetting(setting)
        return setting
    }

    // 添加数值类型设置（滑块）
    protected fun number(name: String, defaultValue: Double, min: Double, max: Double, increment: Double = 0.1): NumberSetting {
        val setting = NumberSetting(name, this, defaultValue, min, max, increment)
        SettingManager.registerSetting(setting)
        return setting
    }

    // 添加整数类型设置（滑块）- 便捷方法
    protected fun integer(name: String, defaultValue: Int, min: Int, max: Int, increment: Int = 1): NumberSetting {
        return number(name, defaultValue.toDouble(), min.toDouble(), max.toDouble(), increment.toDouble())
    }
    
    // 添加浮点数类型设置（滑块）- 便捷方法
    protected fun float(name: String, defaultValue: Float, min: Float, max: Float, increment: Float = 0.01f): NumberSetting {
        return number(name, defaultValue.toDouble(), min.toDouble(), max.toDouble(), increment.toDouble())
    }

    // 模块按键绑定
    private var _key = -1
    var key: Int
        get() = _key
        set(value) {
            if (_key != value) {
                _key = value
                // 通知配置系统按键绑定已更改
                cn.cheneya.skypvp.config.Config.onKeybindChange()
            }
        }
    
    // 状态变化监听器列表
    private val toggleListeners = mutableListOf<(Boolean) -> Unit>()
    
    /**
     * 启用模块
     */
    open fun enable() {
        if (!enabled) {
            enabled = true
            onEnable()
            notifyToggleListeners()
            ModuleManager.onModuleToggle(this)
            ConfigManager.saveConfig()
        }
    }
    
    /**
     * 禁用模块
     */
    fun disable() {
        if (enabled) {
            enabled = false
            onDisable()
            notifyToggleListeners()
            ModuleManager.onModuleToggle(this)
            ConfigManager.saveConfig()
        }
    }
    
    /**
     * 切换模块状态
     */
    fun toggle() {
        if (enabled) disable() else enable()
    }
    
    /**
     * 添加状态变化监听器
     */
    fun addToggleListener(listener: (Boolean) -> Unit) {
        toggleListeners.add(listener)
    }
    
    /**
     * 移除状态变化监听器
     */
    fun removeToggleListener(listener: (Boolean) -> Unit) {
        toggleListeners.remove(listener)
    }
    
    /**
     * 通知所有监听器状态已改变
     */
    private fun notifyToggleListeners() {
        toggleListeners.forEach { it(enabled) }
    }
    
    /**
     * 模块启用时调用
     */
    open fun onEnable() {}
    
    /**
     * 模块禁用时调用
     */
    open fun onDisable() {}
    
    /**
     * 游戏更新时调用
     */
    open fun onUpdate() {}
}