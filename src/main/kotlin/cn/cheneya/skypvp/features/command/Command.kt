/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233
 */
package cn.cheneya.skypvp.features.command

import cn.cheneya.skypvp.utils.ChatUtil
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

/**
 * 命令处理器接口
 * 用于处理命令的执行
 */
interface CommandHandler {
    /**
     * 执行命令
     *
     * @param command 当前命令对象
     * @param args 命令参数数组
     */
    fun invoke(command: Command, args: Array<Any>)
}

/**
 * 命令类
 * 表示一个可执行的命令
 *
 * @param name 命令名称
 * @param aliases 命令别名数组
 * @param description 命令描述
 * @param parameters 命令参数数组
 * @param handler 命令处理器
 * @param subcommands 子命令列表
 * @param executable 是否可执行
 * @param requiresIngame 是否需要在游戏中执行
 */
class Command(
    val name: String,
    val aliases: Array<String> = emptyArray(),
    val description: String = "",
    val parameters: Array<Parameter<*>> = emptyArray(),
    val handler: CommandHandler? = null,
    val subcommands: List<Command> = emptyList(),
    val executable: Boolean = true,
    val requiresIngame: Boolean = false
) {
    private val mc = MinecraftClient.getInstance()

    /**
     * 生成命令用法信息
     *
     * @return 用法信息列表
     */
    fun usage(): List<String> {
        val result = mutableListOf<String>()

        // 添加当前命令的用法
        if (executable) {
            val builder = StringBuilder(name)

            for (parameter in parameters) {
                val paramName = parameter.name

                if (parameter.required) {
                    builder.append(" <$paramName>")
                } else {
                    builder.append(" [$paramName]")
                }

                if (parameter.vararg) {
                    builder.append("...")
                }
            }

            result.add(builder.toString())
        }

        // 添加子命令的用法
        for (subcommand in subcommands) {
            for (usage in subcommand.usage()) {
                result.add("$name $usage")
            }
        }

        return result
    }



    /**
     * 自动补全命令
     *
     * @param builder 建议构建器
     * @param tokenized 分词结果
     * @param idx 当前索引
     * @param nextParameter 是否为下一个参数
     */
    fun autoComplete(
        builder: SuggestionsBuilder,
        tokenized: Pair<List<String>, List<Int>>,
        idx: Int,
        nextParameter: Boolean
    ) {
        val args = tokenized.first

        // 如果不是下一个参数，则补全当前参数
        if (!nextParameter) {
            val paramIdx = args.size - idx - 1

            // 如果参数索引有效
            if (paramIdx >= 0 && paramIdx < parameters.size) {
                val parameter = parameters[paramIdx]
                val verifier = parameter.verifier

                // 如果有验证器，使用验证器提供建议
                if (verifier != null) {
                    verifier.provideSuggestions(builder, args.last())
                }
            }

            return
        }

        // 如果是下一个参数，则补全子命令或参数名称
        val paramIdx = args.size - idx - 1

        // 如果有子命令，补全子命令名称
        if (paramIdx == 0) {
            for (subcommand in subcommands) {
                builder.suggest(subcommand.name)

                for (alias in subcommand.aliases) {
                    builder.suggest(alias)
                }
            }
        }

        // 如果参数索引有效，补全参数名称
        if (paramIdx >= 0 && paramIdx < parameters.size) {
            val parameter = parameters[paramIdx]
            val verifier = parameter.verifier

            // 如果有验证器，使用验证器提供建议
            if (verifier != null) {
                verifier.provideSuggestions(builder, "")
            }
        }
    }
}
