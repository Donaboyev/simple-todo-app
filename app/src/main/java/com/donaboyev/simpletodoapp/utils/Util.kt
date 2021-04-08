package com.donaboyev.simpletodoapp.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Spinner
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.donaboyev.simpletodoapp.R
import com.donaboyev.simpletodoapp.data.models.Priority

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun parsePriorityColor(cardView: CardView, priority: Priority) {
    when (priority) {
        Priority.HIGH -> cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                cardView.context,
                R.color.high
            )
        )
        Priority.MEDIUM -> cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                cardView.context,
                R.color.medium
            )
        )
        Priority.LOW -> cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                cardView.context,
                R.color.low
            )
        )

    }
}

fun parsePriorityToInt(view: Spinner, priority: Priority) {
    when (priority) {
        Priority.HIGH -> view.setSelection(0)
        Priority.MEDIUM -> view.setSelection(1)
        Priority.LOW -> view.setSelection(2)
    }
}