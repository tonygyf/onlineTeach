package com.example.onlineteach

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener

import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.onlineteach.data.model.ChatMessage
import com.example.onlineteach.ui.ai.ChatMessageAdapter
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

class VoiceAssistantActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var micButton: ImageButton
    private lateinit var voiceAnimationView: LottieAnimationView
    private lateinit var chatAdapter: ChatMessageAdapter

    private var generativeModel: GenerativeModel? = null
    private var isWaitingForResponse = false

    private lateinit var speechRecognizer: SpeechRecognizer
    private val RECORD_AUDIO_REQUEST_CODE = 101

    private lateinit var ttsHelper: TTSHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_voice_assistant)

        initViews()
        initGeminiApi()
        setupClickListeners()

        checkPermission()
        initSpeechRecognizer()

        // 初始化TTS
        initTTS()
    }

    private fun initTTS() {
        ttsHelper = TTSHelper(this)
        ttsHelper.setOnInitCallback { success ->
            if (success) {
                Log.d("VoiceAssistant", "TTS初始化成功")
                // 测试TTS
                ttsHelper.speak("TTS测试")
            } else {
                Log.e("VoiceAssistant", "TTS初始化失败")
                Toast.makeText(this, "TTS初始化失败，请检查系统TTS设置", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initViews() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        inputEditText = findViewById(R.id.input_edit_text)
        sendButton = findViewById(R.id.send_button)
        micButton = findViewById(R.id.mic_button)
        voiceAnimationView = findViewById(R.id.voice_animation_view)

        chatAdapter = ChatMessageAdapter().apply {
            setOnItemClickListener { message ->
                if (!message.isFromUser()) { // 修改这里：从 message.fromUser == false 改为 !message.isFromUser()
                    speakText(message.content)
                }
            }
        }
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        voiceAnimationView.setAnimation(R.raw.voice_wave)
        voiceAnimationView.playAnimation()
    }

    private fun initGeminiApi() {
        val apiKey = "AIzaSyDMkxnpZG5N5WyT0y4sbjII_M65oR-Eh5c"

        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey
        )
    }


    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val message = inputEditText.text.toString().trim()
            if (isValidMessage(message)) {
                sendMessage(message)
            }
        }

        micButton.visibility = View.GONE // Make sure this import is present if not already.

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
        chatAdapter.addMessage(ChatMessage(message, true))
        inputEditText.setText("")
        sendButton.isEnabled = false

        val userContent: Content = content(role = "user") {
            text(message)
        }

        lifecycleScope.launch {
            try {
                val response: GenerateContentResponse = currentGenerativeModel.generateContent(userContent)

                val assistantMessage = response.candidates.firstOrNull()?.content?.parts?.filterIsInstance<TextPart>()?.joinToString(" ") { it.text }

                if (!assistantMessage.isNullOrEmpty()) {
                    chatAdapter.addMessage(ChatMessage(assistantMessage, false))
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)

                    speakText(assistantMessage)
                } else {
                    Log.e("VoiceAssistant", "Empty or no text response from API: ${response.candidates.firstOrNull()?.finishReason}")
                    response.promptFeedback?.blockReason?.let {
                        Log.e("VoiceAssistant", "Prompt blocked: $it. Ratings: ${response.promptFeedback?.safetyRatings}")
                        Toast.makeText(this@VoiceAssistantActivity, "Request blocked: $it", Toast.LENGTH_LONG).show()
                    } ?: Toast.makeText(this@VoiceAssistantActivity, "Received an empty response.", Toast.LENGTH_SHORT).show()

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
            }
        }
    }

    private fun getFriendlyErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException, is ConnectException -> "No network connection. Please check your network."
            is IllegalArgumentException -> "Invalid input. Please try again."
            else -> "Failed to get a response. Please try again later."
        }
    }

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
        micButton.isEnabled = false
    }

    private fun startSpeechRecognition() {
        // 语音识别功能已禁用
    }

    private fun speakText(text: String) {
        if (!ttsHelper.isReady()) {
            Log.e("VoiceAssistant", "TTS未就绪")
            Toast.makeText(this, "TTS未就绪，请检查设备是否支持TTS", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            ttsHelper.speak(text)
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "TTS播放失败", e)
            Toast.makeText(this, "TTS播放失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::ttsHelper.isInitialized) {
            ttsHelper.shutdown()
        }
        super.onDestroy()
    }
}