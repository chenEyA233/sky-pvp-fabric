package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.kotlin.DirectionalInput
import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.api.utils.client.misc.WebSocketEvent
import cn.cheneya.skypvp.event.Event
import cn.cheneya.skypvp.event.CancellableEvent
import net.skypvpteam.skypvp.api.event
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen
import net.minecraft.client.network.CookieStorage
import net.minecraft.client.network.ServerAddress
import net.minecraft.client.network.ServerInfo
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.option.Perspective
import net.minecraft.client.session.Session
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

@event("GameEvents")
@Nameable("gameTick")
object GameTickEvent : Event()

@Nameable("gameRenderTaskQueue")
object GameRenderTaskQueueEvent : Event()

@Nameable("tickPacketProcess")
object TickPacketProcessEvent : Event()

@Nameable("key")
@WebSocketEvent
class KeyEvent(
    val key: InputUtil.Key,
    val action: Int,
) : Event()

// Input events
@Nameable("inputHandle")
object InputHandleEvent : Event()

@Nameable("movementInput")
class MovementInputEvent(
    var directionalInput: DirectionalInput,
    var jump: Boolean,
    var sneak: Boolean,
) : Event()

@Nameable("sprint")
class SprintEvent(
    val directionalInput: DirectionalInput,
    var sprint: Boolean,
    val source: Source,
) : Event() {
    enum class Source {
        INPUT,
        MOVEMENT_TICK,
        NETWORK,
    }
}

@Nameable("sneakNetwork")
class SneakNetworkEvent(
    val directionalInput: DirectionalInput,
    var sneak: Boolean,
) : Event()

@Nameable("mouseRotation")
class MouseRotationEvent(
    var cursorDeltaX: Double,
    var cursorDeltaY: Double,
) : CancellableEvent()

@Nameable("keybindChange")
@WebSocketEvent
object KeybindChangeEvent : Event()

@Nameable("keybindIsPressed")
class KeybindIsPressedEvent(
    val keyBinding: KeyBinding,
    var isPressed: Boolean,
) : Event()

@Nameable("useCooldown")
class UseCooldownEvent(
    var cooldown: Int,
) : Event()

@Nameable("cancelBlockBreaking")
class CancelBlockBreakingEvent : CancellableEvent()

@Nameable("autoJump")
class MinecraftAutoJumpEvent(
    var autoJump: Boolean,
) : Event()

/**
 * All events which are related to the minecraft client
 */

@Nameable("session")
@WebSocketEvent
class SessionEvent(
    val session: Session,
) : Event()

@Nameable("screen")
class ScreenEvent(
    val screen: Screen?,
) : CancellableEvent()

@Nameable("chatSend")
@WebSocketEvent
class ChatSendEvent(
    val message: String,
) : CancellableEvent()

@Nameable("chatReceive")
@WebSocketEvent
class ChatReceiveEvent(
    val message: String,
    val textData: Text,
    val type: ChatType,
    val applyChatDecoration: (Text) -> Text,
) : CancellableEvent() {
    enum class ChatType {
        CHAT_MESSAGE,
        DISGUISED_CHAT_MESSAGE,
        GAME_MESSAGE,
    }
}

@Nameable("serverConnect")
class ServerConnectEvent(
    val connectScreen: ConnectScreen,
    val address: ServerAddress,
    val serverInfo: ServerInfo,
    val cookieStorage: CookieStorage?,
) : CancellableEvent()

@Nameable("disconnect")
@WebSocketEvent
object DisconnectEvent : Event()

@Nameable("overlayMessage")
@WebSocketEvent
class OverlayMessageEvent(
    val text: Text,
    val tinted: Boolean,
) : Event()

@Nameable("titleReceive")
@WebSocketEvent
class TitleReceiveEvent(
    val title: Text?,
    val subtitle: Text?,
    val fadeInTicks: Int,
    val stayTicks: Int,
    val fadeOutTicks: Int,
) : CancellableEvent()

@Nameable("perspective")
class PerspectiveEvent(
    var perspective: Perspective,
) : Event()

@Nameable("itemLoreQuery")
class ItemLoreQueryEvent(
    val itemStack: ItemStack,
    val lore: ArrayList<Text>,
) : Event()