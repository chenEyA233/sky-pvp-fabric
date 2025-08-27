package cn.cheneya.skypvp.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object ChatUtil {
    private val mc = MinecraftClient.getInstance()

    fun info(message: String) {
        mc.inGameHud?.chatHud?.addMessage(Text.literal("[§cS§r] §f$message"))
    }

    fun success(message: String) {
        mc.inGameHud?.chatHud?.addMessage(Text.literal("[§cS§r] §a$message"))
    }

    fun warn(message: String) {
        mc.inGameHud?.chatHud?.addMessage(Text.literal("[§cS§r] §e$message"))
    }

    fun error(message: String) {
        mc.inGameHud?.chatHud?.addMessage(Text.literal("[§cS§r] §c$message"))
    }

    fun debug(message: String) {
        mc.inGameHud?.chatHud?.addMessage(Text.literal("[§cS§r] §f$message"))
    }

    fun silence(message: String){
        mc.inGameHud?.chatHud?.addMessage(Text.literal("§dSilenceFix >> §f$message"))
    }
}
