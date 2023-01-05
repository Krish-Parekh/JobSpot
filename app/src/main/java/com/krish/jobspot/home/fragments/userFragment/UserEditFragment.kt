package com.krish.jobspot.home.fragments.userFragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.github.dhaval2404.imagepicker.ImagePicker
import com.krish.jobspot.databinding.FragmentUserEditBinding
import com.krish.jobspot.home.viewmodel.UserEditViewModel
import com.krish.jobspot.util.addTextWatcher
import com.krish.jobspot.util.getInputValue
import com.krish.jobspot.util.showToast

class UserEditFragment : Fragment() {
    private var _binding: FragmentUserEditBinding? = null
    private val binding get() = _binding!!
    private val args: UserEditFragmentArgs by navArgs()
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedImage(result)
        }
    private val userEditViewModel : UserEditViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserEditBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.apply {
            profileImage.load(args.student.details?.imageUrl)
            etUsername.setText(args.student.details?.username)
            etEmail.setText(args.student.details?.email)
            etSapId.setText(args.student.details?.sapId)
            etMobile.setText(args.student.details?.mobile)

            if (userEditViewModel.getImageUri() != null) {
                profileImage.setImageURI(userEditViewModel.getImageUri())
            }

            profileImage.setOnClickListener {
                startCrop()
            }

            etUsernameContainer.addTextWatcher()
            etEmailContainer.addTextWatcher()
            etSapIdContainer.addTextWatcher()
            etMobileContainer.addTextWatcher()

            btnSaveChange.setOnClickListener {
                val username = etUsername.getInputValue()
                val email = etEmail.getInputValue()
                val sapId = etSapId.getInputValue()
                val mobile = etMobile.getInputValue()
                val imageUrl = userEditViewModel.getImageUri() ?: args.student.details?.imageUrl

                args.student.details?.username = username
                args.student.details?.email = email
                args.student.details?.sapId = sapId
                args.student.details?.mobile = mobile
                args.student.details?.imageUrl = imageUrl.toString()

                userEditViewModel.uploadStudentData(student = args.student)
            }

        }
    }

    private fun startCrop() {
        ImagePicker.with(this)
            .galleryOnly()
            .crop()
            .compress(1024)
            .maxResultSize(300, 300)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun handleCapturedImage(result: ActivityResult) {
        val resultCode = result.resultCode
        val data = result.data

        when (resultCode) {
            Activity.RESULT_OK -> {
                userEditViewModel.setImageUri(imageUri = data?.data!!)
                binding.profileImage.setImageURI(userEditViewModel.getImageUri())
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                showToast(requireContext(), "Task Cancelled")
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}