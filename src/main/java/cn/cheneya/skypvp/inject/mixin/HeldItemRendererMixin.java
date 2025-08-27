package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.render.ModuleAnimations;
import cn.cheneya.skypvp.features.module.modules.render.ModuleBlockingSwing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    @Final
    private static float EQUIP_OFFSET_TRANSLATE_Y;
    
    /**
     * 隐藏第一人称视角下的副手物品
     */
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void hideShield(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                            Hand hand, float swingProgress, ItemStack item, float equipProgress,
                            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                            CallbackInfo ci) {
        if(ModuleBlockingSwing.INSTANCE.getEnabled() && ModuleBlockingSwing.INSTANCE.getShouldHideOffhand().getValue()) {
            if (hand == Hand.OFF_HAND && ModuleBlockingSwing.INSTANCE.shouldHideOffhand(player, item.getItem())) {
                ci.cancel();
            }
        }
    }

    /**
     * 注入物品渲染，应用自定义变换
     */
    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void hookRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ModuleAnimations animations = ModuleAnimations.INSTANCE;
        
        if (animations.getEnabled()) {
            boolean isMainHand = Hand.MAIN_HAND == hand;
            
            if (isMainHand) {
                animations.applyTransformations(
                    matrices,
                    animations.getMainHandX().getValue().floatValue(),
                    animations.getMainHandY().getValue().floatValue(),
                    animations.getMainHandItemScale().getValue().floatValue(),
                    animations.getMainHandPositiveX().getValue().floatValue(),
                    animations.getMainHandPositiveY().getValue().floatValue(),
                    animations.getMainHandPositiveZ().getValue().floatValue()
                );
            } else {
                animations.applyTransformations(
                    matrices,
                    animations.getOffHandX().getValue().floatValue(),
                    animations.getOffHandY().getValue().floatValue(),
                    animations.getOffHandItemScale().getValue().floatValue(),
                    animations.getOffHandPositiveX().getValue().floatValue(),
                    animations.getOffHandPositiveY().getValue().floatValue(),
                    animations.getOffHandPositiveZ().getValue().floatValue()
                );
            }
        }
    }

    /**
     * 注入剑格挡动画
     * 
     * 参考水影的代码实现，处理第一人称视角下的剑格挡动画
     */
    @Inject(method = "renderFirstPersonItem",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/item/consume/UseAction;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2, shift = At.Shift.AFTER))
    private void transformLegacyBlockAnimations(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                                                Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                                MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                                CallbackInfo ci) {
        // 检查是否应该应用动画：模块启用 + 手持剑
        boolean shouldAnimate = ModuleBlockingSwing.INSTANCE.getEnabled() && ModuleBlockingSwing.canSwing;

        if (shouldAnimate && item.getItem() instanceof SwordItem) {
            final Arm arm = (hand == Hand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
            
            ModuleAnimations animations = ModuleAnimations.INSTANCE;
            if (animations.getEnabled()) {
                // 根据选择的动画类型应用不同的动画
                if (animations.getBlockingAnimation().getValue() == ModuleAnimations.BlockStyle.Pushdown.toString()) {
                    animations.applyPushdownAnimation(matrices, arm, equipProgress, swingProgress);
                } else {
                    // 默认使用1.7风格的动画
                    animations.applyOneSevenAnimation(matrices, arm, equipProgress, swingProgress);
                }
                return;
            }
            
            // 如果动画模块未启用，则使用默认的1.7风格动画
            animations.applyOneSevenAnimation(matrices, arm, equipProgress, swingProgress);
        }
    }

    /**
     * 修改装备偏移
     */
    @ModifyArg(method = "applyEquipOffset", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), index = 1)
    private float injectDisableEquipOffset(float y) {
        ModuleAnimations animations = ModuleAnimations.INSTANCE;
        if (animations.getEnabled() && animations.getIgnoreBlocking().getValue()) {
            return EQUIP_OFFSET_TRANSLATE_Y;
        }
        return y;
    }
}