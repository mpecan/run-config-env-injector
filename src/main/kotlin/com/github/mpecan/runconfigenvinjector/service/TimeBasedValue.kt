package com.github.mpecan.runconfigenvinjector.service

class TimeBasedValue(
    val expirationTime: Long,
    override val value: String
) : CacheableValue {

    override val isExpired: Boolean
        get() = System.currentTimeMillis() > expirationTime

    override fun toString(): String {
        return "TimeBasedCacheableValue(expirationTime=$expirationTime, value='$value')"
    }
}