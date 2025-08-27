package cn.cheneya.skypvp.features.module.modules.player

import cn.cheneya.skypvp.features.module.Category
import cn.cheneya.skypvp.features.module.Module
import net.minecraft.client.MinecraftClient
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import java.lang.reflect.Field

object ModuleFastPlace : Module("FastPlace", "快速放置", Category.PLAYER) {
    private val mc: MinecraftClient = MinecraftClient.getInstance()
    private var tickCallback: ClientTickEvents.EndTick? = null
    private var rightClickDelayField: Field? = null

    // 定义模式枚举
    enum class Mode {
        ALL,       // 所有方块
        BUILDING,  // 建筑方块（石头、木头等）
        REDSTONE   // 红石相关方块
    }

    // 使用Module基类的enumChoice方法创建设置
    private val mode = enumChoice("Mode", Mode.ALL)

    // 放置延迟设置 (0-4 ticks, 默认值为0，即无延迟)
    private val delay = integer("Delay", 0, 0, 4)

    init {
        // 尝试多个可能的字段名称
        val fieldNames = listOf("itemUseCooldown", "field_1752", "rightClickDelay", "field_1760")
        
        for (name in fieldNames) {
            try {
                rightClickDelayField = MinecraftClient::class.java.getDeclaredField(name)
                rightClickDelayField?.isAccessible = true
                println("FastPlace: 成功访问右键延迟字段: $name")
                break
            } catch (e: Exception) {
                continue
            }
        }
        
        if (rightClickDelayField == null) {
            println("FastPlace: 警告: 无法找到右键延迟字段，模块功能将受限")
        }
    }

    override fun onEnable() {
        tickCallback = ClientTickEvents.EndTick(this::onTick).also {
            ClientTickEvents.END_CLIENT_TICK.register(it)
        }
    }

    private fun onTick(client: MinecraftClient) {
        if (!enabled) return
        val field = rightClickDelayField ?: return
        
        try {
            val player = mc.player ?: return
            val stack = player.mainHandStack.takeIf { it.item is BlockItem } ?: return
            
            val blockItem = stack.item as BlockItem
            val block = blockItem.block

            // 根据模式决定是否应用快速放置
            val shouldApply = when (Mode.valueOf(mode.value)) {
                Mode.ALL -> true
                Mode.BUILDING -> isBuilding(block)
                Mode.REDSTONE -> isRedstone(block)
            }

            if (shouldApply) {
                // 设置右键点击延迟为配置的值
                val currentDelay = field.getInt(mc)
                if (currentDelay > delay.value) {
                    field.set(mc, delay.value.toInt())
                }
            } else {
                // 恢复默认延迟
                field.set(mc, 4)
            }
        } catch (e: Exception) {
            println("FastPlace: 处理tick时出错: ${e.message}")
            try {
                rightClickDelayField?.set(mc, 4) // 出错时恢复默认延迟
            } catch (e: Exception) {
                println("FastPlace: 恢复默认延迟失败: ${e.message}")
            }
        }
    }

    override fun onDisable() {
        tickCallback?.let {
            // 使用更可靠的方式注销事件监听器
            try {
                ClientTickEvents.END_CLIENT_TICK.register { _ -> } // 注册空回调覆盖
            } catch (e: Exception) {
                println("FastPlace: 事件注销失败, 使用覆盖方式: ${e.message}")
            }
            tickCallback = null
        }
        
        // 重置右键点击延迟字段
        try {
            rightClickDelayField?.set(mc, 4) // 恢复默认延迟
        } catch (e: Exception) {
            println("FastPlace: 重置右键延迟失败: ${e.message}")
        }
    }

    // 判断是否为建筑方块
    private fun isBuilding(block: Block): Boolean {
        val name = block.toString().lowercase()
        return name.contains("stone") || name.contains("wood") ||
               name.contains("planks") || name.contains("log") ||
               name.contains("dirt") || name.contains("grass") ||
               name.contains("sand") || name.contains("gravel") ||
               name.contains("brick") || name.contains("concrete")
    }

    // 判断是否为红石相关方块
    private fun isRedstone(block: Block): Boolean {
        val name = block.toString().lowercase()
        return name.contains("redstone") || name.contains("piston") ||
               name.contains("observer") || name.contains("repeater") ||
               name.contains("comparator") || name.contains("hopper") ||
               name.contains("dropper") || name.contains("dispenser")
    }

    fun init() {
        try {
            rightClickDelayField = MinecraftClient::class.java.getDeclaredField("itemUseCooldown")
            // 在某些版本中可能是不同的字段名
            if (rightClickDelayField == null) {
                rightClickDelayField = MinecraftClient::class.java.getDeclaredField("field_1752") // 混淆名
            }
            rightClickDelayField?.isAccessible = true
        } catch (e: Exception) {
            println("FastPlace: 无法访问右键点击延迟字段: ${e.message}")
        }
    }
}
