package cn.cheneya.skypvp.event.events

import cn.cheneya.skypvp.api.utils.client.misc.Nameable
import cn.cheneya.skypvp.event.Event
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.Camera
import net.minecraft.client.util.math.MatrixStack
import net.skypvpteam.skypvp.api.event

@event("DrawEvents")
@Nameable("gameRender")
object GameRenderEvent : Event()

@Nameable("screenRender")
class ScreenRenderEvent(val context: DrawContext, val partialTicks: Float) : Event()

@Nameable("worldRender")
class WorldRenderEvent(val matrixStack: MatrixStack, val camera: Camera, val partialTicks: Float) : Event()

/**
 * Sometimes, modules might want to contribute something to the glow framebuffer. They can hook this event
 * in order to do so.
 *
 * Note: After writing to the outline framebuffer [markDirty] must be called.
 */
@Nameable("drawOutlines")
class DrawOutlinesEvent(
    val matrixStack: MatrixStack,
    val camera: Camera,
    val partialTicks: Float,
    val type: OutlineType,
) : Event() {
    var dirtyFlag: Boolean = false
        private set

    /**
     * Called when the framebuffer was edited.
     */
    fun markDirty() {
        this.dirtyFlag = true
    }

    enum class OutlineType {
        INBUILT_OUTLINE,
        MINECRAFT_GLOW
    }
}

@Nameable("overlayRender")
class OverlayRenderEvent(val context: DrawContext, val tickDelta: Float) : Event()
