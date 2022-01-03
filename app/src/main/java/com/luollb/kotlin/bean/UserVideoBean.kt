package com.luollb.kotlin.bean

import org.json.JSONObject

class UserVideoBean(jsonObject: JSONObject) {

    val width = jsonObject.optInt("width")

    val height = jsonObject.optInt("height")

    val ratio = jsonObject.optString("ratio")

    val duration = jsonObject.optInt("duration")

    var playApi = jsonObject.optString("playApi")

    var originCover = jsonObject.optString("originCover")

    init {
        if (playApi.startsWith("//")) {
            playApi = "https:$playApi"
        }

        if (originCover.startsWith("//")) {
            originCover = "https:$originCover"
        }
    }

    override fun toString(): String {
        return "UserVideoBean(width=$width, height=$height, ratio='$ratio', duration=$duration, playApi='$playApi', originCover='$originCover')"
    }
}