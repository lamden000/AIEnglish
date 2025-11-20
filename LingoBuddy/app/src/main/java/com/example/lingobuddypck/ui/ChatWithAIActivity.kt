package com.example.lingobuddypck.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lingobuddypck.ViewModel.ChatViewModel
import com.example.lingobuddypck.adapter.ChatAdapter
import com.example.lingobuddypck.data.ChatItemDecoration
import android.Manifest
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.lingobuddypck.BuildConfig
import com.example.lingobuddypck.R
import com.example.lingobuddypck.repository.FirebaseWordRepository
import com.example.lingobuddypck.services.Message
import com.google.android.gms.tasks.Tasks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class ChatWithAIActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    private lateinit var inputMessage: EditText
    private lateinit var sendButton: Button
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognitionIntent: Intent
    private lateinit var speechActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var micButton: ImageButton
    private var currentActualMessages: List<Message> = listOf()
    private val RECORD_AUDIO_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_with_ai)

        inputMessage = findViewById(R.id.inputMessage)
        sendButton = findViewById(R.id.sendButton)
        loadingSpinner = findViewById(R.id.loadingSpinner)
        recyclerView = findViewById(R.id.chatRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(ChatItemDecoration(50))
        micButton = findViewById(R.id.micButton)

        viewModel.chatMessages.observe(this, Observer { messages ->
            currentActualMessages = messages
            val lastMessageOriginal = messages.lastOrNull()

            adapter.setMessages(messages)

            if (messages.isNotEmpty()) {
                recyclerView.scrollToPosition(messages.size - 1)
            }
            updateAdapterWithTypingIndicator(viewModel.isWaitingForResponse.value ?: false)
        })

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }

        speechActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val results = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    results?.let {
                        if (it.isNotEmpty()) {
                            inputMessage.setText(it[0])
                            sendMessage()
                        }
                    }
                } else {
                    Toast.makeText(this, "Không nhận diện được giọng nói", Toast.LENGTH_SHORT).show()
                }
            }

        viewModel.isLoading.observe(this, Observer { isLoading ->
            loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
            sendButton.isEnabled = !isLoading
        })

        viewModel.isWaitingForResponse.observe(this, Observer { isWaitingForResponse ->
            updateAdapterWithTypingIndicator(isWaitingForResponse)
        })

        sendButton.setOnClickListener { sendMessage() }
        micButton.setOnClickListener { checkAudioPermissionAndStartRecognition() }
    }

    private fun updateAdapterWithTypingIndicator(isAiTyping: Boolean) {
        val displayList = ArrayList(currentActualMessages.filter { it.role != "typing_indicator" })

        if (isAiTyping) {
            displayList.add(Message("typing_indicator", null))
        }

        adapter.setMessages(displayList) // Cập nhật adapter với danh sách mới

        if (displayList.isNotEmpty()) {
            recyclerView.scrollToPosition(displayList.size - 1)
        }
    }


    private fun checkAudioPermissionAndStartRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
        } else {
            startSpeechRecognition()
        }
    }

    private fun startSpeechRecognition() {
        try {
            speechActivityResultLauncher.launch(speechRecognitionIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khởi động nhận diện giọng nói: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("STT", "Error launching speech recognizer", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition()
            } else {
                Toast.makeText(this, "Cần cấp quyền thu âm để sử dụng tính năng này", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::speechRecognizer.isInitialized) speechRecognizer.destroy()
    }

    private fun sendMessage() {
        val message = inputMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            viewModel.sendMessage(message)
            inputMessage.text.clear()
        } else {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show()
        }
    }

}