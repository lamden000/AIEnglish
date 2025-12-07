package com.example.lingobuddypck.data

data class DisplayableQuizContent(
    val passage: String,
    val questions: List<QuestionData>, // Sử dụng QuestionData chung
    val type: QuizDisplayType
)
