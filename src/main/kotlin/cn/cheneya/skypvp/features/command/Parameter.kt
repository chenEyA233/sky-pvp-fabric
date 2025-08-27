/**
 * Copyright (C) SKY PVP Team 2024~2025
 *
 * Code writing: chenEyA233
 */
package cn.cheneya.skypvp.features.command

import com.mojang.brigadier.suggestion.SuggestionsBuilder

/**
 * 参数验证结果
 * 用于表示参数验证的结果
 */
sealed class ParameterValidationResult<T> {
    /**
     * 验证成功
     *
     * @param mappedResult 映射后的结果
     */
    class Ok<T>(val mappedResult: T) : ParameterValidationResult<T>()

    /**
     * 验证失败
     *
     * @param errorMessage 错误消息
     */
    class Error<T>(val errorMessage: String) : ParameterValidationResult<T>()
}

/**
 * 参数验证器接口
 * 用于验证和解析参数
 */
interface ParameterVerifier<T> {
    /**
     * 验证并解析参数
     *
     * @param input 输入字符串
     * @return 验证结果
     */
    fun verifyAndParse(input: String): ParameterValidationResult<T>

    /**
     * 提供自动补全建议
     *
     * @param builder 建议构建器
     * @param currentArg 当前参数
     */
    fun provideSuggestions(builder: SuggestionsBuilder, currentArg: String) {
        // 默认不提供任何建议
    }
}

/**
 * 命令参数
 *
 * @param name 参数名称
 * @param required 是否必需
 * @param verifier 参数验证器
 * @param vararg 是否为可变参数
 */
class Parameter<T>(
    val name: String,
    val required: Boolean = true,
    val verifier: ParameterVerifier<T>? = null,
    val vararg: Boolean = false
)

/**
 * 整数参数验证器
 */
object IntVerifier : ParameterVerifier<Int> {
    /**
     * 验证并解析整数参数
     *
     * @param input 输入字符串
     * @return 验证结果
     */
    override fun verifyAndParse(input: String): ParameterValidationResult<Int> {
        return try {
            ParameterValidationResult.Ok(input.toInt())
        } catch (e: NumberFormatException) {
            ParameterValidationResult.Error("不是有效的整数")
        }
    }
}

/**
 * 浮点数参数验证器
 */
object FloatVerifier : ParameterVerifier<Float> {
    /**
     * 验证并解析浮点数参数
     *
     * @param input 输入字符串
     * @return 验证结果
     */
    override fun verifyAndParse(input: String): ParameterValidationResult<Float> {
        return try {
            ParameterValidationResult.Ok(input.toFloat())
        } catch (e: NumberFormatException) {
            ParameterValidationResult.Error("不是有效的浮点数")
        }
    }
}

/**
 * 布尔参数验证器
 */
object BooleanVerifier : ParameterVerifier<Boolean> {
    private val trueValues = setOf("true", "yes", "y", "1", "on", "enable", "enabled")
    private val falseValues = setOf("false", "no", "n", "0", "off", "disable", "disabled")

    /**
     * 验证并解析布尔参数
     *
     * @param input 输入字符串
     * @return 验证结果
     */
    override fun verifyAndParse(input: String): ParameterValidationResult<Boolean> {
        val lowerInput = input.lowercase()

        return when {
            trueValues.contains(lowerInput) -> ParameterValidationResult.Ok(true)
            falseValues.contains(lowerInput) -> ParameterValidationResult.Ok(false)
            else -> ParameterValidationResult.Error("不是有效的布尔值")
        }
    }

    /**
     * 提供自动补全建议
     *
     * @param builder 建议构建器
     * @param currentArg 当前参数
     */
    override fun provideSuggestions(builder: SuggestionsBuilder, currentArg: String) {
        val lowerArg = currentArg.lowercase()

        for (value in trueValues + falseValues) {
            if (value.startsWith(lowerArg)) {
                builder.suggest(value)
            }
        }
    }
}

/**
 * 枚举参数验证器
 *
 * @param enumClass 枚举类
 */
class EnumVerifier<T : Enum<T>>(private val enumClass: Class<T>) : ParameterVerifier<T> {
    private val enumValues = enumClass.enumConstants

    /**
     * 验证并解析枚举参数
     *
     * @param input 输入字符串
     * @return 验证结果
     */
    override fun verifyAndParse(input: String): ParameterValidationResult<T> {
        val upperInput = input.uppercase()

        for (value in enumValues) {
            if (value.name == upperInput) {
                return ParameterValidationResult.Ok(value)
            }
        }

        return ParameterValidationResult.Error("不是有效的选项: ${enumValues.joinToString(", ") { it.name.lowercase() }}")
    }

    /**
     * 提供自动补全建议
     *
     * @param builder 建议构建器
     * @param currentArg 当前参数
     */
    override fun provideSuggestions(builder: SuggestionsBuilder, currentArg: String) {
        val lowerArg = currentArg.lowercase()

        for (value in enumValues) {
            val name = value.name.lowercase()
            if (name.startsWith(lowerArg)) {
                builder.suggest(name)
            }
        }
    }
}
