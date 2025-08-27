package cn.cheneya.skypvp.render

import net.minecraft.client.render.VertexFormat

enum class DrawMode(val glMode: VertexFormat.DrawMode) {
    Lines(VertexFormat.DrawMode.LINES),
    LineStrip(VertexFormat.DrawMode.LINE_STRIP),
    Triangles(VertexFormat.DrawMode.TRIANGLES),
    TriangleStrip(VertexFormat.DrawMode.TRIANGLE_STRIP),
    TriangleFan(VertexFormat.DrawMode.TRIANGLE_FAN),
    Quads(VertexFormat.DrawMode.QUADS)
}