package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.render.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Nullable
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "renderMainHud", at = @At("HEAD"))
    private void skypvpapi$onRenderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ModuleHUD.INSTANCE.renderMode(context);
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void skypvpapi$onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (ModuleNotification.INSTANCE.isEnabled()) {
            ModuleNotification.INSTANCE.onOverlayMessage(message, tinted);
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void skypvpapi$onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        //准心
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void skypvpapi$onRenderHotbar(CallbackInfo ci) {
        // 在这里处理热键栏渲染逻辑
    }
}
