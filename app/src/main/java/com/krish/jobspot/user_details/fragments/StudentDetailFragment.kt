package com.krish.jobspot.user_details.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentStudentDetailBinding


class StudentDetailFragment : Fragment() {

    private lateinit var binding: FragmentStudentDetailBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
}