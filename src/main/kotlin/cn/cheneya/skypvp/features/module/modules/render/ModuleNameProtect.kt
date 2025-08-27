package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.api.utils.NameProtectMappings
import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.modules.misc.ModuleIRC
import cn.cheneya.skypvp.skypvp.mc
import cn.cheneya.skypvp.render.gui.guis.LoginGui
import net.minecraft.client.network.PlayerListEntry
import java.util.HashMap

object ModuleNameProtect: Module("NameProtect","名字保护", Category.RENDER) {

    private val baseHideName = "§dHide§f§r" // 基础隐藏名称，不会被修改

    private var mappings: NameProtectMappings? = null
    private var lastUpdate = 0L
    private var _moduleActive = true

    /**
     * 检查名称保护是否实际运行中
     */
    @JvmName("isNameProtectActive") // 使用不同的JVM方法名避免冲突
    fun isActive(): Boolean {
        return enabled
    }

    /**
     * 更新名称保护映射
     */
    private fun updateMappings() {
        // 如果玩家为null，不进行更新
        if (mc.player == null) return

        // 限制更新频率，避免性能问题
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate < 1000) return
        lastUpdate = currentTime

        // 延迟初始化mappings
        if (mappings == null) {
            mappings = NameProtectMappings()
        }

        try {
            // 获取当前玩家名称，安全处理可能的空指针
            val playerName = mc.player?.gameProfile?.name ?: return

            // 设置替换名称 - 修复叠加问题
            val hidename = if (ModuleIRC.enabled) {
                "§dHide (§1${LoginGui.currentUsername}§f | §6${LoginGui.currentUserGroup}§f§r)"
            } else {
                baseHideName
            }

            // 获取其他玩家列表
            val otherPlayers = ArrayList<String>()
            mc.networkHandler?.playerList?.forEach { entry: PlayerListEntry? ->
                entry?.profile?.name?.let { name ->
                    if (name != playerName) {
                        otherPlayers.add(name)
                    }
                }
            }

            // 更新映射，传递protectOtherPlayers设置
            mappings?.update(playerName, hidename, HashMap(), otherPlayers, false)

            // 标记为运行中
            _moduleActive = true
        } catch (e: Exception) {
            // 出现异常时，标记为非运行状态
            _moduleActive = true
        }
    }

    /**
     * 获取保护后的名称
     */
    fun replace(string: String): String {
        if (string.isEmpty()) {
            return string
        }

        var result = string
        result = result.replace("[VetaIRC Public qq879261871]", "§f[§2Veta§f] §d")

        // 如果名称保护未启用或玩家为null，只进行IRC前缀简化
        if (!enabled || mc.player == null) {
            return result
        }

        // 更新映射
        updateMappings()

        // 替换名称
        return mappings?.replace(result) ?: result
    }

    /**
     * 名称保护OrderedText包装类
     */
    class NameProtectOrderedText(private val original: net.minecraft.text.OrderedText) : net.minecraft.text.OrderedText {
        override fun accept(visitor: net.minecraft.text.CharacterVisitor): Boolean {
            // 将OrderedText转换为字符串
            val builder = StringBuilder()
            original.accept { _, style, codePoint ->
                builder.appendCodePoint(codePoint)
                true
            }

            // 应用名称保护
            val protected_text = replace(builder.toString())

            // 如果文本没有变化，直接使用原始OrderedText
            if (protected_text == builder.toString()) {
                return original.accept(visitor)
            }

            // 否则创建新的OrderedText
            return net.minecraft.text.Text.of(protected_text).asOrderedText().accept(visitor)
        }
    }
}
