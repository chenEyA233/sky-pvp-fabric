package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.render.ModuleBlockingSwing;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.item.ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
import static net.minecraft.item.ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;

/**
 * 物品渲染器Mixin
 * 
 * 用于处理第三人称视角下的物品渲染
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    /**
     * 注入物品渲染方法，处理第三人称视角下的剑格挡动画
     * 
     * 当玩家在第三人称视角下使用剑格挡时，隐藏副手物品
     */
    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V", at = @At("HEAD"), cancellable = true)
    private void hookRenderItem(LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        if(ModuleBlockingSwing.INSTANCE.getEnabled() && ModuleBlockingSwing.INSTANCE.getShouldHideOffhand().getValue()){
            if (renderMode == (leftHanded ? THIRD_PERSON_LEFT_HAND : THIRD_PERSON_RIGHT_HAND) && entity instanceof PlayerEntity player) {
                // 如果应该隐藏副手物品，则取消渲染
                if (ModuleBlockingSwing.INSTANCE.shouldHideOffhand(player, item.getItem())) {
                    ci.cancel();
                }
            }
        }
    }
}