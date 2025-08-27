package cn.cheneya.skypvp.api.kotlin

import net.minecraft.client.util.InputUtil

fun inputByName(name: String): InputUtil.Key {
    if (name.equals("NONE", true)) {
        return InputUtil.UNKNOWN_KEY
    }

    val formattedName = name.replace('_', '.')
    val translationKey =
        when {
            formattedName.startsWith("key.mouse.", ignoreCase = true) ||
                formattedName.startsWith("key.keyboard.", ignoreCase = true) -> formattedName.lowercase()

            formattedName.startsWith("mouse.", ignoreCase = true) ||
                formattedName.startsWith("keyboard.", ignoreCase = true) -> "key.$formattedName"

            else -> "key.keyboard.${formattedName.lowercase()}"
        }
    return InputUtil.fromTranslationKey(translationKey)
}
