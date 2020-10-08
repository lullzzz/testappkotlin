package com.gshubina.lullzzz.testappkotlin.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.gshubina.lullzzz.testappkotlin.R
import com.gshubina.lullzzz.testappkotlin.service.simulator.SpeedSimulator
import com.gshubina.lullzzz.testappkotlin.service.simulator.TachometerSimulator
import java.util.function.Consumer
import java.util.function.Predicate

class GenerateDataService : Service() {

    private val LOG_TAG: String = GenerateDataService::class.simpleName.toString()

    private val NOTIFICATION_ID = 456784
    private val NOTIFICATION_CHANNEL_ID = "SERVICE_FOREGROUND_NOTIFICATION_CHANNEL"
    private val ACTION_PAUSE = "ACTION_PAUSE"
    private val ACTION_RESUME = "ACTION_RESUME"
    private val ACTION_STOP = "ACTION_STOP"

    private lateinit var mNotificationManager: NotificationManagerCompat
    private lateinit var mForegroundNotificationBuilder: NotificationCompat.Builder

    private val mSpeedThread = HandlerThread("SpeedDataHandler")
    private val mTachometerThread = HandlerThread("TachometerDataHandler")

    private val mCallbackList: RemoteCallbackList<IDataServiceCallback> =
        object : RemoteCallbackList<IDataServiceCallback>() {
            override fun onCallbackDied(callback: IDataServiceCallback?, cookie: Any?) {
                Log.w(LOG_TAG, "Client connection is dead: " + callback?.asBinder().toString())
                return super.onCallbackDied(callback, cookie)
            }
        }

    private var mIsStarted: Boolean = false
    private var mIsSimulationStarted: Boolean = false

    override fun onCreate() {
        super.onCreate()
        if (!mIsStarted) {
            mIsStarted = true
            mSpeedThread.start()
            mTachometerThread.start()
            startForegroundService(Intent(this, GenerateDataService::class.java))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == null) {
            Log.i(LOG_TAG, "Starting service...")
            showNotification()
            mIsSimulationStarted = true
        } else {
            when (intent.action) {
                ACTION_PAUSE -> {
                    Log.i(LOG_TAG, "Pausing sending data...")
                    mIsSimulationStarted = false
                    refreshPauseNotification()
                }
                ACTION_RESUME -> {
                    Log.i(LOG_TAG, "Resuming sending data...")
                    mIsSimulationStarted = true
                    refreshResumeNotification()
                }
                ACTION_STOP -> {
                    Log.i(LOG_TAG, "Stop service")
                    stopService()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun stopService() {
        mCallbackList.kill()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        mSpeedThread.quitSafely()
        mTachometerThread.quitSafely()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(LOG_TAG, "Client is bind to service: " + intent?.getPackage())
        return mBinder
    }

    private fun makeDefaultNotificationBuilder(): NotificationCompat.Builder {
        val stopIntent = Intent(this, GenerateDataService::class.java)
        stopIntent.action = ACTION_STOP
        val pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(resources.getString(R.string.notification_content_title))
            .setContentText(resources.getString(R.string.notification_content_text))
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_speed_24)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(
                0,
                resources.getString(R.string.notification_action_stop),
                pendingStopIntent
            )
        return builder
    }

    private fun showNotification() {
        mNotificationManager = NotificationManagerCompat.from(applicationContext)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            resources.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = resources.getString(R.string.notification_channel_description)
        notificationManager.createNotificationChannel(channel)

        val pauseIntent = Intent(this, GenerateDataService::class.java)
        pauseIntent.action = ACTION_PAUSE
        val pendingIntentPrev = PendingIntent.getService(this, 0, pauseIntent, 0)
        mForegroundNotificationBuilder = makeDefaultNotificationBuilder()

        val foregroundNotification = mForegroundNotificationBuilder
            .addAction(
                0,
                resources.getString(R.string.notification_action_pause),
                pendingIntentPrev
            )
            .build()

        startForeground(NOTIFICATION_ID, foregroundNotification)
    }

    private fun refreshPauseNotification() {
        val resumeIntent = Intent(this, GenerateDataService::class.java)
        resumeIntent.action = ACTION_RESUME
        val pendingResumeIntent = PendingIntent.getService(this, 0, resumeIntent, 0)
        val pausedNotification = mForegroundNotificationBuilder
            .addAction(
                0,
                resources.getString(R.string.notification_action_resume),
                pendingResumeIntent
            )
            .build()
        mNotificationManager.notify(NOTIFICATION_ID, pausedNotification)
    }

    private fun refreshResumeNotification() {
        val pauseIntent = Intent(this, GenerateDataService::class.java)
        pauseIntent.action = ACTION_PAUSE
        val pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0)

        val foregroundNotification = mForegroundNotificationBuilder
            .addAction(
                0,
                resources.getString(R.string.notification_action_pause),
                pendingPrevIntent
            )
            .build()
        mNotificationManager.notify(NOTIFICATION_ID, foregroundNotification)
    }

    private val mBinder = object : IDataProvideService.Stub() {
        override fun registerCallback(callback: IDataServiceCallback?) {
            if (callback != null)
                mCallbackList.register(callback)
        }

        override fun unregisterCallback(callback: IDataServiceCallback?) {
            if (callback != null)
                mCallbackList.unregister(callback)
        }

        override fun requestSpeedData() {
            val handler = Handler(mSpeedThread.looper)
            handler.post {
                SpeedSimulator(Predicate { _ -> mIsSimulationStarted }).speedStream()
                    .forEach(Consumer { data -> sendSpeedData(data) })
            }
        }

        override fun requestTachometerData() {
            val handler = Handler(mTachometerThread.looper)
            handler.post {
                TachometerSimulator(Predicate { _ -> mIsSimulationStarted }).tachometerStream()
                    .forEach(Consumer { data -> sendTachometerData(data) })
            }
        }
    }

    @Synchronized
    private fun sendSpeedData(data: Double): Int {
        val n = mCallbackList.beginBroadcast();
        try {
            for (i in 0 until n) {
                mCallbackList.getBroadcastItem(i).onSpeedometerDataUpdate(data)
            }
        } catch (e: RemoteException) {
            // RemoteCallbackList should processed the case, just log it
            Log.w(LOG_TAG, e.message.toString())
        } finally {
            mCallbackList.finishBroadcast()
        }
        return n;
    }

    @Synchronized
    private fun sendTachometerData(data: Double): Int {
        val n = mCallbackList.beginBroadcast()
        try {
            for (i in 0 until n) {
                mCallbackList.getBroadcastItem(i).onTachometerDataUpdate(data)
            }
        } catch (e: RemoteException) {
            // RemoteCallbackList should processed the case, just log it
            Log.w(LOG_TAG, e.message.toString())
        } finally {
            mCallbackList.finishBroadcast()
        }
        return n
    }

}