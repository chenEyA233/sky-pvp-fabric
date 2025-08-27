package cn.cheneya.skypvp.api.kotlin

import cn.cheneya.skypvp.event.*
import cn.cheneya.skypvp.event.events.*
import cn.cheneya.skypvp.event.handler
import cn.cheneya.skypvp.skypvp.mc
import com.google.common.collect.Queues
import net.minecraft.client.option.Perspective
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket
import net.minecraft.network.packet.c2s.play.*
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 数据包队列管理器，用于暂停和恢复数据包发送
 */
object PacketQueueManager {

    val packetQueue: ConcurrentLinkedQueue<PacketSnapshot> = Queues.newConcurrentLinkedQueue()

    val positions: List<Vec3d>
        get() = packetQueue
            .map { snapshot -> snapshot.packet }
            .filterIsInstance<PlayerMoveC2SPacket>()
            .filter { it.hasPosition() }
            .map { it.getPosition() }

    val isLagging: Boolean
        get() = packetQueue.isNotEmpty()

    fun enableOutgoingQueue() {
        // 启用出站数据包队列
    }

    fun disableAndFlushOutgoing() {
        flush { snapshot -> snapshot.origin == TransferOrigin.OUTGOING }
    }

    fun flushOutgoing() {
        flush { snapshot -> snapshot.origin == TransferOrigin.OUTGOING }
    }

    fun flush(flushWhen: (PacketSnapshot) -> Boolean) {
        packetQueue.removeIf { snapshot ->
            if (flushWhen(snapshot)) {
                flushSnapshot(snapshot)
                true
            } else {
                false
            }
        }
    }

    fun flush(count: Int) {
        var counter = 0
        val iterator = packetQueue.iterator()

        while (iterator.hasNext()) {
            val snapshot = iterator.next()
            val packet = snapshot.packet

            if (packet is PlayerMoveC2SPacket && packet.hasPosition()) {
                counter++
            }

            flushSnapshot(snapshot)
            iterator.remove()

            if (counter >= count) {
                break
            }
        }
    }

    private fun PlayerMoveC2SPacket.hasPosition(): Boolean {
        return try {
            val field = this::class.java.getDeclaredField("changePosition")
            field.isAccessible = true
            field.getBoolean(this)
        } catch (e: Exception) {
            false
        }
    }

    private fun PlayerMoveC2SPacket.getPosition(): Vec3d {
        return try {
            val xField = this::class.java.getDeclaredField("x")
            val yField = this::class.java.getDeclaredField("y")
            val zField = this::class.java.getDeclaredField("z")
            
            xField.isAccessible = true
            yField.isAccessible = true
            zField.isAccessible = true
            
            Vec3d(xField.getDouble(this), yField.getDouble(this), zField.getDouble(this))
        } catch (e: Exception) {
            Vec3d.ZERO
        }
    }

    fun cancel() {
        packetQueue.clear()
    }

    private fun flushSnapshot(snapshot: PacketSnapshot) {
        when (snapshot.origin) {
            TransferOrigin.OUTGOING -> sendPacket(snapshot.packet)
            TransferOrigin.INCOMING -> {} // 不处理入站包
        }
    }

    private fun sendPacket(packet: Packet<*>) {
        // 实际发送数据包的实现
        mc.networkHandler?.sendPacket(packet)
    }
}

data class PacketSnapshot(
    val packet: Packet<*>,
    val origin: TransferOrigin,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransferOrigin {
    INCOMING,
    OUTGOING
}

enum class Action {
    QUEUE,
    PASS,
    FLUSH
}
