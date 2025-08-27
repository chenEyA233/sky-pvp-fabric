package cn.cheneya.skypvp.utils

import cn.cheneya.skypvp.features.module.ModuleManager
import cn.cheneya.skypvp.skypvp.logger
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

object KeyBinds {
    var hasLoggedUpdateError = false
    private val previousKeyState = mutableMapOf<Int, Boolean>()

    fun update() {
        val window = MinecraftClient.getInstance().window.handle
        ModuleManager.modules.forEach { module ->
            val key = module.key
            if (key > 0) {
                try {
                    val isPressed = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS
                    val wasPressed = previousKeyState[key] ?: false

                    if (ClientUtil.inGame && isPressed && !wasPressed) {
                        module.toggle()
                    }

                    previousKeyState[key] = isPressed
                } catch (e: Exception) {
                    if (!hasLoggedUpdateError) {
                        logger.error("Error checking key state for key $key", e)
                        hasLoggedUpdateError = true
                    }
                }
            }
        }
    }
}
