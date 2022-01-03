package com.luollb.kotlin.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment

import android.webkit.WebView
import androidx.collection.arrayMapOf
import com.luollb.kotlin.http.OkhttpManager
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup

class WebViewFragment : Fragment() {

    private val url = "https://www.douyin.com/"

    private lateinit var webView: WebView

    private var cookie = ""

    private val webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            println("onProgressChanged : newProgress=$newProgress")
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            println("title = $title")
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            val newWebView = WebView(requireContext())
            newWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val curUrl = request?.url.toString()
                    println("newWebView shouldOverrideUrlLoading = $curUrl")
                    webView.loadUrl(request?.url!!.toString())
                    return true
                }
            }
            val transport = resultMsg?.obj as WebView.WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }


    }

    private val webViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            println("shouldOverrideUrlLoading = ${request?.url}")
            val curUrl = request?.url!!.toString()
            webView.loadUrl(curUrl)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            println("onPageStarted : url=$url")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            cookie = CookieManager.getInstance().getCookie(url)
            super.onPageFinished(view, url)
            println("onPageFinished : url=$url")
//            view?.settings!!.apply {
//                loadsImagesAutomatically = true
//            }
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            println("onPageCommitVisible : url=$url")
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val urlCur = request?.url.toString()
            println("shouldInterceptRequest = $urlCur")
            if (jsonUrl.length < 2 && urlCur.contains("aweme") && urlCur.contains("tab/feed")) {
                jsonUrl = urlCur
                jsonData(urlCur)
            }
            return super.shouldInterceptRequest(view, request)
        }
    }

    private var jsonUrl = ""

    private var count = 0

    private fun jsonData(url: String) {
        Thread {
            val client = OkhttpManager.getSinger().getClient()

            val request = Request.Builder()
                .url(url)
                .addHeader("cookie", cookie)
                .addHeader(
                    "user-agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36"
                )
                .addHeader("referer", "https://www.douyin.com/")
                .build()

            val body = client.newCall(request).execute()
            val string = body.body?.string()
            println("cookieData string=$string}")
            val awemeList = JSONObject(string!!).optJSONArray("aweme_list")
            for (i in 0 until awemeList?.length()!!) {
                val item = JSONObject(awemeList[i].toString()).optJSONObject("video") ?: break

                val duration = item.optInt("duration")
                val height = item.optInt("height")
                val width = item.optInt("width")
                val ratio = item.optString("ratio")
                val origin_cover =
                    item.optJSONObject("origin_cover")?.optJSONArray("url_list")!![0]
                val play_addr = item.optJSONObject("play_addr")?.optJSONArray("url_list")!![0]
                val dataSize = item.optJSONObject("play_addr")?.optString("data_size")
                println("JSONData ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~$i")
                println("JSONData duration=$duration , height=$height , width=$width , ratio=$ratio , dataSize=$dataSize")
                println("JSONData origin_cover=$origin_cover")
                println("JSONData play_addr=$play_addr")
            }
            if (count < 5) {
                count++
                println()
                Thread.sleep(10000)
                println()
                jsonData(url)
            }
        }.start()

    }

    private var handler: Handler? = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            println("handleMessage: what=${msg.what}")
            webView.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);")
        }
    }

    companion object {
        private const val GET_WEB = 100
        private const val PAUSE_WEB_VIDEO = 101//暂停webView视频播放
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.statusBarColor = Color.BLACK

        webView = WebView(requireContext())
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        webView.layoutParams = params
        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webViewConfig()
        webView.loadUrl(url)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroyView() {
        webView.webChromeClient = null
        webView.clearHistory()
        webView.clearCache(true)
        webView.removeAllViews()
        webView.destroy()

        handler?.removeCallbacksAndMessages(null)
        handler = null
        super.onDestroyView()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun webViewConfig() {

        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            useWideViewPort = false
            loadWithOverviewMode = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

            setSupportZoom(true)//设置支持缩放
            builtInZoomControls = true//设置缩放
            displayZoomControls = false//隐藏图标

            setSupportMultipleWindows(true)

            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
            databaseEnabled = true
            userAgentString =
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36"

            mediaPlaybackRequiresUserGesture = true

            loadsImagesAutomatically = false

        }

        webView.webChromeClient = webChromeClient
        webView.webViewClient = webViewClient
        webView.requestFocus()
        webView.setInitialScale(100)
        webView.addJavascriptInterface(this, "local_obj")

    }

    private var isDown = true
    fun keyEventC() {
        //xgplayer-playswitch-next 下
        //xgplayer-playswitch-prev disabled 上
        println("keyEventC~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        webView.loadUrl("javascript:document.getElementsByClassName(\"xgplayer-playswitch-next\")[0].click();")

    }

    @JavascriptInterface
    fun showSource(html: String) {
        println("showSource html= $html")
        val parse = Jsoup.parse(html)
    }

}