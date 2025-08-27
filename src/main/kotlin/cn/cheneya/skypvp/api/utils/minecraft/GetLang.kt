package cn.cheneya.skypvp.api.utils.minecraft

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 用于获取和检查游戏语言的工具类
 */
object GetLang {
    private val logger: Logger = LoggerFactory.getLogger("skypvp")
    private val languageChangeListeners = mutableListOf<() -> Unit>()

    /**
     * 注册语言变更监听器
     * @param listener 当语言变更时调用的函数
     */
    fun addLanguageChangeListener(listener: () -> Unit) {
        languageChangeListeners.add(listener)
    }

    /**
     * 移除语言变更监听器
     * @param listener 要移除的监听器
     */
    fun removeLanguageChangeListener(listener: () -> Unit) {
        languageChangeListeners.remove(listener)
    }

    /**
     * 通知所有监听器语言已变更
     */
    fun notifyLanguageChanged() {
        languageChangeListeners.forEach { it.invoke() }
    }

    /**
     * 获取当前游戏的语言代码
     * @return 当前语言代码，例如"zh_cn"、"en_us"等
     */
    fun getCurrentLanguage(): String {
        return try {
            MinecraftClient.getInstance().languageManager.language
        } catch (e: Exception) {
            "en_us" // 默认返回英语
        }
    }

    /**
     * 检查当前游戏语言是否为中文
     * @return 如果当前语言是中文（简体或繁体）则返回true，否则返回false
     */
    fun isChinese(): Boolean {
        val lang = getCurrentLanguage().lowercase()
        return lang.startsWith("zh_")
    }

    /**
     * 检查当前游戏语言是否为英文
     * @return 如果当前语言是英文则返回true，否则返回false
     */
    fun isEnglish(): Boolean {
        val lang = getCurrentLanguage().lowercase()
        return lang.startsWith("en_")
    }

    /**
     * 根据当前语言获取对应的翻译文本
     * @param chineseText 中文文本
     * @param englishText 英文文本
     * @return 根据当前语言返回相应的文本
     */
    fun getLocalizedText(chineseText: String, englishText: String): String {
        return if (isChinese()) chineseText else englishText
    }

    /**
     * 获取游戏内翻译键对应的本地化文本
     * @param translationKey 翻译键
     * @return 本地化后的文本
     */
    fun getTranslation(translationKey: String): String {
        return try {
            Text.translatable(translationKey).string
        } catch (e: Exception) {
            logger.error("Failed to get translation for key: $translationKey", e)
            translationKey // 如果获取失败，返回原键
        }
    }
}