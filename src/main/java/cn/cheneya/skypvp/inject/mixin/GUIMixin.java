package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.skypvp;
import cn.cheneya.skypvp.render.gui.guis.LoginGui;
import cn.cheneya.skypvp.render.gui.guis.TitleGui;
import cn.cheneya.skypvp.api.utils.LoginStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class GUIMixin {
    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    private void replaceWithModernTitleScreen(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!skypvp.Verification == true) {
            client.setScreen(new TitleGui());
            ci.cancel();
            return;
        }

        Screen screenToShow;
        if (LoginStateManager.isFirstLaunch()) {
            screenToShow = new LoginGui();
            LoginStateManager.setFirstLaunch(false);
        } else {
            screenToShow = new TitleGui();
        }
        client.setScreen(screenToShow);
        ci.cancel();
    }
}
