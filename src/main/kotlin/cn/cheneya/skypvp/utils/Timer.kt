package cn.cheneya.skypvp.utils

class Timer {
    private var time = System.currentTimeMillis()

    /**
     * 检查是否已经过去了指定的毫秒数
     * @param ms 毫秒数
     * @return 如果已经过去了指定的毫秒数，则返回true
     */
    fun hasReached(ms: Long): Boolean {
        return System.currentTimeMillis() - time >= ms
    }

    /**
     * 重置计时器
     */
    fun reset() {
        time = System.currentTimeMillis()
    }

    /**
     * 获取自上次重置以来经过的时间（毫秒）
     * @return 经过的时间（毫秒）
     */
    fun getTimePassed(): Long {
        return System.currentTimeMillis() - time
    }

    private var delayTime: Long = 0
    
    /**
     * 设置延迟时间
     * @param ms 延迟毫秒数
     */
    fun setDelay(ms: Long) {
        delayTime = ms
    }
    
    /**
     * 检查是否已经达到延迟时间
     */
    fun hasDelayed(): Boolean {
        return System.currentTimeMillis() - time >= delayTime
    }
}
