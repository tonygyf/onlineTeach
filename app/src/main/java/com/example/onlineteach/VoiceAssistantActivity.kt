package com.example.onlineteach



import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import java.util.Properties

class VoiceAssistantActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var voiceAnimationView: LottieAnimationView
    private lateinit var chatAdapter: ChatMessageAdapter

    private var generativeModel: GenerativeModel? = null // Nullable in case API key fails
    private var isWaitingForResponse = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_voice_assistant)

        initViews()
        initGeminiApi()
        setupClickListeners()
    }

    private fun initViews() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        inputEditText = findViewById(R.id.input_edit_text)
        sendButton = findViewById(R.id.send_button)
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
}