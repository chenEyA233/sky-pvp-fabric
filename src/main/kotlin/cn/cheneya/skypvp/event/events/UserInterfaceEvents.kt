package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.api.utils.client.misc.WebSocketEvent
import cn.cheneya.skypvp.event.Event
import cn.cheneya.skypvp.utils.PlayerData
import cn.cheneya.skypvp.utils.PlayerInventoryData

@Nameable("fps")
@WebSocketEvent
@Suppress("unused")
class FpsChangeEvent(val fps: Int) : Event()

@Nameable("clientPlayerData")
@WebSocketEvent
@Suppress("unused")
class ClientPlayerDataEvent(val playerData: PlayerData) : Event() {
    companion object {
        fun fromPlayerStatistics(stats: PlayerData) = ClientPlayerDataEvent(stats)
    }
}

@Nameable("clientPlayerInventory")
@WebSocketEvent
@Suppress("unused")
class ClientPlayerInventoryEvent(val inventory: PlayerInventoryData) : Event() {
    companion object {
        fun fromPlayerInventory(inventory: PlayerInventoryData) = ClientPlayerInventoryEvent(inventory)
    }
}