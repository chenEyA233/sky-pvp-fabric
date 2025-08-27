package cn.cheneya.skypvp.features.module.modules.player

import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.features.module.Category
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.block.Blocks

object ModuleAutoTools : Module("AutoTools", "自动工具", Category.PLAYER) {
    // 模式设置
    private enum class Mode {
        SIMPLE, // 只切换不返回
        SMART   // 记录并返回原点
    }

    // 设置选项
    private val mode = enumChoice("Mode", Mode.SMART)
    private val checkSword = boolean("Check Sword", true)
    private val silent = boolean("Silent", true).apply {
        mode.value == Mode.SMART.toString()
    }

    // 状态变量
    private var originSlot = -1
    private var originStack = ItemStack.EMPTY
    private var isMining = false
    private var lastMiningState = false
    private var lastTargetPos: net.minecraft.util.math.BlockPos? = null

    private val mc = MinecraftClient.getInstance()

    override fun onUpdate() {
        if (mc.player == null || mc.world == null) return

        // 检查玩家是否在挖掘方块
        val currentMiningState = mc.interactionManager?.isBreakingBlock == true
        
        // 获取当前目标方块位置
        val hitResult = mc.crosshairTarget
        val currentTargetPos = if (hitResult is net.minecraft.util.hit.BlockHitResult) hitResult.blockPos else null
        
        // 在以下情况处理工具切换：
        // 1. 挖掘状态变化
        // 2. 正在挖掘且目标方块变化
        if (currentMiningState != lastMiningState || 
            (currentMiningState && currentTargetPos != null && currentTargetPos != lastTargetPos)) {
            
            lastMiningState = currentMiningState
            lastTargetPos = currentTargetPos
            
            if (currentMiningState) {
                // 开始挖掘或切换目标方块
                handleMiningStart()
            } else {
                // 结束挖掘
                handleMiningEnd()
            }
        }
    }

    private fun handleMiningStart() {
        // 检查是否需要跳过剑
        if (checkSword.value && mc.player?.mainHandStack?.item is SwordItem) {
            return
        }

        // 获取标方块
        val hitResult = mc.crosshairTarget
        if (hitResult !is net.minecraft.util.hit.BlockHitResult) return

        val targetPos = hitResult.blockPos
        val blockState = mc.world?.getBlockState(targetPos) ?: return

        // 查找最佳工具
        val bestToolSlot = findBestToolSlot(blockState)
        if (bestToolSlot == -1) return // 没有合适工具

        // 当前已经是最佳工具，不需要切换
        if (bestToolSlot == mc.player?.inventory?.selectedSlot) return

        when (Mode.valueOf(mode.value)) {
            Mode.SIMPLE -> {
                // 简单模式：直接切换工具
                switchToSlot(bestToolSlot)
            }
            Mode.SMART -> {
                // 智能模式：记录原点并切换
                if (originSlot == -1) {
                    originSlot = mc.player?.inventory?.selectedSlot ?: -1
                    originStack = mc.player?.inventory?.getStack(originSlot) ?: ItemStack.EMPTY
                }
                switchToSlot(bestToolSlot)
            }
        }
    }

    private fun handleMiningEnd() {
        // 只有在SMART模式且有记录原点时才返回
        if (Mode.valueOf(mode.value) == Mode.SMART && originSlot != -1) {
            returnToOrigin()
        }
    }

    // 切换到指定槽位
    private fun switchToSlot(slot: Int) {
        if (mc.player?.inventory?.selectedSlot == slot) return

        mc.player?.inventory?.selectedSlot = slot
        if (!silent.value || mode.value != Mode.SMART.toString()) {
            // 非静默模式或非SMART模式时播放动画
            mc.player?.swingHand(net.minecraft.util.Hand.MAIN_HAND)
        }
    }

    // 返回原点
    private fun returnToOrigin() {
        if (originSlot != -1) {
            switchToSlot(originSlot)
            originSlot = -1
            originStack = ItemStack.EMPTY
        }
    }

    // 查找最佳工具槽位
    private fun findBestToolSlot(blockState: BlockState): Int {
        var bestSlot = -1
        var bestSpeed = 1.0f

        // 遍历快捷栏
        for (i in 0..8) {
            val stack = mc.player?.inventory?.getStack(i) ?: continue
            if (stack.isEmpty) continue

            // 跳过剑（除非是蜘蛛网）
            if (stack.item is SwordItem && blockState.block != Blocks.COBWEB) continue

            // 计算工具对方块的挖掘速度
            var speed = stack.getMiningSpeedMultiplier(blockState)

            // 直接使用物品的挖掘速度，不需要单独检查效率附魔
            // 因为getMiningSpeedMultiplier已经考虑了所有附魔的效果

            // 更新最佳工具
            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = i
            }
        }

        return if (bestSpeed > 1.0f) bestSlot else -1
    }

    override fun onDisable() {
        // 模块禁用时返回原点
        if (originSlot != -1) {
            returnToOrigin()
        }
    }
}
