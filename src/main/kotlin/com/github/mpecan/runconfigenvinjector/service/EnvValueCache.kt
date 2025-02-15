package com.github.mpecan.runconfigenvinjector.service

import com.intellij.openapi.components.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class EnvValueCache {
    private val cachedTokens = ConcurrentHashMap<String, CacheableValue>()

    fun getValue(delegate: EnvProvider<*>): CacheableValue? {
        val cacheKey = delegate.getCacheKey()
        cachedTokens[cacheKey]?.let {
            if (!it.isExpired) {
                return it
            }
        }
        return delegate.getValue()?.also {
            if (it.isExpired) {
                return@also
            }
            cachedTokens[cacheKey] = it
        }

    }

}