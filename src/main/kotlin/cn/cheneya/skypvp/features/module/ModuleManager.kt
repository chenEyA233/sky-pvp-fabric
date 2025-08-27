package cn.cheneya.skypvp.features.module

import cn.cheneya.skypvp.config.Config
import cn.cheneya.skypvp.features.module.modules.combot.*
import cn.cheneya.skypvp.features.module.modules.misc.*
import cn.cheneya.skypvp.features.module.modules.movement.*
import cn.cheneya.skypvp.features.module.modules.render.*
import cn.cheneya.skypvp.features.module.modules.player.*
import cn.cheneya.skypvp.utils.ClientUtil.logger

object ModuleManager {
    private val _modules = mutableListOf<Module>()
    val modules: List<Module> get() = _modules

    fun init() {
        register(ModuleClickGui)
        register(ModuleAutoClick)
        register(ModuleRotation)
        register(ModuleKillAura)
        register(ModuleHUD)
        register(ModuleNotification)
        register(ModuleFullBright)
        register(ModuleESP)
        register(ModuleEagle)
        register(ModuleHackerDetector)
        register(ModuleAutoTools)
        register(ModuleEffectRenderOptimize)
        register(ModuleTarget)
        register(ModuleNameProtect)
        register(ModuleSprint)
        register(ModuleItemPhysic)
        register(ModuleAnimations)
        register(ModuleVelocity)
        register(ModuleInventoryClean)
        register(ModuleStuck)
        register(ModuleBlink)
        register(ModuleChestStealer)
        register(ModuleIRC)
        register(ModuleFastPlace)
        register(ModuleNewClickGui)
        register(ModuleLongJump)
        register(ModuleFly)
        register(ModuleAimBot)
        register(ModuleBlockingSwing)
        register(ModuleProtocol)
        
        logger.info("Load ${modules.size} module!")
    }

    private fun register(module: Module) {
        _modules.add(module)
    }

    fun getModulesByCategory(category: Category): List<Module> {
        return modules.filter { it.category == category }
    }

    fun getModuleByName(name: String): Module? {
        val normalizedName = name.replace(" ", "").lowercase()
        
        return modules.find { module -> 
            module.getLocalizedName().replace(" ", "").lowercase() == normalizedName ||
            module.name.replace(" ", "").lowercase() == normalizedName || 
            module.displayName.replace(" ", "").lowercase() == normalizedName
        }
    }

    fun getModuleByDisplayName(displayName: String): Module? {
        return modules.find { 
            it.getLocalizedName() == displayName ||
            it.displayName == displayName 
        }
    }
    fun getEnabledModules(): List<Module> {
        return modules.filter { it.enabled }
    }
    fun onModuleToggle(module: Module) {
        ModuleNotification.onModuleToggle(module)
        Config.onModuleToggle(module)
    }
}