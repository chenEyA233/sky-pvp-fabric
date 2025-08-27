package cn.cheneya.skypvp.interfaces;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import java.util.function.Function;

/**
 * Addition to {@link net.minecraft.client.gui.DrawContext}.
 */
public interface DrawContextAddition {

    void skypvpapi$drawTexture(Function<Identifier, RenderLayer> renderLayers, Identifier texture, float x, float y, int width, int height);

    void skypvpapi$drawTexturedQuad(Function<Identifier, RenderLayer> renderLayers, Identifier texture, float x1, float x2, float y1, float y2, float u1, float u2, float v1, float v2, int color);
}
