package com.example.onlineteach

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TTS助手类，用于处理文字转语音功能
 * @param context 上下文对象
 */
class TTSHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = AtomicBoolean(false)
    private var onInitCallback: ((Boolean) -> Unit)? = null
    private var retryCount = 0
    private val MAX_RETRY = 3
    private val isXiaomiDevice = Build.MANUFACTURER.lowercase().contains("xiaomi")
    private var isBinding = AtomicBoolean(false)

    init {
        initTTS()
    }

    private fun initTTS() {
        if (isBinding.get()) {
            Log.d("TTSHelper", "TTS正在初始化中，跳过重复初始化")
            return
        }

        try {
            isBinding.set(true)

            val contextToUse = if (isXiaomiDevice) {
                context.applicationContext
            } else {
                context
            }

            // 不要启动 ACTION_CHECK_TTS_DATA 的Activity，直接初始化
            releaseTTS()

            tts = TextToSpeech(contextToUse, this)
            Log.d("TTSHelper", "开始初始化TTS，设备类型: ${if (isXiaomiDevice) "小米" else "其他"}")

        } catch (e: Exception) {
            Log.e("TTSHelper", "TTS初始化失败", e)
            isBinding.set(false)
            retryInit()
        }
    }


    private fun releaseTTS() {
        try {
            tts?.let {
                if (isInitialized.get()) {
                    it.stop()
                    it.shutdown()
                }
            }
        } catch (e: Exception) {
            Log.e("TTSHelper", "释放TTS失败", e)
        } finally {
            tts = null
            isInitialized.set(false)
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
        try {
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
                                isInitialized.set(true)
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
        } finally {
            isBinding.set(false)
        }
    }

    private fun retryInit() {
        if (retryCount < MAX_RETRY) {
            retryCount++
            Log.d("TTSHelper", "尝试重新初始化TTS，第${retryCount}次")
            // 延迟3秒后重试，给系统更多时间
            android.os.Handler(context.mainLooper).postDelayed({
                initTTS()
            }, 3000)
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
        if (!isInitialized.get()) {
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
        if (isInitialized.get()) {
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
        releaseTTS()
    }

    /**
     * 检查TTS是否已初始化
     * @return 是否已初始化
     */
    fun isReady(): Boolean = isInitialized.get()
} 