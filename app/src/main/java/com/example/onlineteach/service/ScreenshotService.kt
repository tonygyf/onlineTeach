package com.example.onlineteach.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat
import com.example.onlineteach.R
import com.example.onlineteach.ScreenshotAnalysisActivity
import com.example.onlineteach.utils.ScreenshotHelper
import java.io.File
import java.io.FileOutputStream

class ScreenshotService : Service() {
    companion object {
        private const val TAG = "ScreenshotService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "screenshot_service_channel"
        private const val CHANNEL_NAME = "截图服务"
        private const val ACTION_TAKE_SCREENSHOT = "com.example.onlineteach.ACTION_TAKE_SCREENSHOT"
        private const val ACTION_CANCEL = "com.example.onlineteach.ACTION_CANCEL"

        private var resultCode: Int = 0
        private var data: Intent? = null
        private var isServiceRunning = false

        fun start(context: Context) {
            val intent = Intent(context, ScreenshotService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ScreenshotService::class.java)
            context.stopService(intent)
            isServiceRunning = false
        }

        fun setMediaProjectionData(code: Int, intent: Intent) {
            resultCode = code
            data = intent
        }

        fun getMediaProjectionData(): Pair<Int, Intent>? {
            return if (resultCode != 0 && data != null) {
                Pair(resultCode, data!!)
            } else {
                null
            }
        }

        /**
         * 检查服务是否正在运行
         * @return 服务是否正在运行
         */
        fun isRunning(): Boolean = isServiceRunning
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createFloatingWindow()
        
        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        isServiceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TAKE_SCREENSHOT -> {
                try {
                    if (resultCode == 0 || data == null) {
                        // 如果没有权限数据，启动Activity请求权限
                        val screenshotIntent = Intent(this, ScreenshotAnalysisActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(screenshotIntent)
                    } else {
                        // 有权限数据，直接进行截图
                        takeScreenshot()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error taking screenshot", e)
                }
            }
            ACTION_CANCEL -> {
                stopSelf()
            }
        }
        return Service.START_STICKY
    }

    private fun takeScreenshot() {
        try {
            val projectionData = getMediaProjectionData()
            if (projectionData != null) {
                val (resultCode, data) = projectionData
                val screenshotHelper = ScreenshotHelper(applicationContext, resultCode, data)
                screenshotHelper.takeScreenshot { bitmap ->
                    if (bitmap.width > 1 && bitmap.height > 1) {
                        try {
                            // 将bitmap保存到临时文件
                            val file = File(applicationContext.cacheDir, "screenshot_${System.currentTimeMillis()}.png")
                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            
                            // 启动分析界面并传递文件路径
                            val screenshotIntent = Intent(this, ScreenshotAnalysisActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                putExtra("screenshot_path", file.absolutePath)
                            }
                            startActivity(screenshotIntent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error saving screenshot", e)
                        }
                    } else {
                        Log.e(TAG, "Screenshot failed: invalid bitmap")
                    }
                }
            } else {
                Log.e(TAG, "No media projection data available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error taking screenshot", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于保持截图服务运行"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("截图服务")
            .setContentText("截图服务正在运行")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.layout_floating_screenshot, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        // 设置截图按钮点击事件
        floatingView?.findViewById<Button>(R.id.btnTakeScreenshot)?.setOnClickListener {
            val takeScreenshotIntent = Intent(this, ScreenshotService::class.java).apply {
                action = ACTION_TAKE_SCREENSHOT
            }
            startService(takeScreenshotIntent)
        }

        // 设置取消按钮点击事件
        floatingView?.findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
            stopSelf()
        }

        // 添加触摸监听器实现拖动
        var initialX: Int = 0
        var initialY: Int = 0
        var initialTouchX: Float = 0f
        var initialTouchY: Float = 0f

        floatingView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录初始位置
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // 计算移动距离
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    // 更新窗口位置
                    windowManager?.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding floating window", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (floatingView != null && windowManager != null) {
                windowManager?.removeView(floatingView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing floating window", e)
        }
        stopForeground(true)
        isServiceRunning = false
    }
} 