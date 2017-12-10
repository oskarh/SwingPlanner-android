package com.oskhoj.swingplanner.ui.component

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.AppCompatImageView
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.Language
import com.oskhoj.swingplanner.util.getCompatColor
import com.oskhoj.swingplanner.util.loadFlagIconOrDisappear
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.textColor

class BottomSheetDialogHelper private constructor() {

    companion object {
        private val largeMargin = 16
        private val cardMargin = 8

        fun showFilterDialog(context: Context?, listener: DialogInterface.() -> Unit) {
            context?.run {
                val filterDialog = BottomSheetDialog(context)
                val rootLayout = LinearLayout(context).apply {
                    padding = dip(largeMargin)
                    orientation = VERTICAL
                    layoutParams = linearLayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
                val rowLayoutParams = linearLayoutParams(width = MATCH_PARENT, gravity = Gravity.CENTER_VERTICAL or Gravity.START)
                val viewParams = linearLayoutParams()

                val titleText = TextView(context).apply {
                    text = getString(R.string.filter_options_description)
                    layoutParams = linearLayoutParams(bottomMargin = dip(largeMargin))
                }
                rootLayout.addView(titleText)

                val rippleBackground = TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackground, rippleBackground, true)

                DanceStyle.values().forEach { danceStyle ->
                    val languageCheckBox = CheckBox(context).apply {
                        layoutParams = viewParams
                        isChecked = AppPreferences.hasFavoriteDanceStyle(danceStyle)
                        onClick { AppPreferences.toggleFilterOption(danceStyle) }
                    }
                    val languageText = TextView(context).apply {
                        layoutParams = viewParams
                        text = danceStyle.description
                    }
                    val rowLayout = LinearLayout(context).apply {
                        layoutParams = rowLayoutParams
                        setBackgroundResource(rippleBackground.resourceId)
                        addView(languageCheckBox)
                        addView(languageText)
                        onClick { languageCheckBox.performClick() }
                    }
                    rootLayout.addView(rowLayout)
                }

                val buttonBackground = TypedValue()
                theme.resolveAttribute(R.attr.selectableItemBackground, buttonBackground, true)
                val okButton = Button(context).apply {
                    layoutParams = linearLayoutParams(gravity = Gravity.END)
                    text = getString(R.string.dialog_ok)
                    setBackgroundResource(buttonBackground.resourceId)
                    textColor = getCompatColor(R.color.colorAccent)
                    onClick { filterDialog.dismiss() }
                }
                rootLayout.addView(okButton)

                filterDialog.run {
                    setContentView(rootLayout)
                    setOnDismissListener(listener)
                    show()
                }
            }
        }

        fun showLanguageDialog(context: Context) {
            val largeMargins = context.dip(largeMargin)
            val smallMargins = context.dip(cardMargin)

            val languagesDialog = BottomSheetDialog(context)
            val rootLayout = LinearLayout(context).apply {
                orientation = VERTICAL
                layoutParams = linearLayoutParams(width = MATCH_PARENT, height = WRAP_CONTENT)
            }
            val viewParams = linearLayoutParams(width = WRAP_CONTENT, height = MATCH_PARENT, startMargin = largeMargins,
                    endMargin = smallMargins, topMargin = smallMargins, bottomMargin = smallMargins)

            val rippleBackground = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, rippleBackground, true)

            Language.values().forEach { language ->
                val languageFlag = AppCompatImageView(context).apply {
                    layoutParams = viewParams
                    loadFlagIconOrDisappear(language.isoCode, context)
                }
                val languageText = TextView(context).apply {
                    layoutParams = viewParams
                    text = language.nativeName
                }
                val rowLayout = LinearLayout(context).apply {
                    setBackgroundResource(rippleBackground.resourceId)
                    addView(languageFlag)
                    addView(languageText)
                    onClick {
                        AppPreferences.selectedLanguage = language.name
                        it?.postDelayed({ languagesDialog.dismiss() }, 150)
                    }
                }
                rootLayout.addView(rowLayout)
            }

            languagesDialog.setContentView(rootLayout)
            languagesDialog.show()
        }

        private fun linearLayoutParams(width: Int = WRAP_CONTENT, height: Int = WRAP_CONTENT,
                                       gravity: Int = Gravity.TOP or Gravity.START, startMargin: Int = 0,
                                       endMargin: Int = 0, topMargin: Int = 0, bottomMargin: Int = 0) =
                LayoutParams(width, height).apply {
                    this.gravity = gravity
                    this.marginStart = startMargin
                    this.marginEnd = endMargin
                    this.topMargin = topMargin
                    this.bottomMargin = bottomMargin
                }
    }
}