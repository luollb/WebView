package com.luollb.kotlin

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("https://www.douyin.com/video/7009905367727508766")
            .addHeader("cookie","MONITOR_WEB_ID=0a7aff4c-7ccc-41d2-9220-4c9753c4276c; MONITOR_DEVICE_ID=29c9e16f-104f-438b-9c86-4eea982ec72d; douyin.com; ttcid=d958b91481c24659a6516ffa5638afa022; ttwid=1%7CRKRQS1LE_xE2TTNI6GefmsQ6nXi3RdCKye4LLyDLsAg%7C1640765212%7Ce1883ebc9b7cc35de35e070129e3b68fc975dd341adea488d784c6cce39364be; _tea_utm_cache_6383=undefined; home_can_add_dy_2_desktop=0; passport_csrf_token_default=5a4d8c448310ebe51fc0d1338c7f8209; passport_csrf_token=5a4d8c448310ebe51fc0d1338c7f8209; s_v_web_id=verify_kxr98w61_d0MxGKOK_muzW_4oeB_8Dyo_bLFzwoyfSkae; THEME_STAY_TIME=299582; IS_HIDE_THEME_CHANGE=1; AB_LOGIN_GUIDE_TIMESTAMP=1640818705241; MONITOR_WEB_ID=c44e81f8-fa1b-4bc7-b6ed-712905727f48; msToken=W7OjhivrTwt_a7hCdoxKEItBufMGIhjTpi3Xay8bLls8TR3XOSxJ6pShRBxFrvKDME7MJImffBql5VKb86dxr_BL21GWPb9POTa_oPjnKtQr1yCNJovGEac=; msToken=lmJ0OkMmjZD8Bc4RGH5QLpzdVzSRHpY-Pq46bvfJTy6CBhwEm9-9qcDsj-Wzxuam56Ox33KMHVBrSYpj64XzxMdDYofMmcIxjnoui2inNfAo8SEb6RXRcqkD5W59ukZmpw==; __ac_nonce=061cd7829001149b33c7e; __ac_signature=_02B4Z6wo00f01gUuvfwAAIDA2cPnKWrupRoFGrlAAOCUhJfWvOS3kYsK8gw3QqXMouBZI2TbYEE8N.yE8ZqxaHQZYgqbYY11099XtEYGOMor3fxT1AapCWRS0OtNt-75.Fjaj6Sg41Aftkapf9; tt_scid=vECTtwbpDDAXKF31nDsAS2Th-Y9-G-BIW9H53FT19CZXfidbIVqcx8pmKEc9sTS49d19")
            .build()

        val body = client.newCall(request).execute()

        println("body = ${body.body?.string()}")
    }
}