package com.krish.jobspot.home.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.krish.jobspot.databinding.FragmentMockTestBinding
import com.krish.jobspot.home.activity.MockQuestionActivity
import com.krish.jobspot.home.activity.MockResultActivity
import com.krish.jobspot.home.adapter.MockTestAdapter
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.model.MockTestState
import com.krish.jobspot.util.Status.*
import com.krish.jobspot.util.showToast
import kotlinx.coroutines.launch

private const val TAG = "MockTestFragmentTAG"

class MockTestFragment : Fragment() {
    private var _binding: FragmentMockTestBinding? = null
    private val binding get() = _binding!!
    private var _mockTestAdapter: MockTestAdapter? = null
    private val mockTestAdapter get() = _mockTestAdapter!!

    private val mockTestViewModel by viewModels<MockTestViewModel>()
    private val mockTestStateList: MutableList<MockTestState> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMockTestBinding.inflate(inflater, container, false)
        _mockTestAdapter = MockTestAdapter(this@MockTestFragment)

        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupUI() {
        binding.apply {

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mockTestViewModel.fetchMockTestStatus()
                }
            }

            etSearch.addTextChangedListener { text: Editable? ->
                filterMockTest(text)
            }

            rvQuiz.adapter = mockTestAdapter
            rvQuiz.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObserver() {
        mockTestViewModel.mockTestStatus.observe(viewLifecycleOwner) { mockTestState ->
            when (mockTestState.status) {
                LOADING -> Unit
                SUCCESS -> {
                    val mockState = mockTestState.data!!
                    mockTestStateList.clear()
                    mockTestStateList.addAll(mockState)
                    mockTestAdapter.setMockState(mockState)
                }
                ERROR -> {
                    val errorMessage = mockTestState.message!!
                    showToast(requireContext(), errorMessage)
                }
            }
        }
    }

    private fun filterMockTest(text: Editable?) {
        if (!text.isNullOrEmpty()) {
            val filteredQuizList = mockTestStateList.filter { quizState ->
                val title = quizState.quizName.lowercase()
                val inputText = text.toString().lowercase()
                title.contains(inputText)
            }
            mockTestAdapter.setMockState(newQuizDetail = filteredQuizList)
        } else {
            mockTestAdapter.setMockState(newQuizDetail = mockTestStateList)
        }
    }


    fun navigateToMockQuestion(mockId: String) {
        val mockQuestion = Intent(requireContext(), MockQuestionActivity::class.java)
        mockQuestion.putExtra("MOCK_ID", mockId)
        startActivity(mockQuestion)
    }

    fun navigateToMockResult(mockId: String) {
        val mockResult = Intent(requireContext(), MockResultActivity::class.java)
        mockResult.putExtra("MOCK_ID", mockId)
        startActivity(mockResult)
    }

    override fun onDestroyView() {
        mockTestStateList.clear()
        _mockTestAdapter = null
        _binding = null
        super.onDestroyView()
    }
}