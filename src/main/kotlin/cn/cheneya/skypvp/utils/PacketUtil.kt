package cn.cheneya.skypvp.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.Packet
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 数据包工具类，用于拦截和处理网络数据包
 */
object PacketUtil {
    private val mc = MinecraftClient.getInstance()

    // 发送数据包监听器列表
    private val sendListeners = CopyOnWriteArrayList<(Packet<*>) -> Boolean>()

    // 接收数据包监听器列表
    private val receiveListeners = CopyOnWriteArrayList<(Packet<*>) -> Boolean>()

    /**
     * 添加发送数据包监听器
     */
    @JvmStatic
    fun addSendListener(listener: (Packet<*>) -> Boolean) {
        sendListeners.add(listener)
    }

    /**
     * 移除发送数据包监听器
     */
    @JvmStatic
    fun removeSendListener(listener: (Packet<*>) -> Boolean) {
        sendListeners.remove(listener)
    }

    /**
     * 添加接收数据包监听器
     */
    @JvmStatic
    fun addReceiveListener(listener: (Packet<*>) -> Boolean) {
        receiveListeners.add(listener)
    }

    /**
     * 移除接收数据包监听器
     */
    @JvmStatic
    fun removeReceiveListener(listener: (Packet<*>) -> Boolean) {
        receiveListeners.remove(listener)
    }

    /**
     * 处理发送数据包
     * 返回true表示允许发送，false表示拦截
     */
    @JvmStatic
    fun handleSendPacket(packet: Packet<*>): Boolean {
        for (listener in sendListeners) {
            if (!listener(packet)) {
                return false
            }
        }
        return true
    }

    /**
     * 处理接收数据包
     * 返回true表示允许处理，false表示拦截
     */
    @JvmStatic
    fun handleReceivePacket(packet: Packet<*>): Boolean {
        for (listener in receiveListeners) {
            if (!listener(packet)) {
                return false
            }
        }
        return true
    }

    /**
     * 发送数据包
     */
    @JvmStatic
    fun sendPacket(packet: Packet<*>) {
        mc.networkHandler?.sendPacket(packet)
    }
    
    /**
     * 判断数据包是否是必要的（不能被拦截的）
     */
    @JvmStatic
    fun isEssential(packet: Packet<*>): Boolean {
        // 这里可以添加一些必要的数据包类型
        // 例如，保持连接的数据包、认证数据包等
        return false
    }
}
