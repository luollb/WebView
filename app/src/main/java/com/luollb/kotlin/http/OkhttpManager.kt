package com.luollb.kotlin.http

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class OkhttpManager private constructor() {

    companion object {

        private val client = OkHttpClient.Builder()
            .connectTimeout(20000, TimeUnit.MILLISECONDS)
            .readTimeout(15000, TimeUnit.MILLISECONDS)
            .writeTimeout(15000, TimeUnit.MILLISECONDS)
            .build()

        @JvmStatic
        fun getSinger() = OkhttpManager()

    }

    fun getClient(): OkHttpClient {
        return client
    }

    fun accessNetworks(url: String, callback: Callback) {
        val request = Request.Builder()
            .url(url)
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36"
            )
            .build()

        val call = client.newCall(request)

        call.enqueue(callback)
    }

}