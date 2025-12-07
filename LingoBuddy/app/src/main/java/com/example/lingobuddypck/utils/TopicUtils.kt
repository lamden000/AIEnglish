package com.example.lingobuddypck.utils

import android.content.Context
import android.util.Log
import kotlin.collections.random
import kotlin.io.bufferedReader
import kotlin.io.useLines
import kotlin.sequences.toList

object TopicUtils {
    private const val PREF_NAME = "pronunciation_tasks"
    private const val KEY_SCORE_TASK = "score_task_completed_"
    private const val KEY_TOPIC_TASK = "topic_task_completed_"
    private const val KEY_CURRENT_TOPIC = "current_topic_"

    fun getRandomTopicFromAssets(context: Context): String {
        return try {
            val inputStream = context.assets.open("topics.txt")
            val topics = inputStream.bufferedReader().useLines { it.toList() }
            topics.random()
        } catch (e: Exception) {
            Log.e("TopicUtils", "Error reading topics from assets: ${e.message}")
            "General English" // Fallback topic
        }
    }

}