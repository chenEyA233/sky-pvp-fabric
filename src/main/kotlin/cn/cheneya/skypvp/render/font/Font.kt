package cn.cheneya.skypvp.render.font

import cn.cheneya.skypvp.render.Mesh
import net.minecraft.client.texture.AbstractTexture
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTTPackContext
import org.lwjgl.stb.STBTTPackedchar
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import java.awt.Color
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.HashMap

class Font(
    buffer: ByteBuffer,
    private val height: Int,
    private val from: Int,
    charRangeTo: Int,
    textureSize: Int
) {
    var texture: AbstractTexture? = null
    private val scale: Float
    private val ascent: Float
    private val charData: Array<CharData>
    private val widthCache = HashMap<String, Double>()

    init {
        val fontInfo = STBTTFontinfo.create()
        STBTruetype.stbtt_InitFont(fontInfo, buffer)
        
        charData = Array(charRangeTo + 1 - from) { CharData(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }
        val cdata = STBTTPackedchar.create(charData.size)
        val bitmap = BufferUtils.createByteBuffer(textureSize * textureSize)
        
        val packContext = STBTTPackContext.create()
        STBTruetype.stbtt_PackBegin(packContext, bitmap, textureSize, textureSize, 0, 1)
        STBTruetype.stbtt_PackSetOversampling(packContext, 2, 2)
        STBTruetype.stbtt_PackFontRange(packContext, buffer, 0, height.toFloat(), from, cdata)
        STBTruetype.stbtt_PackEnd(packContext)
        
        // 创建纹理
        texture = BufferedTexture(
            textureSize, textureSize, bitmap, 
            BufferedTexture.Format.A, 
            BufferedTexture.Filter.Linear, 
            BufferedTexture.Filter.Linear
        )
        
        scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())
        
        MemoryStack.stackPush().use { stack ->
            val ascent = stack.mallocInt(1)
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, null, null)
            this.ascent = ascent.get(0).toFloat()
        }
        
        // 初始化字符数据
        for (i in charData.indices) {
            val packedChar = cdata.get(i)
            val ipw = 1.0f / textureSize
            val iph = 1.0f / textureSize
            
            charData[i] = CharData(
                packedChar.xoff(),
                packedChar.yoff(),
                packedChar.xoff2(),
                packedChar.yoff2(),
                packedChar.x0() * ipw,
                packedChar.y0() * iph,
                packedChar.x1() * ipw,
                packedChar.y1() * iph,
                packedChar.xadvance()
            )
        }
    }

    fun getWidth(string: String): Double {
        widthCache[string]?.let { return it }
        
        var width = 0.0
        var i = 0
        while (i < string.length) {
            var cp = string[i].code - from
            if (cp == 167 && i + 1 < string.length) {
                i++
            } else {
                if (cp >= charData.size) {
                    cp = 0
                }
                val c = charData[cp]
                width += c.xAdvance.toDouble()
            }
            i++
        }
        
        widthCache[string] = width
        return width
    }

    fun getHeight(): Double {
        return height.toDouble()
    }

    fun render(mesh: Mesh, string: String, x: Double, y: Double, color: Color, scale: Double, shadow: Boolean): Double {
        var currentColor = color
        var currentX = x
        var currentY = y + (ascent * this.scale) * scale
        
        var i = 0
        while (i < string.length) {
            var cp = string[i].code - from
            if (cp == 167 && i + 1 < string.length) {
                // 修复颜色处理逻辑
                val ctrl = string[i + 1]
                // 简化颜色处理，移除对TextColor的依赖
                if (!shadow && ctrl in '0'..'9' || ctrl in 'a'..'f') {
                    // 简单的颜色映射
                    val colorCode = if (ctrl in '0'..'9') ctrl - '0' else ctrl - 'a' + 10
                    val colors = arrayOf(
                        Color(0, 0, 0),
                        Color(0, 0, 170),
                        Color(0, 170, 0),
                        Color(0, 170, 170),
                        Color(170, 0, 0),
                        Color(170, 0, 170),
                        Color(255, 170, 0),
                        Color(170, 170, 170),
                        Color(85, 85, 85),
                        Color(85, 85, 255),
                        Color(85, 255, 85),
                        Color(85, 255, 255),
                        Color(255, 85, 85),
                        Color(255, 85, 255),
                        Color(255, 255, 85),
                        Color(255, 255, 255)
                    )
                    if (colorCode in 0..15) {
                        currentColor = colors[colorCode]
                    }
                }
                i++
            } else {
                if (cp >= charData.size) {
                    cp = 0
                }
                val c = charData[cp]
                
                // 渲染字符
                mesh.quad(
                    mesh.vec2(currentX + c.x0 * scale, currentY + c.y0 * scale).vec2(c.u0.toDouble(), c.v0.toDouble()).color(currentColor).next(),
                    mesh.vec2(currentX + c.x0 * scale, currentY + c.y1 * scale).vec2(c.u0.toDouble(), c.v1.toDouble()).color(currentColor).next(),
                    mesh.vec2(currentX + c.x1 * scale, currentY + c.y1 * scale).vec2(c.u1.toDouble(), c.v1.toDouble()).color(currentColor).next(),
                    mesh.vec2(currentX + c.x1 * scale, currentY + c.y0 * scale).vec2(c.u1.toDouble(), c.v0.toDouble()).color(currentColor).next()
                )
                
                currentX += c.xAdvance * scale
            }
            i++
        }
        
        return currentX
    }

    // 字符数据类
    data class CharData(
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val u0: Float,
        val v0: Float,
        val u1: Float,
        val v1: Float,
        val xAdvance: Float
    )
}

// 缓冲纹理类
class BufferedTexture(
    width: Int,
    height: Int,
    data: ByteBuffer,
    format: Format,
    minFilter: Filter,
    magFilter: Filter
) : AbstractTexture() {
    
    enum class Format {
        A, RGB, RGBA
    }
    
    enum class Filter {
        Nearest, Linear
    }
    
    init {
    }
    
    // AbstractTexture已经有getGlId方法，不需要额外实现getId
}