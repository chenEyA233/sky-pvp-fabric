package cn.cheneya.skypvp.event

import cn.cheneya.skypvp.api.kotlin.sortedInsert
import cn.cheneya.skypvp.event.events.*
import cn.cheneya.skypvp.skypvp.logger
import cn.cheneya.skypvp.utils.ClientUtil.isDestructed
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.forEach
import kotlin.reflect.KClass

val ALL_EVENT_CLASSES = arrayOf<KClass<out Event>>(
    MotionEvent::class,
    GameTickEvent::class,
    GameRenderTaskQueueEvent::class,
    TickPacketProcessEvent::class,
    BlockChangeEvent::class,
    ChunkLoadEvent::class,
    ChunkDeltaUpdateEvent::class,
    ChunkUnloadEvent::class,
    DisconnectEvent::class,
    GameRenderEvent::class,
    WorldRenderEvent::class,
    OverlayRenderEvent::class,
    ScreenRenderEvent::class,
    WindowResizeEvent::class,
    FrameBufferResizeEvent::class,
    MouseButtonEvent::class,
    MouseScrollEvent::class,
    MouseCursorEvent::class,
    KeyboardKeyEvent::class,
    KeyboardCharEvent::class,
    InputHandleEvent::class,
    MovementInputEvent::class,
    SprintEvent::class,
    SneakNetworkEvent::class,
    KeyEvent::class,
    MouseRotationEvent::class,
    KeybindChangeEvent::class,
    KeybindIsPressedEvent::class,
    SessionEvent::class,
    ScreenEvent::class,
    ChatSendEvent::class,
    ChatReceiveEvent::class,
    UseCooldownEvent::class,
    BlockShapeEvent::class,
    BlockBreakingProgressEvent::class,
    BlockVelocityMultiplierEvent::class,
    BlockSlipperinessMultiplierEvent::class,
    EntityMarginEvent::class,
    HealthUpdateEvent::class,
    DeathEvent::class,
    PlayerTickEvent::class,
    PlayerPostTickEvent::class,
    PlayerMovementTickEvent::class,
    PlayerNetworkMovementTickEvent::class,
    PlayerPushOutEvent::class,
    PlayerMoveEvent::class,
    PlayerJumpEvent::class,
    PlayerAfterJumpEvent::class,
    PlayerUseMultiplier::class,
    PlayerInteractItemEvent::class,
    PlayerInteractedItemEvent::class,
    ClientPlayerInventoryEvent::class,
    PlayerVelocityStrafe::class,
    PlayerStrideEvent::class,
    PlayerSafeWalkEvent::class,
    CancelBlockBreakingEvent::class,
    PlayerStepEvent::class,
    PlayerStepSuccessEvent::class,
    FluidPushEvent::class,
    WorldChangeEvent::class,
    FpsChangeEvent::class,
    ClientPlayerDataEvent::class,
    ServerConnectEvent::class,
    DrawOutlinesEvent::class,
    OverlayMessageEvent::class,
    TagEntityEvent::class,
    MouseScrollInHotbarEvent::class,
    PlayerFluidCollisionCheckEvent::class,
    PlayerSneakMultiplier::class,
    PerspectiveEvent::class,
    ItemLoreQueryEvent::class,
    PlayerEquipmentChangeEvent::class,
    BlockAttackEvent::class,
    MinecraftAutoJumpEvent::class,
    WorldEntityRemoveEvent::class,
)

object EventManager {

    private val registry: Map<Class<out Event>, CopyOnWriteArrayList<EventHook<in Event>>> =
        ALL_EVENT_CLASSES.associate { Pair(it.java, CopyOnWriteArrayList()) }

    init {
        SequenceManager
    }

    /**
     * Used by handler methods
     */
    fun <T : Event> registerEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>): EventHook<T> {
        val handlers = registry[eventClass]
            ?: error("The event '${eventClass.name}' is not registered in Events.kt::ALL_EVENT_CLASSES.")

        @Suppress("UNCHECKED_CAST")
        val hook = eventHook as EventHook<in Event>

        if (!handlers.contains(hook)) {
            // `handlers` is sorted descending by EventHook.priority
            handlers.sortedInsert(hook) { -it.priority }
        }

        return eventHook
    }

    /**
     * Unregisters a handler.
     */
    fun <T : Event> unregisterEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>) {
        registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    fun unregisterEventHandler(eventListener: EventListener) {
        registry.values.forEach {
            it.removeIf { it.handlerClass == eventListener }
        }
    }

    fun unregisterAll() {
        registry.values.forEach {
            it.clear()
        }
    }

    /**
     * Call event to listeners
     *
     * @param event to call
     */
    fun <T : Event> callEvent(event: T): T {
        if (isDestructed) {
            return event
        }

        val target = registry[event.javaClass] ?: return event

        for (eventHook in target) {
            if (!eventHook.handlerClass.running) {
                continue
            }

            runCatching {
                eventHook.handler(event)
            }.onFailure {
                logger.error("Exception while executing handler.", it)
            }
        }

        return event
    }
}
