package cn.cheneya.skypvp.features.module.modules.combot

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.skypvp.mc
import cn.cheneya.skypvp.utils.MathUtils
import cn.cheneya.skypvp.utils.Timer
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult

object ModuleAutoClick : Module("AutoClicker", "自动点击", Category.COMBOT) {
    private val maxCps = number("Max CPS", 14.0, 1.0, 20.0, 1.0)
    private val minCps = number("Min CPS", 10.0, 1.0, 20.0, 1.0)
    private val rightClick = boolean("Right Click", false)
    private val leftClick = boolean("Left Click", true)
    private val hitSelect = boolean("Hit Select", false)
    private val clickStopWatch = Timer()
    private var ticksDown = 0
    private var attackTicks = 0
    private var nextSwing = 0L


     override fun onUpdate() {
        if (mc.player == null || mc.world == null) return
        mc.attackCooldown = 0
        attackTicks++

        if (clickStopWatch.hasReached(nextSwing) &&
            (!hitSelect.value || attackTicks >= 10 || (mc.player?.hurtTime ?: 0) > 0 && clickStopWatch.hasReached(
                nextSwing
            )) &&
            mc.currentScreen == null
        ) {

            val clicks = (Math.round(
                MathUtils.getRandomNumber(
                minCps.value.toInt(),
                maxCps.value.toInt()
            )) * 1.5).toLong()

            if (mc.options.attackKey.isPressed) {
                ticksDown++
            } else {
                ticksDown = 0
            }

            nextSwing = 1000 / clicks

            if (rightClick.value && mc.options.useKey.isPressed) {
                sendClick(1, true)

                if (Math.random() > 0.9) {
                    sendClick(1, true)
                }
            }

            if (leftClick.value && ticksDown > 1 &&
                (Math.sin(nextSwing.toDouble()) + 1 > Math.random() ||
                 Math.random() > 0.25 ||
                 clickStopWatch.hasReached(4 * 50)) &&
                (mc.crosshairTarget == null ||
                 mc.crosshairTarget?.type != HitResult.Type.BLOCK ||
                 mc.world?.getBlockState((mc.crosshairTarget as BlockHitResult).blockPos)?.isAir == true ||
                 mc.crosshairTarget?.type == HitResult.Type.MISS)) {
                sendClick(0, true)
            }

            clickStopWatch.reset()
        }
    }

    private fun sendClick(button: Int, state: Boolean) {
        val keyBind = if (button == 0) mc.options.attackKey else mc.options.useKey
        keyBind.isPressed = state

        if (state) {
            KeyBinding.onKeyPressed(InputUtil.fromTranslationKey(keyBind.getBoundKeyTranslationKey()))
        }
    }

}
