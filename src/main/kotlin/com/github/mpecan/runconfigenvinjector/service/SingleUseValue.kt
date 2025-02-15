package com.github.mpecan.runconfigenvinjector.service

class SingleUseValue(
    override val value: String
) : CacheableValue {

    override val isExpired: Boolean
        get() = true

    override fun toString(): String {
        return "SingleUseValue(value='$value')"
    }
}