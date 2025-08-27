package cn.cheneya.skypvp.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.GL11

/**
 * 模板缓冲区工具类，用于实现裁剪效果
 */
object StencilUtils {
    private val mc = MinecraftClient.getInstance()
    /**
     * 开始写入模板缓冲区
     * @param renderClipLayer 是否渲染裁剪层
     */
    fun write(renderClipLayer: Boolean) {
        setupFBO()
        GL11.glClear(1024)
        GL11.glEnable(1960)
        GL11.glStencilFunc(519, 1, 65535)
        GL11.glStencilOp(7680, 7680, 7681)

        if (!renderClipLayer) {
            RenderSystem.colorMask(false, false, false, false)
        }
    }

    /**
     * 开始擦除模板缓冲区
     * @param invert 是否反转模板测试
     */
    fun erase(invert: Boolean) {
        RenderSystem.colorMask(true, true, true, true)
        GL11.glStencilFunc(if (invert) GL11.GL_NOTEQUAL else GL11.GL_EQUAL, 1, 0xFFFF)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE)
    }

    /**
     * 禁用模板测试
     */
    fun dispose() {
        GL11.glDisable(GL11.GL_STENCIL_TEST)
    }

    /**
     * 设置帧缓冲对象
     */
    fun setupFBO() {
        val mainFramebuffer = mc.framebuffer
        if (mainFramebuffer.depthAttachment > -1) {
            setupFBO(mainFramebuffer)
        }
    }

    /**
     * 设置指定的帧缓冲对象
     * @param fbo 帧缓冲对象
     */
    fun setupFBO(fbo: Framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthAttachment)
        val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT()
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferID)
        EXTFramebufferObject.glRenderbufferStorageEXT(
            36161,
            34041,
            mc.window.scaledWidth,
            mc.window.scaledHeight
        )
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            36160,
            36128,
            36161,
            stencilDepthBufferID
        )
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            36160,
            36096,
            36161,
            stencilDepthBufferID
        )
    }
}