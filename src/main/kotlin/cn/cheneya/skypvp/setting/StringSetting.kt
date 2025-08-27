package cn.cheneya.skypvp.setting

import cn.cheneya.skypvp.features.module.Module

class StringSetting(
    name: String,
    module: Module,
    value: String,
    description: String = "",
    onChange: (String) -> Unit = {}
) : Setting<String>(name, module, value, description) {
    init {
        if (onChange != {}) {
            addListener(onChange)
        }
    }
}
