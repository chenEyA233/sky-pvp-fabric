package cn.cheneya.skypvp.render.gui.guis

import cn.cheneya.skypvp.skypvp
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.net.InetSocketAddress
import kotlin.math.sin

class RegisterGui : Screen(Text.literal("fuckyou!")) {
    private lateinit var usernameField: TextFieldWidget
    private lateinit var passwordField: TextFieldWidget
    private lateinit var cardKeyField: TextFieldWidget
    private lateinit var registerButton: ButtonWidget
    private lateinit var backButton: ButtonWidget
    private var animationTicks = 0f
    private var errorMessage: String? = null
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null

    override fun init() {
        super.init()

        val centerX = width / 2
        val centerY = height / 2 - 30
        val fieldWidth = 200
        val fieldHeight = 20
        val spacing = 24

        // 用户名输入框
        usernameField = TextFieldWidget(textRenderer, centerX - fieldWidth / 2, centerY - spacing, fieldWidth, fieldHeight, Text.literal("用户名"))
        usernameField.setMaxLength(32)
        addDrawableChild(usernameField)

        // 密码输入框
        passwordField = TextFieldWidget(textRenderer, centerX - fieldWidth / 2, centerY, fieldWidth, fieldHeight, Text.literal("密码"))
        passwordField.setMaxLength(32)
        passwordField.setRenderTextProvider { input, _ -> Text.literal(String(CharArray(input.length) { '•' })).asOrderedText() }
        addDrawableChild(passwordField)

        // 卡密输入框
        cardKeyField = TextFieldWidget(textRenderer, centerX - fieldWidth / 2, centerY + spacing, fieldWidth, fieldHeight, Text.literal("卡密"))
        cardKeyField.setMaxLength(32)
        addDrawableChild(cardKeyField)

        // 注册按钮
        registerButton = ButtonWidget.builder(Text.literal("注册")) {
            attemptRegister()
        }
            .dimensions(centerX - fieldWidth / 2, centerY + spacing * 2, fieldWidth, fieldHeight)
            .build()
        addDrawableChild(registerButton)

        // 返回按钮
        backButton = ButtonWidget.builder(Text.literal("返回登录")) {
            client?.setScreen(LoginGui())
        }
            .dimensions(centerX - fieldWidth / 2, centerY + spacing * 3, fieldWidth, fieldHeight)
            .build()
        addDrawableChild(backButton)

        setInitialFocus(usernameField)
    }

    private fun attemptRegister() {
        val username = usernameField.text.trim()
        val password = passwordField.text.trim()
        val cardKey = cardKeyField.text.trim()

        if (username.isEmpty() || password.isEmpty() || cardKey.isEmpty()) {
            errorMessage = "请填写所有字段"
            return
        }

        try {
            // 连接到服务器，设置5秒超时
            socket = Socket().apply {
                connect(InetSocketAddress(skypvp.serverAddress, skypvp.serverPort), 5000)
            }
            outputStream = DataOutputStream(socket?.getOutputStream())
            inputStream = DataInputStream(socket?.getInputStream())
            skypvp.logger.info("成功连接到服务器 ${skypvp.serverAddress}:${skypvp.serverPort}")

            // 发送注册请求
            outputStream?.writeUTF("REGISTER")  // 请求类型
            outputStream?.writeUTF(username)   // 用户名
            outputStream?.writeUTF(password)   // 密码
            outputStream?.writeUTF("") // 邮箱(已移除)
            outputStream?.flush()

            // 接收服务器响应
            val success = inputStream?.readBoolean() ?: false
            val message = inputStream?.readUTF() ?: "未知错误"
            if (!success) {
                throw Exception("注册失败: $message")
            }

            // 发送注册请求
            outputStream?.writeUTF("REGISTER")
            outputStream?.writeUTF(username)
            outputStream?.writeUTF(password)
            outputStream?.writeUTF(cardKey)
            outputStream?.flush()

            // 接收服务器响应
            val response = inputStream?.readUTF()
            when (response) {
                "SUCCESS" -> {
                    val userGroup = inputStream?.readUTF() ?: "user"
                    errorMessage = null
                    client?.player?.sendMessage(Text.of("§a注册成功"), false)
                    client?.player?.sendMessage(Text.of("§6您的用户组: $userGroup"), false)
                    client?.setScreen(LoginGui())
                }
                "FAILURE" -> {
                    val errorMsg = inputStream?.readUTF()
                    errorMessage = when (errorMsg) {
                        "USERNAME_EXISTS" -> "用户名已存在"
                        "INVALID_CARD_KEY" -> "卡密无效或已被使用"
                        else -> "注册失败"
                    }
                }
                else -> {
                    errorMessage = "服务器响应异常"
                }
            }
        } catch (e: Exception) {
            errorMessage = "无法连接到服务器"
        } finally {
            socket?.close()
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        animationTicks += delta * 0.5f
        renderBackground(context, mouseX, mouseY, delta)

        // 绘制标题
        val title = "注册SKYPVP账户"
        val titleX = width / 2 - textRenderer.getWidth(title) / 2
        val titleY = height / 2 - 80
        context.drawText(textRenderer, title, titleX, titleY, 0xFFFFFFFF.toInt(), true)

        // 绘制输入框标签
        context.drawText(textRenderer, "用户名:", usernameField.x - 60, usernameField.y + 6, 0xFFFFFFFF.toInt(), true)
        context.drawText(textRenderer, "密码:", passwordField.x - 60, passwordField.y + 6, 0xFFFFFFFF.toInt(), true)
        context.drawText(textRenderer, "卡密:", cardKeyField.x - 60, cardKeyField.y + 6, 0xFFFFFFFF.toInt(), true)

        // 绘制错误信息
        errorMessage?.let {
            val errorX = width / 2 - textRenderer.getWidth(it) / 2
            val errorY = height / 2 + 100
            context.drawText(textRenderer, it, errorX, errorY, 0xFFFF0000.toInt(), true)
        }

        // 绘制版权信息
        val copyrightText = "Copyright SKY PVP Team!"
        val copyrightX = width / 2 - textRenderer.getWidth(copyrightText) / 2
        val copyrightY = height - 10
        context.drawText(textRenderer, copyrightText, copyrightX, copyrightY, 0xFFFFFFFF.toInt(), true)

        super.render(context, mouseX, mouseY, delta)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // 使用与TitleGui类似的背景渲染逻辑，但颜色略有不同
        context.fill(0, 0, width, height, 0xFF000000.toInt())

        val gridSize = 40
        val gridColor = 0x22FFFFFF

        for (y in 0 until height step gridSize) {
            val alpha = (0x33 * (1.0f + 0.5f * sin(y * 0.01f + animationTicks * 0.2f))).toInt()
            val lineColor = gridColor and 0xFFFFFF or (alpha shl 24)
            context.fill(0, y, width, y + 1, lineColor)
        }

        for (x in 0 until width step gridSize) {
            val alpha = (0x33 * (1.0f + 0.5f * sin(x * 0.01f + animationTicks * 0.2f))).toInt()
            val lineColor = gridColor and 0xFFFFFF or (alpha shl 24)
            context.fill(x, 0, x + 1, height, lineColor)
        }
    }
}
