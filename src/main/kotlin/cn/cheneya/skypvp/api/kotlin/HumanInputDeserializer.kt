package cn.cheneya.skypvp.api.kotlin

import cn.cheneya.skypvp.api.math.Color4b
import com.mojang.brigadier.StringReader

import net.minecraft.block.Block
import net.minecraft.client.util.InputUtil
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.awt.Color
import java.util.*
import kotlin.jvm.optionals.getOrNull

object HumanInputDeserializer {
    val textDeserializer = StringDeserializer { it }
    val booleanDeserializer = StringDeserializer { str ->
        when (str.lowercase(Locale.ROOT)) {
            "true", "on", "yes" -> true
            "false", "off", "no" -> false
            else -> require(false) { "Unknown boolean value '$str' (allowed are true/on/yes or false/off/no)" }
        }
    }

    val floatDeserializer = StringDeserializer(String::toFloat)
    val floatRangeDeserializer = StringDeserializer { str ->
        parseRange(str, floatDeserializer) { lhs, rhs -> lhs..rhs }
    }

    val intDeserializer = StringDeserializer(String::toInt)
    val intRangeDeserializer = StringDeserializer { str ->
        parseRange(str, intDeserializer) { lhs, rhs -> lhs..rhs }
    }
    val textArrayDeserializer = StringDeserializer { parseArray(it, textDeserializer) }

    val colorDeserializer = StringDeserializer {
        if (it.startsWith('#')) {
            Color4b.fromHex(it)
        } else {
            Color4b(Color(it.toInt()))
        }
    }

    val blockDeserializer: StringDeserializer<Block> = StringDeserializer {
        val block = Registries.BLOCK.getOptionalValue(Identifier.fromCommandInput(StringReader(it))).getOrNull()

        requireNotNull(block) { "Unknown block '$it'" }
    }
    val blockListDeserializer: StringDeserializer<MutableList<Block>> = StringDeserializer {
        parseArray(it, blockDeserializer)
    }
    val itemListDeserializer: StringDeserializer<MutableList<Item>> = StringDeserializer {
        parseArray(it, itemDeserializer)
    }

    val itemDeserializer: StringDeserializer<Item> = StringDeserializer {
        val block = Registries.ITEM.getOptionalValue(Identifier.fromCommandInput(StringReader(it))).getOrNull()

        requireNotNull(block) { "Unknown item '$it'" }
    }

    val keyDeserializer: StringDeserializer<InputUtil.Key> = StringDeserializer(::inputByName)

    private fun <T> parseArray(str: String, componentDeserializer: StringDeserializer<T>): MutableList<T> {
        return str.split(",").mapTo(ArrayList(), componentDeserializer::deserializeThrowing)
    }

    private inline fun <N, R> parseRange(
        str: String,
        numberParser: StringDeserializer<N>,
        rangeSupplier: (N, N) -> R
    ): R {
        val split = str.split("..")

        require(split.size == 2) { "Invalid range '$str', must be in the format 'min..max'" }

        val lhs = numberParser.deserializeThrowing(split[0])
        val rhs = numberParser.deserializeThrowing(split[1])

        return rangeSupplier(lhs, rhs)
    }

    private fun fail(s: String): Boolean {
        throw IllegalArgumentException(s)
    }

    fun interface StringDeserializer<out T> {
        /**
         * Tries to parse the input.
         *
         * @throws IllegalArgumentException if the input is invalid
         */
        @Throws(IllegalArgumentException::class)
        fun deserializeThrowing(str: String): T
    }
}
