/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: shanyang
 */
package cn.shanyang.skypvp

import java.awt.event.*
import javax.swing.*


object dy4m {
    fun showWindow() {
        SwingUtilities.invokeLater(Runnable {
            try {
                val icon = ImageIcon("D:/ide/sky/src/main/resources/assets/skypvp/textures/client/icon/d3Dvw9Dxo2.png")

                // 创建无边框窗口
                val frame = JFrame()
                frame.setUndecorated(true)
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)


                // 设置窗口大小与图片一致
                frame.setSize(icon.getIconWidth(), icon.getIconHeight())
                frame.setLocationRelativeTo(null) // 居中显示


                // 添加图片标签
                val label = JLabel(icon)
                frame.add(label)


                // 显示窗口
                frame.setVisible(true)


                val timer = Timer(5000, ActionListener { e: ActionEvent? -> frame.dispose() })
                timer.setRepeats(false)
                timer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // 独立运行测试
        SwingUtilities.invokeLater(Runnable { showWindow() })
    }
}
