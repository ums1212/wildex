package dev.comon.wildex.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiHeaderDto(
    val resultCode: String?,
    val resultMsg: String?
)
