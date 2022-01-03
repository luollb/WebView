package com.luollb.kotlin.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.luollb.kotlin.R
import com.luollb.kotlin.adapter.BaseRecyclerViewAdapter
import com.luollb.kotlin.bean.UserItemBean
import com.luollb.kotlin.bean.UserVideoBean
import com.luollb.kotlin.http.OkhttpManager
import com.luollb.kotlin.model.UserVideoModel
import com.luollb.kotlin.widget.LoadingView
import com.luollb.kotlin.widget.UserVideoView
import com.squareup.picasso.Picasso
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLDecoder

class WebViewUserFragment : Fragment(), BaseRecyclerViewAdapter.RecyclerViewCreate {

    companion object {
        private const val GET_WEB_DATA = 100
        private const val UPDATE_DATA = 101
        private const val SLIDE = 102

        private const val GET_WEB_DATA_DELAYED = 200L
    }

    private var handler: Handler? = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_WEB_DATA -> {
                    webView.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);")
                }
                UPDATE_DATA -> {

                }
                SLIDE -> {
                    if (isWebViewSlide) {
                        webView.scrollBy(0, 500)
                        removeMessages(GET_WEB_DATA)
                        sendMessageDelayed(obtainMessage(GET_WEB_DATA), GET_WEB_DATA_DELAYED)
                    }
                }
            }
        }
    }

    private val url =
        "https://www.douyin.com/user/MS4wLjABAAAAI9rp9kZDbJ81bp0urmkJOJ7RfzTiVD_VK6v1KFRazjc"

    private lateinit var webView: WebView
    private lateinit var viewPager2: ViewPager2
    private lateinit var loadingView: LoadingView

    private lateinit var adapter: BaseRecyclerViewAdapter<UserItemBean>

    private lateinit var userVideoModel: UserVideoModel

    private val listVideo = arrayListOf<UserItemBean>()

    private var isEnd = false //是否是最后，没有更多了

    private var isWebViewSlide = false//WebView是否滑动

    private lateinit var cookie: String

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
            if (webView.visibility != View.GONE) {
                webView.visibility = View.GONE
            }

            if (listVideo.isEmpty()) {
                handler?.sendMessageDelayed(
                    handler?.obtainMessage(GET_WEB_DATA)!!,
                    GET_WEB_DATA_DELAYED
                )
            }
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            println("onPageCommitVisible : url=$url")
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val requestUrl = request?.url.toString()
            println("shouldInterceptRequest = $requestUrl")
            return super.shouldInterceptRequest(view, request)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_user_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userVideoModel = ViewModelProvider(requireActivity()).get(UserVideoModel::class.java)
        userVideoModel.getData().observe(this, {
            loadingView.visibility = View.GONE
            adapter.notifyDataSetChangedAll(it)
        })

        webView = view.findViewById(R.id.webView)
        viewPager2 = view.findViewById(R.id.page)
        loadingView = view.findViewById(R.id.loadingView)

        viewPager2.offscreenPageLimit = 1

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == 0) {
                    val rv = viewPager2.getChildAt(0) as RecyclerView

                    if (!isWebViewSlide && !isEnd && viewPager2.currentItem == (listVideo.size - 5)) {
                        isWebViewSlide = true
                        handler?.sendMessage(handler?.obtainMessage(SLIDE)!!)
                    }

                    for (i in 0 until rv.childCount) {
                        (rv.getChildAt(i) as UserVideoView).sizeChange()
                    }
                }
            }
        })

        adapter = BaseRecyclerViewAdapter(requireContext(), listVideo, this)
        viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
        viewPager2.adapter = adapter

        webViewConfig()
        webView.visibility = View.INVISIBLE
        webView.loadUrl(url)
    }

    override fun onResume() {
        super.onResume()
        webView.resumeTimers()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.pauseTimers()
        webView.onPause()
    }

    override fun onDestroyView() {
        webView.webChromeClient = null
        webView.clearSslPreferences()
        webView.clearMatches()
        webView.clearHistory()
        webView.clearCache(true)
        webView.removeAllViews()
        webView.destroy()

        handler?.removeCallbacksAndMessages(null)
        handler = null
        userVideoModel.getData().removeObservers(this)
        super.onDestroyView()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun webViewConfig() {

        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

            setSupportZoom(true)//设置支持缩放
            builtInZoomControls = true//设置缩放
            displayZoomControls = false//隐藏图标

            setSupportMultipleWindows(false)

            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
            databaseEnabled = true
            userAgentString =
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36"

            mediaPlaybackRequiresUserGesture = true
            loadsImagesAutomatically = false//禁止加载图片
        }

        webView.webChromeClient = webChromeClient
        webView.webViewClient = webViewClient
        webView.requestFocus()
        webView.setInitialScale(100)
        webView.addJavascriptInterface(this, "local_obj")

    }

    @JavascriptInterface
    fun showSource(html: String) {
        synchronized(WebViewUserFragment::class.java) {
            if (isEnd) {
                return
            }

            if (html.contains("暂时没有更多了")) {
                println("暂时没有更多了~~~~~~~~~~~~~~~~~~~~~~~~~")
                isEnd = true
            }

            val parse = Jsoup.parse(html)

            val liItems = parse.getElementsByClass("eHM3dHJS")
            println("showSource liItems=${liItems.size}")

            val increaseNumber = (liItems.size - listVideo.size)

            //item一样不处理
            if (increaseNumber == 0 && isWebViewSlide) {
                handler?.sendMessage(handler?.obtainMessage(SLIDE)!!)
                return
            } else if (increaseNumber == 0) {
                return
            } else {
                isWebViewSlide = false
            }

            for (i in listVideo.size until liItems.size) {
                val href = "https:${liItems[i].attr("href")}"
                val src = "https:${liItems[i].select("img")[0].attr("src")}"
                val alt = liItems[i].select("img")[0].attr("alt")
                val userVideoBean = getUserVideoData(href)

                listVideo.add(UserItemBean(href, src, alt, userVideoBean))
                //只更新一次
                if (i == 5) {
                    userVideoModel.getData().postValue(listVideo)
                }
            }

            println("showSource listVideo=${listVideo.size}")
        }
    }

    /**
     * 获取视频页面,视频数据，同步执行
     */
    private fun getUserVideoData(href: String): UserVideoBean {
        val userVideoBean: UserVideoBean
        val client = OkhttpManager.getSinger().getClient()
        val request = Request.Builder()
            .url(href)
            .addHeader("cookie", cookie)
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36"
            )
            .build()

        val body = client.newCall(request).execute()
        val string = body.body?.string()
        userVideoBean = if (string != null) {
            val html = Jsoup.parse(string).getElementById("RENDER_DATA")?.html()
            val decode = URLDecoder.decode(html, "utf-8")
            val video = JSONObject(decode).optJSONObject("21")?.optJSONObject("aweme")
                ?.optJSONObject("detail")?.optJSONObject("video")
            UserVideoBean(video!!)
        } else {
            getUserVideoData(href)
        }
        println("showSource userVideoBean=$userVideoBean ")
        return userVideoBean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View {
        val view = UserVideoView(requireContext())
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.layoutParams = params
        return view
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.BaseViewHolder, position: Int) {
        (holder.itemView as UserVideoView).updateData(
            listVideo[position].userVideoBean,
            position
        )
    }
}