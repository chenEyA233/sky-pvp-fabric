package cn.cheneya.skypvp.inject.mixin;

import cn.cheneya.skypvp.api.utils.client.misc.WindowMisc;
import cn.cheneya.skypvp.features.module.modules.render.ModuleHUD;
import cn.cheneya.skypvp.skypvp;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Mixin(MinecraftClient.class)
public class WindowsMixin {
    private static String window_icon = WindowMisc.icon;
    private static String window_text = WindowMisc.WindowText;

    @Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true)
    private void modifyWindowTitle(CallbackInfoReturnable<String> cir) {
            if (skypvp.Verification) {
                window_text = WindowMisc.WindowText;
            } else {
                window_text = WindowMisc.SilenceText;
            }
        cir.setReturnValue(window_text);
    }
    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void setWindowIcon(CallbackInfo ci) {
        try {
            if (skypvp.Verification) {
                window_icon = WindowMisc.icon;
            } else {
                window_icon = WindowMisc.Slienceicon;
            }
            InputStream iconStream= getClass().getClassLoader().getResourceAsStream(window_icon);
            if (iconStream == null) {
                return;
            }

            try {
                // 读取图像
                BufferedImage image = ImageIO.read(iconStream);

                if (image == null) {
                    return;
                }

                // 获取图像尺寸，并限制最大尺寸以避免内存问题
                int width = Math.min(image.getWidth(), 128);
                int height = Math.min(image.getHeight(), 128);

                // 如果图像太大，缩小它
                if (width != image.getWidth() || height != image.getHeight()) {
                    BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    resized.getGraphics().drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
                    image = resized;
                }

                // 在堆上分配内存，而不是栈上
                ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

                // 将图像数据转换为RGBA格式，使用更高效的方法
                int[] pixels = new int[width * height];
                image.getRGB(0, 0, width, height, pixels, 0, width);

                for (int i = 0; i < pixels.length; i++) {
                    int pixel = pixels[i];
                    buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                    buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                    buffer.put((byte) (pixel & 0xFF));         // Blue
                    buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
                }
                buffer.flip();

                // 创建GLFW图像对象
                GLFWImage glfwImage = GLFWImage.create();
                glfwImage.width(width);
                glfwImage.height(height);
                glfwImage.pixels(buffer);

                // 创建图像缓冲区
                GLFWImage.Buffer images = GLFWImage.create(1);
                images.put(0, glfwImage);

                // 设置窗口图标
                long windowHandle = ((MinecraftClient) (Object) this).getWindow().getHandle();
                GLFW.glfwSetWindowIcon(windowHandle, images);
            } finally {
                // 确保输入流被关闭
                try {
                    iconStream.close();
                } catch (Exception e) {
                    // 忽略关闭流时的错误
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}