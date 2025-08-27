package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.render.ModuleNameProtect;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        if (cir.getReturnValue() != null) {
            String original = cir.getReturnValue().getString();

            // 应用文本替换（包括名称保护和IRC前缀简化）
            String modified = ModuleNameProtect.INSTANCE.replace(original);

            // 只有当文本被修改时才替换
            if (!original.equals(modified)) {
                cir.setReturnValue(Text.of(modified));
            }
        }
    }
}
