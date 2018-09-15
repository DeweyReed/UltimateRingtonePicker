package xyz.aprildown.ringtone.music

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.media.AudioManager.*
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.telephony.TelephonyManager
import xyz.aprildown.ringtone.R
import xyz.aprildown.ringtone.getResourceUri
import xyz.aprildown.ringtone.isLOrLater
import xyz.aprildown.ringtone.isOOrLater
import java.io.IOException

/**
 * This class controls playback of ringtones. Uses [MediaPlayer] in a
 * dedicated thread so that this class can be called from the main thread. Consequently, problems
 * controlling the ringtone do not cause ANRs in the main thread of the application.
 *
 * Ringtone playback is accomplished using
 * [MediaPlayer]. android.permission.READ_EXTERNAL_STORAGE is required to play custom
 * ringtones located on the SD card using this mechanism. [MediaPlayer] allows clients to
 * adjust the volume of the stream and specify that the stream should be looped.
 *
 * If the [MediaPlayer] fails to play the requested audio, an
 * [in-app fallback][.getFallbackRingtoneUri] is used because playing **some**
 * sort of noise is always preferable to remaining silent.
 */
class AsyncRingtonePlayer(
        /** The context.  */
        private val mContext: Context) {

    companion object {
        // Volume suggested by media team for in-call alarms.
        private const val IN_CALL_VOLUME = 0.125f

        // Message codes used with the ringtone thread.
        private const val EVENT_PLAY = 1
        private const val EVENT_STOP = 2
        private const val EVENT_VOLUME = 3
        private const val RINGTONE_URI_KEY = "RINGTONE_URI_KEY"
        private const val CRESCENDO_DURATION_KEY = "CRESCENDO_DURATION_KEY"
        private const val LOOP = "LOOP"
        private const val STREAM_TYPE = "STREAM_TYPE"

        /**
         * @return `true` iff the device is currently in a telephone call
         */
        private fun isInTelephoneCall(context: Context): Boolean {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.callState != TelephonyManager.CALL_STATE_IDLE
        }

        private fun now(): Long {
            return SystemClock.elapsedRealtime()
        }

        /**
         * @return Uri of the ringtone to play when the user is in a telephone call
         */
        private fun getInCallRingtoneUri(context: Context): Uri {
            return context.getResourceUri(R.raw.default_ringtone)
        }

        /**
         * @return Uri of the ringtone to play when the chosen ringtone fails to play
         */
        private fun getFallbackRingtoneUri(context: Context): Uri {
            return context.getResourceUri(R.raw.default_ringtone)
        }

        /**
         * @param currentTime current time of the device
         * @param stopTime time at which the crescendo finishes
         * @param duration length of time over which the crescendo occurs
         * @return the scalar volume value that produces a linear increase in volume (in decibels)
         */
        private fun computeVolume(currentTime: Long, stopTime: Long, duration: Long): Float {
            // Compute the percentage of the crescendo that has completed.
            val elapsedCrescendoTime = (stopTime - currentTime).toFloat()
            val fractionComplete = 1 - elapsedCrescendoTime / duration

            // Use the fraction to compute a target decibel between -40dB (near silent) and 0dB (max).
            val gain = fractionComplete * 40 - 40

            // Convert the target gain (in decibels) into the corresponding volume scalar.
            @Suppress("UnnecessaryVariable")
            val volume = Math.pow(10.0, (gain / 20f).toDouble()).toFloat()

//            Timber.v("Ringtone crescendo %,.2f%% complete (scalar: %f, volume: %f dB)",
//                    fractionComplete * 100, volume, gain)

            return volume
        }
    }

    /** Handler running on the ringtone thread.  */
    @SuppressLint("StaticFieldLeak")
    private val mHandler: Handler = object : Handler(HandlerThread("ringtone-player").apply { start() }.looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EVENT_PLAY -> {
                    val data = msg.data
                    if (mPlaybackDelegate.play(mContext,
                                    data.getParcelable(RINGTONE_URI_KEY),
                                    data.getLong(CRESCENDO_DURATION_KEY),
                                    data.getBoolean(LOOP),
                                    data.getInt(STREAM_TYPE))) {
                        scheduleVolumeAdjustment()
                    }
                }
                EVENT_STOP -> mPlaybackDelegate.stop(mContext)
                EVENT_VOLUME -> if (mPlaybackDelegate.adjustVolume(mContext)) {
                    scheduleVolumeAdjustment()
                }
            }
        }
    }

    /**
     * Use the newer Ringtone-based playback delegate because it does not require
     * any permissions to read from the SD card. (M+)
     * Fall back to the older MediaPlayer-based playback delegate because it is the only
     * way to force the looping of the ringtone before M. (pre M)
     */
    /** [MediaPlayerPlaybackDelegate] */
    private val mPlaybackDelegate: PlaybackDelegate by lazy {
        MediaPlayerPlaybackDelegate()
    }

    fun play(ringtoneUri: Uri, crescendoDuration: Long, loop: Boolean, streamType: Int) {
        postMessage(EVENT_PLAY, ringtoneUri, crescendoDuration, loop, streamType, 0)
    }

    fun stop() {
        postMessage(EVENT_STOP, null, 0,
                false, 0, 0)
    }

    /** Schedules an adjustment of the playback volume 50ms in the future.  */
    private fun scheduleVolumeAdjustment() {
        // Ensure we never have more than one volume adjustment queued.
        mHandler.removeMessages(EVENT_VOLUME)
        // Queue the next volume adjustment.
        postMessage(EVENT_VOLUME, null, 0,
                false, 0, 50)
    }

    /**
     * Posts a message to the ringtone-thread handler.
     *
     * @param messageCode the message to post
     * @param ringtoneUri the ringtone in question, if any
     * @param crescendoDuration the length of time, in ms, over which to crescendo the ringtone
     * @param delayMillis the amount of time to delay sending the message, if any
     */
    private fun postMessage(messageCode: Int, ringtoneUri: Uri?, crescendoDuration: Long,
                            loop: Boolean, streamType: Int, delayMillis: Long) {
        synchronized(this) {
            val message = mHandler.obtainMessage(messageCode)
            if (ringtoneUri != null) {
                val bundle = Bundle()
                bundle.putParcelable(RINGTONE_URI_KEY, ringtoneUri)
                bundle.putLong(CRESCENDO_DURATION_KEY, crescendoDuration)
                bundle.putBoolean(LOOP, loop)
                bundle.putInt(STREAM_TYPE, streamType)
                message.data = bundle
            }

            mHandler.sendMessageDelayed(message, delayMillis)
        }
    }

    private fun checkAsyncRingtonePlayerThread() {
        if (Looper.myLooper() != mHandler.looper) {
            throw IllegalStateException("Must be on the AsyncRingtonePlayer thread!")
        }
    }

    /**
     * This interface abstracts away the differences between playing ringtones via [Ringtone]
     * vs [MediaPlayer].
     */
    private interface PlaybackDelegate {
        /**
         * @return `true` iff a [volume adjustment][.adjustVolume] should be scheduled
         */
        fun play(context: Context, ringtoneUri: Uri?, crescendoDuration: Long,
                 loop: Boolean, streamType: Int): Boolean

        fun stop(context: Context)

        /**
         * @return `true` iff another volume adjustment should be scheduled
         */
        fun adjustVolume(context: Context): Boolean
    }

    /**
     * Loops playback of a ringtone using [MediaPlayer].
     */
    private inner class MediaPlayerPlaybackDelegate : PlaybackDelegate,
            AudioManager.OnAudioFocusChangeListener {

        /** The audio focus manager. Only used by the ringtone thread.  */
        private var mAudioManager: AudioManager? = null

        /** Non-`null` while playing a ringtone; `null` otherwise.  */
        private var mMediaPlayer: MediaPlayer? = null

        /** The duration over which to increase the volume.  */
        private var mCrescendoDuration: Long = 0

        /** The time at which the crescendo shall cease; 0 if no crescendo is present.  */
        private var mCrescendoStopTime: Long = 0

        private var mLoop: Boolean = false
        private var mStreamType: Int = 0
        private var audioAttributes: AudioAttributes? = null

        /**
         * Starts the actual playback of the ringtone. Executes on ringtone-thread.
         */
        override fun play(context: Context, ringtoneUri: Uri?, crescendoDuration: Long,
                          loop: Boolean, streamType: Int): Boolean {
            checkAsyncRingtonePlayerThread()
            mCrescendoDuration = crescendoDuration
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
            var alarmNoise: Uri? = if (inTelephoneCall) getInCallRingtoneUri(context) else ringtoneUri
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
                mMediaPlayer?.setDataSource(context, alarmNoise!!)

                return startPlayback(inTelephoneCall)
            } catch (t: Throwable) {
                // The alarmNoise may be on the sd card which could be busy right now.
                // Use the fallback ringtone.
                try {
                    // Must reset the media player to clear the error state.
                    mMediaPlayer?.reset()
                    mMediaPlayer?.setDataSource(context, getFallbackRingtoneUri(context))
                    return startPlayback(inTelephoneCall)
                } catch (t2: Throwable) {
                    // At this point we just don't play anything.
//                    Timber.e("Failed to play fallback ringtone")
                }
            }
            return false
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
        private fun startPlayback(inTelephoneCall: Boolean): Boolean {
            // Do not play alarms if stream volume is 0 (typically because ringer mode is silent).
            if (mAudioManager?.getStreamVolume(mStreamType) == 0) {
                return false
            }

            // Indicate the ringtone should be played via the alarm stream.
            if (isLOrLater()) {
                mMediaPlayer?.setAudioAttributes(audioAttributes)
            }

            // Check if we are in a call. If we are, use the in-call alarm resource at a low volume
            // to not disrupt the call.
            var scheduleVolumeAdjustment = false
            if (inTelephoneCall) {
                mMediaPlayer?.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME)
            } else if (mCrescendoDuration > 0) {
                mMediaPlayer?.setVolume(0f, 0f)

                // Compute the time at which the crescendo will stop.
                mCrescendoStopTime = now() + mCrescendoDuration
                scheduleVolumeAdjustment = true
            }

            mMediaPlayer?.run {
                // We've set AudioAttributes up there
//                setAudioStreamType(mStreamType)
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
                    mAudioManager?.requestAudioFocus(this@MediaPlayerPlaybackDelegate,
                            mStreamType, AUDIOFOCUS_GAIN_TRANSIENT)
                }

                start()
            }
            return scheduleVolumeAdjustment
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

            mCrescendoDuration = 0
            mCrescendoStopTime = 0

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer?.stop()
                mMediaPlayer?.release()
                mMediaPlayer = null
            }

            if (isOOrLater()) {
                if (audioAttributes != null) {
                    mAudioManager?.abandonAudioFocusRequest(
                            createAudioFocusRequest(audioAttributes!!))
                }
            } else {
                @Suppress("DEPRECATION")
                mAudioManager?.abandonAudioFocus(this)
            }
        }

        /**
         * Adjusts the volume of the ringtone being played to create a crescendo effect.
         */
        override fun adjustVolume(context: Context): Boolean {
            checkAsyncRingtonePlayerThread()

            // If media player is absent or not playing, ignore volume adjustment.
            if (mMediaPlayer == null || mMediaPlayer?.isPlaying != true) {
                mCrescendoDuration = 0
                mCrescendoStopTime = 0
                return false
            }

            // If the crescendo is complete set the volume to the maximum; we're done.
            val currentTime = now()
            if (currentTime > mCrescendoStopTime) {
                mCrescendoDuration = 0
                mCrescendoStopTime = 0
                mMediaPlayer?.setVolume(1f, 1f)
                return false
            }

            // The current volume of the crescendo is the percentage of the crescendo completed.
            val volume = computeVolume(currentTime, mCrescendoStopTime, mCrescendoDuration)
            mMediaPlayer?.setVolume(volume, volume)
            // Schedule the next volume bump in the crescendo.
            return true
        }

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