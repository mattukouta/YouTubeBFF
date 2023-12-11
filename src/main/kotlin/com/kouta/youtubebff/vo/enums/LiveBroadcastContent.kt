package com.kouta.youtubebff.vo.enums

import com.fasterxml.jackson.annotation.JsonProperty

enum class LiveBroadcastContent {
    @JsonProperty("upcoming") UPCOMING,
    @JsonProperty("live") LIVE,
    @JsonProperty("none") NONE;
}