package com.prayertv.oujda

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * مشغل الصوت للأذان والإقامة
 */
class AudioPlayer(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null
    
    fun playAdhan(onComplete: () -> Unit) {
        onCompletionListener = onComplete
        playAudio(R.raw.adhan_makkah)
    }
    
    fun playIqama(onComplete: () -> Unit) {
        onCompletionListener = onComplete
        playAudio(R.raw.iqama)
    }
    
    private fun playAudio(resourceId: Int) {
        try {
            stop()
            
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.setOnCompletionListener {
                onCompletionListener?.invoke()
                stop()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            onCompletionListener?.invoke()
        }
    }
    
    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}
