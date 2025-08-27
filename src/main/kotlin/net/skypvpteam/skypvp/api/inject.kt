package net.skypvpteam.skypvp.api

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class ClassInject(
    val className: String,
    val required: Boolean = true
)

/**
 * API加载器接口
 */
interface ApiLoader {
    /**
     * 加载指定类
     */
    fun <T : Any> loadApi(apiClass: KClass<T>): T?

    /**
     * 检查API是否可用
     */
    fun isApiAvailable(className: String): Boolean
}

/**
 * API注入工具类
 */
object ApiInjector {
    val loader: ApiLoader by lazy { DefaultApiLoader() }

    /**
     * 获取API实例
     */
    inline fun <reified T : Any> getApi(): T? {
        return loader.loadApi(T::class)
    }
}

/**
 * 默认API加载器实现
 */
private class DefaultApiLoader : ApiLoader {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> loadApi(apiClass: KClass<T>): T? {
        val Inject = apiClass.annotations.find { it is ClassInject } as? ClassInject
            ?: return null

        return try {
            Class.forName(Inject.className).kotlin.objectInstance as? T
                ?: Class.forName(Inject.className).newInstance() as? T
        } catch (e: Exception) {
            if (Inject.required) {
                throw IllegalStateException("Failed to load required API: $Inject.className}", e)
            }
            null
        }
    }

    override fun isApiAvailable(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}
