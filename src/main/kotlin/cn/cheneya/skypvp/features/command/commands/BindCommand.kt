/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233
 */
package cn.cheneya.skypvp.features.command.commands

import cn.cheneya.skypvp.config.ConfigManager
import cn.cheneya.skypvp.features.command.*
import cn.cheneya.skypvp.features.module.ModuleManager
import cn.cheneya.skypvp.utils.ChatUtil
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.client.util.InputUtil

object KeyVerifier : ParameterVerifier<Int> {
    override fun verifyAndParse(input: String): ParameterValidationResult<Int> {
        // 处理特殊情况：none 或 clear 表示清除绑定
        if (input.equals("none", ignoreCase = true) || input.equals("clear", ignoreCase = true)) {
            return ParameterValidationResult.Ok(-1)
        }

        // 尝试解析按键代码
        val keyCode = try {
            // 尝试直接解析为数字
            input.toIntOrNull()?.let { return ParameterValidationResult.Ok(it) }

            // 尝试解析为按键名称
            val key = InputUtil.fromTranslationKey("key.keyboard.${input.lowercase()}")
            key.code
        } catch (e: Exception) {
            return ParameterValidationResult.Error("无效的按键名称: $input")
        }

        return ParameterValidationResult.Ok(keyCode)
    }
    
    override fun provideSuggestions(builder: SuggestionsBuilder, currentArg: String) {
        val lowerArg = currentArg.lowercase()
        
        // 添加特殊选项
        if ("none".startsWith(lowerArg)) builder.suggest("none")
        if ("clear".startsWith(lowerArg)) builder.suggest("clear")
        
        // 添加常用按键
        val commonKeys = listOf(
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10", "f11", "f12",
            "tab", "caps_lock", "lshift", "rshift", "lcontrol", "rcontrol",
            "lalt", "ralt", "space", "enter", "backspace", "escape"
        )
        
        for (key in commonKeys) {
            if (key.startsWith(lowerArg)) {
                builder.suggest(key)
            }
        }
    }
}

/**
 * 模块验证器
 */
object ModuleVerifier : ParameterVerifier<String> {
    override fun verifyAndParse(input: String): ParameterValidationResult<String> {
        // 处理特殊情况：none 或 clear 表示清除绑定
        if (input.equals("none", ignoreCase = true) || input.equals("clear", ignoreCase = true)) {
            return ParameterValidationResult.Ok(input.lowercase())
        }
        
        // 移除所有空格并转为小写，实现不区分大小写和空格
        val normalizedInput = input.replace(" ", "").lowercase()
        
        // 检查模块是否存在（匹配英文名称或中文名称，不区分大小写和空格）
        val module = ModuleManager.modules.find { module -> 
            module.name.replace(" ", "").lowercase() == normalizedInput || 
            module.displayName.replace(" ", "").lowercase() == normalizedInput
        }
        
        return if (module != null) {
            ParameterValidationResult.Ok(module.name)
        } else {
            ParameterValidationResult.Error("找不到模块: $input")
        }
    }
    
    override fun provideSuggestions(builder: SuggestionsBuilder, currentArg: String) {
        val normalizedArg = currentArg.replace(" ", "").lowercase()
        
        // 添加特殊选项
        if ("none".startsWith(normalizedArg)) builder.suggest("none")
        if ("clear".startsWith(normalizedArg)) builder.suggest("clear")
        
        // 添加所有模块名称（英文和中文）
        for (module in ModuleManager.modules) {
            val normalizedEnName = module.name.replace(" ", "").lowercase()
            val normalizedCnName = module.displayName.replace(" ", "").lowercase()
            
            // 检查英文名称是否匹配
            if (normalizedEnName.startsWith(normalizedArg)) {
                builder.suggest(module.name)
            }
            
            // 检查中文名称是否匹配（避免重复添加）
            if (normalizedCnName.startsWith(normalizedArg) && normalizedCnName != normalizedEnName) {
                builder.suggest(module.displayName)
            }
        }
    }
}

/**
 * 绑定命令处理器
 */
object BindCommandHandler : CommandHandler {
    /**
     * 执行绑定命令
     *
     * @param command 当前命令对象
     * @param args 命令参数数组
     */
    override fun invoke(command: Command, args: Array<Any>) {
        if (args.isEmpty()) {
            ChatUtil.error("请提供要绑定的模块和按键")
            return
        }

        val moduleName = args[0] as String
        val keyCode = if (args.size > 1) args[1] as Int else -1

        // 处理特殊情况：按键代码为 -1 表示清除绑定
        if (keyCode == -1) {
            ChatUtil.error("请提供有效的按键")
            return
        }

        // 获取按键名称
        val keyName = try {
            InputUtil.fromKeyCode(keyCode, 0).translationKey.replace("key.keyboard.", "").uppercase()
        } catch (e: Exception) {
            "KEY_$keyCode"
        }

        // 处理清除绑定的情况
        if (moduleName.equals("none", ignoreCase = true) || moduleName.equals("clear", ignoreCase = true)) {
            // 查找并清除绑定到该按键的所有模块
            var found = false
            for (module in ModuleManager.modules) {
                if (module.key == keyCode) {
                    module.key = -1
                    found = true
                }
            }
            
            if (found) {
                ChatUtil.info("已清除按键 $keyName 的所有绑定")
                // 保存配置
                ConfigManager.saveConfig()
            } else {
                ChatUtil.info("按键 $keyName 当前未绑定任何模块")
            }
            return
        }

        // 绑定按键到指定模块
        // 由于ModuleVerifier已经验证了模块名称，这里直接使用验证后的结果
        val module = ModuleManager.getModuleByName(moduleName) ?: run {
            // 尝试通过中文名称查找模块
            ModuleManager.modules.find { 
                it.displayName.replace(" ", "").equals(moduleName.replace(" ", ""), ignoreCase = true) 
            }
        }
        
        if (module != null) {
            // 检查是否有其他模块已经绑定到该按键
            val conflictModules = ModuleManager.modules.filter { it != module && it.key == keyCode }
            if (conflictModules.isNotEmpty()) {
                val moduleNames = conflictModules.joinToString(", ") { it.getLocalizedName() }
                ChatUtil.info("按键 $keyName 已绑定到以下模块: $moduleNames")
            }
            
            // 设置新的按键绑定
            module.key = keyCode
            ChatUtil.info("成功将按键 $keyName 绑定到模块: ${module.getLocalizedName()}")

            // 保存配置
            ConfigManager.saveConfig()
        } else {
            ChatUtil.error("找不到模块: $moduleName")
        }
    }
}

/**
 * 绑定命令
 * 用于将按键绑定到特定模块
 */
object BindCommand {
    /**
     * 创建绑定命令
     *
     * @return 绑定命令对象
     */
    fun create(): Command {
        return Command(
            name = "bind",
            aliases = arrayOf("b"),
            parameters = arrayOf(
                Parameter<String>(
                    name = "module",
                    required = true,
                    verifier = ModuleVerifier
                ),
                Parameter<Int>(
                    name = "key",
                    required = true,
                    verifier = KeyVerifier
                )
            ),
            handler = BindCommandHandler,
            requiresIngame = true,
            description = "将按键绑定到模块，或查看/清除现有绑定"
        )
    }
}
