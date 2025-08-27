package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.features.module.modules.render.ModuleItemPhysic;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    @Shadow @Final
    private Random random;

    /**
     * 注入updateRenderState方法，存储物品实体引用
     */
    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(ItemEntity itemEntity, ItemEntityRenderState state, float partialTicks, CallbackInfo ci) {
        // 我们不需要在这里做任何事情，只是确保我们可以在render方法中访问itemEntity
    }

    /**
     * 注入渲染方法，修改物品的渲染方式以实现物理掉落效果
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onRender(ItemEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (state.itemRenderState.isEmpty()) {
            return;
        }

        // 我们无法直接从state获取ItemEntity，所以我们使用state中的数据来模拟物理效果
        // 这里我们根据物品的age和uniqueOffset来判断物品是否在地面上
        // 如果age大于一定值，我们假设物品已经落地
        boolean simulateOnGround = state.age > 20.0F;

        // 取消原版渲染
        ci.cancel();

        // 保存矩阵状态
        matrices.push();

        // 计算物品在地面上的旋转角度
        float rotationFactor = 0.0F;

        // 无论物品是否在地面上，都应用平躺效果
        if (ModuleItemPhysic.INSTANCE.getEnabled()) {
            // 物理效果逻辑
            matrices.push();
            // 先平移旋转点到模型中心
            matrices.translate(0.0D, -0.5D, 0.0D);
            // 再旋转90度平躺
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            // 恢复模型位置
            matrices.translate(0.0D, 0.5D, 0.002D);
            if (!simulateOnGround) {
                float bobbing = MathHelper.sin(state.age / 10.0F + state.uniqueOffset) * 0.05F + 0.05F;
                matrices.translate(0.0F, 0.0F, -bobbing);
            }
            // 添加Y轴旋转修正
            float rotation = ItemEntity.getRotation(state.age, state.uniqueOffset);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rotation));
            ItemEntityRenderer.renderStack(matrices, vertexConsumers, light, state, this.random);
            matrices.pop();
        } else {
            // 完全恢复原版渲染逻辑
            matrices.push();
            float g = MathHelper.sin(state.age / 10.0F + state.uniqueOffset) * 0.1F + 0.1F;
            float h = state.itemRenderState.getTransformation().scale.y();
            matrices.translate(0.0F, g + 0.25F * h, 0.0F);
            float j = ItemEntity.getRotation(state.age, state.uniqueOffset);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(j));
            ItemEntityRenderer.renderStack(matrices, vertexConsumers, light, state, this.random);
            matrices.pop();
        }


        // 恢复矩阵状态
        matrices.pop();
    }

}
