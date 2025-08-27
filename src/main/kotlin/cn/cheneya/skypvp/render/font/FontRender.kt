package cn.cheneya.skypvp.render.font

import cn.cheneya.skypvp.render.DrawMode
import cn.cheneya.skypvp.render.GL
import cn.cheneya.skypvp.render.Mesh
import cn.cheneya.skypvp.render.ShaderMesh
import cn.cheneya.skypvp.render.Shaders
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import java.lang.IllegalStateException
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.logging.log4j.LogManager
import org.lwjgl.BufferUtils
import java.awt.Color
import java.io.IOException
import java.nio.ByteBuffer

class FontRender(
    name: String,
    size: Int,
    from: Int,
    to: Int,
    textureSize: Int
) {
    private val logger = LogManager.getLogger(FontRender::class.java)
    private val SHADOW_COLOR = Color(60, 60, 60, 180)
    private val mesh = ShaderMesh(Shaders.TEXT, DrawMode.Triangles, Mesh.Attrib.Vec2, Mesh.Attrib.Vec2, Mesh.Attrib.Color)
    private val font: Font

    init {
        val resourcePath = "/assets/skypvp/textures/client/font/$name.ttf"
        val inputStream = javaClass.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Font not found: $name")

        val bytes = try {
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                out.write(buffer, 0, len)
            }
            out.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException("Failed to read font: $name", e)
        }

        val buffer = BufferUtils.createByteBuffer(bytes.size).put(bytes)
        buffer.flip()

        val startTime = System.currentTimeMillis()
        font = Font(buffer, size, from, to, textureSize)
        logger.info("Loaded font {} in {}ms", name, System.currentTimeMillis() - startTime)
    }

    fun setAlpha(alpha: Float) {
        mesh.alpha = alpha.toDouble()
    }

    fun getWidth(text: String, scale: Double): Float {
        return getWidth(text, false, scale).toFloat()
    }

    fun getWidth(text: String, shadow: Boolean, scale: Double): Double {
        return (font.getWidth(text) + if (shadow) 0.5 else 0.0) * scale
    }

    fun getHeight(shadow: Boolean, scale: Double): Double {
        return (font.getHeight() + if (shadow) 0.5 else 0.0) * scale
    }

    fun render(stack: MatrixStack, text: String, x: Double, y: Double, color: Color, shadow: Boolean, scale: Double): Double {
        try {
            mesh.begin()
            
            val width = if (shadow) {
                font.render(mesh, text, x + 0.5, y + 0.5, SHADOW_COLOR, scale, true)
                font.render(mesh, text, x, y, color, scale, false)
            } else {
                font.render(mesh, text, x, y, color, scale, false)
            }
            
            mesh.end()
            // 修复texture.getId()的调用，使用getGlId()
            GL.bindTexture(font.texture?.getGlId() ?: 0)
            mesh.render(stack)
            
            return width
        } catch (e: IllegalStateException) {
            logger.error("Tessellator not initialized, using fallback rendering", e)
            // 使用Minecraft原生字体渲染作为回退
            val client = MinecraftClient.getInstance()
//            client.textRenderer.draw(stack, text, x.toFloat(), y.toFloat(), color.rgb)
            return client.textRenderer.getWidth(text).toDouble()
        }
    }
}