package com.teamisland.zzazz.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.text.InputType
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Customize keyboard
 */
class CustomKeyboard(
    private val activity: Activity,
    viewId: Int,
    layoutId: Int
) {

    init {
        val keyboardView = activity.findViewById<KeyboardView>(viewId)
        keyboardView.keyboard = Keyboard(activity, layoutId)
        keyboardView.setOnKeyboardActionListener(CustomOnKeyboardActionListener(activity))
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private class CustomOnKeyboardActionListener(private val activity: Activity) :
        KeyboardView.OnKeyboardActionListener {

        private val DONE = -4

        @SuppressLint("WrongConstant")
        override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
            if (activity.window.currentFocus == null)
                return
            val editText = activity.window.currentFocus as EditText
            val editable = editText.text
            val start = editText.selectionStart

            when (primaryCode) {
                Keyboard.KEYCODE_DELETE -> {
                    if (editable != null && start > 0) editable.delete(start - 1, start)
                }
                DONE -> {
                    Intent().apply {
                        putExtra(SaveProjectActivity.PROJECT_NAME, editable.toString())
                        activity.setResult(Activity.RESULT_OK, this)
                    }
                    activity.finish()
                }
                else -> {
                    editable.insert(start, primaryCode.toChar().toString())
                }
            }
        }

        override fun swipeRight() {
        }

        override fun onPress(primaryCode: Int) {
        }

        override fun onRelease(primaryCode: Int) {
        }

        override fun swipeLeft() {
        }

        override fun swipeUp() {
        }

        override fun swipeDown() {
        }

        override fun onText(text: CharSequence?) {
        }
    }

    /**
     * Add EditText.
     *
     * We can use multiple EditText
     */
    @SuppressLint("ClickableViewAccessibility")
    fun registerEditText(resId: Int) {
        val editText = activity.findViewById<EditText>(resId)

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && v != null) {
                val imm =
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
        editText.setOnClickListener {
            val imm =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
        editText.setOnTouchListener { v, event ->
            val text = v as EditText
            val inType = text.inputType
            text.inputType = InputType.TYPE_NULL
            text.onTouchEvent(event)
            text.inputType = inType
            true
        }

        editText.inputType = editText.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
    }
}