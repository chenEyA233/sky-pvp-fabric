package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.utils.PacketUtil;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    /**
     * 拦截客户端发送的数据包
     */
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
        // 如果PacketUtil处理返回false，则取消数据包发送    
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!PacketUtil.INSTANCE.handleSendPacket(packet)) {
            ci.cancel();
        }
    }

    /**
     * 拦截客户端接收的数据包
     */
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onReceivePacket(Packet<?> packet, net.minecraft.network.listener.PacketListener listener, CallbackInfo ci) {
        // 如果PacketUtil处理返回false，则取消数据包处理
        if (!PacketUtil.INSTANCE.handleReceivePacket(packet)) {
            ci.cancel();
        }
    }
}
