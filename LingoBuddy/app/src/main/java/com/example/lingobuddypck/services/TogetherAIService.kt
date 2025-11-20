package com.example.lingobuddypck.services

import android.net.Uri
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TogetherApi {
    @POST("inference")
    fun chatWithAI(@Body request: ChatRequest): Call<ChatResponse>

    @POST("inference")
    fun chatWithImageAI(@Body request: ChatRequestImage): Call<ChatImageResponse>
}

interface OpenAIAPI {
    @POST("chat/completions")
    fun chatWithAI(@Body request: ChatRequest): Call<ChatResponse>

    @POST("chat/completions")
    fun chatWithImageAI(@Body request: ChatRequestImage): Call<ChatImageResponse>
}

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int = 3000,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String? = null,
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatResponse(
    val output: Output
)

data class Output(
    val choices: List<Choice>
)

data class Choice(
    val text: String
)

data class ChatImageResponse(
    val choices: List<ChoiceImage>
)

data class ChatRequestImage(
    val model: String,
    val messages: List<Map<String, Any>>,
    val max_tokens: Int = 1000
)

data class ChoiceImage(
    val message: MessageContent,
    val logprobs: Any?,
    val finish_reason: String
)

data class MessageContent(
    val role: String,
    val content: String,
    val tool_calls: List<Any>
)

data class PronunciationFeedback(
    val score: Double,
    val mistakes: List<String>,
    val suggestions: List<String>
)