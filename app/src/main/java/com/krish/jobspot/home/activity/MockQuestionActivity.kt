package com.krish.jobspot.home.activity


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.krish.jobspot.databinding.ActivityMockQuestionBinding
import com.krish.jobspot.home.viewmodel.MockPagesViewModel



class MockQuestionActivity : AppCompatActivity() {
    private var _binding: ActivityMockQuestionBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<MockQuestionActivityArgs>()
    private val mockPagesViewModel : MockPagesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMockQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mockPagesViewModel.mock = args.mock
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}