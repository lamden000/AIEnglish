package com.example.lingobuddypck.ui

import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lingobuddypck.network.RetrofitClient
import com.example.lingobuddypck.services.PronunciationAiService
import com.example.lingobuddypck.viewmodel.PronunciationViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlin.getValue

class PronunciationActivity : AppCompatActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var btnStart: Button
    private lateinit var btnGenerateReference: Button
    private lateinit var etTopicInput: TextInputEditText
    private lateinit var txtResult: TextView
    private lateinit var tvReference: TextView
    private lateinit var txtStatus: TextView
    private lateinit var progressBar: ProgressBar

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private val viewModel: PronunciationViewModel by viewModels {
        PronunciationViewModel.Factory(
            PronunciationAiService(RetrofitClient.instance, Gson())
        )
    }
}