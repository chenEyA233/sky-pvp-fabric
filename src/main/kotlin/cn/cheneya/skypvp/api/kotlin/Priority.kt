package cn.cheneya.skypvp.api.kotlin

import cn.cheneya.skypvp.api.kotlin.EventPriorityConvention.OBJECTION_AGAINST_EVERYTHING


enum class Priority(val priority: Int) {
    NOT_IMPORTANT(-20),
    NORMAL(0),

    IMPORTANT_FOR_USAGE_1(20),
    /**
     * KillAura, etc.
     */
    IMPORTANT_FOR_USAGE_2(30),
    IMPORTANT_FOR_USAGE_3(35),
    /**
     * Scaffold, etc.
     */
    IMPORTANT_FOR_PLAYER_LIFE(40),

    IMPORTANT_FOR_USER_SAFETY(60);
}

object EventPriorityConvention {

    /**
     * The event should be called first.
     */
    const val FIRST_PRIORITY: Short = 1000

    /**
     * Priority for critical modifications that need to happen early in the event chain,
     * after input preparation but before model state processing
     */
    const val CRITICAL_MODIFICATION: Short = 500

    /**
     * At the stage of modeling what the player is actually going to do after other events added their suggestions
     */
    const val MODEL_STATE: Short = -10

    /**
     * Should be the one of the last functionalities that run, because the player safety depends on it.
     * Can be objected though by handlers with [OBJECTION_AGAINST_EVERYTHING] priority
     */
    const val SAFETY_FEATURE: Short = -50

    /**
     * Used when the event handler should be able to object anything that happened previously
     */
    const val OBJECTION_AGAINST_EVERYTHING: Short = -100

    /**
     * Used when the event handler should be able to object anything that happened previously
     */
    const val FINAL_DECISION: Short = -500

    /**
     * The event should be called last. It should not only be used for events that want to read the final state of the
     * event
     */
    const val READ_FINAL_STATE: Short = -1000
}
