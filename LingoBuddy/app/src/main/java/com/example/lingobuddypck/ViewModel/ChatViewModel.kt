package com.example.lingobuddypck.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lingobuddypck.network.RetrofitClient
import com.example.lingobuddypck.services.ChatRequest
import com.example.lingobuddypck.services.ChatResponse
import com.example.lingobuddypck.services.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatViewModel : ViewModel() {

    private val maxHistorySize = 8
    private val _chatMessages = MutableLiveData<List<Message>>()
    val chatMessages: LiveData<List<Message>> = _chatMessages
    val isLoading = MutableLiveData<Boolean>(false)
    val isWaitingForResponse = MutableLiveData<Boolean>(false)

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val systemMessageContentBase = ("INSTRUCTION:"+
            "You are a virtual assistant who helps learners improve their spoken English. Your name is Lingo. " +
            "If the user speaks in Vietnamese, you may use Vietnamese to explain, but you must still wrap all English parts separately in <en>...</en> tags. " +
            "Example: 'Để giới thiệu về bản thân bạn có thể nói: <en>I'm [your name], it's nice to meet you</en>'"+
            "For each sentence, make sure every English word or phrase is properly wrapped in <en> and </en> tags. If you forget, it will be considered a mistake."+
            "IMPORTANT: If the user sends a message entirely in English, you MUST respond entirely in English and wrap the ENTIRE response in <en>...</en> tags " +
            "Example: <en>Let's have a conversation in English. I'd be happy to help you practice English</en> "
            )
    private var currentSystemMessageContent: String = systemMessageContentBase

    private val fullHistory = mutableListOf<Message>()

    private fun rebuildSystemMessageAndFinalizeSetup() {
        isLoading.value = false // Kết thúc trạng thái loading ban đầu
    }

    private fun setupInitialMessages() {
        fullHistory.clear()
        val assistantWelcomeMessage = Message("assistant",  "Xin chào! Tôi là Lingo. Chúng ta cùng bắt đầu buổi học tiếng Anh nhé?")
        fullHistory.add(assistantWelcomeMessage)
        _chatMessages.value = fullHistory.toList()
    }

    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return

        val userMessage = Message("user", userInput)
        fullHistory.add(userMessage)
        _chatMessages.value = fullHistory.toList()
        isWaitingForResponse.value=true

        val historyForAI = getHistoryForAI()
        Log.d("ChatViewModel", "Gửi tới AI với system message: ${historyForAI.firstOrNull { it.role == "system" }?.content}")

        val request = ChatRequest(
            model = "meta-llama/Llama-3.3-70B-Instruct-Turbo-Free",
            messages = historyForAI,
        )

        RetrofitClient.instance.chatWithAI(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                isLoading.postValue(false)
                val aiResponseText = response.body()?.output?.choices?.getOrNull(0)?.text
                if (response.isSuccessful && !aiResponseText.isNullOrEmpty()) {
                    val assistantMessage = Message("assistant", aiResponseText)
                    Log.d("DEBUG_CL",aiResponseText)
                    fullHistory.add(assistantMessage)
                    _chatMessages.postValue(fullHistory.toList())
                    isWaitingForResponse.value=false
                } else {
                    Log.e("ChatViewModel", "AI response không thành công hoặc trống. Code: ${response.code()}")
                    val errorMessage = Message("assistant", "Xin lỗi, tôi đang gặp chút vấn đề. Bạn thử lại sau nhé.")
                    fullHistory.add(errorMessage)
                    _chatMessages.postValue(fullHistory.toList())
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                isLoading.postValue(false)
                Log.e("ChatViewModel", "AI Chat API call failed", t)
                val errorMessage = Message("assistant", "Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng và thử lại.")
                fullHistory.add(errorMessage)
                _chatMessages.postValue(fullHistory.toList())
            }
        })
    }

    private fun getHistoryForAI(): List<Message> {
        val conversationTurns = mutableListOf<Message>()
        conversationTurns.addAll(fullHistory)
        while (conversationTurns.size > maxHistorySize) {
            conversationTurns.removeAt(0)
        }
        // Luôn tạo mới Message object cho system để đảm bảo nội dung là mới nhất
        val systemMsg = Message("system", currentSystemMessageContent)
        return listOf(systemMsg)+conversationTurns
    }
}