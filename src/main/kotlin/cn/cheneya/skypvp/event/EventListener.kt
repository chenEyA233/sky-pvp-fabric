package cn.cheneya.skypvp.event

import cn.cheneya.skypvp.event.events.GameTickEvent
import cn.cheneya.skypvp.utils.ClientUtil.isDestructed

typealias Handler<T> = (T) -> Unit

class EventHook<T : Event>(
    val handlerClass: EventListener,
    val handler: Handler<T>,
    val priority: Short = 0
)

interface EventListener {

    /**
     * Returns whether the listenable is running or not, this is based on the parent listenable
     * and if no parent is present, it will return the opposite of [isDestructed].
     *
     * When destructed, the listenable will not handle any events. This is likely to be overridden by
     * the implementing class to provide a toggleable feature.
     *
     * This can be ignored by handlers when [ignoreNotRunning] is set to true on the [EventHook].
     */
    val running: Boolean
        get() = parent()?.running ?: !isDestructed

    /**
     * Parent [EventListener]
     */
    fun parent(): EventListener? = null

    /**
     * Children [EventListener]
     */
    fun children(): List<EventListener> = emptyList()

    /**
     * Unregisters the event handler from the manager. This decision is FINAL!
     * After the class was unregistered we cannot restore the handlers.
     */
    fun unregister() {
        EventManager.unregisterEventHandler(this)

        for (child in children()) {
            child.unregister()
        }
    }

}

inline fun <reified T : Event> EventListener.handler(
    priority: Short = 0,
    noinline handler: Handler<T>
): EventHook<T> {
    return EventManager.registerEventHook(T::class.java,
        EventHook(this, handler, priority)
    )
}

inline fun <reified T : Event> EventListener.once(
    priority: Short = 0,
    crossinline handler: Handler<T>
): EventHook<T> {
    lateinit var eventHook: EventHook<T>
    eventHook = EventHook(this, {
        handler(it)
        EventManager.unregisterEventHook(T::class.java, eventHook)
    }, priority)
    return EventManager.registerEventHook(T::class.java, eventHook)
}

/**
 * Registers an event hook for events of type [T] and launches a sequence
 */
inline fun <reified T : Event> EventListener.sequenceHandler(
    priority: Short = 0,
    crossinline eventHandler: SuspendableEventHandler<T>
) {
    handler<T>(priority) { event -> Sequence(this) { eventHandler(event) } }
}

/**
 * Registers a repeatable sequence which repeats the execution of code on GameTickEvent.
 */
fun EventListener.tickHandler(eventHandler: SuspendableHandler) {
    // We store our sequence in this variable.
    // That can be done because our variable will survive the scope of this function
    // and can be used in the event handler function. This is a very useful pattern to use in Kotlin.
    var sequence: TickSequence? = TickSequence(this, eventHandler)

    SequenceManager.handler<GameTickEvent> {
        // Check if we should start or stop the sequence
        if (this.running) {
            // Check if the sequence is already running
            if (sequence == null) {
                // If not, start it
                // This will start a new repeating sequence which will run until the condition is false
                sequence = TickSequence(this, eventHandler)
            }
        } else if (sequence != null) { // This condition is only true if the sequence is running
            // If the sequence is running, we should stop it
            sequence?.cancel()
            sequence = null
        }
    }
}
