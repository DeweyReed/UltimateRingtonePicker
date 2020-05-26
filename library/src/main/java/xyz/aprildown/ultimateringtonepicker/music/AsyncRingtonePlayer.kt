package xyz.aprildown.ultimateringtonepicker.music

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import xyz.aprildown.ultimateringtonepicker.ASSET_URI_PREFIX
import xyz.aprildown.ultimateringtonepicker.isLOrLater
import xyz.aprildown.ultimateringtonepicker.isOOrLater
import java.io.IOException

/**
 * This class controls playback of ringtones. Uses [MediaPlayer] in a
 * dedicated thread so that this class can be called from the main thread. Consequently, problems
 * controlling the ringtone do not cause ANRs in the main thread of the application.
 *
 * Ringtone playback is accomplished using
 * [MediaPlayer]. android.permission.READ_EXTERNAL_STORAGE is required to play custom
 * ringtones located on the SD card using this mechanism.
 */
internal class AsyncRingtonePlayer(
    /** The context. */
    private val mContext: Context
) {

    companion object {
        // Volume suggested by media team for in-call alarms.
        private const val IN_CALL_VOLUME = 0.125f

        // Message codes used with the ringtone thread.
        private const val EVENT_PLAY = 1
        private const val EVENT_STOP = 2
        private const val RINGTONE_URI_KEY = "RINGTONE_URI_KEY"
        private const val LOOP = "LOOP"
        private const val STREAM_TYPE = "STREAM_TYPE"

        /**
         * @return `true` iff the device is currently in a telephone call
         */
        private fun isInTelephoneCall(context: Context): Boolean {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.callState != TelephonyManager.CALL_STATE_IDLE
        }
    }

    /** Handler running on the ringtone thread.  */
    private val mHandler: Handler =
        object : Handler(HandlerThread("ringtone-player").apply { start() }.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    EVENT_PLAY -> {
                        val data = msg.data
                        val uri = data.getParcelable<Uri?>(RINGTONE_URI_KEY)
                        if (uri != mPlaybackDelegate.currentPlayingUri) {
                            mPlaybackDelegate.stop(mContext)
                            mPlaybackDelegate.play(
                                mContext,
                                uri,
                                data.getBoolean(LOOP),
                                data.getInt(STREAM_TYPE)
                            )
                        }
                    }
                    EVENT_STOP -> mPlaybackDelegate.stop(mContext)
                }
            }
        }

    private val mPlaybackDelegate: PlaybackDelegate by lazy {
        MediaPlayerPlaybackDelegate()
    }

    fun play(ringtoneUri: Uri, loop: Boolean, streamType: Int) {
        postMessage(EVENT_PLAY, ringtoneUri, loop, streamType)
    }

    fun stop() {
        postMessage(EVENT_STOP, null, false, 0)
    }

    val isPlaying: Boolean get() = mPlaybackDelegate.currentPlayingUri != null

    /**
     * Posts a message to the ringtone-thread handler.
     *
     * @param messageCode the message to post
     * @param ringtoneUri the ringtone in question, if any
     */
    private fun postMessage(messageCode: Int, ringtoneUri: Uri?, loop: Boolean, streamType: Int) {
        synchronized(this) {
            val message = mHandler.obtainMessage(messageCode)
            if (ringtoneUri != null) {
                val bundle = Bundle()
                bundle.putParcelable(RINGTONE_URI_KEY, ringtoneUri)
                bundle.putBoolean(LOOP, loop)
                bundle.putInt(STREAM_TYPE, streamType)
                message.data = bundle
            }

            mHandler.sendMessage(message)
        }
    }

    private fun checkAsyncRingtonePlayerThread() {
        check(Looper.myLooper() == mHandler.looper) {
            "Must be on the AsyncRingtonePlayer thread!"
        }
    }

    /**
     * This interface abstracts away the differences between playing ringtones via [MediaPlayer].
     */
    private interface PlaybackDelegate {

        var currentPlayingUri: Uri?

        fun play(context: Context, ringtoneUri: Uri?, loop: Boolean, streamType: Int)

        fun stop(context: Context)
    }

    /**
     * Loops playback of a ringtone using [MediaPlayer].
     */
    private inner class MediaPlayerPlaybackDelegate : PlaybackDelegate, OnAudioFocusChangeListener {

        /** The audio focus manager. Only used by the ringtone thread.  */
        private var mAudioManager: AudioManager? = null

        /** Non-`null` while playing a ringtone; `null` otherwise.  */
        private var mMediaPlayer: MediaPlayer? = null

        private var mLoop: Boolean = false
        private var mStreamType: Int = 0
        private var audioAttributes: AudioAttributes? = null

        /**
         * Starts the actual playback of the ringtone. Executes on ringtone-thread.
         */

        override var currentPlayingUri: Uri? = null

        override fun play(context: Context, ringtoneUri: Uri?, loop: Boolean, streamType: Int) {
            checkAsyncRingtonePlayerThread()
            mLoop = loop
            mStreamType = streamType
            if (isLOrLater()) {
                audioAttributes = AudioAttributes.Builder()
                    .setLegacyStreamType(mStreamType)
                    .build()
            }

            if (mAudioManager == null) {
                mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            }

            val inTelephoneCall = isInTelephoneCall(context)
            var alarmNoise: Uri? = if (inTelephoneCall) null else ringtoneUri
            // Fall back to the system default alarm if the database does not have an alarm stored.
            if (alarmNoise == null) {
                alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            mMediaPlayer = MediaPlayer()
            mMediaPlayer?.setOnErrorListener { _, _, _ ->
                stop(context)
                true
            }

            try {
                // If alarmNoise is a custom ringtone on the sd card the app must be granted
                // android.permission.READ_EXTERNAL_STORAGE. Pre-M this is ensured at app
                // installation time. M+, this permission can be revoked by the user any time.
                currentPlayingUri = alarmNoise

                when {
                    alarmNoise?.toString()?.startsWith(ASSET_URI_PREFIX) == true -> {
                        val fileName = alarmNoise.toString().removePrefix(ASSET_URI_PREFIX)
                        mContext.assets.openFd(fileName).use { afd ->
                            mMediaPlayer?.setDataSource(
                                afd.fileDescriptor,
                                afd.startOffset,
                                afd.length
                            )
                        }
                    }
                    else -> {
                        mMediaPlayer?.setDataSource(context, alarmNoise!!)
                    }

                }


                startPlayback(inTelephoneCall)
            } catch (t: Throwable) {
                currentPlayingUri = null
                // The alarmNoise may be on the sd card which could be busy right now.
                try {
                    // Must reset the media player to clear the error state.
                    mMediaPlayer?.reset()
                } catch (t2: Throwable) {
                    // At this point we just don't play anything.
                }
            }
        }

        /**
         * Prepare the MediaPlayer for playback if the alarm stream is not muted, then start the
         * playback.
         *
         * @param inTelephoneCall `true` if there is currently an active telephone call
         * @return `true` if a crescendo has started and future volume adjustments are
         * required to advance the crescendo effect
         */
        @Throws(IOException::class)
        private fun startPlayback(inTelephoneCall: Boolean) {
            // Do not play alarms if stream volume is 0 (typically because ringer mode is silent).
            if (mAudioManager?.getStreamVolume(mStreamType) == 0) {
                return
            }

            // Indicate the ringtone should be played via the alarm stream.
            if (isLOrLater()) {
                mMediaPlayer?.setAudioAttributes(audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                mMediaPlayer?.setAudioStreamType(mStreamType)
            }

            // Check if we are in a call. If we are, use the in-call alarm resource at a low volume
            // to not disrupt the call.
            if (inTelephoneCall) {
                mMediaPlayer?.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME)
            }

            mMediaPlayer?.run {
                isLooping = mLoop
                if (!mLoop) {
                    setOnCompletionListener {
                        stop(mContext)
                    }
                }
                prepare()
                if (isOOrLater()) {
                    mAudioManager?.requestAudioFocus(createAudioFocusRequest(audioAttributes!!))
                } else {
                    @Suppress("DEPRECATION")
                    mAudioManager?.requestAudioFocus(
                        this@MediaPlayerPlaybackDelegate,
                        mStreamType, AUDIOFOCUS_GAIN_TRANSIENT
                    )
                }

                start()
            }
        }

        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AUDIOFOCUS_LOSS, AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> stop()
            }
        }

        /**
         * Stops the playback of the ringtone. Executes on the ringtone-thread.
         */
        override fun stop(context: Context) {
            checkAsyncRingtonePlayerThread()

            currentPlayingUri = null

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer?.stop()
                mMediaPlayer?.release()
                mMediaPlayer = null
            }

            if (isOOrLater()) {
                if (audioAttributes != null) {
                    mAudioManager?.abandonAudioFocusRequest(
                        createAudioFocusRequest(audioAttributes!!)
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                mAudioManager?.abandonAudioFocus(this)
            }
        }

        @TargetApi(Build.VERSION_CODES.O)
        @RequiresApi(Build.VERSION_CODES.O)
        private fun createAudioFocusRequest(aa: AudioAttributes): AudioFocusRequest {
            return AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT)
                .setOnAudioFocusChangeListener(this@MediaPlayerPlaybackDelegate)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(false)
                .setAudioAttributes(aa)
                .build()
        }
    }
}