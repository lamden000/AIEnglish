package com.example.lingobuddypck.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lingobuddypck.R
import com.example.lingobuddypck.ViewModel.SettingViewModel
import com.example.lingobuddypck.databinding.FragmentSettingBinding
import com.example.lingobuddypck.ui.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null // Giả sử bạn dùng ViewBinding
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingViewModel
    private lateinit var scoreTextView: TextView
    private lateinit var rankTextView: TextView
    private lateinit var buttonEditInfo: Button
    private lateinit var buttonOpenAiToneDialog: Button // Nút bạn đã khai báo
    private lateinit var buttonLogout: Button
    private lateinit var buttonChangePassword: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Khởi tạo views từ binding
        scoreTextView = binding.textViewUserScore
        rankTextView = binding.textViewUserRank
        buttonEditInfo = binding.buttonPersonalInfo
        buttonOpenAiToneDialog = binding.buttonPersonalize // Đây là button bạn muốn dùng
        buttonLogout = binding.buttonLogout
        buttonChangePassword = binding.buttonChangePassword

        buttonEditInfo.setOnClickListener {
        }

        buttonOpenAiToneDialog.setOnClickListener { // Gán listener cho nút này
        }

        buttonChangePassword.setOnClickListener {
            handlePasswordChange()
        }

        buttonLogout.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("rememberMe", false)
                apply()
            }
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish() // Đóng Activity chứa Fragment này
        }

        // Fetch dữ liệu khi view được tạo
        // fetchUserProficiencyData() đã có, fetchCurrentUserInfo() sẽ lấy cả AI tone
        viewModel.fetchUserProficiencyData() // Nếu bạn vẫn muốn gọi riêng
        viewModel.fetchCurrentUserInfo()

        return root
    }

    private fun handlePasswordChange() {
        val user = FirebaseAuth.getInstance().currentUser

        // Check if user signed in with Google
        val isGoogleSignIn = user?.providerData?.any {
            it.providerId == "google.com"
        } ?: false

        if (isGoogleSignIn) {
            // Show dialog for Google users
            AlertDialog.Builder(requireContext())
                .setTitle("Không thể đổi mật khẩu")
                .setMessage("Bạn đã đăng nhập bằng tài khoản Google. Vui lòng quản lý mật khẩu thông qua cài đặt tài khoản Google của bạn.")
                .setPositiveButton("Đã hiểu") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            // Show confirmation dialog for email users
            AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setMessage("Chúng tôi sẽ gửi link đặt lại mật khẩu đến email của bạn: ${user?.email}")
                .setPositiveButton("Gửi") { dialog, _ ->
                    user?.email?.let { email ->
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Đã gửi link đặt lại mật khẩu đến email của bạn",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Lỗi: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}