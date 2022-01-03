package com.luollb.kotlin.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.luollb.kotlin.R
import com.luollb.kotlin.bean.UserVideoBean
import com.luollb.kotlin.utils.MediaPlayerInterface
import com.luollb.kotlin.utils.MediaPlayerUtils
import com.squareup.picasso.Picasso
import kotlin.math.roundToInt

class UserVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle), MediaPlayerInterface,
    TextureView.SurfaceTextureListener {

    companion object {
        private const val TAG = "UserVideoView"
    }

    private var ivPreview: ImageView
    private var textureView: TextureView
    private var position = 0

    private lateinit var userVideoBean: UserVideoBean

    private val mediaPlayerUtils: MediaPlayerUtils = MediaPlayerUtils()

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.user_video_item_view, this, true)
        ivPreview = view.findViewById(R.id.iv_preview)
        textureView = view.findViewById(R.id.texture_view)

        textureView.setOnClickListener {
            mediaPlayerUtils.playerState()
        }

        textureView.surfaceTextureListener = this
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE && top == 0) {
            postDelayed({
                onPrepared()
            }, 100)
        } else if (visibility == GONE && top == 0) {
            mediaPlayerUtils.pause()
        }
    }

    /**
     * 添加数据更新UI
     */
    fun updateData(userVideoBean: UserVideoBean, position: Int) {
        this.userVideoBean = userVideoBean
        this.position = position
        Log.d(TAG, "updateData: position=$position")
        Picasso.get().load(userVideoBean.originCover).into(ivPreview)
    }

    /**
     * 滑动之后处理视频
     */
    fun sizeChange() {
        Log.d(TAG, "sizeChange: top=$top")
        if (textureView.surfaceTexture != null) {
            mediaPlayerUtils.createMediaPlayer(
                userVideoBean.playApi,
                Surface(textureView.surfaceTexture),
                this
            )
        }

        if (top == 0) {
            adjustVideoWindow()
            textureView.surfaceTextureListener = this
            onPrepared()
        } else {
            mediaPlayerUtils.pause()
        }
    }

    /**
     * 视频准备完成后开始播放,只播放当前显示的
     */
    override fun onPrepared() {
        if (top == 0) {
            mediaPlayerUtils.start()
        }
    }

    /**
     * 视频缓存进度
     */
    override fun onBufferingUpdate(percent: Int) {
        Log.d(TAG, "onBufferingUpdate: percent=$percent")
    }

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {

    }

    /**
     * TextureView显示之后调用
     */
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureAvailable: width=$width , height=$height")
        if (top == 0) {
            adjustVideoWindow()
        }
        mediaPlayerUtils.createMediaPlayer(
            userVideoBean.playApi,
            Surface(textureView.surfaceTexture),
            this
        )
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    /**
     * View销毁处理
     */
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.d(TAG, "onSurfaceTextureDestroyed: ")
        ivPreview.visibility = VISIBLE

        textureView.translationX = 0f
        textureView.translationY = 0f
        mediaPlayerUtils.destroyedMediaPlayer()
        textureView.surfaceTextureListener = null
        return true
    }

    /**
     * 缓存画面后隐藏占位图片
     */
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        if (ivPreview.visibility != GONE && mediaPlayerUtils.currentPosition() > 0) {
            ivPreview.visibility = GONE
        }
    }

    /**
     * 调整视频播放窗口大小，设置视频画面居中
     */
    private fun adjustVideoWindow() {
        val videoWidth = userVideoBean.width
        val videoHeight = userVideoBean.height

        val ratioWidth = videoWidth / width.toFloat()
        val ratioHeight = videoHeight / height.toFloat()

        val scaleWidth: Int
        val scaleHeight: Int

        if (ratioWidth == ratioHeight) {
            return
        }

        if (ratioWidth > ratioHeight) {
            scaleWidth = width
            scaleHeight = ((scaleWidth / videoWidth.toFloat()) * videoHeight).roundToInt()
        } else {
            scaleHeight = height
            scaleWidth = ((scaleHeight / videoHeight.toFloat()) * videoWidth).roundToInt()
        }

        val params = LayoutParams(textureView.layoutParams)
        params.width = scaleWidth
        params.height = scaleHeight
        textureView.layoutParams = params

        textureView.translationY = ((height - scaleHeight) / 2).toFloat()
        textureView.translationX = ((width - scaleWidth) / 2).toFloat()
    }
}