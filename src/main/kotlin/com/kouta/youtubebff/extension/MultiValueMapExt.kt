package com.kouta.youtubebff.extension

import org.springframework.util.MultiValueMap

fun MultiValueMap<String, String>.setAccessToken(accessToken: String) = this.set("access_token", accessToken)