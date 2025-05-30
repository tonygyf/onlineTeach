package com.example.onlineteach

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.onlineteach.utils.ScreenshotHelper
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.* // 确保这个导入存在且正确
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ScreenshotAnalysisActivity : AppCompatActivity() {

    companion object {
        /**
         * 启动截图分析活动
         * @param context 上下文对象
         */
        fun startScreenshotAnalysis(context: Context) {
            val intent = Intent(context, ScreenshotAnalysisActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private val REQUEST_SCREENSHOT = 1001
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var captureButton: Button
    private var generativeModel: GenerativeModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot_analysis)

        imageView = findViewById(R.id.screenshotImageView)
        resultTextView = findViewById(R.id.resultTextView)
        captureButton = findViewById(R.id.captureButton)

        // 初始化 Gemini 模型（支持图片）
        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash", // 或者您选择的最新模型，如 "gemini-1.5-flash-latest"
            apiKey = "AIzaSyDMkxnpZG5N5WyT0y4sbjII_M65oR-Eh5c"
        )

        captureButton.setOnClickListener {
            startScreenshotCapture()
        }
    }

    private fun startScreenshotCapture() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_SCREENSHOT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SCREENSHOT && resultCode == Activity.RESULT_OK && data != null) {
            val screenshotHelper = ScreenshotHelper(this, resultCode, data)
            screenshotHelper.takeScreenshot { bitmap ->
                imageView.setImageBitmap(bitmap)
                analyzeImage(bitmap)
            }
        } else {
            Toast.makeText(this, "截图失败", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun analyzeImage(bitmap: Bitmap) {
        generativeModel?.let { model ->
            lifecycleScope.launch {
                try {
                    // *** 关键修改在这里 ***
                    // 直接在 content 块中使用 image(bitmap)
                    val input = content {
                        image(bitmap) // 这里直接传入 Bitmap 对象
                        text(
                            "请识别图中出现的内容，重点是：数学公式、数学题、英文专业名词、流行 App 和特殊事件。"
                        )
                    }

                    val response = model.generateContent(input)
                    val text = response.text ?: "无内容识别"

                    resultTextView.text = text

                } catch (e: Exception) {
                    Log.e("ScreenshotAnalysis", "识别失败: ${e.message}")
                    Toast.makeText(this@ScreenshotAnalysisActivity, "识别失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}