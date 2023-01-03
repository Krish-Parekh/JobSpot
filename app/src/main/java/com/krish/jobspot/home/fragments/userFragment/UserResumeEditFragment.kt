package com.krish.jobspot.home.fragments.userFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentUserResumeEditBinding
import com.krish.jobspot.home.viewmodel.UserEditViewModel

class UserResumeEditFragment : Fragment() {
    private var _binding : FragmentUserResumeEditBinding? = null
    private val binding get() = _binding!!
    private val userEditViewModel : UserEditViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserResumeEditBinding.inflate(inflater, container, false)

        setupViews()

        return binding.root
    }

    private fun setupViews() {
        userEditViewModel.fetchStudentResume()
        userEditViewModel.fileData.observe(viewLifecycleOwner, Observer { metadata ->
            if (metadata != null){
                binding.layoutUploadedPdf.tvFileName.text = metadata.first
                binding.layoutUploadedPdf.tvFileMetaData.text = metadata.second
            }
        })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}