package com.example.onlineteach.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager

class ScreenshotHelper(
    private val context: Context,
    private val resultCode: Int,
    private val data: Intent
) {
    private var mediaProjection: MediaProjection? = null

    fun takeScreenshot(callback: (Bitmap) -> Unit) {
        try {
            // 每次截图时重新创建MediaProjection
            mediaProjection = (context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
                .getMediaProjection(resultCode, data)

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)
            
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi
            
            val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

            val virtualDisplay: VirtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenCapture",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface, null, Handler(Looper.getMainLooper())
            ) ?: throw IllegalStateException("MediaProjection is null")

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val image = imageReader.acquireLatestImage()
                    image?.let {
                        val planes = it.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bitmap = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height, Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)
                        it.close()
                        callback(bitmap)
                    } ?: run {
                        Log.e("ScreenshotHelper", "Failed to acquire image")
                        callback(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                    }
                } catch (e: Exception) {
                    Log.e("ScreenshotHelper", "Error taking screenshot", e)
                    callback(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                } finally {
                    virtualDisplay.release()
                    mediaProjection?.stop()
                    mediaProjection = null
                }
            }, 100) // 减少延迟时间，提高响应速度
        } catch (e: Exception) {
            Log.e("ScreenshotHelper", "Error setting up screenshot", e)
            callback(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        }
    }
}
