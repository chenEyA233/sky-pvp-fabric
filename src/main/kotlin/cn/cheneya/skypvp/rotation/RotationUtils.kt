package cn.cheneya.skypvp.rotation

import cn.cheneya.skypvp.api.math.Vector2f
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper as MinecraftMathHelper
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 旋转工具类，提供各种与视角旋转相关的实用方法
 */
class RotationUtils {
    companion object {
        private val mc = MinecraftClient.getInstance()
        
        /**
         * 获取从起点到终点的旋转角度
         */
        fun getRotationTo(from: Vec3d, to: Vec3d): Rotation {
            val diff = to.subtract(from)
            val yaw = Math.toDegrees(atan2(diff.z, diff.x)).toFloat() - 90.0f
            val pitch = -Math.toDegrees(atan2(diff.y, sqrt(diff.x * diff.x + diff.z * diff.z))).toFloat()
            return Rotation(yaw, pitch)
        }
        
        /**
         * 获取两个角度之间的差值
         */
        fun getAngleDifference(a: Float, b: Float): Float {
            return ((a - b) % 360.0f + 540.0f) % 360.0f - 180.0f
        }
        
        /**
         * 获取当前玩家视角的朝向向量
         */
        fun getLook(): Vec3d {
            val player = mc.player ?: return Vec3d.ZERO
            return getLook(player.yaw, player.pitch)
        }
        
        /**
         * 获取修正后的旋转角度
         */
        fun getFixedRotation(yaw: Float, pitch: Float, lastYaw: Float, lastPitch: Float): Vec2f {
            val sensitivity = mc.options.mouseSensitivity.value.toFloat()
            val f = sensitivity * 0.6f + 0.2f
            val gcd = f * f * f * 1.2f
            
            val deltaYaw = yaw - lastYaw
            val deltaPitch = pitch - lastPitch
            val fixedDeltaYaw = deltaYaw - deltaYaw % gcd
            val fixedDeltaPitch = deltaPitch - deltaPitch % gcd
            val fixedYaw = lastYaw + fixedDeltaYaw
            val fixedPitch = lastPitch + fixedDeltaPitch
            
            return Vec2f(fixedYaw, fixedPitch)
        }
        
        /**
         * 根据yaw和pitch获取朝向向量
         */
        fun getLook(yaw: Float, pitch: Float): Vec3d {
            val f = cos(-yaw * (Math.PI / 180.0).toFloat() - Math.PI.toFloat())
            val f1 = sin(-yaw * (Math.PI / 180.0).toFloat() - Math.PI.toFloat())
            val f2 = -cos(-pitch * (Math.PI / 180.0).toFloat())
            val f3 = sin(-pitch * (Math.PI / 180.0).toFloat())
            
            return Vec3d((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
        }
        
        /**
         * 检查向量是否在包围盒内
         */
        fun isVecInside(self: Box, vec: Vec3d): Boolean {
            return vec.x > self.minX && vec.x < self.maxX && 
                   vec.y > self.minY && vec.y < self.maxY && 
                   vec.z > self.minZ && vec.z < self.maxZ
        }
        
        /**
         * 获取从眼睛位置到目标位置的旋转角度
         */
        fun getRotations(eye: Vec3d, target: Vec3d): Rotation {
            val x = target.x - eye.x
            val y = target.y - eye.y
            val z = target.z - eye.z
            val diffXZ = sqrt(x * x + z * z)
            
            val yaw = Math.toDegrees(Math.atan2(z, x)).toFloat() - 90.0f
            val pitch = (-Math.toDegrees(Math.atan2(y, diffXZ))).toFloat()
            
            return Rotation(MinecraftMathHelper.wrapDegrees(yaw), MinecraftMathHelper.wrapDegrees(pitch))
        }
        
        /**
         * 获取从玩家到方块位置的旋转角度
         */
        fun getRotations(pos: BlockPos, partialTicks: Float): Rotation {
            val player = mc.player ?: return Rotation()
            
            val playerVector = Vec3d(
                player.x + player.velocity.x * partialTicks,
                player.y + player.getEyeHeight(player.pose) + player.velocity.y * partialTicks,
                player.z + player.velocity.z * partialTicks
            )
            
            val x = pos.x - playerVector.x + 0.5
            val y = pos.y - playerVector.y + 0.5
            val z = pos.z - playerVector.z + 0.5
            
            return diffCalc(randomization(x), randomization(y), randomization(z))
        }
        
        /**
         * 计算差值旋转角度
         */
        fun diffCalc(diffX: Double, diffY: Double, diffZ: Double): Rotation {
            val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
            val yaw = Math.toDegrees(Math.atan2(diffZ, diffX)).toFloat() - 90.0f
            val pitch = (-Math.toDegrees(Math.atan2(diffY, diffXZ))).toFloat()
            
            return Rotation(MinecraftMathHelper.wrapDegrees(yaw), MinecraftMathHelper.wrapDegrees(pitch))
        }
        
        /**
         * 添加随机化到值
         */
        private fun randomization(value: Double): Double {
            return value + MathUtils.getRandomDoubleInRange(0.05, 0.08) * 
                   (MathUtils.getRandomDoubleInRange(0.0, 1.0) * 2.0 - 1.0)
        }
        
        /**
         * 获取到目标实体的最小距离
         */
        fun getMinDistance(target: Entity, rotations: Vec2f): Double {
            var minDistance = Double.MAX_VALUE
            
            for (eye in getPossibleEyeHeights()) {
                val player = mc.player ?: continue
                val playerPosition = Vec3d(player.x, player.y, player.z)
                val eyePos = playerPosition.add(0.0, eye.toDouble(), 0.0)
                minDistance = minOf(minDistance, getDistance(target, eyePos, rotations))
            }
            
            return minDistance
        }
        
        /**
         * 获取从眼睛位置到目标实体的距离
         */
        fun getDistance(target: Entity, eyePos: Vec3d, rotations: Vec2f): Double {
            val targetBox = getTargetBoundingBox(target)
            val position = getIntercept(targetBox, rotations, eyePos, 6.0)
            
            return if (position != null) {
                position.pos.distanceTo(eyePos)
            } else {
                1000.0
            }
        }
        
        /**
         * 获取射线与包围盒的交点
         */
        fun getIntercept(targetBox: Box, rotations: Vec2f, eyePos: Vec3d, reach: Double): HitResult? {
            val vec31 = getLook(rotations.x, rotations.y)
            val vec32 = eyePos.add(vec31.x * reach, vec31.y * reach, vec31.z * reach)
            
            val player = mc.player ?: return null
            return ProjectileUtil.raycast(
                player,
                eyePos,
                vec32,
                targetBox,
                { entity -> !entity.isSpectator && entity.canHit() },
                reach * reach
            )
        }
        
        /**
         * 获取射线与包围盒的交点（使用默认距离）
         */
        fun getIntercept(targetBox: Box, rotations: Vec2f, eyePos: Vec3d): HitResult? {
            return getIntercept(targetBox, rotations, eyePos, 6.0)
        }
        
        /**
         * 计算差值旋转向量
         */
        fun diffCalcVector(diffX: Double, diffY: Double, diffZ: Double): Vec2f {
            val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
            val yaw = Math.toDegrees(Math.atan2(diffZ, diffX)).toFloat() - 90.0f
            val pitch = (-Math.toDegrees(Math.atan2(diffY, diffXZ))).toFloat()
            
            return Vec2f(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch))
        }
        
        /**
         * 获取到向量的旋转角度
         */
        fun getRotationsVector(vec: Vec3d): Vec2f {
            val player = mc.player ?: return Vec2f(0f, 0f)
            val playerVector = Vec3d(player.x, player.y + player.getEyeHeight(player.pose), player.z)
            val x = vec.x - playerVector.x
            val y = vec.y - playerVector.y
            val z = vec.z - playerVector.z
            
            return diffCalcVector(x, y, z)
        }
        
        /**
         * 检查射线结果是否命中目标实体
         */
        private fun checkHitResult(eyePos: Vec3d, result: HitResult, target: Entity): Boolean {
            if (result.type == HitResult.Type.ENTITY && (result as EntityHitResult).entity == target) {
                val intercept = result.pos
                return isVecInside(getTargetBoundingBox(target), eyePos) || intercept.distanceTo(eyePos) <= 3.0
            }
            return false
        }
        
        /**
         * 执行射线追踪
         */
        private fun rayTrace(rotations: Rotation): HitResult? {
            val player = mc.player ?: return null
            val gameMode = mc.interactionManager ?: return null
            
            var d0 = 4.5 // 默认的交互距离
            var hitResult = RayTraceUtil.rayCast(d0, 1.0f, false, rotations)
            val vec3 = player.eyePos
            var flag = false
            var d1 = d0
            
            if (false) { // 移除扩展交互距离检查，默认使用标准距离
                d1 = 6.0
                d0 = d1
            } else if (d0 > 3.0) {
                flag = true
            }
            
            d1 *= d1
            if (hitResult != null) {
                d1 = hitResult.pos.squaredDistanceTo(vec3)
            }
            
            val vec31 = getLook(rotations.yaw, rotations.pitch)
            val vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0)
            val aabb = player.boundingBox.stretch(vec31.multiply(d0)).expand(1.0, 1.0, 1.0)
            
            val entityhitresult = ProjectileUtil.raycast(
                player,
                vec3,
                vec32,
                aabb,
                { entity -> !entity.isSpectator && entity.canHit() },
                d1
            )
            
            if (entityhitresult != null) {
                val vec33 = entityhitresult.pos
                val d2 = vec3.squaredDistanceTo(vec33)
                
                if (flag && d2 > 9.0) {
                    hitResult = BlockHitResult.createMissed(
                        vec33,
                        Direction.getFacing(vec31.x, vec31.y, vec31.z),
                        BlockPos.ofFloored(vec33)
                    )
                } else if (d2 < d1 || hitResult == null) {
                    hitResult = entityhitresult
                }
            }
            
            return hitResult
        }
        
        /**
         * 获取旋转角度对应的向量
         */
        fun getVectorForRotation(rotation: Rotation): Vec3d {
            val yawCos = cos((-rotation.yaw * (Math.PI / 180.0).toFloat() - Math.PI.toFloat()).toDouble()).toFloat()
            val yawSin = sin((-rotation.yaw * (Math.PI / 180.0).toFloat() - Math.PI.toFloat()).toDouble()).toFloat()
            val pitchCos = (-cos((-rotation.pitch * (Math.PI / 180.0).toFloat()).toDouble())).toFloat()
            val pitchSin = sin((-rotation.pitch * (Math.PI / 180.0).toFloat()).toDouble()).toFloat()
            
            return Vec3d((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
        }
        
        /**
         * 获取到实体的旋转数据
         */
        fun getRotationDataToEntity(target: Entity): Data {
            val player = mc.player ?: return Data(Vec3d.ZERO, Vec3d.ZERO, 1000.0, null)
            val playerPosition = Vec3d(player.x, player.y, player.z)
            val eyePos = playerPosition.add(0.0, player.getEyeHeight(player.pose).toDouble(), 0.0)
            val targetBox = getTargetBoundingBox(target)
            
            val minX = targetBox.minX
            val minY = targetBox.minY
            val minZ = targetBox.minZ
            val maxX = targetBox.maxX
            val maxY = targetBox.maxY
            val maxZ = targetBox.maxZ
            val spacing = 0.1
            
            val points = mutableSetOf<Vec3d>()
            points.add(Vec3d(minX + maxX / 2.0, minY + maxY / 2.0, minZ + maxZ / 2.0))
            points.add(getClosestPoint(eyePos, targetBox))
            
            for (x in generateSequence(minX) { it + spacing }.takeWhile { it <= maxX }) {
                for (y in generateSequence(minY) { it + spacing }.takeWhile { it <= maxY }) {
                    points.add(Vec3d(x, y, minZ))
                    points.add(Vec3d(x, y, maxZ))
                }
            }
            
            for (x in generateSequence(minX) { it + spacing }.takeWhile { it <= maxX }) {
                for (z in generateSequence(minZ) { it + spacing }.takeWhile { it <= maxZ }) {
                    points.add(Vec3d(x, minY, z))
                    points.add(Vec3d(x, maxY, z))
                }
            }
            
            for (y in generateSequence(minY) { it + spacing }.takeWhile { it <= maxY }) {
                for (z in generateSequence(minZ) { it + spacing }.takeWhile { it <= maxZ }) {
                    points.add(Vec3d(minX, y, z))
                    points.add(Vec3d(maxX, y, z))
                }
            }
            
            for (point in points) {
                val bruteRotations = getRotations(eyePos, point)
                val bruteHitResult = rayTrace(bruteRotations)
                
                if (bruteHitResult != null && checkHitResult(eyePos, bruteHitResult, target)) {
                    val location = bruteHitResult.pos
                    val lastRotations = RotationManager.lastRotations
                    return Data(
                        eyePos,
                        location,
                        location.distanceTo(eyePos),
                        getFixedRotation(bruteRotations?.yaw ?: 0f, bruteRotations?.pitch ?: 0f, lastRotations?.x ?: 0f, lastRotations?.y ?: 0f)
                    )
                }
            }
            
            return Data(eyePos, eyePos, 1000.0, null)
        }
        
        /**
         * 获取实体的包围盒
         */
        private fun getTargetBoundingBox(entity: Entity): Box {
            return entity.boundingBox
        }
        
        /**
         * 获取可能的眼睛高度列表
         */
        fun getPossibleEyeHeights(): List<Float> {
            val player = mc.player ?: return emptyList()
            return listOf(player.getEyeHeight(player.pose))
        }
        
        /**
         * 获取包围盒上最接近给定向量的点
         */
        fun getClosestPoint(vec: Vec3d, aabb: Box): Vec3d {
            val closestX = maxOf(aabb.minX, minOf(vec.x, aabb.maxX))
            val closestY = maxOf(aabb.minY, minOf(vec.y, aabb.maxY))
            val closestZ = maxOf(aabb.minZ, minOf(vec.z, aabb.maxZ))
            
            return Vec3d(closestX, closestY, closestZ)
        }
        
        /**
         * 获取到实体的旋转角度
         */
        fun getRotations(entity: Entity?): Vec2f? {
            if (entity == null) {
                return null
            }
            
            val player = mc.player ?: return null
            val diffX = entity.x - player.x
            val diffZ = entity.z - player.z
            val diffY = entity.y + entity.getEyeHeight(entity.pose) - (player.y + player.getEyeHeight(player.pose))
            
            return diffCalcVector(diffX, diffY, diffZ)
        }
        
        /**
         * 旋转到指定的yaw角度
         */
        fun rotateToYaw(yawSpeed: Float, currentYaw: Float, calcYaw: Float): Float {
            return updateRotation(currentYaw, calcYaw, yawSpeed)
        }
        
        /**
         * 更新旋转角度
         */
        fun updateRotation(current: Float, calc: Float, maxDelta: Float): Float {
            var f = MathHelper.wrapDegrees(calc - current)
            
            if (f > maxDelta) {
                f = maxDelta
            }
            
            if (f < -maxDelta) {
                f = -maxDelta
            }
            
            return current + f
        }
        
        /**
         * 检查实体是否在视野范围内
         */
        fun inFoV(entity: Entity, fov: Float): Boolean {
            val player = mc.player ?: return false
            val rotations = getRotations(entity) ?: return false
            val diff = Math.abs(player.yaw % 360.0f - rotations.x)
            val minDiff = Math.abs(minOf(diff, 360.0f - diff))
            
            return minDiff <= fov
        }
        
        /**
         * 获取两个角度之间的距离
         */
        fun getDistanceBetweenAngles(angle1: Float, angle2: Float): Float {
            var angle3 = Math.abs(angle1 - angle2) % 360.0f
            
            if (angle3 > 180.0f) {
                angle3 = 0.0f
            }
            
            return angle3
        }
        
        /**
         * 获取玩家眼睛位置
         */
        fun getEyesPos(): Vec3d {
            val player = mc.player ?: return Vec3d.ZERO
            return Vec3d(player.x, player.y + player.getEyeHeight(player.pose), player.z)
        }
    }
    
    /**
     * 旋转数据类
     */
    data class Data(
        val eye: Vec3d,
        val hitVec: Vec3d,
        val distance: Double,
        val rotation: Vec2f?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Data) return false
            if (!other.canEqual(this)) return false
            if (java.lang.Double.compare(this.distance, other.distance) != 0) return false
            
            if (eye != other.eye) return false
            if (hitVec != other.hitVec) return false
            return rotation == other.rotation
        }
        
        fun canEqual(other: Any?): Boolean {
            return other is Data
        }
        
        override fun hashCode(): Int {
            var result = 1
            val temp = java.lang.Double.doubleToLongBits(distance)
            result = 59 * result + (temp ushr 32 xor temp).toInt()
            result = 59 * result + (eye?.hashCode() ?: 43)
            result = 59 * result + (hitVec?.hashCode() ?: 43)
            result = 59 * result + (rotation?.hashCode() ?: 43)
            return result
        }
        
        override fun toString(): String {
            return "RotationUtils.Data(eye=$eye, hitVec=$hitVec, distance=$distance, rotation=$rotation)"
        }
    }
}