package cn.shanyang.skypvp.xyz;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ililillllilil {
    public static void showWindow() {
        SwingUtilities.invokeLater(() -> {
            try {

                ImageIcon icon = null;
                
                // 尝试多种方式加载图标
                try {
                    // 1. 使用类加载器加载资源
                    System.out.println("[SkyPVP] 尝试使用类加载器加载图标");
                    java.net.URL iconUrl = ililillllilil.class.getResource("/assets/skypvp/textures/client/icon/d3Dvw9Dxo2.png");
                    if (iconUrl != null) {
                        icon = new ImageIcon(iconUrl);
                    } else {
                        String[] possiblePaths = {
                            "/assets/skypvp/textures/client/icon/d3Dvw9Dxo2.png"
                        };
                        
                        for (String path : possiblePaths) {
                            File file = new File(path);
                            if (file.exists()) {
                                icon = new ImageIcon(file.getAbsolutePath());
                                break;
                            }
                        }
                        
                        // 3. 如果还是找不到，尝试提取资源到临时文件
                        if (icon == null) {
                            System.out.println("[SkyPVP] 尝试提取资源到临时文件");
                            InputStream is = ililillllilil.class.getResourceAsStream("/assets/skypvp/textures/client/icon/d3Dvw9Dxo2.png");
                            if (is != null) {
                                Path tempFile = Files.createTempFile("skypvp_icon", ".png");
                                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                                is.close();
                                System.out.println("[SkyPVP] 提取图标到临时文件: " + tempFile);
                                icon = new ImageIcon(tempFile.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[SkyPVP] 加载图标时出错: " + e.getMessage());
                    e.printStackTrace();
                }
                
                // 如果所有方法都失败，使用默认图标
                if (icon == null || icon.getIconWidth() <= 0) {
                    System.out.println("[SkyPVP] 使用默认图标");
                    // 创建一个简单的彩色图标作为后备
                    int width = 400;
                    int height = 200;
                    java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                        width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics g = image.getGraphics();
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, width, height);
                    g.setColor(Color.RED);
                    g.setFont(new Font("Arial", Font.BOLD, 24));
                    g.drawString("SkyPVP", 150, 100);
                    g.dispose();
                    icon = new ImageIcon(image);
                }
                
                // 创建无边框窗口
                JFrame frame = new JFrame("SkyPVP");
                frame.setUndecorated(true);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setAlwaysOnTop(true); // 确保窗口总是在最前面
                
                // 设置窗口大小与图片一致
                frame.setSize(icon.getIconWidth(), icon.getIconHeight());
                frame.setLocationRelativeTo(null); // 居中显示
                
                // 添加图片标签
                JLabel label = new JLabel(icon);
                frame.add(label);
                
                // 显示窗口
                System.out.println("[SkyPVP] 显示窗口");
                frame.setVisible(true);
                
                // 5秒后自动关闭
                Timer timer = new Timer(5000, e -> {
                    System.out.println("[SkyPVP] 关闭窗口");
                    frame.dispose();
                });
                timer.setRepeats(false);
                timer.start();
                
                System.out.println("[SkyPVP] 窗口创建完成，将在5秒后自动关闭");
            } catch (Exception e) {
                System.err.println("[SkyPVP] 显示窗口时出错: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        // 独立运行测试
        showWindow();
    }
}
