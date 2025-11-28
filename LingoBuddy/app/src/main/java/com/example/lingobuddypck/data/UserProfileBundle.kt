package com.example.lingobuddypck.data

data class UserProfileBundle( // Đặt tên cho phù hợp, ví dụ: UserDisplayProfile
    val name: String? = null,
    val job: String? = null,
    val interest: String? = null,
    val otherInfo: String? = null,
    val aiChatTone: String? = null // Thêm trường này
    // Thêm các trường khác nếu fetchCurrentUserInfo() của bạn lấy nhiều hơn
)