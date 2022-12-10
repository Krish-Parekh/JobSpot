package com.krish.jobspot.util

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.krish.jobspot.R


class LoadingDialog(context : Context) : Dialog(context) {
    init {
        // Set the layout for the dialog
        setContentView(R.layout.loading_dialog)

        // Set the dialog's window properties
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        setCancelable(false)
    }

    fun changeLoadingText(){
        val textView = findViewById<TextView>(R.id.tvLoading)
        val animation = ObjectAnimator.ofInt(textView, "text", R.string.loading, R.string.completed)
        animation.duration = 5000
        animation.start()
    }
}