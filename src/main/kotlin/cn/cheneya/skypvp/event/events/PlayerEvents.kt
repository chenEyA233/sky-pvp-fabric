package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.api.utils.client.misc.WebSocketEvent
import cn.cheneya.skypvp.event.CancellableEvent
import cn.cheneya.skypvp.event.Event
import cn.cheneya.skypvp.event.EventState
import net.minecraft.entity.MovementType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d

// Entity events bound to client-user entity
@Nameable("healthUpdate")
class HealthUpdateEvent(val health: Float, val food: Int, val saturation: Float, val previousHealth: Float) : Event()

@Nameable("death")
@WebSocketEvent
object DeathEvent : Event()

@Nameable("playerTick")
class PlayerTickEvent : CancellableEvent()

@Nameable("playerPostTick")
object PlayerPostTickEvent : Event()

@Nameable("playerMovementTick")
object PlayerMovementTickEvent : Event()

@Nameable("playerNetworkMovementTick")
class PlayerNetworkMovementTickEvent(val state: EventState,
                                     var x: Double,
                                     var y: Double,
                                     var z: Double,
                                     var ground: Boolean
                                    ): Event()

@Nameable("playerPushOut")
class PlayerPushOutEvent : CancellableEvent()

@Nameable("playerMove")
class PlayerMoveEvent(val type: MovementType, var movement: Vec3d) : Event()

@Nameable("playerJump")
class PlayerJumpEvent(var motion: Float) : CancellableEvent()

@Nameable("playerAfterJump")
object PlayerAfterJumpEvent : Event()

@Nameable("playerUseMultiplier")
class PlayerUseMultiplier(var forward: Float, var sideways: Float) : Event()

@Nameable("playerSneakMultiplier")
class PlayerSneakMultiplier(var multiplier: Double) : Event()

/**
 * Warning: UseHotbarSlotOrOffHand won't stimulate this event
 */
@Nameable("playerInteractItem")
class PlayerInteractItemEvent : CancellableEvent()

@Nameable("playerInteractedItem")
class PlayerInteractedItemEvent(val player: PlayerEntity, val hand: Hand, val actionResult: ActionResult) : Event()

@Nameable("playerStrafe")
class PlayerVelocityStrafe(val movementInput: Vec3d, val speed: Float, val yaw: Float, var velocity: Vec3d) : Event()

@Nameable("playerStride")
class PlayerStrideEvent(var strideForce: Float) : Event()

@Nameable("playerSafeWalk")
class PlayerSafeWalkEvent(var isSafeWalk: Boolean = false) : Event()

@Nameable("playerStep")
class PlayerStepEvent(var height: Float) : Event()

@Nameable("playerStepSuccess")
class PlayerStepSuccessEvent(val movementVec: Vec3d, var adjustedVec: Vec3d) : Event()

@Nameable("playerFluidCollisionCheck")
class PlayerFluidCollisionCheckEvent(val fluid: TagKey<Fluid>) : CancellableEvent()
