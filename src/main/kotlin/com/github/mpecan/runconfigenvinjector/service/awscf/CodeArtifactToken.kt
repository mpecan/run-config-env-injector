package com.github.mpecan.runconfigenvinjector.service.awscf

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.mpecan.runconfigenvinjector.service.CacheableValue
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class CodeArtifactToken(private val token: String) : CacheableValue {
    private val header = token.substringBefore('.').let{ Base64.getDecoder().decode(it.toByteArray()) }
        .let { ObjectMapper().readValue(it, CodeArtifactTokenHeader::class.java) }

    override val isExpired: Boolean
        get() = (header.exp - 120) < ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()

    override val value: String
        get() = token

    data class CodeArtifactTokenHeader(
        val ver: Int,
        val isu: Long,
        val enc: String,
        val tag: String,
        val exp: Long,
        val alg: String,
        val iv: String
    ){
        @SuppressWarnings("unused") // Required for deserialization
        constructor(): this(0, 0, "", "", 0, "", "")
    }
}