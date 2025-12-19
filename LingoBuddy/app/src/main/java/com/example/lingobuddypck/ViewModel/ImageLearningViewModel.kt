package com.example.lingobuddypck.ViewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingobuddypck.data.ImageQuiz
import com.example.lingobuddypck.network.RetrofitClient
import com.example.lingobuddypck.services.ChatImageResponse
import com.example.lingobuddypck.services.ChatRequestImage
import com.example.lingobuddypck.services.Message
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.collections.orEmpty
import kotlin.collections.toMutableList

class ImageLearningViewModel : ViewModel(){
    private val _chatMessages = MutableLiveData<List<Message>>()
    val chatMessages: LiveData<List<Message>> = _chatMessages

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _currentQuiz = MutableLiveData<ImageQuiz?>()
    val currentQuiz: LiveData<ImageQuiz?> = _currentQuiz

    private val _isGeneratingQuiz = MutableLiveData<Boolean>()
    val isGeneratingQuiz: LiveData<Boolean> = _isGeneratingQuiz

    private val _quizScore = MutableLiveData<Pair<Int, Int>?>() // Pair of (score, total)
    val quizScore: LiveData<Pair<Int, Int>?> = _quizScore

    private val chatHistory = mutableListOf<Message>()
    private val MAX_CHAT_HISTORY = 5
    val isWaitingForResponse = MutableLiveData<Boolean>(false)
    val imageModel ="meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8"

    data class QuizFeedback(
        val status: String, // "correct" or "incorrect"
        val explanation: String? = null // Only present for incorrect answers
    )

    data class QuizResult(
        val score: Int,
        val totalQuestions: Int,
        val feedback: Map<String, QuizFeedback>
    )

    private val _quizResult = MutableLiveData<QuizResult?>()
    val quizResult: LiveData<QuizResult?> = _quizResult

    init {
        val aiMessage = Message(
            content = "Xin chào, tôi có thể giúp gì cho bạn?",
            role = "assistant",
            imageUri = null
        )
        val currentMessages = _chatMessages.value.orEmpty().toMutableList()
        currentMessages.add(aiMessage)
        _chatMessages.value = currentMessages
    }

    fun sendImageAndMessage(context: Context, message: String, imageUri: Uri?) {
        _loading.value = true

        viewModelScope.launch { // This is your coroutine scope
            try {
                val contentList = mutableListOf<Map<String, Any>>()
                val currentMessages = _chatMessages.value.orEmpty().toMutableList()
                var base64ImageForUserMessage: String? = null

                if (message.isNotEmpty()) {
                    contentList.add(mapOf("type" to "text", "text" to message))
                }

                if (imageUri != null) {
                    base64ImageForUserMessage = encodeImageToBase64(context, imageUri)
                    contentList.add(
                        mapOf(
                            "type" to "image_url",
                            "image_url" to mapOf("url" to base64ImageForUserMessage)
                        )
                    )
                }

                val userMessage = Message(
                    content = message,
                    role = "user",
                    imageUri = imageUri,
                    imageUrl = base64ImageForUserMessage
                )

                currentMessages.add(userMessage)
                _chatMessages.value = currentMessages

                // Prepare the messages for the request, including chat history
                val messagesForRequest = mutableListOf<Map<String, Any>>()

                // Add past messages from history to the request
                chatHistory.forEach { msg ->
                    val historyContentList = mutableListOf<Map<String, Any>>()
                    msg.content?.let {
                        historyContentList.add(mapOf("type" to "text", "text" to it))
                    }
                    msg.imageUrl?.let {
                        historyContentList.add(mapOf("type" to "image_url", "image_url" to mapOf("url" to it)))
                    }
                    messagesForRequest.add(mapOf("role" to msg.role, "content" to historyContentList))
                }

                // Add the current user message to the request
                messagesForRequest.add(mapOf("role" to "user", "content" to contentList))

                val request = ChatRequestImage(
                    model = imageModel,
                    messages = messagesForRequest,
                    max_tokens = 1000
                )

                ///////
                val gson = GsonBuilder().setPrettyPrinting().create()

                val requestForLogging = request.copy() // giữ nguyên request thật để gửi

// Tạo phiên bản log-safe
                val messagesForLogging = requestForLogging.messages.map { msg ->
                    val contentList = msg["content"] as? MutableList<Map<String, Any>> ?: mutableListOf()
                    val contentListMasked = contentList.map { contentMap ->
                        if (contentMap["type"] == "image_url") {
                            // Thay base64 bằng placeholder
                            mapOf("type" to "image_url", "image_url" to mapOf("url" to "--- base64 removed ---"))
                        } else contentMap
                    }
                    mapOf("role" to msg["role"], "content" to contentListMasked)
                }

                val jsonMasked = gson.toJson(
                    mapOf(
                        "model" to requestForLogging.model,
                        "max_tokens" to requestForLogging.max_tokens,
                        "messages" to messagesForLogging
                    )
                )

                Log.d("API_REQUEST_SAFE", jsonMasked)
                //////

                isWaitingForResponse.value=true
                // Call the API (using enqueue for Retrofit, which handles its own threading)
                RetrofitClient.instance.chatWithImageAI(request).enqueue(object : Callback<ChatImageResponse> {
                    override fun onResponse(call: Call<ChatImageResponse>, response: Response<ChatImageResponse>) {
                        _loading.value = false
                        isWaitingForResponse.value=false
                        if (response.isSuccessful) {
                            val responseContent = response.body()?.choices?.get(0)?.message?.content ?: "No response from AI."

                            val aiMessage = Message(
                                content = responseContent,
                                role = "assistant",
                                imageUri = null
                            )

                            // Update the LiveData with the new list of messages
                            val currentMessages = _chatMessages.value.orEmpty().toMutableList()
                            currentMessages.add(aiMessage)
                            _chatMessages.value = currentMessages

                            // Update chat history
                            chatHistory.add(userMessage)
                            chatHistory.add(aiMessage)

                            // Trim history
                            if (chatHistory.size > MAX_CHAT_HISTORY * 2) {
                                chatHistory.subList(0, chatHistory.size - MAX_CHAT_HISTORY * 2).clear()
                            }

                        } else {
                            _loading.value = false
                            _chatMessages.value = listOf(Message( "AI","Error: ${response.message()}"))
                        }
                    }

                    override fun onFailure(call: Call<ChatImageResponse>, t: Throwable) {
                        _loading.value = false
                        isWaitingForResponse.value=false
                        _chatMessages.value = listOf(Message("AI", "Error: ${t.message}"))
                        Log.e("API_ERROR", "API call failed", t)
                    }
                })
            } catch (e: Exception) {
                _loading.value = false
                isWaitingForResponse.value=false
                _chatMessages.value = listOf(Message("AI", "Error encoding image: ${e.message}"))
                Log.e("IMAGE_ENCODING", "Image encoding failed", e)
            }
        }
    }

    private suspend fun encodeImageToBase64(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return@withContext "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun clearQuiz() {
        _currentQuiz.value = null
        _quizScore.value = null
        _quizResult.value = null
    }

    fun submitQuizAnswers(answers: Map<String, String>) {
        val quiz = _currentQuiz.value ?: return
        var correctAnswers = 0
        val feedback = mutableMapOf<String, QuizFeedback>()

        quiz.questions.forEach { question ->
            val userAnswer = answers[question.id]
            val isCorrect = userAnswer == question.correctAnswer

            if (isCorrect) {
                correctAnswers++
                feedback[question.id] = QuizFeedback(status = "correct")
            } else {
                feedback[question.id] = QuizFeedback(
                    status = "incorrect",
                    explanation = question.explanation
                )
            }
        }

        _quizResult.value = QuizResult(
            score = correctAnswers,
            totalQuestions = quiz.questions.size,
            feedback = feedback
        )
        _quizScore.value = Pair(correctAnswers, quiz.questions.size)
    }
}