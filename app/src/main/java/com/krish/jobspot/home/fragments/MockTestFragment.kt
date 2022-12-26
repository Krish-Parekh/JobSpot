package com.krish.jobspot.home.fragments

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.krish.jobspot.databinding.FragmentMockTestBinding
import com.krish.jobspot.home.adapter.QuizListAdapter
import com.krish.jobspot.home.viewmodel.MockTestViewModel
import com.krish.jobspot.model.QuizState


class MockTestFragment : Fragment() {
    private lateinit var binding: FragmentMockTestBinding
    private val mockTestViewModel: MockTestViewModel by viewModels()
    private val quizListAdapter: QuizListAdapter by lazy { QuizListAdapter(this@MockTestFragment) }
    private val quizStateList: MutableList<QuizState> = mutableListOf()
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
            mockTestViewModel.fetchQuiz()

            etSearch.addTextChangedListener { text: Editable? ->
                filterQuiz(text)
            }


            rvQuiz.adapter = quizListAdapter
            rvQuiz.layoutManager = LinearLayoutManager(requireContext())
            mockTestViewModel.quiz.observe(viewLifecycleOwner, Observer { quiz ->
                quizListAdapter.setQuizData(quiz)
                this@MockTestFragment.quizStateList.clear()
                this@MockTestFragment.quizStateList.addAll(quiz)
            })
        }
    }

    private fun filterQuiz(text: Editable?) {
        if (!text.isNullOrEmpty()) {
            val filteredQuizList = quizStateList.filter { quizState ->
                val title = quizState.quizName.lowercase()
                val inputText = text.toString().lowercase()
                title.contains(inputText)
            }
            quizListAdapter.setQuizData(newQuizDetail = filteredQuizList)
        } else {
            quizListAdapter.setQuizData(newQuizDetail = quizStateList)
        }
    }


    fun navigateToMockTestActivity(quizState: QuizState) {
        val direction = MockTestFragmentDirections.actionMockTestFragmentToMockTestActivity(quizState)
        findNavController().navigate(direction)
    }
}