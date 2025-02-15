package com.github.mpecan.runconfigenvinjector.service

interface CacheableValue {
    val isExpired: Boolean
    val value: String
}