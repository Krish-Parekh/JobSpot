package com.krish.jobspot.home.fragments.userFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.krish.jobspot.databinding.FragmentUserTpoContactBinding
import com.krish.jobspot.home.adapter.TpoAdapter
import com.krish.jobspot.home.viewmodel.UserEditViewModel

class UserTpoContact : Fragment() {
    private var _binding : FragmentUserTpoContactBinding? = null
    private val binding get() = _binding!!
    private var _tpoAdapter : TpoAdapter? = null
    private val tpoAdapter get() = _tpoAdapter!!
    private val userEditViewModel by viewModels<UserEditViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserTpoContactBinding.inflate(inflater, container, false)
        _tpoAdapter = TpoAdapter()
        setupUI()
        setupObserver()

        return binding.root
    }

    private fun setupUI() {
        userEditViewModel.fetchTpo()

        binding.ivPopOut.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rvContactTPO.adapter = tpoAdapter
        binding.rvContactTPO.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObserver() {
        userEditViewModel.tpoList.observe(viewLifecycleOwner){ tpoList ->
            tpoAdapter.setData(tpoList)
        }
    }

    override fun onDestroyView() {
        _binding = null
        _tpoAdapter = null
        super.onDestroyView()
    }

}