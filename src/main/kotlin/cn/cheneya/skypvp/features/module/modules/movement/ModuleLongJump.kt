package cn.cheneya.skypvp.features.module.modules.movement

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import cn.cheneya.skypvp.utils.PacketUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.item.Items
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW
import java.util.*

object ModuleLongJump : Module("LongJump", "跳远冠军", Category.MOVEMENT) {
    private val mc: MinecraftClient = MinecraftClient.getInstance()
    private var tickCallback: ClientTickEvents.EndTick? = null
    private var sendPacketListener: ((Packet<*>) -> Boolean)? = null
    private var receivePacketListener: ((Packet<*>) -> Boolean)? = null

    init {
        key = GLFW.GLFW_KEY_J
    }

    // 存储数据包的队列
    private val packetQueue: Queue<PlayerMoveC2SPacket> = LinkedList()

    // 标记是否检测到烈焰弹右击地面
    private var blazeRodUsed = false

    // 上次显示信息的时间
    private var lastInfoTime = 0L

    // 信息显示间隔（毫秒）
    private val INFO_INTERVAL = 1000L


    override fun onEnable() {
        // 清空数据包队列
        packetQueue.clear()
        blazeRodUsed = false

        // 注册tick事件
        tickCallback = ClientTickEvents.EndTick { _ ->
            onTick()
        }.also {
            ClientTickEvents.END_CLIENT_TICK.register(it)
        }

        // 注册发送数据包监听器
        sendPacketListener = { packet: Packet<*> ->
            handleSendPacket(packet)
        }.also {
            PacketUtil.addSendListener(it)
        }

        // 注册接收数据包监听器
        receivePacketListener = { packet: Packet<*> ->
            handleReceivePacket(packet)
        }.also {
            PacketUtil.addReceiveListener(it)
        }

    }

    override fun onDisable() {
        // 发送所有存储的数据包
        sendAllPackets()

        // 清理资源
        blazeRodUsed = false
        packetQueue.clear()

        // 注销tick事件监听器
        tickCallback?.let {
            ClientTickEvents.END_CLIENT_TICK.register { _ -> }
            tickCallback = null
        }

        // 注销数据包监听器
        sendPacketListener?.let {
            PacketUtil.removeSendListener(it)
            sendPacketListener = null
        }

        receivePacketListener?.let {
            PacketUtil.removeReceiveListener(it)
            receivePacketListener = null
        }

    }

    private fun handleSendPacket(packet: Packet<*>): Boolean {
        // 处理发送的数据包
        if (packet is PlayerMoveC2SPacket && enabled && blazeRodUsed) {
            // 如果模块启用且检测到烈焰弹使用，存储移动数据包
            packetQueue.offer(packet)
            return false // 拦截数据包
        } else if (packet is PlayerInteractBlockC2SPacket && enabled) {
            val player = mc.player ?: return true
            val mainHandItem = player.mainHandStack.item

            // 检查是否使用烈焰弹对地面右击
            if (mainHandItem == Items.BLAZE_ROD) {
                val hitResult = mc.crosshairTarget
                if (hitResult is BlockHitResult && hitResult.side == Direction.UP) {
                    // 检测到烈焰弹对地面右击
                    blazeRodUsed = true
                }
            }
        }

        return true // 允许其他数据包通过
    }

    private fun handleReceivePacket(packet: Packet<*>): Boolean {
        // 处理接收的数据包
        if (packet is EntityVelocityUpdateS2CPacket && enabled) {
            val player = mc.player ?: return true

            // 检查数据包是否针对玩家
            if (packet.entityId == player.getId()) {
                // 如果收到击退数据包且模块启用，取消击退
                return !cancelKnockback()
            }
        }

        return true // 允许其他数据包通过
    }

    private fun onTick() {
        val player = mc.player ?: return

        // 检测Y键按下
        if (GLFW.glfwGetKey(mc.window.handle, GLFW.GLFW_KEY_Y) == GLFW.GLFW_PRESS) {
            // 释放一个数据包
            releaseOnePacket()
        }

        // 定期显示当前存储的数据包数量
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInfoTime > INFO_INTERVAL) {
            if (blazeRodUsed) {
            }
            lastInfoTime = currentTime
        }
    }

    // 释放一个数据包
    private fun releaseOnePacket() {
        if (packetQueue.isEmpty()) {
            return
        }

        val packet = packetQueue.poll()
        PacketUtil.sendPacket(packet)
    }

    // 发送所有存储的数据包
    private fun sendAllPackets() {
        if (packetQueue.isEmpty()) return

        val count = packetQueue.size

        // 发送所有数据包
        while (packetQueue.isNotEmpty()) {
            PacketUtil.sendPacket(packetQueue.poll())
        }

    }

    // 取消击退
    fun cancelKnockback(): Boolean {
        if (!enabled) return false

        // 如果模块启用且检测到烈焰弹使用，取消击退
        if (blazeRodUsed) {
            return true
        }

        return false
    }
}
