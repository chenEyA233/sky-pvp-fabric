package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.misc.ModuleTarget;
import cn.cheneya.skypvp.features.module.modules.render.ModuleESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        // 检查实体是否是目标
        boolean isTarget = false;
        if (entity instanceof PlayerEntity && ModuleTarget.INSTANCE.getPlayer().getValue()) {
            isTarget = true;
        } else if (entity instanceof MobEntity && ModuleTarget.INSTANCE.getMobs().getValue()) {
            isTarget = true;
        } else if (entity instanceof AnimalEntity && ModuleTarget.INSTANCE.getAnimals().getValue()) {
            isTarget = true;
        }

        if (isTarget) {
            // Glow模式处理
            if (ModuleESP.INSTANCE.getEnabled() && ModuleESP.INSTANCE.getMode().getValue().equals(ModuleESP._mode.Glow.toString())) {
                cir.setReturnValue(true);
            }
        }
    }
}
