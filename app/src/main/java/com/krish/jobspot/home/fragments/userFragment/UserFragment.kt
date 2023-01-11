package com.krish.jobspot.home.fragments.userFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.krish.jobspot.R
import com.krish.jobspot.auth.AuthActivity
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

        binding.cvLogout.setOnClickListener {
            logoutBottomSheet()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun logoutBottomSheet(){
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_logout, null)
        val btnNot: MaterialButton = bottomSheet.findViewById(R.id.btnNo)
        val btnRemove: MaterialButton = bottomSheet.findViewById(R.id.btnLogout)
        btnNot.setOnClickListener {
            dialog.dismiss()
        }
        btnRemove.setOnClickListener {
            dialog.dismiss()
            FirebaseAuth.getInstance().signOut()
            requireActivity().finishAffinity()
            val loginIntent = Intent(requireContext(), AuthActivity::class.java)
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(loginIntent)
        }
        dialog.setContentView(bottomSheet)
        dialog.show()
    }

}