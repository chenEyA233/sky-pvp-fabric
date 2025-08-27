package cn.cheneya.skypvp.render

import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import java.awt.Color
import java.util.*

class ShaderMesh(
    private val shader: Int,
    private val drawMode: DrawMode,
    vararg attributes: Mesh.Attrib
) : Mesh() {
    private val attributes: Array<out Mesh.Attrib>
    private val tessellator: Tessellator
    private var bufferBuilder: BufferBuilder? = null
    private val positions = ArrayList<Double>()
    private val colors = ArrayList<Int>()
    
    init {
        this.attributes = attributes
        this.tessellator = Tessellator.getInstance()
    }
    
    override fun begin() {
        if (building) return
        building = true
        vertices = 0
        positions.clear()
        colors.clear()
        
        // 在Fabric 1.21.4中，Tessellator.begin返回BufferBuilder
        bufferBuilder = tessellator.begin(
            VertexFormat.DrawMode.QUADS, // 使用QUADS代替Triangles
            VertexFormats.POSITION_TEXTURE_COLOR
        )
    }
    
    override fun end() {
        if (!building) return
        building = false
        // 在Fabric 1.21.4中，不需要调用tessellator.draw()
        // BufferBuilder会自动处理绘制
        bufferBuilder = null
    }
    
    override fun render(matrices: MatrixStack) {
        // 使用着色器渲染
        // 在实际实现中，这里需要使用Fabric的渲染API
    }
    
    // 使用open方法而不是override
    override fun vec2(x: Double, y: Double): Mesh {
        if (!building) return this
        positions.add(x)
        positions.add(y)
        return this
    }
    
    override fun vec3(x: Double, y: Double, z: Double): Mesh {
        if (!building) return this
        positions.add(x)
        positions.add(y)
        positions.add(z)
        return this
    }
    
    override fun color(color: Color): Mesh {
        if (!building) return this
        val alpha = (color.alpha * this.alpha).toInt().coerceIn(0, 255)
        colors.add(color.red)
        colors.add(color.green)
        colors.add(color.blue)
        colors.add(alpha)
        return this
    }
}