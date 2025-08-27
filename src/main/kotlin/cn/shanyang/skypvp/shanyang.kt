/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: shanyang
 */
package cn.shanyang.skypvp

import cn.shanyang.skypvp.utils.illililiilil
import net.skypvpteam.skypvp.api.ClassInject
import org.apache.logging.log4j.LogManager

@ClassInject("cn.shanyang.skypvp.shanyang")
object shanyang {
    val logger = LogManager.getLogger(illililiilil.logger)!!

    init {
        logger.info("initialize shanyang api...  fuck inject module text")
    }
}
