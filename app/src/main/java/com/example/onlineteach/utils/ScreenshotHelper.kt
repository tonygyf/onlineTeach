package com.example.onlineteach.utils
import android.app.Activity
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
import android.view.WindowManager

class ScreenshotHelper(
    private val activity: Activity,
    resultCode: Int,
    data: Intent
) {
    private val mediaProjection: MediaProjection =
        (activity.getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
            .getMediaProjection(resultCode, data)

    fun takeScreenshot(callback: (Bitmap) -> Unit) {
        val windowManager = activity.getSystemService(WindowManager::class.java)
        val display = windowManager.defaultDisplay
        val width = display.width
        val height = display.height
        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        val virtualDisplay: VirtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width, height, activity.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, Handler(Looper.getMainLooper())
        )

        Handler(Looper.getMainLooper()).postDelayed({
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
            }
            virtualDisplay.release()
        }, 1000) // Delay to ensure image is ready
    }
}
