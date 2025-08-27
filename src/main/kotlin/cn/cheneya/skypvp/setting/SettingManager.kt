package cn.cheneya.skypvp.setting

import cn.cheneya.skypvp.features.module.Module

/**
 * 设置管理器
 */
object SettingManager {
    private val settings = mutableListOf<Setting<*>>()

    /**
     * 注册设置
     */
    fun <T> registerSetting(setting: Setting<T>): Setting<T> {
        settings.add(setting)
        return setting
    }

    /**
     * 获取所有设置
     */
    fun getAllSettings(): List<Setting<*>> {
        return settings
    }

    /**
     * 获取模块的所有设置
     */
    fun getSettingsByModule(module: Module): List<Setting<*>> {
        return settings.filter { it.module == module }
    }

    /**
     * 通过名称获取模块的设置
     */
    fun getSettingByName(module: Module, name: String): Setting<*>? {
        return settings.find { it.module == module && it.name == name }
    }

    /**
     * 创建布尔设置
     */
    fun createBooleanSetting(
        name: String,
        module: Module,
        value: Boolean,
        description: String = ""
    ): BooleanSetting {
        val setting = BooleanSetting(name, module, value, description)
        registerSetting(setting)
        return setting
    }

    /**
     * 创建数值设置
     */
    fun createNumberSetting(
        name: String,
        module: Module,
        value: Double,
        min: Double,
        max: Double,
        increment: Double = 0.1,
        description: String = ""
    ): NumberSetting {
        val setting = NumberSetting(name, module, value, min, max, increment, description)
        registerSetting(setting)
        return setting
    }

    /**
     * 创建模式设置
     */
    fun createModeSetting(
        name: String,
        module: Module,
        value: String,
        modes: List<String>,
        description: String = ""
    ): ModeSetting {
        val setting = ModeSetting(name, module, value, modes, description)
        registerSetting(setting)
        return setting
    }

    /**
     * 创建颜色设置
     */
    fun createColorSetting(
        name: String,
        module: Module,
        value: Int,
        description: String = ""
    ): ColorSetting {
        val setting = ColorSetting(name, module, value, description)
        registerSetting(setting)
        return setting
    }
}