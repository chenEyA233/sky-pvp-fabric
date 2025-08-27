package cn.cheneya.skypvp.features.module.modules.combot

import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.Category
import org.lwjgl.glfw.GLFW

object ModuleKillAura : Module("KillAura", "杀戮光环", Category.COMBOT) {

    init {
        key = GLFW.GLFW_KEY_R
    }

}