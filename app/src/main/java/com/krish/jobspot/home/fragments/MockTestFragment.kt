package com.krish.jobspot.home.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
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
import com.krish.jobspot.home.adapter.MockTestAdapter
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.model.MockTestState
import com.krish.jobspot.util.UiState
import com.krish.jobspot.util.UiState.*
import kotlinx.coroutines.awaitAll

private const val TAG = "MockTestFragmentTAG"
class MockTestFragment : Fragment() {
    private lateinit var binding: FragmentMockTestBinding
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val mockTestAdapter: MockTestAdapter by lazy { MockTestAdapter(this@MockTestFragment) }
    private val mockTestStateList: MutableList<MockTestState> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMockTestBinding.inflate(inflater, container, false)

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
            mockTestViewModel.mockTestStatus.observe(viewLifecycleOwner, Observer { quiz ->
                mockTestAdapter.setQuizData(quiz)
                this@MockTestFragment.mockTestStateList.clear()
                this@MockTestFragment.mockTestStateList.addAll(quiz)
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
            mockTestViewModel.eventFlow.collect{ uiState ->
                when(uiState){
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
}