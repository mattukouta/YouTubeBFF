package com.kouta.youtubebff.controller

import com.kouta.youtubebff.vo.SubscriptionVideo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SubscriptionVideoController {
    @GetMapping("/test")
    fun getGreetingByQuery(
        @RequestParam(
            name = "name", required = false, defaultValue = "world"
        ) name: String
    ) = SubscriptionVideo.Response(
        title = name
    )
}