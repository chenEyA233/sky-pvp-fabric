package cn.cheneya.skypvp.rotation

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
// 移除 EntityPredicates 导入，使用自定义判断
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import java.util.*

/**
 * 射线追踪工具类
 */
object RayTraceUtil {
    private val mc = MinecraftClient.getInstance()
    
    /**
     * 执行射线追踪
     */
    fun rayCast(partialTicks: Float, rotations: Rotation): HitResult? {
        val entity = mc.cameraEntity ?: return null
        if (mc.world == null) return null
        
        val distance = 4.5 // 默认的交互距离
        return pick(distance, partialTicks, true, rotations.yaw, rotations.pitch)
    }
    
    /**
     * 执行射线追踪，指定范围和流体处理
     */
    fun rayCast(range: Double, partialTicks: Float, hitFluids: Boolean, rotations: Rotation): HitResult? {
        val entity = mc.cameraEntity ?: return null
        if (mc.world == null) return null
        
        return pick(range, partialTicks, hitFluids, rotations.yaw, rotations.pitch)
    }
    
    /**
     * 计算视角向量
     */
    fun calculateViewVector(pitch: Float, yaw: Float): Vec3d {
        val f = Math.cos((-yaw * Math.PI / 180.0 - Math.PI).toDouble()).toFloat()
        val f1 = Math.sin((-yaw * Math.PI / 180.0 - Math.PI).toDouble()).toFloat()
        val f2 = -Math.cos((-pitch * Math.PI / 180.0).toDouble()).toFloat()
        val f3 = Math.sin((-pitch * Math.PI / 180.0).toDouble()).toFloat()
        
        return Vec3d((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }
    
    /**
     * 执行射线追踪，返回命中结果
     */
    fun pick(hitDistance: Double, partialTicks: Float, hitFluids: Boolean, yaw: Float, pitch: Float): HitResult? {
        val player = mc.player ?: return null
        val vec3 = Vec3d(player.x, player.y + player.standingEyeHeight, player.z)
        val vec31 = calculateViewVector(pitch, yaw)
        val vec32 = vec3.add(vec31.x * hitDistance, vec31.y * hitDistance, vec31.z * hitDistance)
        
        return mc.world?.raycast(
            RaycastContext(
                vec3,
                vec32,
                RaycastContext.ShapeType.OUTLINE,
                if (hitFluids) RaycastContext.FluidHandling.ANY else RaycastContext.FluidHandling.NONE,
                player
            )
        )
    }
    
    /**
     * 射线追踪方块，返回命中结果
     */
    fun rayTraceBlocks(
        start: Vec3d,
        end: Vec3d,
        stopOnLiquid: Boolean,
        ignoreBlockWithoutBoundingBox: Boolean,
        returnLastUncollidableBlock: Boolean,
        entity: Entity?
    ): HitResult? {
        val shapeType = when {
            ignoreBlockWithoutBoundingBox -> RaycastContext.ShapeType.COLLIDER
            returnLastUncollidableBlock -> RaycastContext.ShapeType.VISUAL
            else -> RaycastContext.ShapeType.OUTLINE
        }
        
        val fluidHandling = if (stopOnLiquid) RaycastContext.FluidHandling.ANY else RaycastContext.FluidHandling.NONE
        val context = RaycastContext(start, end, shapeType, fluidHandling, entity)
        
        return mc.world?.raycast(context)
    }
    
    /**
     * 计算射线与包围盒的交点
     */
    fun calculateIntercept(box: Box, start: Vec3d, end: Vec3d): EntityHitResult? {
        val optional = box.raycast(start, end)
        return optional.map { vec3 -> EntityHitResult(null, vec3) }.orElse(null)
    }
    
    /**
     * 执行复杂的射线追踪，支持实体检测和穿墙
     */
    fun rayCast(rotation: Rotation, range: Double, expand: Float, filterEntity: Entity?, targetEntity: Entity?, throughWalls: Boolean): HitResult? {
        if (filterEntity == null || mc.world == null) return null

        val eyePosition = filterEntity.eyePos
        val lookVector = RotationUtils.getVectorForRotation(rotation)
        val targetVec = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range)
        
        var blockHit: BlockHitResult? = null
        var blockDistance = range
        
        if (!throughWalls) {
            blockHit = mc.world?.raycast(
                RaycastContext(
                    eyePosition,
                    targetVec,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    filterEntity
                )
            )
            
            blockDistance = if (blockHit?.type == HitResult.Type.BLOCK) {
                eyePosition.distanceTo(blockHit.pos)
            } else {
                range
            }
        }
        
        val expandedRange = Math.min(range, blockDistance) + expand
        val searchBox = Box(
            eyePosition.x - expandedRange,
            eyePosition.y - expandedRange,
            eyePosition.z - expandedRange,
            eyePosition.x + expandedRange,
            eyePosition.y + expandedRange,
            eyePosition.z + expandedRange
        )
        
        val entities = mc.world?.getEntitiesByClass(
            Entity::class.java,
            searchBox
        ) { ex ->
            ex != filterEntity && 
            (targetEntity == null || ex == targetEntity) && 
            !ex.isSpectator && 
            ex.isCollidable
        } ?: emptyList()
        
        var pointedEntity: Entity? = null
        var hitVec: Vec3d? = null
        var closestDistance = Math.min(range, blockDistance)
        closestDistance *= closestDistance
        
        for (e in entities) {
            val entityBox = e.boundingBox.expand(expand.toDouble())
            val intercept = entityBox.raycast(eyePosition, targetVec).orElse(null) ?: continue
            
            val distSq = eyePosition.squaredDistanceTo(intercept)
            if (distSq < closestDistance) {
                var canHit = true
                
                if (!throughWalls) {
                    val entityCenter = entityBox.center
                    val wallCheck = mc.world?.raycast(
                        RaycastContext(
                            eyePosition,
                            entityCenter,
                            RaycastContext.ShapeType.OUTLINE,
                            RaycastContext.FluidHandling.NONE,
                            filterEntity
                        )
                    )
                    
                    if (wallCheck?.type == HitResult.Type.BLOCK && 
                        eyePosition.squaredDistanceTo(wallCheck.pos) <= distSq) {
                        canHit = false
                    }
                }
                
                if (canHit) {
                    closestDistance = distSq
                    pointedEntity = e
                    hitVec = intercept
                }
            }
        }
        
        if (pointedEntity != null && hitVec != null) {
            return EntityHitResult(pointedEntity, hitVec)
        }
        
        return if (!throughWalls && blockHit != null) {
            blockHit
        } else {
            mc.world?.raycast(
                RaycastContext(
                    eyePosition,
                    targetVec,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    filterEntity
                )
            )
        }
    }
}