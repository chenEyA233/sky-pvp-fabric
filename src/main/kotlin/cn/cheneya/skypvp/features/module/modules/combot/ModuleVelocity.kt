package cn.cheneya.skypvp.features.module.modules.combot

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

object ModuleVelocity : Module("Velocity", "反击退", Category.COMBOT) {
    private val mc = MinecraftClient.getInstance()
    private var mode = enumChoice("Mode", AntiKB_Mode.Jump_Reset)
    
    enum class AntiKB_Mode {
        Jump_Reset,
        GrimFull
    }

    private fun sendJump(state: Boolean) {
        mc.options.jumpKey.isPressed = state
        if (state) {
            KeyBinding.onKeyPressed(InputUtil.fromTranslationKey(mc.options.jumpKey.boundKeyTranslationKey))
        }
    }

    override fun onEnable() {
        if (mc.player == null) return


        when (mode.value) {
            AntiKB_Mode.Jump_Reset.toString() -> {
                if (mc.player!!.isOnGround && mc.player!!.hurtTime > 0) {
                    mc.player!!.setSprinting(false)
                    sendJump(true)
                }
            }
            
            AntiKB_Mode.GrimFull.toString() -> {
            }
        }
    }
}
