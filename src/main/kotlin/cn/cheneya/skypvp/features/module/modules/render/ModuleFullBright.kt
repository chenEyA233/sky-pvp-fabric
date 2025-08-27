package cn.cheneya.skypvp.features.module.modules.render

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

object ModuleFullBright : Module("FullBright", "夜视", Category.RENDER) {
    private val mc: MinecraftClient = MinecraftClient.getInstance()
    private var originalGamma: Double = 1.0
    private var isRegistered = false

    // 定义模式枚举
    enum class Mode {
        NIGHT_VISION,  // 夜视效果
        GAMMA          // 修改Gamma值
    }

    // 使用Module基类的enumChoice方法创建设置
    private val mode = enumChoice("Mode", Mode.NIGHT_VISION)

    // Gamma值设置 (0.0-1.0, 默认值为1.0)
    private val gammaValue = number("Gamma", 1.0, 0.0, 1.0, 0.1)


    override fun onEnable() {
        // 保存原始Gamma值
        originalGamma = mc.options.gamma.value
    }

    override fun onUpdate() {
        if (!enabled) return

        val player = mc.player ?: return

        when (Mode.valueOf(mode.value)) {
            Mode.NIGHT_VISION -> {
                // 添加夜视效果 (255级，无限时长，隐藏粒子)
                player.addStatusEffect(
                    StatusEffectInstance(
                        StatusEffects.NIGHT_VISION,
                        Int.MAX_VALUE,  // 无限时长
                        0,              // 效果等级 (0 = 级别 I)
                        false,          // 不显示环境粒子
                        false,          // 不显示图标
                        false           // 不显示粒子
                    )
                )

                // 确保Gamma值恢复正常
                if (mc.options.gamma.value != originalGamma) {
                    mc.options.gamma.value = originalGamma
                }
            }
            Mode.GAMMA -> {
                // 移除可能存在的夜视效果
                player.removeStatusEffect(StatusEffects.NIGHT_VISION)

                // 设置Gamma值 (确保在有效范围内)
                val safeGammaValue = gammaValue.value.coerceIn(0.0, 1.0)
                mc.options.gamma.value = safeGammaValue
            }
        }
    }

    override fun onDisable() {
        // 移除夜视效果
        mc.player?.removeStatusEffect(StatusEffects.NIGHT_VISION)

        // 恢复原始Gamma值
        mc.options.gamma.value = originalGamma
    }
}
