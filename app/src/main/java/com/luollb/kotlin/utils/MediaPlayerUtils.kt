package com.luollb.kotlin.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.luollb.kotlin.widget.UserVideoView

class MediaPlayerUtils {

    companion object {
        private const val TAG = "MediaPlayerUtils"
    }

    private var mediaPlayer: MediaPlayer? = null

    private var listener: MediaPlayerInterface? = null

    private val preparedListener = MediaPlayer.OnPreparedListener { mp ->
        listener?.onPrepared()
    }

    private val onBufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { mp, percent ->
        listener?.onBufferingUpdate(percent)
    }

    private val onVideoSizeChangedListener =
        MediaPlayer.OnVideoSizeChangedListener { mp, width, height ->
            listener?.onVideoSizeChanged(mp, width, height)
        }

    fun createMediaPlayer(url: String, surface: Surface, listener: MediaPlayerInterface) {
        if (mediaPlayer != null) {
            return
        }

        this.listener = listener
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer = MediaPlayer().apply {
            reset()
            setAudioAttributes(audioAttributes)
            setDataSource(url)
            //setDisplay(holder)
            setSurface(surface)
            setOnPreparedListener(preparedListener)
            setOnBufferingUpdateListener(onBufferingUpdateListener)
            setOnVideoSizeChangedListener(onVideoSizeChangedListener)
            setScreenOnWhilePlaying(true)//保证视频播放时屏幕不关闭，默认false
            isLooping = true
            prepareAsync()
        }
    }

    fun start() {
        mediaPlayer?.start()
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun playerState() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        } else {
            start()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun currentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun destroyedMediaPlayer() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        listener = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}