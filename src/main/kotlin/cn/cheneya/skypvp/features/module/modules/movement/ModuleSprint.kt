package cn.cheneya.skypvp.features.module.modules.movement

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.skypvp.mc
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

object ModuleSprint: Module("Sprint","自动疾跑", Category.MOVEMENT, true) {

    override fun onUpdate() {
        if (mc.player != null) {
            mc.options.sprintKey.isPressed = true
            KeyBinding.onKeyPressed(InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()))
        }
    }

    override fun onDisable() {
        super.onDisable()
        if (mc.options != null) {
            mc.options.sprintKey.isPressed = false
        }
    }
}