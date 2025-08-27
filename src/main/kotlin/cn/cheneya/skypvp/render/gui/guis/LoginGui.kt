package cn.cheneya.skypvp.render.gui.guis

import cn.cheneya.skypvp.skypvp
import cn.cheneya.skypvp.skypvp.Verification
import cn.cheneya.skypvp.utils.WindowsUtil
import net.skypvpteam.skypvp.api.gui
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import kotlin.math.sin
import java.io.File
import java.net.Socket
import java.net.InetSocketAddress
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.Properties

@gui("Login gui")
class LoginGui : Screen(Text.literal("SKYPVP Login Gui")) {
    companion object {
        @Volatile
        var currentUsername: String = "FreeUser"
            private set
            
        @Volatile
        var currentUserGroup: String? = "Error"
            private set
    }
    val logger = skypvp.logger
    private lateinit var usernameField: TextFieldWidget
    private lateinit var passwordField: TextFieldWidget
    private lateinit var loginButton: ButtonWidget
    private lateinit var rememberMeButton: ButtonWidget
    private lateinit var refreshButton: ButtonWidget
    private lateinit var exitButton: ButtonWidget
    private var isRememberPassword = false
    private var errorMessage: String? = null
    private var animationTicks = 0f
    private var isLoggingIn = false
    private var loginStartTime: Long = 0
    private val loginTimeoutSeconds = 10
    private var isLoginTimedOut = false
    private var isServerConnected = false
    private var serverConnectedTime: Long = 0
    private val serverConnectedDisplaySeconds = 5
    private val serverAddress = skypvp.serverAddress
    private val serverPort = skypvp.serverPort
    private var heartbeatThread: Thread? = null
    private var keepAlive = true
    private val heartbeatInterval = 30000L // 30秒
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null
    private val configDir = File("skypvp/config")
    private val credentialsFile = File(configDir, "LoginUserAndPassWord.cfg")
    private val properties = Properties()

    override fun init() {
        super.init()

        // 确保配置目录存在
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        // 加载保存的凭据和服务器配置
        if (credentialsFile.exists()) {
            credentialsFile.inputStream().use {
                properties.load(it)
            }
        }

        val centerX = width / 2
        val centerY = height / 2 - 30
        val fieldWidth = 200
        val fieldHeight = 20
        val spacing = 24

        // 用户名输入框
        usernameField = TextFieldWidget(textRenderer, centerX - fieldWidth / 2, centerY - spacing, fieldWidth, fieldHeight, Text.literal("用户名"))
        usernameField.setMaxLength(32)
        usernameField.text = properties.getProperty("username", "")
        addDrawableChild(usernameField)

        // 密码输入框
        passwordField = TextFieldWidget(textRenderer, centerX - fieldWidth / 2, centerY + spacing, fieldWidth, fieldHeight, Text.literal("密码"))
        passwordField.setMaxLength(32)
        passwordField.isVisible = true
        passwordField.setRenderTextProvider { input, _ -> Text.literal(String(CharArray(input.length) { '•' })).asOrderedText() }

        // 如果记住了密码，则填充密码字段
        if (properties.getProperty("remember", "false").toBoolean()) {
            passwordField.text = properties.getProperty("password", "")
        }

        addDrawableChild(passwordField)

        // 记住我复选框 - 使用按钮代替复选框
        val isRemembered = properties.getProperty("remember", "false").toBoolean()
        isRememberPassword = isRemembered

        rememberMeButton = ButtonWidget.builder(
            Text.literal(if (isRemembered) "✓ 记住密码" else "□ 记住密码"),
            ButtonWidget.PressAction { button ->
                isRememberPassword = !isRememberPassword
                (button as ButtonWidget).setMessage(
                    Text.literal(if (isRememberPassword) "✓ 记住密码" else "□ 记住密码")
                )
            }
        )
            .position(centerX - fieldWidth / 2, centerY + spacing * 2)
            .size(fieldWidth, 20)
            .build()

        addDrawableChild(rememberMeButton)

        // 登录按钮
        loginButton = ButtonWidget.builder(Text.literal("登录")) {
            attemptLogin()
        }
            .dimensions(centerX - fieldWidth / 2, centerY + spacing * 3, fieldWidth, fieldHeight)
            .build()
        addDrawableChild(loginButton)

        // 注册按钮
        val registerButton = ButtonWidget.builder(Text.literal("注册账号")) {
            client?.setScreen(RegisterGui())
        }
            .dimensions(centerX - fieldWidth / 2, centerY + spacing * 4, fieldWidth, fieldHeight)
            .build()
        addDrawableChild(registerButton)

        // 刷新按钮（重新连接服务器）
        refreshButton = ButtonWidget.builder(Text.literal("刷新连接")) {
            refreshServerConnection()
        }
            .dimensions(centerX - fieldWidth / 2, centerY + spacing * 5, fieldWidth / 2 - 5, fieldHeight)
            .build()
        addDrawableChild(refreshButton)

        // 退出游戏按钮
        exitButton = ButtonWidget.builder(Text.literal("退出游戏")) {
            client?.scheduleStop()
        }
            .dimensions(centerX + 5, centerY + spacing * 5, fieldWidth / 2 - 5, fieldHeight)
            .build()
        addDrawableChild(exitButton)

        // 设置初始焦点
        setInitialFocus(usernameField)
    }

    private fun attemptLogin() {
        val username = usernameField.text
        val password = passwordField.text

        if (username.isEmpty()) {
            errorMessage = "请输入用户名"
            return
        }

        if (password.isEmpty()) {
            errorMessage = "请输入密码"
            return
        }

        // 设置登录中状态
        isLoggingIn = true
        loginButton.active = false
        loginButton.message = Text.literal("登录中...")
        errorMessage = null

        // 记录登录开始时间并重置超时标志
        loginStartTime = System.currentTimeMillis()
        isLoginTimedOut = false

        // 创建一个新线程来处理网络请求，避免阻塞主线程
        Thread {
            try {
                // 验证凭据
                val isValid = validateCredentials(username, password)

                // 在主线程中更新UI
                client?.execute {
                    if (isValid) {
                        // 保存凭据（如果选择了记住密码）
                        saveCredentials(username, password, isRememberPassword)
                        currentUsername = username

                        // 登录成功，根据Verification开关决定是否显示加载界面
                        if (Verification == true) {
                            WindowsUtil.info("登录成功!")
                            client?.setScreen(TitleGui())
                        } else {
                            WindowsUtil.info("登录成功!")
                            client?.setScreen(TitleGui())
                        }
                    } else {
                        // 验证失败的错误消息已在validateCredentials方法中设置
                        if (errorMessage == null) {
                            errorMessage = "用户名或密码错误"
                        }

                        // 重置登录按钮状态
                        isLoggingIn = false
                        loginButton.active = true
                        loginButton.message = Text.literal("登录")
                    }
                }
            } catch (e: Exception) {
                // 在主线程中处理异常
                client?.execute {
                    errorMessage = "登录过程中出错: ${e.message ?: "未知错误"}"
                    isLoggingIn = false
                    isLoginTimedOut = false
                    loginButton.active = true
                    loginButton.message = Text.literal("登录")
                }
            }
        }.start()
    }

    private fun validateCredentials(username: String, password: String): Boolean {
        if (!Verification) return true
        
        try {
            // 创建Socket连接到服务器，设置5秒超时
            socket = Socket().apply {
                connect(InetSocketAddress(serverAddress, serverPort), 5000)
            }

            outputStream = DataOutputStream(socket?.getOutputStream())
            inputStream = DataInputStream(socket?.getInputStream())
            
            logger.info("成功连接到服务器 $serverAddress:$serverPort")

            // 发送认证请求
            outputStream?.writeUTF("AUTH")  // 请求类型
            outputStream?.writeUTF(username) // 用户名
            outputStream?.writeUTF(password) // 密码
            outputStream?.flush()

            // 接收服务器响应
            val success = inputStream?.readBoolean() ?: false
            val message = inputStream?.readUTF() ?: "未知错误"
            
            if (success && message == "SUCCESS") {
                // 认证成功，读取用户组信息
                val userGroup = inputStream?.readUTF() ?: "user"
                currentUserGroup = userGroup
                logger.info("用户 $username 登录成功，用户组: $userGroup")
                
                // 设置服务器已连接状态
                isServerConnected = true
                serverConnectedTime = System.currentTimeMillis()
                
                // 启动心跳线程
                startHeartbeat()
                return true
            } else {
                // 认证失败
                errorMessage = if (message == "FAILURE") {
                    inputStream?.readUTF() ?: "用户名或密码错误"
                } else {
                    "服务器响应异常: $message"
                }
                logger.warn("登录失败: $errorMessage")
                closeConnection()
                return false
            }

            return false
        } catch (e: Exception) {
            errorMessage = "无法连接到服务器"
            isServerConnected = false
            return false
        } finally {
            isLoggingIn = false
            isLoginTimedOut = false
            loginButton.message = Text.literal("登录")
        }
    }

    private fun saveCredentials(username: String, password: String, remember: Boolean) {
        properties.setProperty("username", username)
        properties.setProperty("remember", remember.toString())

        if (remember) {
            properties.setProperty("password", password)
        } else {
            properties.remove("password")
        }

        // 保存到文件
        credentialsFile.outputStream().use {
            properties.store(it, "SKYPVP Client Credentials")
        }
    }

    private fun refreshServerConnection() {
        // 禁用刷新按钮，显示正在连接状态
        refreshButton.active = false
        refreshButton.message = Text.literal("正在连接...")
        errorMessage = null

        // 创建一个新线程来处理网络请求
        Thread {
            try {
                // 尝试连接到服务器
                val socket = Socket(serverAddress, serverPort)

                // 设置服务器已连接状态
                isServerConnected = true
                serverConnectedTime = System.currentTimeMillis()

                // 关闭连接
                socket.close()

                // 在主线程中更新UI
                client?.execute {
                    errorMessage = null
                }
            } catch (e: Exception) {
                // 在主线程中处理异常
                client?.execute {
                    errorMessage = "连接服务器失败: ${e.message ?: "未知错误"}"
                    isServerConnected = false
                }
            } finally {
                // 在主线程中恢复按钮状态
                client?.execute {
                    refreshButton.active = true
                    refreshButton.message = Text.literal("刷新连接")
                }
            }
        }.start()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        animationTicks += delta * 0.5f

        renderBackground(context, mouseX, mouseY, delta)

        // 检查登录超时
        if (isLoggingIn && !isLoginTimedOut) {
            val currentTime = System.currentTimeMillis()
            val elapsedSeconds = (currentTime - loginStartTime) / 1000

            if (elapsedSeconds >= loginTimeoutSeconds) {
                isLoginTimedOut = true
                isLoggingIn = false
                loginButton.active = true
                loginButton.message = Text.literal("登录")
                errorMessage = "登录失败，请重试！"
                logger.error("登录超时，请重试！")
            }
        }

        // 显示服务器连接成功提示
        if (isServerConnected) {
            val currentTime = System.currentTimeMillis()
            val elapsedSeconds = (currentTime - serverConnectedTime) / 1000

            if (elapsedSeconds < serverConnectedDisplaySeconds) {
                val connectMessage = "服务器连接成功！"
                val connectX = width / 2 - textRenderer.getWidth(connectMessage) / 2
                val connectY = 20 // 顶部位置

                // 绘制半透明背景
                val bgPadding = 5
                context.fill(
                    connectX - bgPadding,
                    connectY - bgPadding,
                    connectX + textRenderer.getWidth(connectMessage) + bgPadding,
                    connectY + textRenderer.fontHeight + bgPadding,
                    0x80000000.toInt()
                )

                // 绘制文本
                context.drawText(textRenderer, connectMessage, connectX, connectY, 0xFF00FF00.toInt(), true)
            } else {
                // 5秒后重置状态
                isServerConnected = false
            }
        }

        // 绘制标题
        val title = "请登录你的SKYPVP账户"
        val titleX = width / 2 - textRenderer.getWidth(title) / 2
        val titleY = height / 2 - 80
        context.drawText(textRenderer, title, titleX, titleY, 0xFFFFFFFF.toInt(), true)

        // 绘制输入框标签
        context.drawText(textRenderer, "用户名:", usernameField.x - 60, usernameField.y + 6, 0xFFFFFFFF.toInt(), true)
        context.drawText(textRenderer, "密码:", passwordField.x - 60, passwordField.y + 6, 0xFFFFFFFF.toInt(), true)

        // 绘制错误信息
        errorMessage?.let {
            // 如果是超时错误，在右下角显示
            if (isLoginTimedOut) {
                val errorX = width - textRenderer.getWidth(it) - 10
                val errorY = height - 30
                context.drawText(textRenderer, it, errorX, errorY, 0xFFFF0000.toInt(), true)
            } else {
                // 其他错误在中间显示
                val errorX = width / 2 - textRenderer.getWidth(it) / 2
                val errorY = height / 2 + 100
                context.drawText(textRenderer, it, errorX, errorY, 0xFFFF0000.toInt(), true)
            }
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

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        // 当按下回车键时尝试登录
        if (keyCode == 257 || keyCode == 335) {
            attemptLogin()
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun close() {
        currentUserGroup = null
        closeConnection()
        super.close()
    }

    private fun startHeartbeat() {
        keepAlive = true
        heartbeatThread = Thread {
            try {
                while (keepAlive && !Thread.currentThread().isInterrupted) {
                    Thread.sleep(heartbeatInterval)

                    // 发送心跳包
                    outputStream?.writeUTF("HEARTBEAT")
                    outputStream?.flush()

                    // 检查连接是否仍然活跃
                    if (socket?.isClosed == true || socket?.isConnected != true) {
                        logger.warn("连接已断开")
                        closeConnection()
                        break
                    }
                }
            } catch (e: InterruptedException) {
                logger.info("心跳线程被中断")
            } catch (e: Exception) {
                logger.error("心跳线程出错: ${e.message}")
                closeConnection()
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun closeConnection() {
        try {
            keepAlive = false
            heartbeatThread?.interrupt()

            outputStream?.close()
            inputStream?.close()
            socket?.close()

            outputStream = null
            inputStream = null
            socket = null

            isServerConnected = false
        } catch (e: Exception) {
            logger.error("关闭连接时出错: ${e.message}")
        }
    }
}