/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233
 */
package cn.cheneya.skypvp.features.command.commands

import cn.cheneya.skypvp.features.command.Command
import cn.cheneya.skypvp.features.command.CommandHandler
import cn.cheneya.skypvp.features.module.ModuleManager
import cn.cheneya.skypvp.skypvp
import cn.cheneya.skypvp.utils.ChatUtil

/**
 * 客户端命令处理器
 */
object ClientCommandHandler : CommandHandler {
    override fun invoke(command: Command, args: Array<Any>) {
        ChatUtil.info("客户端命令帮助：")
        ChatUtil.info(".client about - 显示客户端的关于信息")
        ChatUtil.info(".client modulelist - 列出所有可用的模块")
    }
}

/**
 * 关于信息命令处理器
 */
object AboutCommandHandler : CommandHandler {
    override fun invoke(command: Command, args: Array<Any>) {
        ChatUtil.info("===== SKY PVP Client =====")
        ChatUtil.info("版本: " + skypvp.CLIENT_VERSION)
        ChatUtil.info("开发者: chenEyA233, shanyang")
        ChatUtil.info("一个强大的 Minecraft Hacker 客户端")
        ChatUtil.info("==========================")
    }
}

/**
 * 模块列表命令处理器
 */
object ModuleListCommandHandler : CommandHandler {
    override fun invoke(command: Command, args: Array<Any>) {
        ChatUtil.info("===== 模块列表 =====")

        // 按类别对模块进行分组
        val modulesByCategory = ModuleManager.modules.groupBy { it.category }

        // 遍历每个类别及其模块
        modulesByCategory.forEach { (category, modules) ->
            ChatUtil.info("${category.getName()}:")

            // 列出该类别下的所有模块
            modules.forEach { module ->
                ChatUtil.info("  ${module.name}（${module.category.getName()}）")
            }
        }

        ChatUtil.info("总计: ${ModuleManager.modules.size} 个模块")
        ChatUtil.info("===================")
    }
}

/**
 * 客户端命令
 * 用于显示客户端信息和模块列表
 */
object ClientCommand {
    /**
     * 创建客户端命令
     *
     * @return 客户端命令对象
     */
    fun create(): Command {
        // 创建子命令：about
        val aboutCommand = Command(
            name = "about",
            description = "显示客户端的关于信息",
            handler = AboutCommandHandler
        )

        // 创建子命令：modulelist
        val moduleListCommand = Command(
            name = "modulelist",
            aliases = arrayOf(),
            description = "列出所有可用的模块",
            handler = ModuleListCommandHandler
        )

        // 创建主命令，并包含子命令
        return Command(
            name = "client",
            aliases = arrayOf("cl"),
            handler = ClientCommandHandler,
            description = "显示客户端信息和模块列表",
            subcommands = listOf(aboutCommand, moduleListCommand)
        )
    }
}
