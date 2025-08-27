package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.render.ModuleAnimations;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = LivingEntity.class, priority = 999)
public abstract class LivingEntityCompatibilityMixin {

    @ModifyConstant(method = "getHandSwingDuration", constant = @Constant(intValue = 6), require = 0)
    private int hookSwingSpeed(int constant) {
        return ModuleAnimations.INSTANCE.getEnabled() ? ModuleAnimations.INSTANCE.getSwingDuration().getValue().intValue() : constant;
    }

}
