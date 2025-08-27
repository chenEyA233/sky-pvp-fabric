package cn.cheneya.skypvp.config

import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.ModuleManager
import cn.cheneya.skypvp.setting.Setting
import cn.cheneya.skypvp.setting.SettingManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 配置管理器
 * 负责将配置数据序列化为JSON格式并保存到文件，以及从文件中读取配置数据并反序列化
 */
object ConfigManager {
    /**
     * 重置配置文件
     * 删除现有的配置文件并创建一个新的默认配置文件
     */
    fun resetConfig() {
        try {
            // 备份现有的配置文件
            if (configFile.exists()) {
                val backupFile = File("${configFile.absolutePath}.backup.${System.currentTimeMillis()}")
                configFile.copyTo(backupFile, overwrite = true)
                println("已将现有的配置文件备份到: ${backupFile.absolutePath}")
                
                // 删除现有的配置文件
                configFile.delete()
                println("已删除现有的配置文件")
            }
            
            // 创建新的默认配置文件
            saveConfig()
            println("已创建新的默认配置文件")
        } catch (e: Exception) {
            println("重置配置文件失败: ${e.message}")
            e.printStackTrace()
        }
    }
    /**
     * 验证配置文件的完整性
     * 检查所有必要的配置项是否存在，并且值的类型是否正确
     * @return 如果配置文件完整且有效，则返回true；否则返回false
     */
    fun validateConfig(): Boolean {
        try {
            // 如果配置文件不存在，则创建默认配置
            if (!configFile.exists()) {
                println("配置文件不存在，将创建默认配置")
                saveConfig()
                return true
            }

            // 解析JSON文件
            val rootObject = try {
                FileReader(configFile).use { reader ->
                    JsonParser.parseReader(reader).asJsonObject
                }
            } catch (e: Exception) {
                println("解析配置文件失败: ${e.message}")
                e.printStackTrace()
                return false
            }

            // 检查modules对象是否存在
            val modulesObject = rootObject.getAsJsonObject("modules")
            if (modulesObject == null) {
                println("配置文件中没有找到modules对象")
                return false
            }

            // 检查所有模块的配置是否存在
            var isValid = true
            ModuleManager.modules.forEach { module ->
                val encodedName = encodeModuleName(module.name)
                if (!modulesObject.has(encodedName)) {
                    println("配置文件中没有找到模块 ${module.name} 的配置")
                    isValid = false
                } else {
                    val moduleObject = modulesObject.getAsJsonObject(encodedName)
                    
                    // 检查模块的enabled属性是否存在
                    if (!moduleObject.has("enabled")) {
                        println("模块 ${module.name} 的配置中没有找到enabled属性")
                        isValid = false
                    } else {
                        try {
                            moduleObject.get("enabled").asBoolean
                        } catch (e: Exception) {
                            println("模块 ${module.name} 的enabled属性类型不正确")
                            isValid = false
                        }
                    }
                    
                    // 检查模块的key属性是否存在
                    if (!moduleObject.has("key")) {
                        println("模块 ${module.name} 的配置中没有找到key属性")
                        isValid = false
                    } else {
                        try {
                            moduleObject.get("key").asInt
                        } catch (e: Exception) {
                            println("模块 ${module.name} 的key属性类型不正确")
                            isValid = false
                        }
                    }
                    
                    // 检查模块的设置项是否存在
                    SettingManager.getAllSettings()
                        .filter { it.module == module }
                        .forEach { setting ->
                            if (!moduleObject.has(setting.name)) {
                                println("模块 ${module.name} 的配置中没有找到设置项 ${setting.name}")
                                isValid = false
                            } else {
                                try {
                                    val jsonElement = moduleObject.get(setting.name)
                                    when (setting.value) {
                                        is Boolean -> jsonElement.asBoolean
                                        is Double -> jsonElement.asDouble
                                        is Float -> jsonElement.asFloat
                                        is Int -> jsonElement.asInt
                                        is String -> jsonElement.asString
                                        is Enum<*> -> jsonElement.asString
                                        else -> {
                                            println("不支持的设置类型: ${setting.value?.javaClass?.name} 用于设置 ${module.name}.${setting.name}")
                                            isValid = false
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("设置项 ${module.name}.${setting.name} 的类型不正确")
                                    isValid = false
                                }
                            }
                        }
                }
            }
            
            return isValid
        } catch (e: Exception) {
            println("验证配置文件失败: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    private fun encodeModuleName(name: String): String {
        return try {
            // 先替换空格为%20，再编码其他特殊字符
            URLEncoder.encode(name.replace(" ", "%20"), "UTF-8")
                .replace("+", "%20") // 确保空格保持为%20
        } catch (e: Exception) {
            println("模块名编码失败，使用简单替换: ${e.message}")
            name.replace(" ", "%20")
                .replace("[^a-zA-Z0-9%-_]".toRegex(), "_") // 替换其他特殊字符为下划线
        }
    }

    private fun decodeModuleName(encodedName: String): String {
        return try {
            // 先解码，然后处理%20
            URLDecoder.decode(encodedName, "UTF-8")
                .replace("%20", " ")
        } catch (e: Exception) {
            println("模块名解码失败，使用简单替换: ${e.message}")
            encodedName.replace("%20", " ")
                .replace("_", " ") // 还原下划线为空格
        }
    }
    private val configFile = File("skypvp/config/config.json").apply {
        // 确保配置目录存在
        parentFile?.mkdirs()
    }

    // Gson实例，用于JSON序列化和反序列化
    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * 保存配置到文件
     */
    fun saveConfig() {
        try {
            // 确保配置目录存在
            configFile.parentFile?.mkdirs()

            // 创建根JSON对象
            val rootObject = JsonObject()

            // 保存模块配置
            val modulesObject = JsonObject()
            ModuleManager.modules.forEach { module ->
                val moduleObject = JsonObject().apply {
                    addProperty("enabled", module.enabled)
                    addProperty("key", module.key)
                    
                    // 保存模块的设置项
                    SettingManager.getAllSettings()
                        .filter { it.module == module }
                        .forEach { setting ->
                            when (val value = setting.value) {
                                is Boolean -> addProperty(setting.name, value)
                                is Number -> addProperty(setting.name, value.toDouble())
                                is String -> addProperty(setting.name, value)
                                else -> addProperty(setting.name, value.toString())
                            }
                        }
                }
                modulesObject.add(encodeModuleName(module.name), moduleObject)
            }
            rootObject.add("modules", modulesObject)

            // 写入文件
            FileWriter(configFile).use { writer ->
                gson.toJson(rootObject, writer)
            }
        } catch (e: Exception) {
            println("保存配置失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 从文件加载配置
     */
    fun loadConfig() {
        try {
            // 验证配置文件的完整性
            if (!validateConfig()) {
                println("配置文件验证失败，将创建新的默认配置")
                // 备份损坏的配置文件
                if (configFile.exists()) {
                    val backupFile = File("${configFile.absolutePath}.invalid.${System.currentTimeMillis()}")
                    configFile.copyTo(backupFile, overwrite = true)
                    println("已将无效的配置文件备份到: ${backupFile.absolutePath}")
                }
                saveConfig()
                return
            }

            // 解析JSON文件
            val rootObject = try {
                FileReader(configFile).use { reader ->
                    JsonParser.parseReader(reader).asJsonObject
                }
            } catch (e: Exception) {
                println("解析配置文件失败: ${e.message}")
                e.printStackTrace()
                // 备份损坏的配置文件
                val backupFile = File("${configFile.absolutePath}.backup.${System.currentTimeMillis()}")
                configFile.copyTo(backupFile, overwrite = true)
                println("已将损坏的配置文件备份到: ${backupFile.absolutePath}")
                // 创建新的默认配置
                saveConfig()
                return
            }

            // 加载模块配置
            val modulesObject = rootObject.getAsJsonObject("modules")
            if (modulesObject == null) {
                println("配置文件中没有找到modules对象，将使用默认配置")
                saveConfig()
                return
            }
            
            modulesObject.let { 
                it.keySet().forEach { encodedName ->
                    try {
                        val moduleName = decodeModuleName(encodedName)
                        ModuleManager.modules.find { it.name == moduleName }?.let { module ->
                            val moduleObject = it.getAsJsonObject(encodedName)
                            if (moduleObject != null) {
                                // 设置按键绑定
                                if (moduleObject.has("key")) {
                                    module.key = moduleObject.get("key").asInt
                                }

                                // 设置模块状态
                                if (moduleObject.has("enabled")) {
                                    val enabled = moduleObject.get("enabled").asBoolean
                                    if (enabled != module.enabled) {
                                        if (enabled) module.enable() else module.disable()
                                    }
                                }

                                // 加载模块设置项
                                SettingManager.getAllSettings()
                                    .filter { it.module == module }
                                    .forEach { setting ->
                                        if (moduleObject.has(setting.name)) {
                                            try {
                                                @Suppress("UNCHECKED_CAST")
                                                try {
                                                    val jsonElement = moduleObject.get(setting.name)
                                                    when (val value = setting.value) {
                                                        is Boolean -> (setting as Setting<Boolean>).value = jsonElement.asBoolean
                                                        is Double -> (setting as Setting<Double>).value = jsonElement.asDouble
                                                        is Float -> (setting as Setting<Float>).value = jsonElement.asFloat
                                                        is Int -> (setting as Setting<Int>).value = jsonElement.asInt
                                                        is String -> (setting as Setting<String>).value = jsonElement.asString
                                                        is Enum<*> -> {
                                                            try {
                                                                val enumValue = jsonElement.asString
                                                                val enumClass = value.javaClass
                                                                val enumConstants = enumClass.enumConstants
                                                                val matchingEnum = enumConstants.find { 
                                                                    it.name.equals(enumValue, ignoreCase = true) 
                                                                }
                                                                if (matchingEnum != null) {
                                                                    (setting as Setting<Enum<*>>).value = matchingEnum
                                                                    println("成功加载枚举设置 ${module.name}.${setting.name} = ${matchingEnum.name}")
                                                                } else {
                                                                    println("找不到枚举值 ${enumValue} 用于设置 ${module.name}.${setting.name}")
                                                                }
                                                            } catch (e: Exception) {
                                                                println("加载枚举设置 ${module.name}.${setting.name} 失败: ${e.message}")
                                                                e.printStackTrace()
                                                            }
                                                        }
                                                        else -> {
                                                            println("不支持的设置类型: ${value?.javaClass?.name} 用于设置 ${module.name}.${setting.name}")
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    println("加载设置 ${module.name}.${setting.name} 失败: ${e.message}")
                                                    e.printStackTrace()
                                                }
                                            } catch (e: Exception) {
                                                println("加载设置 ${module.name}.${setting.name} 失败: ${e.message}")
                                            }
                                        }
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        println("加载模块 $encodedName 配置失败: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("加载配置失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 保存单个模块的配置
     */
    fun saveModuleConfig(module: Module) {
        try {
            // 如果配置文件不存在，则创建完整配置
            if (!configFile.exists()) {
                saveConfig()
                return
            }

            // 解析现有JSON文件
            val rootObject = if (configFile.exists()) {
                FileReader(configFile).use { reader ->
                    JsonParser.parseReader(reader).asJsonObject
                }
            } else {
                JsonObject()
            }

            // 获取或创建modules对象
            val modulesObject = rootObject.getAsJsonObject("modules") ?: JsonObject().also {
                rootObject.add("modules", it)
            }

            // 更新模块配置
            val moduleObject = modulesObject.getAsJsonObject(encodeModuleName(module.name)) ?: JsonObject().also {
                modulesObject.add(encodeModuleName(module.name), it)
            }
            moduleObject.addProperty("enabled", module.enabled)
            moduleObject.addProperty("key", module.key)

            // 保存模块的设置项
            SettingManager.getAllSettings()
                .filter { it.module == module }
                .forEach { setting ->
                    try {
                        when (val value = setting.value) {
                            is Boolean -> moduleObject.addProperty(setting.name, value)
                            is Number -> moduleObject.addProperty(setting.name, value.toDouble())
                            is String -> moduleObject.addProperty(setting.name, value)
                            is Enum<*> -> moduleObject.addProperty(setting.name, value.name)
                            else -> moduleObject.addProperty(setting.name, value.toString())
                        }
                    } catch (e: Exception) {
                        println("保存设置 ${module.name}.${setting.name} 失败: ${e.message}")
                        e.printStackTrace()
                    }
                }

            // 写入文件
            FileWriter(configFile).use { writer ->
                gson.toJson(rootObject, writer)
            }
        } catch (e: Exception) {
            println("保存模块配置失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 保存单个设置的配置
     */
    fun saveSettingConfig(setting: Setting<*>) {
        try {
            // 如果配置文件不存在，则创建完整配置
            if (!configFile.exists()) {
                saveConfig()
                return
            }

            // 解析现有JSON文件
            val rootObject = if (configFile.exists()) {
                FileReader(configFile).use { reader ->
                    JsonParser.parseReader(reader).asJsonObject
                }
            } else {
                JsonObject()
            }

            // 获取或创建modules对象
            val modulesObject = rootObject.getAsJsonObject("modules") ?: JsonObject().also {
                rootObject.add("modules", it)
            }

            // 获取或创建模块配置对象
            val moduleObject = modulesObject.getAsJsonObject(encodeModuleName(setting.module.name)) ?: JsonObject().also {
                modulesObject.add(encodeModuleName(setting.module.name), it)
            }

            // 根据设置类型保存值
            try {
                when (val value = setting.value) {
                    is Boolean -> moduleObject.addProperty(setting.name, value)
                    is Number -> moduleObject.addProperty(setting.name, value.toDouble())
                    is String -> moduleObject.addProperty(setting.name, value)
                    is Enum<*> -> moduleObject.addProperty(setting.name, value.name)
                    else -> moduleObject.addProperty(setting.name, value.toString())
                }
            } catch (e: Exception) {
                println("保存设置 ${setting.module.name}.${setting.name} 失败: ${e.message}")
                e.printStackTrace()
            }

            // 写入文件
            FileWriter(configFile).use { writer ->
                gson.toJson(rootObject, writer)
            }
        } catch (e: Exception) {
            println("保存设置配置失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 保存所有按键绑定的配置
     */
    fun saveKeybindsConfig() {
        try {
            // 如果配置文件不存在，则创建完整配置
            if (!configFile.exists()) {
                saveConfig()
                return
            }

            // 解析现有JSON文件
            val rootObject = if (configFile.exists()) {
                FileReader(configFile).use { reader ->
                    JsonParser.parseReader(reader).asJsonObject
                }
            } else {
                JsonObject()
            }

            // 获取或创建modules对象
            val modulesObject = rootObject.getAsJsonObject("modules") ?: JsonObject().also {
                rootObject.add("modules", it)
            }

            // 更新所有模块的按键绑定
            ModuleManager.modules.forEach { module ->
                val moduleObject = modulesObject.getAsJsonObject(encodeModuleName(module.name)) ?: JsonObject().also {
                    modulesObject.add(encodeModuleName(module.name), it)
                }
                moduleObject.addProperty("key", module.key)
            }

            // 写入文件
            FileWriter(configFile).use { writer ->
                gson.toJson(rootObject, writer)
            }
        } catch (e: Exception) {
            println("保存按键绑定配置失败: ${e.message}")
            e.printStackTrace()
        }
    }
}