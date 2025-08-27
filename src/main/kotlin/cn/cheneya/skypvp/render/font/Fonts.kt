package cn.cheneya.skypvp.render.font

import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import java.awt.FontFormatException
import java.io.IOException

object Fonts {
    private val logger = LogManager.getLogger(Fonts::class.java)
    
    // 定义字体实例
    lateinit var opensans: FontRender
    lateinit var harmony: FontRender
    lateinit var icons: FontRender
    
    // 字体加载状态
    private var initialized = false
    
    /**
     * 检查字体是否已加载
     */
    fun isInitialized(): Boolean = initialized
    
    /**
     * 加载所有字体
     * 
     * @throws IOException 如果字体文件无法读取
     * @throws FontFormatException 如果字体格式无效
     */
    @Throws(IOException::class, FontFormatException::class)
    fun loadFonts() {
        if (initialized) return
        
        logger.info("Loading fonts...")
        val startTime = System.currentTimeMillis()
        
        try {
            // 确保在渲染线程中加载
            if (MinecraftClient.getInstance().isOnThread) {
                // 加载字体
                opensans = FontRender("fonten", 32, 0, 255, 512)
                harmony = FontRender("fontzh", 32, 0, 65535, 16384)
                icons = FontRender("fonten", 32, 59648, 59652, 512)
                
                initialized = true
                logger.info("Fonts loaded in {}ms", System.currentTimeMillis() - startTime)
            } else {
                logger.warn("Font loading must be done on render thread")
            }
        } catch (e: Exception) {
            logger.error("Failed to load fonts", e)
            throw e
        }
    }
    
    /**
     * 初始化字体系统
     */
    fun init() {
        try {
            loadFonts()
        } catch (e: Exception) {
            logger.error("Failed to load fonts", e)
            // 设置默认字体
            if (!::harmony.isInitialized) {
                harmony = createFallbackFont()
                initialized = true
            }
        }
    }
    
    /**
     * 创建回退字体
     */
    private fun createFallbackFont(): FontRender {
        logger.warn("Creating fallback font")
        return FontRender("fonten", 32, 0, 255, 512)
    }
}