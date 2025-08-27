package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.math.Color4b
import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.event.Event
import cn.cheneya.skypvp.utils.EntityTargetClassification
import cn.cheneya.skypvp.utils.EntityTargetingInfo
import cn.cheneya.skypvp.api.kotlin.Priority
import cn.cheneya.skypvp.api.kotlin.PriorityField
import cn.cheneya.skypvp.event.CancellableEvent
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity


@Nameable("entityMargin")
class EntityMarginEvent(val entity: Entity, var margin: Float) : Event()

@Nameable("attack_entity")
class AttackEntityEvent(
    val target: LivingEntity
) : CancellableEvent()

@Nameable("tagEntityEvent")
class TagEntityEvent(val entity: Entity, var targetingInfo: EntityTargetingInfo) : Event() {
    val color: PriorityField<Color4b?> = PriorityField(null, Priority.NOT_IMPORTANT)

    /**
     * Don't start combat this target
     */
    fun dontTarget() {
        if (this.targetingInfo.classification == EntityTargetClassification.TARGET) {
            this.targetingInfo = this.targetingInfo.copy(classification = EntityTargetClassification.INTERESTING)
        }
    }

    /**
     * Fully ignore that target
     */
    fun ignore() {
        this.targetingInfo = targetingInfo.copy(classification = EntityTargetClassification.IGNORED)
    }

    fun assumeFriend() {
        this.targetingInfo = targetingInfo.copy(isFriend = true)
    }

    fun color(col: Color4b, priority: Priority) {
        this.color.trySet(col, priority)
    }
}
