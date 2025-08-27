package cn.cheneya.skypvp.features.module

import cn.cheneya.skypvp.api.utils.minecraft.GetLang

enum class Category(val ENName: String,val CNName: String) {
    COMBOT("Combot","战斗"),
    PLAYER("Player","玩家"), 
    MOVEMENT("Movement","移动"),
    RENDER("Render","渲染"),
    WORLD("World","世界"),
    MISC("Misc","其他");

    /**
     * 获取当前语言的分类名称
     */
    fun getName(): String {
        return if (GetLang.isChinese()) CNName else ENName
    }
}