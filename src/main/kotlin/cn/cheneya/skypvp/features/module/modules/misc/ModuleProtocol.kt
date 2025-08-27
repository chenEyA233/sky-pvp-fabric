package cn.cheneya.skypvp.features.module.modules.misc

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module

object ModuleProtocol: Module("Protocol","协议", Category.MISC) {
    val server = enumChoice("Server", server_.Heypixel)

    enum class server_{
        Heypixel,
        QuickMacro,
        Dmc,
        OMG;

    }
}