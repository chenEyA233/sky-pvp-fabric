package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.skypvp.mc


object ModuleESP : Module("ESP", "透视", Category.RENDER) {
    val mode = enumChoice("Mode", _mode.Glow)

    enum class _mode {
        Glow,
        _2D,
        box
    }

}
