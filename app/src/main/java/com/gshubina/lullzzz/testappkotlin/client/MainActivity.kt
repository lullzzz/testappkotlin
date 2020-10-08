package com.gshubina.lullzzz.testappkotlin.client

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gshubina.lullzzz.testappkotlin.R
import com.gshubina.lullzzz.testappkotlin.client.viewmodel.SpeedometerDataViewModel
import com.gshubina.lullzzz.testappkotlin.client.viewmodel.TachometerDataViewModel
import com.gshubina.lullzzz.testappkotlin.service.GenerateDataService
import com.gshubina.lullzzz.testappkotlin.service.IDataProvideService
import com.gshubina.lullzzz.testappkotlin.service.IDataServiceCallback

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.simpleName.toString()

    private lateinit var mViewPager: ViewPager2
    private lateinit var mPagerAdapter: FragmentStateAdapter

    private lateinit var mSpeedometerDataViewModel: SpeedometerDataViewModel
    private lateinit var mTachometerDataViewModel: TachometerDataViewModel

    private var mService: IDataProvideService? = null
    private lateinit var mServiceIntent: Intent

    private val mDataCallback: IDataServiceCallback = object : IDataServiceCallback.Stub() {
        override fun onSpeedometerDataUpdate(data: Double) {
            runOnUiThread {
                mSpeedometerDataViewModel.receive(data)
            }
        }

        override fun onTachometerDataUpdate(data: Double) {
            runOnUiThread {
                mTachometerDataViewModel.receive(data)
            }
        }
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "Bind to service")
            mService = IDataProvideService.Stub.asInterface(service)
            try {
                mService!!.registerCallback(mDataCallback)
                mService!!.requestSpeedData()
                mService!!.requestTachometerData()
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to complete service request " + e.message)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(TAG, "Unbind from service")
            mService = null
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.i(TAG, "Service connection died. Trying to reconnect...")
            bindService(mServiceIntent, this, BIND_AUTO_CREATE)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mSpeedometerDataViewModel = ViewModelProvider(this).get(
            SpeedometerDataViewModel::class.java
        )
        mTachometerDataViewModel = ViewModelProvider(this).get(
            TachometerDataViewModel::class.java
        )

        mViewPager = findViewById(R.id.fragments_pager)
        mPagerAdapter = ViewPagerFragmentStateAdapter(this)
        mViewPager.adapter = mPagerAdapter
        mViewPager.setPageTransformer(SwipePageTransformer())
        // set custom gesture handling

        // set custom gesture handling
        mViewPager.isUserInputEnabled = false
        mViewPager.setOnTouchListener(mViewPagerOnTouchListener)

        mServiceIntent = Intent(applicationContext, GenerateDataService::class.java)
    }

    override fun onStart() {
        super.onStart()
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (mService != null) {
            try {
                mService!!.unregisterCallback(mDataCallback)
            } catch (e: RemoteException) {
                Log.w(TAG, e.message.toString())
            }
            unbindService(mConnection)
        }
        super.onStop()
    }

    private val mViewPagerOnTouchListener: OnTouchListener = object : OnTouchListener {
        private var mTwoFingerSwipeFlag = false
        private var mStartX = 0f
        private var mStopX = 0f
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        mTwoFingerSwipeFlag = true
                        mStartX = event.getX(0)
                        if (!mViewPager.isFakeDragging) {
                            mViewPager.beginFakeDrag()
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount != 2) {
                        mTwoFingerSwipeFlag = false
                    } else {
                        if (mTwoFingerSwipeFlag) {
                            mStopX = event.getX(0)
                            mViewPager.fakeDragBy(mStopX - mStartX)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mTwoFingerSwipeFlag = false
                    mViewPager.endFakeDrag()
                }
            }
            return true
        }
    }

}