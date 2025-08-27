package cn.cheneya.skypvp.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.GameRenderer
import org.lwjgl.opengl.GL11

object GL {
    fun bindTexture(id: Int) {
        // 修复setShader和getPositionTexColorProgram的调用
        RenderSystem.setShaderTexture(0, id)
    }
    
    fun enableBlend() {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
    }
    
    fun disableBlend() {
        RenderSystem.disableBlend()
    }
    
    // 移除不存在的enableTexture和disableTexture方法
    
    fun enableDepth() {
        RenderSystem.enableDepthTest()
    }
    
    fun disableDepth() {
        RenderSystem.disableDepthTest()
    }
}