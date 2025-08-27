package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.interfaces.DrawContextAddition;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.GuiAtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements DrawContextAddition {
    @Unique
    private static final Logger LOGGER = LogManager.getLogger("SkyPVP/DrawContextMixin");

    @Final
    private VertexConsumerProvider.Immediate vertexConsumers;

    @Shadow @Final
    private GuiAtlasManager guiAtlasManager;

    @Unique
    public abstract Matrix4f getMatrices();

    @Override
    public void skypvpapi$drawTexture(Function<Identifier, RenderLayer> renderLayers, Identifier texture, float x, float y, int width, int height) {
        LOGGER.info("Drawing texture: {}", texture);
        try {
            Sprite sprite = guiAtlasManager.getSprite(texture);
            float o = 1 / 32768f;
            skypvpapi$drawTexturedQuad(renderLayers, sprite.getAtlasId(), x, x + width, y, y + height, sprite.getMinU() + o, sprite.getMaxU() + o, sprite.getMinV() - o, sprite.getMaxV() - o, -1);
        } catch (Exception e) {
            LOGGER.error("Error drawing texture: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void skypvpapi$drawTexturedQuad(Function<Identifier, RenderLayer> renderLayers, Identifier texture, float x1, float x2, float y1, float y2, float u1, float u2, float v1, float v2, int color) {
        LOGGER.info("Drawing textured quad: {}", texture);
        try {
            RenderLayer renderLayer = renderLayers.apply(texture);
            Matrix4f matrix4f = getMatrices();
            VertexConsumer bufferBuilder = vertexConsumers.getBuffer(renderLayer);
            bufferBuilder.vertex(matrix4f, x1, y1, 0f).texture(u1, v1).color(color);
            bufferBuilder.vertex(matrix4f, x1, y2, 0f).texture(u1, v2).color(color);
            bufferBuilder.vertex(matrix4f, x2, y2, 0f).texture(u2, v2).color(color);
            bufferBuilder.vertex(matrix4f, x2, y1, 0f).texture(u2, v1).color(color);
        } catch (Exception e) {
            LOGGER.error("Error drawing textured quad: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
