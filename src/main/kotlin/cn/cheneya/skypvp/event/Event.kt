package cn.cheneya.skypvp.event

/**
 * A callable event
 */
abstract class Event

/**
 * A cancellable event
 */
abstract class CancellableEvent : Event() {
    /**
     * Let you know if the event is cancelled
     *
     * @return state of cancel
     */
    var isCancelled: Boolean = false
        private set

    /**
     * Allows you to cancel an event
     */
    fun cancelEvent() {
        isCancelled = true
    }

}

/**
 * MixinEntityRenderState of event. Might be PRE or POST.
 */
enum class EventState(val stateName: String) {
    PRE("PRE"), POST("POST")
}

