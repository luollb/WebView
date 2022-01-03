package com.luollb.kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.media.Image
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.luollb.kotlin.fragment.*
import com.luollb.kotlin.http.OkhttpManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class MainActivity : AppCompatActivity() {

    private val manager = supportFragmentManager
    private var controller: WindowInsetsControllerCompat? = null

    private val permissionS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideStatusBars()

        setContentView(R.layout.activity_main)

        val begin = manager.beginTransaction()
        //begin.replace(R.id.cur_fragment, CustomFragment())
        //begin.replace(R.id.cur_fragment, CameraFragment())
        //begin.replace(R.id.cur_fragment, CameraXFragment())
        //begin.replace(R.id.cur_fragment, VideoPlayerFragment())
        //begin.replace(R.id.cur_fragment, WebViewFragment())
        begin.replace(R.id.cur_fragment, WebViewUserFragment())
        begin.commit()

        permissionUtil()
    }

    //申请相机权限
    private fun permissionUtil() {
        var curIndex = 0
        for (permission in permissionS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                curIndex++
            }
        }
        if (curIndex > 0) {
            ActivityCompat.requestPermissions(this, permissionS, 11)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    /**
     * 隐藏状态栏
     */
    @SuppressLint("WrongConstant")
    private fun hideStatusBars() {

        //刘海屏适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attributes = window.attributes as WindowManager.LayoutParams
            attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = attributes
        }

        //设置内容延伸到状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        //设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT

        if (controller == null) {
            controller = ViewCompat.getWindowInsetsController(findViewById(android.R.id.content))
        }

        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            controller?.hide(WindowInsets.Type.statusBars())
        } else {
            //隐藏状态栏
            controller?.hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        println("event ~~~~~~~~~~~~~~~ ${Thread.currentThread()}")
        println("event ~~~~~~~~~~~~~~~ ${event.url}")
    }

    fun demoClass() {
        val clzz = this::class.java

        val method = clzz.getMethod("onMessageEvent", MessageEvent::class.java)
        val annotation = method.isAnnotationPresent(Subscribe::class.java)
        if (annotation) {
            val methodAnno = method.getAnnotation(Subscribe::class.java)
            println("method methodAnno.threadMode = ${methodAnno.threadMode}")
            println("method methodAnno.priority = ${methodAnno.priority}")
            println("method methodAnno.sticky = ${methodAnno.sticky}")

            if (methodAnno.threadMode == ThreadMode.BACKGROUND) {
                Thread {
                    val mess = MessageEvent()
                    mess.url = "demo"
                    method.invoke(clzz.newInstance(), mess)
                }.start()
            }
        }
        println("method annotation = $annotation")
        println("method = ${method.name}")
    }
}
