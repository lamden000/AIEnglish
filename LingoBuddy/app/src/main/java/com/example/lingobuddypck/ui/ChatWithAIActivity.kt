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

    private var currentActualMessages: List<Message> = listOf()


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

        viewModel.chatMessages.observe(this, Observer { messages ->
            currentActualMessages = messages
            val lastMessageOriginal = messages.lastOrNull()

            adapter.setMessages(messages)

            if (messages.isNotEmpty()) {
                recyclerView.scrollToPosition(messages.size - 1)
            }
            updateAdapterWithTypingIndicator(viewModel.isWaitingForResponse.value ?: false)
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
            sendButton.isEnabled = !isLoading
        })

        viewModel.isWaitingForResponse.observe(this, Observer { isWaitingForResponse ->
            updateAdapterWithTypingIndicator(isWaitingForResponse)
        })

        sendButton.setOnClickListener { sendMessage() }

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