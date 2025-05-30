package com.example.onlineteach

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener // ✅ 正确

import android.util.Log
import java.util.*

/**
 * TTS助手类，用于处理文字转语音功能
 * @param context 上下文对象
 */
class TTSHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onInitCallback: ((Boolean) -> Unit)? = null
    private var retryCount = 0
    private val MAX_RETRY = 3

    init {
        initTTS()
    }

    private fun initTTS() {
        try {
            // 检查设备是否支持TTS
            val checkIntent = Intent()
            checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            context.startActivity(checkIntent)

            // 创建TTS实例
            tts = TextToSpeech(context.applicationContext, this)
            Log.d("TTSHelper", "开始初始化TTS")
        } catch (e: Exception) {
            Log.e("TTSHelper", "TTS初始化失败", e)
            onInitCallback?.invoke(false)
        }
    }

    /**
     * 设置初始化回调
     * @param callback 初始化完成后的回调函数
     */
    fun setOnInitCallback(callback: (Boolean) -> Unit) {
        onInitCallback = callback
    }

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                try {
                    // 获取可用的语言列表
                    val availableLanguages = tts?.availableLanguages
                    Log.d("TTSHelper", "可用的TTS语言: $availableLanguages")

                    // 获取当前引擎信息
                    val engineInfo = tts?.engines
                    Log.d("TTSHelper", "当前TTS引擎: ${engineInfo?.joinToString { it.name }}")

                    // 检查是否支持中文
                    val result = tts?.setLanguage(Locale.CHINESE)
                    when (result) {
                        TextToSpeech.LANG_MISSING_DATA -> {
                            Log.e("TTSHelper", "TTS语言包缺失")
                            // 尝试安装语言包
                            val installIntent = Intent()
                            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                            context.startActivity(installIntent)
                            onInitCallback?.invoke(false)
                        }
                        TextToSpeech.LANG_NOT_SUPPORTED -> {
                            Log.e("TTSHelper", "TTS不支持中文")
                            onInitCallback?.invoke(false)
                        }
                        else -> {
                            isInitialized = true
                            tts?.setSpeechRate(1.0f)
                            tts?.setPitch(1.0f)
                            
                            // 设置进度监听器
                            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                    Log.d("TTSHelper", "开始朗读: $utteranceId")
                                }

                                override fun onDone(utteranceId: String?) {
                                    Log.d("TTSHelper", "朗读完成: $utteranceId")
                                }

                                override fun onError(utteranceId: String?) {
                                    Log.e("TTSHelper", "朗读错误: $utteranceId")
                                }
                            })

                            Log.d("TTSHelper", "TTS初始化成功")
                            onInitCallback?.invoke(true)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TTSHelper", "TTS设置语言失败", e)
                    retryInit()
                }
            }
            TextToSpeech.ERROR -> {
                Log.e("TTSHelper", "TTS初始化错误")
                retryInit()
            }
            else -> {
                Log.e("TTSHelper", "TTS初始化失败，状态码: $status")
                retryInit()
            }
        }
    }

    private fun retryInit() {
        if (retryCount < MAX_RETRY) {
            retryCount++
            Log.d("TTSHelper", "尝试重新初始化TTS，第${retryCount}次")
            // 延迟1秒后重试
            android.os.Handler(context.mainLooper).postDelayed({
                initTTS()
            }, 1000)
        } else {
            Log.e("TTSHelper", "TTS初始化重试次数超过限制")
            onInitCallback?.invoke(false)
        }
    }

    /**
     * 朗读文本
     * @param text 要朗读的文本
     */
    fun speak(text: String) {
        if (!isInitialized) {
            Log.e("TTSHelper", "TTS尚未初始化，无法朗读")
            return
        }
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        } catch (e: Exception) {
            Log.e("TTSHelper", "TTS朗读失败", e)
        }
    }

    /**
     * 停止朗读
     */
    fun stop() {
        if (isInitialized) {
            try {
                tts?.stop()
            } catch (e: Exception) {
                Log.e("TTSHelper", "TTS停止失败", e)
            }
        }
    }

    /**
     * 关闭TTS引擎
     */
    fun shutdown() {
        if (isInitialized) {
            try {
                tts?.stop()
                tts?.shutdown()
                isInitialized = false
            } catch (e: Exception) {
                Log.e("TTSHelper", "TTS关闭失败", e)
            }
        }
    }

    /**
     * 检查TTS是否已初始化
     * @return 是否已初始化
     */
    fun isReady(): Boolean = isInitialized
} 