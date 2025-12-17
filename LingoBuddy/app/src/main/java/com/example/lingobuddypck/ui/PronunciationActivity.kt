package com.example.lingobuddypck.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lingobuddypck.R
import com.example.lingobuddypck.network.RetrofitClient
import com.example.lingobuddypck.services.PronunciationAiService
import com.example.lingobuddypck.ViewModel.PronunciationViewModel
import com.example.lingobuddypck.services.PronunciationFeedback
import com.example.lingobuddypck.utils.TaskManager
import com.example.lingobuddypck.utils.TopicUtils
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pronunciation)

        btnStart = findViewById(R.id.btnStart)
        btnGenerateReference = findViewById(R.id.btnGenerateReference)
        etTopicInput = findViewById(R.id.etTopicInput)
        txtResult = findViewById(R.id.txtResult)
        tvReference = findViewById(R.id.tvReferenceText)
        txtStatus = findViewById(R.id.txtStatus)
        progressBar = findViewById(R.id.progressBar)

        // Get topic from intent if available
        intent.getStringExtra("topic")?.let { topic ->
            etTopicInput.setText(topic)
            // Automatically generate reference text for the topic
            viewModel.generateNewReferenceText(topic, true)
        }

        checkAudioPermission()
        setupListeners()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(createRecognitionListener())
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val userSpeech = matches[0]
                    viewModel.setUserSpeechResult(userSpeech)
                }
                // CORRECTED: Call ViewModel function to update status
                viewModel.updateStatusMessage("Ghi âm kết thúc.")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NETWORK -> "Lỗi mạng: Không kết nối được."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Không nhận diện được giọng nói."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Thiếu quyền RECORD_AUDIO."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được lời nói."
                    SpeechRecognizer.ERROR_CLIENT -> "Lỗi client nhận diện giọng nói."
                    else -> "Lỗi không xác định: $error"
                }
                Log.e("SpeechRecognizer", errorMessage)
                // CORRECTED: Call ViewModel function to set error
                viewModel.setErrorMessage(errorMessage)
                // CORRECTED: Call ViewModel function to update status
                viewModel.updateStatusMessage("Sẵn sàng.")
            }

            override fun onReadyForSpeech(params: Bundle?) {
                // CORRECTED: Call ViewModel function to update status
                viewModel.updateStatusMessage("Đang nghe...")
            }

            override fun onBeginningOfSpeech() {
                // CORRECTED: Call ViewModel function to update status
                viewModel.updateStatusMessage("Đã nhận diện giọng nói...")
            }

            override fun onEndOfSpeech() {
                // CORRECTED: Call ViewModel function to update status
                viewModel.updateStatusMessage("Đang xử lý...")
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun setupListeners() {
        btnStart.setOnClickListener {
            startListening()
        }

        btnGenerateReference.setOnClickListener {
            btnGenerateReference.isEnabled = false
            if (!etTopicInput.text.isNullOrEmpty()) {
                val topic = etTopicInput.text.toString().trim()
                viewModel.generateNewReferenceText(topic, true)
            } else {
                viewModel.generateNewReferenceText(TopicUtils.getRandomTopicFromAssets(this), false)
            }
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cần quyền ghi âm để sử dụng tính năng này.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)

        try {
            speechRecognizer.startListening(intent)
            // CORRECTED: Call ViewModel function to update status
            viewModel.updateStatusMessage("Đang nghe...")
        } catch (e: Exception) {
            Log.e("PronunciationActivity", "Error starting speech recognition: ${e.message}")
            // CORRECTED: Call ViewModel function to set error
            viewModel.setErrorMessage("Lỗi khi bắt đầu nhận diện giọng nói: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}