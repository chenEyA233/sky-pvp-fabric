package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.render.ModuleNameProtect;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

    @ModifyArg(
        method = "drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawLayer(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)F"),
        index = 0
    )
    private String injectNameProtectA(String text) {
        return ModuleNameProtect.INSTANCE.replace(text);
    }

    @Redirect(
        method = "drawLayer(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)F",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/text/OrderedText;accept(Lnet/minecraft/text/CharacterVisitor;)Z")
    )
    private boolean injectNameProtectB(OrderedText orderedText, CharacterVisitor visitor) {
        if (ModuleNameProtect.INSTANCE.isNameProtectActive()) {
            final OrderedText wrapped = new ModuleNameProtect.NameProtectOrderedText(orderedText);
            return wrapped.accept(visitor);
        }

        return orderedText.accept(visitor);
    }

    @ModifyArg(
        method = "getWidth(Ljava/lang/String;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextHandler;getWidth(Ljava/lang/String;)F"),
        index = 0
    )
    private @Nullable String injectNameProtectWidthA(@Nullable String text) {
        if (text != null && ModuleNameProtect.INSTANCE.isNameProtectActive()) {
            return ModuleNameProtect.INSTANCE.replace(text);
        }

        return text;
    }

    @ModifyArg(
        method = "getWidth(Lnet/minecraft/text/OrderedText;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextHandler;getWidth(Lnet/minecraft/text/OrderedText;)F"),
        index = 0
    )
    private OrderedText injectNameProtectWidthB(OrderedText text) {
        if (ModuleNameProtect.INSTANCE.isNameProtectActive()) {
            return new ModuleNameProtect.NameProtectOrderedText(text);
        }

        return text;
    }
}
