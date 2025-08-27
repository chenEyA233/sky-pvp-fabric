package cn.cheneya.skypvp.render.gui

import cn.cheneya.skypvp.skypvp.logger
import java.io.File
import java.net.URL
import java.util.*

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Gui

object GuiManager {
    private val registeredGuis = mutableListOf<Any>()

    fun autoRegister() {
        val packageName = "cn.cheneya.skypvp.render.gui.guis"
        try {
            val classLoader = Thread.currentThread().contextClassLoader
            val path = packageName.replace('.', '/')
            val resources = classLoader.getResources(path)

            while (resources.hasMoreElements()) {
                val resource = resources.nextElement()
                if (resource.protocol == "file") {
                    val directory = File(resource.file)
                    if (directory.exists()) {
                        directory.listFiles()?.forEach { file ->
                            if (file.isFile && file.name.endsWith(".class")) {
                                val className = "$packageName.${file.name.substring(0, file.name.length - 6)}"
                                try {
                                    val clazz = Class.forName(className)
                                    if (clazz.getAnnotation(Gui::class.java) != null) {
                                        registeredGuis.add(clazz.getDeclaredConstructor().newInstance())
                                    }
                                } catch (e: Exception) {
                                    logger.warn("Failed to load class $className: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to scan GUI classes: ${e.message}")
        }
    }

    fun getRegisteredGuis(): List<Any> = registeredGuis.toList()

    fun init() {
        autoRegister()
        logger.info("Load ${registeredGuis.size} gui!")
    }
}