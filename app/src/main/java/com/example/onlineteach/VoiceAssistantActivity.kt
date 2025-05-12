package com.example.onlineteach

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.onlineteach.data.model.ChatMessage // Assuming this is your model
import com.example.onlineteach.ui.ai.ChatMessageAdapter // Assuming this is your adapter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.Locale
import java.util.Properties

class VoiceAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var micButton: ImageButton
    private lateinit var voiceAnimationView: LottieAnimationView
    private lateinit var chatAdapter: ChatMessageAdapter

    private var generativeModel: GenerativeModel? = null // Nullable in case API key fails
    private var isWaitingForResponse = false
    
    // 语音识别相关
    private lateinit var speechRecognizer: SpeechRecognizer
    private val RECORD_AUDIO_REQUEST_CODE = 101
    
    // 文本转语音相关
    private lateinit var textToSpeech: TextToSpeech
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_voice_assistant)

        initViews()
        initGeminiApi()
        setupClickListeners()
        
        // 检查并请求录音权限
        checkPermission()
        
        // 初始化语音识别
        initSpeechRecognizer()
        
        // 初始化文本转语音
        textToSpeech = TextToSpeech(this, this)
    }

    private fun initViews() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        inputEditText = findViewById(R.id.input_edit_text)
        sendButton = findViewById(R.id.send_button)
        micButton = findViewById(R.id.mic_button)
        voiceAnimationView = findViewById(R.id.voice_animation_view)

        chatAdapter = ChatMessageAdapter() // Assuming default constructor
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        voiceAnimationView.setAnimation(R.raw.voice_wave)
        voiceAnimationView.playAnimation()
    }

    private fun initGeminiApi() {
        val apiKey = "AIzaSyDMkxnpZG5N5WyT0y4sbjII_M65oR-Eh5c"
        
        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash", // 使用正确的模型名称
            apiKey = apiKey
            // You can also add generationConfig, safetySettings etc. here if needed
            // generationConfig = GenerationConfig(...)
        )
    }

    // 不再需要从properties文件读取API密钥
    // 已在initGeminiApi方法中直接使用硬编码的API密钥


    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val message = inputEditText.text.toString().trim()
            if (isValidMessage(message)) {
                sendMessage(message)
            }
        }

        micButton.setOnClickListener {
            // 开始语音识别
            startSpeechRecognition()
        }

        inputEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val message = inputEditText.text.toString().trim()
                if (isValidMessage(message)) {
                    sendMessage(message)
                    return@setOnEditorActionListener true
                }
            }
            false
        }
    }

    private fun isValidMessage(message: String): Boolean {
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (generativeModel == null) {
            Toast.makeText(this, "AI Model not initialized. Check API Key.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun sendMessage(message: String) {
        if (isWaitingForResponse) {
            Toast.makeText(this, "Please wait for the previous response.", Toast.LENGTH_SHORT).show()
            return
        }
        val currentGenerativeModel = generativeModel
        if (currentGenerativeModel == null) {
            Toast.makeText(this, "AI Model not initialized.", Toast.LENGTH_SHORT).show()
            return
        }

        isWaitingForResponse = true
        chatAdapter.addMessage(ChatMessage(message, true)) // Assuming ChatMessage constructor
        inputEditText.setText("")
        sendButton.isEnabled = false
        // Optionally, start a loading animation on voiceAnimationView or disable it

        // Create content using the Kotlin DSL `content { ... }`
        val userContent: Content = content(role = "user") { // Explicitly setting role for clarity
            text(message)
        }

        lifecycleScope.launch {
            try {
                val response: GenerateContentResponse = currentGenerativeModel.generateContent(userContent)

                // Extract text from response, assuming the desired text is in the first candidate's parts
                val assistantMessage = response.candidates.firstOrNull()?.content?.parts?.filterIsInstance<TextPart>()?.joinToString(" ") { it.text }

                if (!assistantMessage.isNullOrEmpty()) {
                    chatAdapter.addMessage(ChatMessage(assistantMessage, false))
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    
                    // 使用TTS朗读回复
                    speakText(assistantMessage)
                } else {
                    Log.e("VoiceAssistant", "Empty or no text response from API: ${response.candidates.firstOrNull()?.finishReason}")
                    // Check promptFeedback for blocking reasons
                    response.promptFeedback?.blockReason?.let {
                        Log.e("VoiceAssistant", "Prompt blocked: $it. Ratings: ${response.promptFeedback?.safetyRatings}")
                        Toast.makeText(this@VoiceAssistantActivity, "Request blocked: $it", Toast.LENGTH_LONG).show()
                    } ?: Toast.makeText(this@VoiceAssistantActivity, "Received an empty response.", Toast.LENGTH_SHORT).show()

                    // If no block reason, check finish reason of the candidate
                    response.candidates.firstOrNull()?.finishReason?.let { finishReason ->
                        if (finishReason != com.google.ai.client.generativeai.type.FinishReason.STOP) {
                            Log.w("VoiceAssistant", "Response finished due to: $finishReason")
                            Toast.makeText(this@VoiceAssistantActivity, "Response issue: $finishReason", Toast.LENGTH_LONG).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("VoiceAssistant", "Error getting response from Gemini API", e)
                Toast.makeText(this@VoiceAssistantActivity, "Error: ${getFriendlyErrorMessage(e)}", Toast.LENGTH_SHORT).show()
            } finally {
                isWaitingForResponse = false
                sendButton.isEnabled = true
                // Optionally, stop loading animation on voiceAnimationView or re-enable it
            }
        }
    }

    private fun getFriendlyErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException, is ConnectException -> "No network connection. Please check your network."
            is IllegalArgumentException -> "Invalid input. Please try again."
            // You might want to catch specific exceptions from the GenerativeAI SDK if they are public
            // e.g. is com.google.ai.client.generativeai.type.PromptBlockedException -> "Your prompt was blocked."
            // e.g. is com.google.ai.client.generativeai.type.ResponseStoppedException -> "Response generation was stopped."
            else -> "Failed to get a response. Please try again later."
        }
    }
    
    // 语音识别相关方法
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "语音权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要语音权限才能使用语音功能", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Toast.makeText(this@VoiceAssistantActivity, "请开始说话...", Toast.LENGTH_SHORT).show()
                    voiceAnimationView.playAnimation() // 开始动画
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    voiceAnimationView.pauseAnimation() // 暂停动画
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "音频错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "没有匹配的结果"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                        else -> "未知错误"
                    }
                    Toast.makeText(this@VoiceAssistantActivity, "错误: $errorMessage", Toast.LENGTH_SHORT).show()
                    voiceAnimationView.pauseAnimation() // 暂停动画
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0] // 获取最佳匹配结果
                        inputEditText.setText(recognizedText)
                        if (isValidMessage(recognizedText)) {
                            sendMessage(recognizedText)
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
            Toast.makeText(this, "您的设备不支持语音识别", Toast.LENGTH_SHORT).show()
            micButton.isEnabled = false
        }
    }

    private fun startSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString()) // 设置为中文
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...")
            }
            try {
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "语音识别启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "需要语音权限才能使用此功能", Toast.LENGTH_SHORT).show()
            checkPermission()
        }
    }
    
    // 文本转语音相关方法
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 设置语言为中文
            val result = textToSpeech.setLanguage(Locale.CHINESE)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "不支持中文TTS", Toast.LENGTH_SHORT).show()
            } else {
                isTtsReady = true
                textToSpeech.setSpeechRate(1.0f) // 设置语速
                textToSpeech.setPitch(1.0f) // 设置音调
                
                // 设置TTS回调
                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    
                    override fun onDone(utteranceId: String?) {}
                    
                    override fun onError(utteranceId: String?) {}
                })
            }
        } else {
            Toast.makeText(this, "TTS初始化失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun speakText(text: String) {
        if (isTtsReady) {
            // 停止当前正在播放的语音
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
            
            // 播放新的语音
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
        }
    }
    
    override fun onDestroy() {
        // 释放资源
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        
        super.onDestroy()
    }
}