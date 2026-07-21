package de.friedhofsender.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class StreamInfo(
    val title: String,
    val artist: String,
    val isLive: Boolean = true
)