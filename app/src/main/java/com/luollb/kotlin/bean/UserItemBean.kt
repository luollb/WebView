package com.luollb.kotlin.bean

import com.luollb.kotlin.http.OkhttpManager
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLDecoder

class UserItemBean(
    val href: String,
    val src: String,
    val alt: String,
    val userVideoBean: UserVideoBean
) {

}