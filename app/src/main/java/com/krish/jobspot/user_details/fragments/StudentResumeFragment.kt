package com.krish.jobspot.user_details.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.krish.jobspot.R
import com.krish.jobspot.databinding.FragmentStudentResumeBinding
import com.krish.jobspot.user_details.viewmodel.UserDetailViewModel
import com.krish.jobspot.util.showToast
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import androidx.lifecycle.Observer
import com.krish.jobspot.util.LoadingDialog
import java.util.*

private const val TAG = "StudentResumeFragment"

class StudentResumeFragment : Fragment() {

    private lateinit var binding: FragmentStudentResumeBinding
    private val args by navArgs<StudentResumeFragmentArgs>()
    private val userDetailViewModel : UserDetailViewModel by  viewModels()
    private val pdfLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleCapturedPdf(result)
        }
    private val loadingDialog : LoadingDialog by lazy { LoadingDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStudentResumeBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {

        binding.apply {
            ivPopOut.setOnClickListener {
                findNavController().popBackStack()
            }
            layoutUploadPdf.root.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT);
                intent.type = "application/pdf";
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                pdfLauncher.launch(intent)
            }

            if(userDetailViewModel.getPdfUri() != null){
                hidePdfUploadView()
                getFileInfo(userDetailViewModel.getPdfUri()!!)
            }

            layoutUploadedPdf.llFileRemoveContainer.setOnClickListener {
                userDetailViewModel.setPdfUri(null)
                showDeleteDialog()
            }

            btnSubmit.setOnClickListener {
                val imageUri = Uri.parse(args.student.details?.imageUrl)
                val pdfUri = userDetailViewModel.getPdfUri()
                if(pdfUri != null){
                    Log.d(TAG, "User: ${args.student}")
                    userDetailViewModel.uploadStudentData(pdfUri, imageUri, args.student)
                    handleUploadResponse()
                } else {
                    showToast(requireContext(), "Please attach your resume")
                }
            }
        }
    }

    private fun handleUploadResponse() {
        userDetailViewModel.uploadDataStatus.observe(viewLifecycleOwner, Observer { uiState ->
            if(uiState.loading){
                loadingDialog.show()
            }else if(uiState.success){
                hidePdfUploadedView()
                userDetailViewModel.setPdfUri(null)
                loadingDialog.dismiss()
            }else if(uiState.failed){
                loadingDialog.dismiss()
            }
        })
    }


    private fun handleCapturedPdf(result: ActivityResult) {
        val resultCode = result.resultCode
        val data = result.data
        when (resultCode) {
            Activity.RESULT_OK -> {
                userDetailViewModel.setPdfUri(pdfUri = data?.data!!)
                Log.d(TAG, "Data : ${userDetailViewModel.getPdfUri()}")
                getFileInfo(userDetailViewModel.getPdfUri()!!)
            }
            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "TASK CANCELLED")
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileInfo(pdfUri: Uri) {
        try {
            if (pdfUri.scheme.equals("content")) {
                val pdfCursor: Cursor? =
                    requireActivity().contentResolver.query(pdfUri, null, null, null, null)
                pdfCursor.use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val fileName =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        val fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE))
                        val fileDateTime: Long = try {
                            val tempColDate = cursor
                                .getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                            cursor.moveToFirst()
                            cursor.getLong(tempColDate)
                        } catch (e: Exception) {
                            Log.d(TAG, "Exception: ${e.message}")
                            0
                        }

                        val simpleFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = fileDateTime
                        val formattedDtm = simpleFormatter.format(calendar.time)

                        val fileSizeInMb = fileSize.toDouble() / (1024 * 1024)
                        val sizeInFormat = DecimalFormat("#.##").format(fileSizeInMb)
                        val maxSize = 5  // 5 MB in bytes
                        Log.d(TAG, "FileSize : $fileSizeInMb, MaxFileSize : $maxSize")
                        if(fileSizeInMb > maxSize){
                            showToast(requireContext(), "File size above 5MB")
                        }else{
                            setupFileView(fileName, sizeInFormat, formattedDtm)
                            hidePdfUploadView()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception: ${e.message}")
        }
    }

    private fun setupFileView(fileName: String?, sizeInFormat: String, formattedDtm: String) {
        binding.layoutUploadedPdf.tvFileName.text = fileName
        binding.layoutUploadedPdf.tvFileMetaData.text =
            getString(R.string.resume_meta_data, sizeInFormat, formattedDtm)
    }

    private fun showDeleteDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_delete_file, null)
        val btnNot: MaterialButton = bottomSheet.findViewById(R.id.btnNo)
        val btnRemove: MaterialButton = bottomSheet.findViewById(R.id.btnRemoveFile)
        btnNot.setOnClickListener {
            dialog.dismiss()
        }
        btnRemove.setOnClickListener {
            dialog.dismiss()
            hidePdfUploadedView()
        }
        dialog.setContentView(bottomSheet)
        dialog.show()
    }

    private fun hidePdfUploadView() {
        binding.layoutUploadPdf.root.visibility = View.GONE
        binding.layoutUploadedPdf.root.visibility = View.VISIBLE
    }

    private fun hidePdfUploadedView() {
        binding.layoutUploadedPdf.root.visibility = View.GONE
        binding.layoutUploadPdf.root.visibility = View.VISIBLE
    }
}