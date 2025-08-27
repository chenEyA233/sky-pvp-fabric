/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233, shanyang
 */
package cn.cheneya.skypvp

import cn.cheneya.skypvp.api.utils.LoggerName
import cn.cheneya.skypvp.config.Config
import cn.cheneya.skypvp.features.command.CommandManager
import cn.cheneya.skypvp.features.command.CommandRegistry
import cn.cheneya.skypvp.features.module.ModuleManager
import cn.cheneya.skypvp.features.module.modules.player.ModuleFastPlace
import cn.cheneya.skypvp.render.font.Fonts
import cn.cheneya.skypvp.render.gui.GuiManager
import cn.cheneya.skypvp.utils.*
import net.skypvpteam.skypvp.api.ApiInjector
import net.skypvpteam.skypvp.api.skypvp
import net.skypvpteam.skypvp.xyz.ClassPathScanner
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoInit
val logger = LogManager.getLogger(LoggerName.logger)!!

fun scanAndInitClasses(packageName: String = "cn.shanyang.skypvp") {
    try {
        val classLoader = Thread.currentThread().contextClassLoader
        val classes = ClassPathScanner.getClasses(packageName, classLoader)

        classes.forEach { clazz ->
            if (clazz.findAnnotation<AutoInit>() != null) {
                try {
                    clazz.createInstance()
                    logger.info("Auto-initialized: ${clazz.simpleName}")
                    logger.info(LoggerName.Heypixel)
                } catch (e: Exception) {
                    logger.error("Failed to auto-initialize ${clazz.simpleName}", e)
                }
            }
        }
    } catch (e: Exception) {
        logger.error("Error scanning for auto-init classes", e)
    }
}

@skypvp
object skypvp : ClientModInitializer {
    val mc = MinecraftClient.getInstance()!!
    val logger = LogManager.getLogger(LoggerName.logger)!!
    const val CLIENT_NAME2 = "SKY PVP"
    const val CLIENT_AUTHOR = "chenEyA233/shanyang"
    const val CLIENT_VERSION = "1.0"
    const val CLIENT = "Client"
    const val V = "v"
    const val text = "|"
    const val serverAddress = "127.0.0.1"
    const val serverPort = 14520
    @JvmField
    var Verification: Boolean = true

    /**
     * 从资源包中提取文件到指定目录
     * 
     * @param resourcePath 资源路径
     * @param outputPath 输出路径
     * @return 是否成功提取
     */
    private fun extractResourceFile(resourcePath: String, outputPath: String): Boolean {
        return try {
            val resource = skypvp::class.java.classLoader.getResourceAsStream(resourcePath)
            if (resource != null) {
                val outputFile = File(outputPath)
                // 确保父目录存在
                outputFile.parentFile?.mkdirs()
                
                // 如果文件已存在，先删除
                if (outputFile.exists()) {
                    outputFile.delete()
                }
                
                // 复制文件
                Files.copy(resource, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                logger.info("Successfully extracted resource: $resourcePath to $outputPath")
                true
            } else {
                logger.error("Resource not found: $resourcePath")
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to extract resource: $resourcePath", e)
            false
        }
    }

    override fun onInitializeClient() {
        logger.info("Loading skypvp client...  " + LoggerName.HeypixelSong)

        // 提取背景视频文件
        val gameDir = FabricLoader.getInstance().gameDir.toFile()
        val resourcePath = "assets/skypvp/textures/client/background/background.mp4"
        val outputPath = File(gameDir, "background.mp4").absolutePath
        
        if (extractResourceFile(resourcePath, outputPath)) {
            logger.info("Background video extracted successfully to: $outputPath")
        } else {
            logger.warn("Failed to extract background video")
        }

        scanAndInitClasses()

        logger.info("Initializing API system...")
        try {
            val testApi = ApiInjector.getApi<Any>()
            if (testApi != null) {
                logger.info("API system initialized successfully")
                logger.info("你可以用注释ClassInject来注入你的新客户端主类，用法：@ClassInject(className=(如dev.xinxin.genshin))")
            } else {
                logger.warn("No external APIs found")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize API system", e)
        }

        ModuleManager.init()
        logger.info("Module system initialized...  " + LoggerName.GenShenImpact)
        
        // 初始化配置系统
        Config.init()
        logger.info("Config system initialized...")
        net.skypvpteam.skypvp.skypvp.init()
        ModuleFastPlace.init()
        StartUtil.init()
        GuiManager.init()
        CommandManager.init()
        CommandRegistry.init()
        Fonts.init()
        // 注册客户端关闭事件，保存配置
        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            logger.info("Client stopping, saving all configurations...")
            try {
                cn.cheneya.skypvp.config.Config.saveAll()
                logger.info("All configurations saved successfully")
            } catch (e: Exception) {
                logger.error("Failed to save configurations", e)
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            ModuleManager.getEnabledModules().forEach { module ->
                try {
                    module.onUpdate()
                } catch (e: Exception) {
                    logger.error("Error while updating module ${module.name}", e)
                }
            }

            try {
                KeyBinds.update()
            } catch (e: Exception) {
                if (!KeyBinds.hasLoggedUpdateError) {
                    logger.error("Error updating key bindings", e)
                    KeyBinds.hasLoggedUpdateError = true
                }
            }
        }
    }
}
