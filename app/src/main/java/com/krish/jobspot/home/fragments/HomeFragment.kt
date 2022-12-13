package com.krish.jobspot.home.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.renderscript.Sampler.Value
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        counterAnimation(0, 100, binding.tvCompaniesCount)
        counterAnimation(0, 50, binding.tvJobAppliedCount)
        return binding.root
    }

    private fun counterAnimation(start : Int, end : Int, textView : TextView){
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = 5000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener {
            val counter = it.animatedValue as Int
            textView.text = counter.toString()
        }
        animator.start()
    }
}