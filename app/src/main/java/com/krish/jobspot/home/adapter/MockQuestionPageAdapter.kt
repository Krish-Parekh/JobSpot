package com.krish.jobspot.home.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.krish.jobspot.home.fragments.QuestionViewFragment
import com.krish.jobspot.model.MockQuestion

class MockQuestionPageAdapter(
    fragmentManager: FragmentManager,
    private val questions: List<MockQuestion>,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putParcelable("QUESTION", questions[position])
        bundle.putInt("QUESTION_ID", position)
        val questionViewFragment = QuestionViewFragment()
        questionViewFragment.arguments = bundle
        return questionViewFragment
    }



}