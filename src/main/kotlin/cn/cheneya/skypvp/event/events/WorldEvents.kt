package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.event.CancellableEvent
import cn.cheneya.skypvp.event.Event
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape

@Nameable("worldChange")
class WorldChangeEvent(val world: ClientWorld?) : Event()

@Nameable("chunkUnload")
class ChunkUnloadEvent(val x: Int, val z: Int) : Event()

@Nameable("chunkLoad")
class ChunkLoadEvent(val x: Int, val z: Int) : Event()

@Nameable("chunkDeltaUpdate")
class ChunkDeltaUpdateEvent(val x: Int, val z: Int) : Event()

@Nameable("blockChange")
class BlockChangeEvent(val blockPos: BlockPos, val newState: BlockState) : Event()

@Nameable("blockShape")
class BlockShapeEvent(var state: BlockState, var pos: BlockPos, var shape: VoxelShape) : Event()

@Nameable("blockBreakingProgress")
class BlockBreakingProgressEvent(val pos: BlockPos) : Event()

@Nameable("blockBreakingProgress")
class BlockAttackEvent(val pos: BlockPos) : CancellableEvent()

@Nameable("blockVelocityMultiplier")
class BlockVelocityMultiplierEvent(val block: Block, var multiplier: Float) : Event()

@Nameable("blockSlipperinessMultiplier")
class BlockSlipperinessMultiplierEvent(val block: Block, var slipperiness: Float) : Event()

@Nameable("entityEquipmentChange")
class PlayerEquipmentChangeEvent(
    val player: PlayerEntity, val equipmentSlot: EquipmentSlot, val itemStack: ItemStack
) : Event()

@Nameable("fluidPush")
class FluidPushEvent : CancellableEvent()

@Nameable("worldEntityRemove")
class WorldEntityRemoveEvent(val entity: Entity) : Event()
