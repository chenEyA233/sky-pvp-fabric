package cn.cheneya.skypvp.utils

data class EntityTargetingInfo(val classification: EntityTargetClassification, val isFriend: Boolean) {
    companion object {
        val DEFAULT = EntityTargetingInfo(EntityTargetClassification.TARGET, false)
    }
}

enum class EntityTargetClassification {
    TARGET,
    INTERESTING,
    IGNORED
}