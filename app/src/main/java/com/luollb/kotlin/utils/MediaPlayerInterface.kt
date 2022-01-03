package com.luollb.kotlin.utils

import android.media.MediaPlayer

interface MediaPlayerInterface {
    fun onPrepared()
    fun onBufferingUpdate(percent: Int)
    fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int)
}