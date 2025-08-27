/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233, shanyang,
 */
package net.skypvpteam.skypvp

import cn.cheneya.skypvp.skypvp.logger
import net.skypvpteam.skypvp.api.skypvp

@skypvp
object skypvp{
    fun init(){
        logger.info("Load protection system!")
    }
}