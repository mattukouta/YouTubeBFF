package com.kouta.youtubebff.vo.enums

import com.fasterxml.jackson.annotation.JsonProperty

enum class LiveBroadcastContent(val value: String) {
    @JsonProperty("upcoming") UPCOMING("upcoming"),
    @JsonProperty("live") LIVE("live"),
    @JsonProperty("none") NONE("none");

    companion object {
        fun findByValue(value: String) = LiveBroadcastContent.entries.firstOrNull { it.value == value }
    }
}