package com.krish.jobspot.home.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.krish.jobspot.databinding.FragmentMockTestBinding
import com.krish.jobspot.home.activity.MockResultActivity
import com.krish.jobspot.home.adapter.MockTestAdapter
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.model.MockTestState
import com.krish.jobspot.util.UiState.*

private const val TAG = "MockTestFragmentTAG"

class MockTestFragment : Fragment() {
    private var _binding: FragmentMockTestBinding? = null
    private val binding get() = _binding!!
    private var _mockTestAdapter: MockTestAdapter? = null
    private val mockTestAdapter get() = _mockTestAdapter!!
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val mockTestStateList: MutableList<MockTestState> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMockTestBinding.inflate(inflater, container, false)
        _mockTestAdapter = MockTestAdapter(this@MockTestFragment)
        setupViews()

        return binding.root
    }

    private fun setupViews() {
        binding.apply {
            mockTestViewModel.fetchMockTestStatus()

            etSearch.addTextChangedListener { text: Editable? ->
                filterMockTest(text)
            }


            rvQuiz.adapter = mockTestAdapter
            rvQuiz.layoutManager = LinearLayoutManager(requireContext())
            mockTestViewModel.mockTestStatus.observe(viewLifecycleOwner, Observer { mockList ->
                mockTestStateList.clear()
                mockTestStateList.addAll(mockList)
                mockTestAdapter.setQuizData(mockTestStateList)
            })
        }
    }

    private fun filterMockTest(text: Editable?) {
        if (!text.isNullOrEmpty()) {
            val filteredQuizList = mockTestStateList.filter { quizState ->
                val title = quizState.quizName.lowercase()
                val inputText = text.toString().lowercase()
                title.contains(inputText)
            }
            mockTestAdapter.setQuizData(newQuizDetail = filteredQuizList)
        } else {
            mockTestAdapter.setQuizData(newQuizDetail = mockTestStateList)
        }
    }


    fun navigateToMockTestActivity(mockTestState: MockTestState) {
        mockTestViewModel.fetchMockTest(mockTestId = mockTestState.quizUid)
        lifecycleScope.launchWhenStarted {
            mockTestViewModel.eventFlow.collect { uiState ->
                when (uiState) {
                    LOADING -> Unit
                    SUCCESS -> {
                        val mock = mockTestViewModel.mock
                        val direction = MockTestFragmentDirections.actionMockTestFragmentToMockQuestionActivity(mock)
                        findNavController().navigate(direction)
                    }
                    FAILURE -> Unit
                }
            }
        }
    }

    fun navigateToMockResultActivity(mockTestState: MockTestState){
        val intent = Intent(requireContext(), MockResultActivity::class.java)
        intent.putExtra("MOCK_ID", mockTestState.quizUid)
        startActivity(intent)
    }

    override fun onDestroyView() {
        mockTestStateList.clear()
        _mockTestAdapter = null
        _binding = null
        super.onDestroyView()
    }
}