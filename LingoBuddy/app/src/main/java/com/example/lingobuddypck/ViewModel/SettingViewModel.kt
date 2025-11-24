package com.example.lingobuddypck.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions


import android.util.Log
import com.example.lingobuddypck.data.UserProfileBundle
import com.example.lingobuddypck.utils.RankUtils

class SettingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userScoreText = MutableLiveData<String?>()
    val userScoreText: LiveData<String?> = _userScoreText

    private val _userRankText = MutableLiveData<String?>()
    val userRankText: LiveData<String?> = _userRankText

    private val _fetchedUserInfo = MutableLiveData<UserProfileBundle?>() // Sử dụng UserProfileBundle
    val fetchedUserInfo: LiveData<UserProfileBundle?> = _fetchedUserInfo

    private val _isFetchingDetails = MutableLiveData<Boolean>() // Dùng chung cho fetch user info & tone
    val isFetchingDetails: LiveData<Boolean> = _isFetchingDetails

    private val _isSavingPersonalInfo = MutableLiveData<Boolean>()
    val isSavingPersonalInfo: LiveData<Boolean> = _isSavingPersonalInfo // Đổi tên từ isSaving

    private val _personalInfoSaveSuccess = MutableLiveData<Boolean>()
    val personalInfoSaveSuccess: LiveData<Boolean> = _personalInfoSaveSuccess // Đổi tên từ saveSuccess

    private val _isSavingAiTone = MutableLiveData<Boolean>()
    val isSavingAiTone: LiveData<Boolean> = _isSavingAiTone

    private val _aiToneSaveSuccess = MutableLiveData<Boolean>()
    val aiToneSaveSuccess: LiveData<Boolean> = _aiToneSaveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchUserProficiencyData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Người dùng chưa đăng nhập."
            _userScoreText.value = null
            _userRankText.value = null
            _isFetchingDetails.value = false
            return
        }

        _isFetchingDetails.value = true

        db.collection("users").document(userId)
            .collection("proficiencyTestResults")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val latestResult = documents.documents[0]
                    val score = latestResult.getLong("score")?.toInt() ?: 0
                    
                    // Update score text
                    _userScoreText.value = "$score/100"
                    
                    // Get and update rank text based on score
                    val rank = RankUtils.getRankFromScore(score)
                    _userRankText.value = rank?.displayName ?: "Chưa xếp hạng"
                } else {
                    _userScoreText.value = "Chưa có điểm"
                    _userRankText.value = "Chưa xếp hạng"
                }
                _isFetchingDetails.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Lỗi khi tải điểm đánh giá: ${e.message}"
                _userScoreText.value = null
                _userRankText.value = null
                _isFetchingDetails.value = false
            }
    }

    fun fetchCurrentUserInfo() { // Hàm này sẽ fetch cả personal info và aiChatTone
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Người dùng chưa đăng nhập."
            _fetchedUserInfo.value = null
            return
        }
        _isFetchingDetails.value = true
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userInfo = UserProfileBundle(
                        name = document.getString("name"),
                        job = document.getString("job"),
                        interest = document.getString("interest"),
                        otherInfo = document.getString("otherInfo"),
                        aiChatTone = document.getString("aiChatTone") ?: "trung lập và thân thiện" // Mặc định nếu null
                    )
                    _fetchedUserInfo.value = userInfo
                } else {
                    // Document không tồn tại, trả về giá trị mặc định cho aiChatTone
                    _fetchedUserInfo.value = UserProfileBundle(aiChatTone = "trung lập và thân thiện")
                }
                _isFetchingDetails.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Lỗi khi tải thông tin người dùng: ${e.message}"
                _fetchedUserInfo.value = null // Hoặc UserProfileBundle với giá trị mặc định
                _isFetchingDetails.value = false
            }
    }
}
