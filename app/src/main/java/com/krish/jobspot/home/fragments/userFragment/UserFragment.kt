package com.krish.jobspot.home.fragments.userFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import coil.load
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentUserBinding
import com.krish.jobspot.home.viewmodel.UserEditViewModel
import com.krish.jobspot.model.Student


class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val userEditViewModel : UserEditViewModel by viewModels()
    private var student : Student? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        setupViews()
        return binding.root
    }

    private fun setupViews() {

        binding.ivPopOut.setOnClickListener {
            requireActivity().finish()
        }

        userEditViewModel.fetchStudent()
        userEditViewModel.student.observe(viewLifecycleOwner, Observer { student ->
            if (student != null){
                this.student = student
                binding.tvUsername.text = student.details?.username
                binding.tvUserEmail.text = student.details?.email
                binding.profileImage.load(student.details?.imageUrl)
            }
        })

        binding.cvManageAccount.setOnClickListener {
            if (this.student != null){
                val directions = UserFragmentDirections.actionUserFragmentToUserEditFragment(this.student!!)
                findNavController().navigate(directions)
            }
        }

        binding.cvUpdateResume.setOnClickListener {
            if (this.student != null){
                findNavController().navigate(R.id.action_userFragment_to_userResumeEditFragment)
            }
        }

        binding.cvContactTpo.setOnClickListener {
            findNavController().navigate(R.id.action_userFragment_to_userTpoContact)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}