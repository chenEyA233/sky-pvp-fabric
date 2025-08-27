package cn.cheneya.skypvp.setting

import cn.cheneya.skypvp.features.module.Module

/**
 * 设置基类
 */
abstract class Setting<T>(
    val name: String,
    val module: Module,
    private var _value: T,
    val description: String = ""
) {
    // 监听器列表
    private val listeners = mutableListOf<(T) -> Unit>()
    
    // 值属性，当值改变时通知所有监听器
    open var value: T
        get() = _value
        set(newValue) {
            val oldValue = _value
            _value = newValue
            
            // 对于Double类型使用epsilon比较，解决浮点数精度问题
            val changed = when {
                oldValue is Double && newValue is Double -> 
                    kotlin.math.abs(oldValue - newValue) > 0.0001
                else -> oldValue != newValue
            }
            
            if (changed) {
                notifyListeners()
            }
        }
    
    /**
     * 添加值变化监听器
     */
    fun addListener(listener: (T) -> Unit) {
        listeners.add(listener)
    }
    
    /**
     * 移除值变化监听器
     */
    fun removeListener(listener: (T) -> Unit) {
        listeners.remove(listener)
    }
    
    /**
     * 通知所有监听器值已改变
     */
    fun notifyListeners() {
        listeners.forEach { it(_value) }
        
        // 通知配置系统设置已更改
        cn.cheneya.skypvp.config.Config.onSettingChange(this)
    }
}

/**
 * 布尔设置
 */
class BooleanSetting(
    name: String,
    module: Module,
    value: Boolean,
    description: String = ""
) : Setting<Boolean>(name, module, value, description)

/**
 * 数值设置
 */
class NumberSetting(
    name: String,
    module: Module,
    value: Double,
    val min: Double,
    val max: Double,
    val increment: Double = 0.1,
    description: String = ""
) : Setting<Double>(name, module, value.coerceIn(min, max), description) {
    // 防抖计时器
    private var saveTimer: Long = 0
    
    override var value: Double
        get() = super.value
        set(newValue) {
            // 确保新值在有效范围内
            val clampedValue = newValue.coerceIn(min, max)
            
            // 使用更精确的比较，考虑增量步长
            if (kotlin.math.abs(super.value - clampedValue) >= increment / 2) {
                super.value = clampedValue
                
                // 防抖机制：延迟500ms保存
                val now = System.currentTimeMillis()
                if (now - saveTimer > 500) {
                    saveTimer = now
                    notifyListeners()
                } else {
                    saveTimer = now
                }
            }
        }
    
    /**
     * 立即触发保存，用于滑块释放时
     */
    fun triggerSave() {
        notifyListeners()
    }
}

/**
 * 模式设置
 */
class ModeSetting(
    name: String,
    module: Module,
    value: String,
    val modes: List<String>,
    description: String = ""
) : Setting<String>(name, module, if (modes.contains(value)) value else modes.first(), description) {

    /**
     * 获取下一个模式
     */
    fun nextMode() {
        val index = modes.indexOf(value)
        value = modes[(index + 1) % modes.size]
    }

    /**
     * 获取上一个模式
     */
    fun prevMode() {
        val index = modes.indexOf(value)
        value = modes[(index - 1 + modes.size) % modes.size]
    }
}

/**
 * 颜色设置
 */
class ColorSetting(
    name: String,
    module: Module,
    value: Int,
    description: String = ""
) : Setting<Int>(name, module, value, description) {
    /**
     * 获取红色分量
     */
    fun getRed(): Int {
        return (value shr 16) and 0xFF
    }

    /**
     * 获取绿色分量
     */
    fun getGreen(): Int {
        return (value shr 8) and 0xFF
    }

    /**
     * 获取蓝色分量
     */
    fun getBlue(): Int {
        return value and 0xFF
    }

    /**
     * 获取透明度分量
     */
    fun getAlpha(): Int {
        return (value shr 24) and 0xFF
    }

    /**
     * 设置红色分量
     */
    fun setRed(red: Int) {
        value = (value and 0xFF00FFFF.toInt()) or ((red and 0xFF) shl 16)
    }

    /**
     * 设置绿色分量
     */
    fun setGreen(green: Int) {
        value = (value and 0xFFFF00FF.toInt()) or ((green and 0xFF) shl 8)
    }

    /**
     * 设置蓝色分量
     */
    fun setBlue(blue: Int) {
        value = (value and 0xFFFFFF00.toInt()) or (blue and 0xFF)
    }

    /**
     * 设置透明度分量
     */
    fun setAlpha(alpha: Int) {
        value = (value and 0x00FFFFFF) or ((alpha and 0xFF) shl 24)
    }
}