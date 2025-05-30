package com.example.onlineteach

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.onlineteach.service.ScreenshotService
import com.example.onlineteach.utils.ScreenshotHelper
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class ScreenshotAnalysisActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ScreenshotAnalysis"
        private const val REQUEST_SCREENSHOT = 1001

        /**
         * 启动截图分析活动
         * @param context 上下文对象
         */
        fun startScreenshotAnalysis(context: Context) {
            val intent = Intent(context, ScreenshotAnalysisActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }

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

        // 初始化 Gemini 模型
        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = "AIzaSyDMkxnpZG5N5WyT0y4sbjII_M65oR-Eh5c"
        )

        captureButton.setOnClickListener {
            startScreenshotCapture()
        }

        // 检查是否有截图数据
        val screenshotPath = intent.getStringExtra("screenshot_path")
        if (screenshotPath != null) {
            try {
                val bitmap = BitmapFactory.decodeFile(screenshotPath)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    analyzeImage(bitmap)
                    // 删除临时文件
                    File(screenshotPath).delete()
                } else {
                    Toast.makeText(this, "无法加载截图", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading screenshot", e)
                Toast.makeText(this, "加载截图失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startScreenshotCapture() {
        try {
            // 启动前台服务
            ScreenshotService.start(this)
            
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val intent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(intent, REQUEST_SCREENSHOT)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting screen capture", e)
            Toast.makeText(this, "启动截图失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_SCREENSHOT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // 保存权限数据到服务
                ScreenshotService.setMediaProjectionData(resultCode, data)
                // 最小化当前Activity
                moveTaskToBack(true)
            } else {
                Toast.makeText(this, "截图权限被拒绝", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        generativeModel?.let { model ->
            lifecycleScope.launch {
                try {
                    resultTextView.text = "正在分析图片..."
                    val input = content {
                        image(bitmap)
                        text("""
                            请分析这张图片的内容，并按以下格式输出：

                            【页面概述】
                            简要描述页面的主要内容和目的

                            【主要内容】
                            1. 文字内容
                            - 主要文字
                            - 数字和符号
                            - 特殊文本

                            2. 界面元素
                            - 按钮和菜单
                            - 图标和图片
                            - 输入框和表单

                            3. 布局结构
                            - 页面布局
                            - 元素排列
                            - 视觉层次

                            4. 特殊内容
                            - 数学公式
                            - 代码片段
                            - 表格和图表

                            5. 交互元素
                            - 可点击按钮
                            - 链接和导航
                            - 操作提示

                            6. 状态信息
                            - 加载状态
                            - 错误提示
                            - 其他状态

                            【补充说明】
                            - 如有数学问题，请用中文简要解答
                            - 如有专业术语，请用中文解释
                            - 如有特殊英语单词，请标注中文含义

                            请确保输出格式清晰，不需要markdown，便于阅读。
                        """.trimIndent())
                    }

                    val response = model.generateContent(input)
                    val text = response.text ?: "无内容识别"
                    resultTextView.text = text

                } catch (e: Exception) {
                    Log.e(TAG, "识别失败", e)
                    resultTextView.text = "识别失败: ${e.message}"
                    Toast.makeText(this@ScreenshotAnalysisActivity, "识别失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // 停止前台服务
        ScreenshotService.stop(this)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止前台服务
        ScreenshotService.stop(this)
    }
}