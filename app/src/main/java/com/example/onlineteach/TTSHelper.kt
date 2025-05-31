package com.example.onlineteach

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Base64 as AndroidBase64
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.LinkedBlockingQueue

/**
 * 讯飞语音合成助手类
 */
class TTSHelper(
    private val context: Context,
    private val appId: String,
    private val apiKey: String,
    private val apiSecret: String
) {
    private var webSocketClient: WebSocketClient? = null
    private var isInitialized = AtomicBoolean(false)
    private var onInitCallback: ((Boolean) -> Unit)? = null
    private var audioTrack: AudioTrack? = null
    private val gson = Gson()
    private var wsCloseFlag = false
    private val audioQueue = LinkedBlockingQueue<ByteArray>()
    private var isPlaying = AtomicBoolean(false)
    
    companion object {
        private const val TAG = "TTSHelper"
        private const val HOST_URL = "https://tts-api.xfyun.cn/v2/tts"
        private const val TTE = "UTF8"
        private const val VCN = "x4_yezi"
        private const val SAMPLE_RATE = 16000 // 讯飞TTS采样率
    }

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM
            )
            
            // 启动播放线程
            Thread {
                while (true) {
                    try {
                        if (!isPlaying.get()) {
                            Thread.sleep(100)
                            continue
                        }
                        
                        val audioData = audioQueue.poll() ?: continue
                        audioTrack?.write(audioData, 0, audioData.size)
                    } catch (e: Exception) {
                        Log.e(TAG, "播放线程错误", e)
                    }
                }
            }.start()
            
        } catch (e: Exception) {
            Log.e(TAG, "初始化AudioTrack失败", e)
        }
    }

    /**
     * 初始化TTS
     */
    fun init() {
        if (isInitialized.get()) {
            Log.d(TAG, "WebSocket连接已存在")
            return
        }

        try {
            val wsUrl = getAuthUrl(HOST_URL, apiKey, apiSecret).replace("https://", "wss://")
            Log.d(TAG, "开始创建WebSocket客户端，URL: $wsUrl")
            
            webSocketClient = object : WebSocketClient(URI(wsUrl)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d(TAG, "WebSocket连接已打开")
                    isInitialized.set(true)
                    onInitCallback?.invoke(true)
                }

                override fun onMessage(text: String?) {
                    Log.d(TAG, "收到消息: $text")
                    text?.let { handleMessage(it) }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(TAG, "WebSocket连接已关闭: code=$code, reason=$reason, remote=$remote")
                    isInitialized.set(false)
                    // 如果连接意外关闭，尝试重新连接
                    if (!wsCloseFlag) {
                        Thread.sleep(1000)
                        init()
                    }
                }

                override fun onError(ex: Exception?) {
                    Log.e(TAG, "WebSocket错误: ${ex?.message}", ex)
                    isInitialized.set(false)
                    onInitCallback?.invoke(false)
                    // 如果发生错误，尝试重新连接
                    if (!wsCloseFlag) {
                        Thread.sleep(1000)
                        init()
                    }
                }
            }
            
            // 建立连接
            webSocketClient?.connect()
            
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket连接失败: ${e.message}", e)
            isInitialized.set(false)
            onInitCallback?.invoke(false)
        }
    }

    /**
     * 设置初始化回调
     */
    fun setOnInitCallback(callback: (Boolean) -> Unit) {
        onInitCallback = callback
    }

    /**
     * 合成并播放文本
     */
    fun speak(text: String) {
        Log.d(TAG, "开始语音合成请求，文本内容: $text")
        
        // 先关闭现有连接
        webSocketClient?.close()
        webSocketClient = null
        isInitialized.set(false)
        
        // 重新初始化连接
        init()
        
        // 等待连接建立
        var retryCount = 0
        while (!isInitialized.get() && retryCount < 10) {
            Thread.sleep(100)
            retryCount++
        }
        
        if (!isInitialized.get()) {
            Log.e(TAG, "WebSocket连接建立失败")
            return
        }

        val requestJson = """
            {
              "common": {
                "app_id": "$appId"
              },
              "business": {
                "aue": "raw",
                "tte": "$TTE",
                "ent": "intp65",
                "vcn": "$VCN",
                "pitch": 50,
                "speed": 50
              },
              "data": {
                "status": 2,
                "text": "${AndroidBase64.encodeToString(text.toByteArray(StandardCharsets.UTF_8), AndroidBase64.NO_WRAP)}"
              }
            }
        """.trimIndent()

        Log.d(TAG, "发送语音合成请求到讯飞服务器")
        webSocketClient?.send(requestJson)
    }

    /**
     * 停止播放
     */
    fun stop() {
        try {
            isPlaying.set(false)
            audioTrack?.stop()
            audioTrack?.flush()
            audioQueue.clear()
        } catch (e: Exception) {
            Log.e(TAG, "停止播放失败", e)
        }
    }

    /**
     * 关闭TTS
     */
    fun shutdown() {
        wsCloseFlag = true
        stop()
        webSocketClient?.close()
        webSocketClient = null
        isInitialized.set(false)
        audioTrack?.release()
        audioTrack = null
    }

    /**
     * 检查TTS是否已初始化
     */
    fun isReady(): Boolean = isInitialized.get()

    private fun getAuthUrl(hostUrl: String, apiKey: String, apiSecret: String): String {
        val url = URL(hostUrl)
        // 时间
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
        format.timeZone = TimeZone.getTimeZone("GMT")
        val date = format.format(Date())
        
        // 拼接
        val preStr = "host: ${url.host}\ndate: $date\nGET ${url.path} HTTP/1.1"
        Log.d(TAG, "签名原文:\n$preStr")
        
        // SHA256加密
        val mac = Mac.getInstance("hmacsha256")
        val spec = SecretKeySpec(apiSecret.toByteArray(StandardCharsets.UTF_8), "hmacsha256")
        mac.init(spec)
        val hexDigits = mac.doFinal(preStr.toByteArray(StandardCharsets.UTF_8))
        
        // Base64加密
        val sha = AndroidBase64.encodeToString(hexDigits, AndroidBase64.NO_WRAP)
        Log.d(TAG, "签名结果: $sha")
        
        // 拼接
        val authorization = "api_key=\"$apiKey\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"$sha\""
        Log.d(TAG, "认证原文: $authorization")
        
        // Base64编码
        val authorizationBase64 = AndroidBase64.encodeToString(authorization.toByteArray(StandardCharsets.UTF_8), AndroidBase64.NO_WRAP)
        Log.d(TAG, "认证信息: $authorizationBase64")
        
        // URL编码参数
        val encodedAuthorization = java.net.URLEncoder.encode(authorizationBase64, "UTF-8")
        val encodedDate = java.net.URLEncoder.encode(date, "UTF-8")
        val encodedHost = java.net.URLEncoder.encode(url.host, "UTF-8")
        
        // 拼接地址
        val finalUrl = "https://${url.host}${url.path}?authorization=$encodedAuthorization&date=$encodedDate&host=$encodedHost"
        Log.d(TAG, "最终URL: $finalUrl")
        return finalUrl
    }

    private fun handleMessage(message: String) {
        try {
            Log.d(TAG, "收到讯飞服务器响应: $message")
            val response = gson.fromJson(message, JsonParse::class.java)
            if (response.code != 0) {
                Log.e(TAG, "语音合成失败: code=${response.code}, sid=${response.sid}")
                return
            }
            
            response.data?.let { data ->
                if (data.audio != null) {
                    Log.d(TAG, "收到音频数据，准备播放")
                    val audioData = AndroidBase64.decode(data.audio, AndroidBase64.NO_WRAP)
                    playAudio(audioData)
                }
                
                if (data.status == 2) {
                    Log.d(TAG, "语音合成完成，sid=${response.sid}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理消息失败", e)
        }
    }

    private fun playAudio(audioData: ByteArray) {
        try {
            Log.d(TAG, "开始播放音频，数据大小: ${audioData.size} bytes")
            isPlaying.set(true)
            audioTrack?.play()
            audioQueue.offer(audioData)
        } catch (e: Exception) {
            Log.e(TAG, "播放音频失败", e)
        }
    }
}

// 返回的json结果拆解
data class JsonParse(
    val code: Int,
    val sid: String,
    val data: Data?
)

data class Data(
    val status: Int,
    val audio: String?
) 