package cn.cheneya.skypvp.config

import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.setting.Setting
import cn.cheneya.skypvp.utils.ClientUtil.logger

/**
 * 配置管理类
 * 负责保存和加载所有配置，包括按键绑定、模块启用状态和设置
 */
object Config {
    /**
     * 初始化配置
     */
    fun init() {
        // 加载配置
        ConfigManager.loadConfig()
        logger.info("配置系统初始化完成")
    }
    
    /**
     * 保存所有配置
     */
    fun saveAll() {
        ConfigManager.saveConfig()
        logger.info("所有配置已保存")
    }
    
    /**
     * 当设置更改时调用
     */
    fun onSettingChange(setting: Setting<*>) {
        // 自动保存设置
        ConfigManager.saveSettingConfig(setting)
    }
    
    /**
     * 当模块状态更改时调用
     */
    fun onModuleToggle(module: Module) {
        // 自动保存模块状态
        ConfigManager.saveModuleConfig(module)
    }
    
    /**
     * 当按键绑定更改时调用
     */
    fun onKeybindChange() {
        // 自动保存按键绑定
        ConfigManager.saveKeybindsConfig()
    }
}