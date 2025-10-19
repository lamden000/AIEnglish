package com.example.lingobuddypck.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lingobuddypck.R
import com.example.lingobuddypck.ViewModel.HomeViewModel
import com.example.lingobuddypck.adapter.FeatureAdapter
import com.example.lingobuddypck.data.Feature
import com.example.lingobuddypck.databinding.FragmentHomeBinding
import com.example.lingobuddypck.ui.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val featureList = listOf(
            Feature("Chat với AI Gia Sư", R.drawable.chat_ai),
            Feature("Luyện phát âm bằng giọng nói", R.drawable.ic_pronounciation),
            Feature("Nhận diện & Học từ vựng từ hình ảnh", R.drawable.ic_camera),
            Feature("Học từ vựng & Ngữ pháp qua đoạn văn AI tạo", R.drawable.ic_test),
            Feature("Nhập vai với AI", R.drawable.ic_role_play)
        )

        //Tự tạo actibity xong map tên với activity đó
        val featureActivities = mapOf(
            "Chat với AI Gia Sư" to MainActivity::class.java,
            /*"Chat với AI Gia Sư" to ChatWithAIActivity::class.java,
            "Luyện phát âm bằng giọng nói" to PronunciationActivity::class.java,
            "Nhận diện & Học từ vựng từ hình ảnh" to ImageLearningActivity::class.java,
            "Nhập vai với AI" to RolePlayActivity::class.java,
            "Học từ vựng & Ngữ pháp qua đoạn văn AI tạo" to PassageQuizActivity::class.java*/
        )


        val adapter = FeatureAdapter(featureList) { feature ->
            val activityClass = featureActivities[feature.name]
            if (activityClass != null) {
                val intent = Intent(requireContext(), activityClass)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Chức năng chưa được hỗ trợ!", Toast.LENGTH_SHORT).show()
            }
        }


        recyclerView.adapter = adapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}