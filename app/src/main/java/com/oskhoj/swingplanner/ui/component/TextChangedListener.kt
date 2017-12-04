package com.oskhoj.swingplanner.ui.component

import android.text.Editable
import android.text.TextWatcher

class TextChangedListener(private val listener: (CharSequence) -> Unit) : TextWatcher {
    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        listener.invoke(charSequence)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable) {}
}