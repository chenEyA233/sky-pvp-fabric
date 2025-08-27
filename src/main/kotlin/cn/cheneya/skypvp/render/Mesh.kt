package cn.cheneya.skypvp.render

import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import java.awt.Color

abstract class Mesh {
    var alpha = 1.0
    protected var building = false
    protected var vertices = 0
    
    enum class Attrib {
        Vec2, Vec3, Color
    }
    
    abstract fun begin()
    abstract fun end()
    abstract fun render(matrices: MatrixStack)
    
    // 将这些方法改为非final，以便子类可以覆盖
    open fun vec2(x: Double, y: Double): Mesh {
        return this
    }
    
    open fun vec3(x: Double, y: Double, z: Double): Mesh {
        return this
    }
    
    open fun color(color: Color): Mesh {
        return this
    }
    
    fun next(): Int {
        vertices++
        return vertices - 1
    }
    
    fun quad(i1: Int, i2: Int, i3: Int, i4: Int) {
        triangle(i1, i2, i3)
        triangle(i3, i4, i1)
    }
    
    fun triangle(i1: Int, i2: Int, i3: Int) {
        // 实现三角形绘制
    }
}