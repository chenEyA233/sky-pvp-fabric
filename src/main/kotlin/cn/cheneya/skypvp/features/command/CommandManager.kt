/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233
 */
package cn.cheneya.skypvp.features.command

import cn.cheneya.skypvp.utils.ChatUtil
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture
import kotlin.math.min

/**
 * 命令异常类
 *
 * @param text 错误消息文本
 * @param cause 异常原因
 * @param usageInfo 命令用法信息
 */
class CommandException(val text: String, cause: Throwable? = null, val usageInfo: List<String>? = null) :
    Exception(text, cause)

/**
 * 命令执行器
 * 负责监听聊天事件并执行命令
 */
object CommandExecutor {

    fun init() {
        // 注册聊天消息发送事件监听器
        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            // 检查消息是否以命令前缀开头
            if (message.startsWith(CommandManager.Options.prefix)) {
                try {
                    // 去除前缀并执行命令
                    CommandManager.execute(message.substring(CommandManager.Options.prefix.length))
                } catch (e: CommandException) {
                    // 处理命令异常
                    handleCommandException(e)
                } catch (e: Throwable) {
                    // 处理其他异常
                    ChatUtil.error("执行命令时发生错误: ${e.message ?: "未知错误"}")
                    e.printStackTrace()
                }

                // 取消原始聊天消息发送
                return@register false
            }

            // 允许发送非命令消息
            true
        }
    }

    /**
     * 处理命令异常
     */
    private fun handleCommandException(e: CommandException) {
        // 显示错误消息
        ChatUtil.error(e.text)

        // 显示命令用法
        if (!e.usageInfo.isNullOrEmpty()) {
            ChatUtil.info("用法:")

            for (usage in e.usageInfo) {
                ChatUtil.error("${CommandManager.Options.prefix}$usage")
            }
        }
    }
}

/**
 * 命令管理器
 * 负责管理命令的注册和执行
 */
object CommandManager {
    private val commands = mutableListOf<Command>()

    /**
     * 命令系统配置选项
     */
    object Options {
        /**
         * 命令前缀
         */
        var prefix = "."

        /**
         * 未知命令时显示的建议数量
         */
        var hintCount = 5
    }

    /**
     * 初始化命令管理器
     */
    fun init() {
        // 初始化命令执行器
        CommandExecutor.init()
    }

    /**
     * 注册命令
     *
     * @param command 要注册的命令
     */
    fun registerCommand(command: Command) {
        commands.add(command)
    }

    /**
     * 注销命令
     *
     * @param command 要注销的命令
     */
    fun unregisterCommand(command: Command) {
        commands.remove(command)
    }

    /**
     * 获取所有已注册的命令
     *
     * @return 命令列表
     */
    fun getCommands(): List<Command> {
        return commands.toList()
    }

    /**
     * 获取子命令
     *
     * @param args 命令参数列表
     * @param currentCommand 当前命令
     * @param idx 当前索引
     * @return 子命令和索引的配对，如果未找到则返回null
     */
    private fun getSubCommand(
        args: List<String>,
        currentCommand: Pair<Command, Int>? = null,
        idx: Int = 0
    ): Pair<Command, Int>? {
        // 如果没有更多参数，返回当前命令
        if (idx >= args.size) {
            return currentCommand
        }

        // 如果当前命令为null，则在所有命令中搜索
        val commandSupplier = currentCommand?.first?.subcommands?.asIterable() ?: commands

        // 查找匹配当前索引的命令
        commandSupplier
            .firstOrNull {
                it.name.equals(args[idx], true) || it.aliases.any { alias ->
                    alias.equals(args[idx], true)
                }
            }
            ?.let { return getSubCommand(args, Pair(it, idx), idx + 1) }

        // 如果未找到匹配项，返回当前命令
        return currentCommand
    }

    /**
     * 执行命令
     *
     * @param cmd 命令字符串
     */
    fun execute(cmd: String) {
        val args = tokenizeCommand(cmd).first

        // 防止空命令
        if (args.isEmpty()) {
            return
        }

        // 获取子命令
        val pair = getSubCommand(args) ?: throw CommandException(
            "未知命令: ${args[0]}",
            usageInfo = if (commands.isEmpty() || Options.hintCount == 0) {
                null
            } else {
                // 根据编辑距离排序命令，提供建议
                commands.sortedBy { command ->
                    var distance = levenshtein(args[0], command.name)
                    if (command.aliases.isNotEmpty()) {
                        distance = min(
                            distance,
                            command.aliases.minOf { levenshtein(args[0], it) }
                        )
                    }
                    distance
                }.take(Options.hintCount).map { command ->
                    buildString {
                        append(command.name)
                        if (command.aliases.isNotEmpty()) {
                            command.aliases.joinTo(this, separator = "/", prefix = " (", postfix = ")")
                        }
                    }
                }
            }
        )

        val command = pair.first

        // 如果命令不可执行，抛出异常
        if (!command.executable) {
            throw CommandException(
                "无效的命令用法: ${args[0]}",
                usageInfo = command.usage()
            )
        }

        // 命令在参数中的索引
        val idx = pair.second

        // 如果命令不接受参数但提供了参数
        if (command.parameters.isEmpty() && idx != args.size - 1) {
            throw CommandException(
                "此命令不接受参数",
                usageInfo = command.usage()
            )
        }

        // 如果缺少必需参数
        if (args.size - idx - 1 < command.parameters.size && command.parameters[args.size - idx - 1].required) {
            throw CommandException(
                "缺少必需参数: ${command.parameters[args.size - idx - 1].name}",
                usageInfo = command.usage()
            )
        }

        // 解析参数值
        val parsedParameters = arrayOfNulls<Any>(args.size - idx - 1)

        // 处理可变参数的特殊情况
        if (command.parameters.lastOrNull()?.vararg == true && command.parameters.size > args.size - idx) {
            parsedParameters[command.parameters.size - 1] = emptyArray<Any>()
        }

        for (i in (idx + 1) until args.size) {
            val paramIndex = i - idx - 1

            // 检查是否有对应的参数
            if (paramIndex >= command.parameters.size) {
                throw CommandException(
                    "未知参数: ${args[i]}",
                    usageInfo = command.usage()
                )
            }

            val parameter = command.parameters[paramIndex]

            // 特殊处理可变参数
            val parameterValue = if (parameter.vararg) {
                val outputArray = arrayOfNulls<Any>(args.size - i)

                for (j in i until args.size) {
                    outputArray[j - i] = parseParameter(command, args[j], parameter)
                }

                outputArray
            } else {
                parseParameter(command, args[i], parameter)
            }

            // 存储解析后的参数值
            parsedParameters[paramIndex] = parameterValue

            // 可变参数只能出现在末尾
            if (parameter.vararg) {
                break
            }
        }

        // 执行命令
        @Suppress("UNCHECKED_CAST")
        command.handler?.invoke(command, parsedParameters as Array<Any>)
    }

    /**
     * 解析单个参数
     */
    private fun parseParameter(command: Command, argument: String, parameter: Parameter<*>): Any? {
        if (parameter.verifier == null) {
            return argument
        }

        when (val validationResult = parameter.verifier.verifyAndParse(argument)) {
            is ParameterValidationResult.Ok -> {
                return validationResult.mappedResult
            }

            is ParameterValidationResult.Error -> {
                throw CommandException(
                    "参数 ${parameter.name} 的值 $argument 无效: ${validationResult.errorMessage}",
                    usageInfo = command.usage()
                )
            }
        }
    }

    /**
     * 分词命令字符串
     *
     * @param line 命令字符串
     * @return 分词结果和索引的配对
     */
    fun tokenizeCommand(line: String): Pair<List<String>, List<Int>> {
        val output = ArrayList<String>()
        val outputIndices = ArrayList<Int>()
        val stringBuilder = StringBuilder()

        outputIndices.add(0)

        var escaped = false
        var quote = false

        var idx = 0

        for (c in line.toCharArray()) {
            idx++

            // 处理转义字符
            if (escaped) {
                stringBuilder.append(c)
                escaped = false
                continue
            }

            // 处理特殊字符
            when (c) {
                '\\' -> escaped = true
                '"' -> quote = !quote
                ' ' -> {
                    if (!quote) {
                        // 如果缓冲区不为空，添加到输出
                        if (stringBuilder.trim().isNotEmpty()) {
                            output.add(stringBuilder.toString())
                            stringBuilder.setLength(0)
                            outputIndices.add(idx)
                        }
                    } else {
                        stringBuilder.append(c)
                    }
                }

                else -> stringBuilder.append(c)
            }
        }

        // 处理缓冲区中剩余的内容
        if (stringBuilder.trim().isNotEmpty()) {
            if (quote) {
                output.add('"' + stringBuilder.toString())
            } else {
                output.add(stringBuilder.toString())
            }
        }

        return Pair(output, outputIndices)
    }

    /**
     * 命令自动补全
     *
     * @param origCmd 原始命令字符串
     * @param start 当前光标位置
     * @return 补全建议
     */
    fun autoComplete(origCmd: String, start: Int): CompletableFuture<Suggestions> {
        if (start < Options.prefix.length) {
            return Suggestions.empty()
        }

        try {
            val cmd = origCmd.substring(Options.prefix.length, start)
            val tokenized = tokenizeCommand(cmd)
            var args = tokenized.first

            if (args.isEmpty()) {
                args = listOf("")
            }

            val nextParameter = !args.last().endsWith(" ") && cmd.endsWith(" ")
            var currentArgStart = tokenized.second.lastOrNull() ?: 0

            if (nextParameter) {
                currentArgStart = cmd.length
            }

            val builder = SuggestionsBuilder(origCmd, currentArgStart + Options.prefix.length)

            // 处理命令名称的自动补全
            val pair = getSubCommand(args)

            if (args.size == 1 && (pair == null || !nextParameter)) {
                for (command in commands) {
                    if (command.name.startsWith(args[0], true)) {
                        builder.suggest(command.name)
                    }

                    command.aliases.filter { it.startsWith(args[0], true) }.forEach { builder.suggest(it) }
                }

                return builder.buildFuture()
            }

            if (pair == null) {
                return Suggestions.empty()
            }

            // 处理参数的自动补全
            pair.first.autoComplete(builder, tokenized, pair.second, nextParameter)

            return builder.buildFuture()
        } catch (e: Exception) {
            e.printStackTrace()
            return Suggestions.empty()
        }
    }

    /**
     * 计算两个字符串之间的编辑距离
     *
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return 编辑距离
     */
    private fun levenshtein(s1: String, s2: String): Int {
        val s1Length = s1.length
        val s2Length = s2.length

        val dp = Array(s1Length + 1) { IntArray(s2Length + 1) }

        for (i in 0..s1Length) {
            dp[i][0] = i
        }

        for (j in 0..s2Length) {
            dp[0][j] = j
        }

        for (i in 1..s1Length) {
            for (j in 1..s2Length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]) + 1
                }
            }
        }

        return dp[s1Length][s2Length]
    }

}