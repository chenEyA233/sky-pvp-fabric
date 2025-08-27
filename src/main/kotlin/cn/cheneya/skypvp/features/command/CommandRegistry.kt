/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233
 */
package cn.cheneya.skypvp.features.command

import cn.cheneya.skypvp.features.command.commands.*

object CommandRegistry {
    fun init() {
        // 注册绑定命令
        CommandManager.registerCommand(BindCommand.create())
        
        // 注册客户端命令
        CommandManager.registerCommand(ClientCommand.create())
    }
}
