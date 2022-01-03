package com.luollb.kotlin.fragment

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.luollb.kotlin.http.OkhttpManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException

class VideoPlayerFragment : Fragment() {

    private var url =
        "https://v26-web.douyinvod.com/440e82020a914f6249ed03e44e2bc75e/61c57c88/video/tos/cn/tos-cn-ve-15-alinc2/cddbf28de62a4f03ba3a995454755d76/?a=6383&br=1551&bt=1551&cd=0%7C0%7C0&ch=26&cr=0&cs=0&cv=1&dr=0&ds=3&er=&ft=VgcwUVIIL7ThWH7yc7AGZ&l=021640328814657fdbddc0200ff2f010a9eda60000000293509b5&lr=all&mime_type=video_mp4&net=0&pl=0&qs=0&rc=MzV0Zzc6Zmg8OjMzNGkzM0ApPDwzZzM2ZTs7NzVmNjc1M2cuXm9kcjRfZy9gLS1kLTBzc2FfXjA0YjM0NTMyYjY1NDI6Yw%3D%3D&vl=&vr="
    private lateinit var surfaceView: SurfaceView
    private lateinit var holder: SurfaceHolder

    private var mediaPlayer: MediaPlayer? = null

    private val preparedListener = MediaPlayer.OnPreparedListener { mp ->
        println("OnPreparedListener ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        mp?.start()
    }

    private val holderCallback = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            println("surfaceCreated")
            createMediaPlayer()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            println("surfaceChanged width = $width , height = $height")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            println("surfaceDestroyed")
            destroyedMediaPlayer()
        }
    }

    fun setUrl(url: String) {
        this.url = url
    }

    private fun createMediaPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer = MediaPlayer().apply {
            reset()
            setAudioAttributes(audioAttributes)
            setDataSource(url)
            setDisplay(holder)
            setOnPreparedListener(preparedListener)
            setScreenOnWhilePlaying(true)//保证视频播放时屏幕不关闭，默认false
            isLooping = true
            prepareAsync()
        }
    }

    private fun destroyedMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        surfaceView = SurfaceView(requireContext())
        holder = surfaceView.holder
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        surfaceView.layoutParams = params
        return surfaceView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        holder.addCallback(holderCallback)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }
}