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

object BottomSheetDialogHelper {

    private const val LARGE_MARGIN = 16
    private const val CARD_MARGIN = 8

    fun showFilterDialog(context: Context?, listener: DialogInterface.() -> Unit) {
        context?.run {
            val rootLayout = LinearLayout(context).apply {
                padding = dip(LARGE_MARGIN)
                orientation = VERTICAL
                layoutParams = linearLayoutParams(MATCH_PARENT, MATCH_PARENT)
            }

            val titleText = TextView(context).apply {
                text = getString(R.string.filter_options_description)
                layoutParams = linearLayoutParams(bottomMargin = dip(LARGE_MARGIN))
            }
            rootLayout.addView(titleText)

            val rowLayoutParams = linearLayoutParams(width = MATCH_PARENT, gravity = Gravity.CENTER_VERTICAL or Gravity.START)

            DanceStyle.values().forEach { danceStyle ->
                val languageCheckBox = CheckBox(context).apply {
                    layoutParams = linearLayoutParams()
                    isChecked = AppPreferences.hasFavoriteDanceStyle(danceStyle)
                    onClick { AppPreferences.toggleFilterOption(danceStyle) }
                }
                val languageText = TextView(context).apply {
                    layoutParams = linearLayoutParams()
                    text = danceStyle.description
                }
                val rowLayout = LinearLayout(context).apply {
                    layoutParams = rowLayoutParams
                    setBackgroundResource(getRippleBackground(this@run))
                    addView(languageCheckBox)
                    addView(languageText)
                    onClick { languageCheckBox.performClick() }
                }
                rootLayout.addView(rowLayout)
            }

            BottomSheetDialog(context).run {
                val okButton = Button(context).apply {
                    layoutParams = linearLayoutParams(gravity = Gravity.END)
                    text = getString(R.string.dialog_ok)
                    setBackgroundResource(getRippleBackground(context))
                    textColor = getCompatColor(R.color.colorAccent)
                    onClick { dismiss() }
                }
                rootLayout.addView(okButton)

                setContentView(rootLayout)
                setOnDismissListener(listener)
                show()
            }
        }
    }

    fun showLanguageDialog(context: Context) {
        val largeMargins = context.dip(LARGE_MARGIN)
        val smallMargins = context.dip(CARD_MARGIN)

        val rootLayout = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = linearLayoutParams(width = MATCH_PARENT, height = WRAP_CONTENT)
        }
        val viewParams = linearLayoutParams(width = WRAP_CONTENT, height = MATCH_PARENT, startMargin = largeMargins,
                endMargin = smallMargins, topMargin = smallMargins, bottomMargin = smallMargins)

        BottomSheetDialog(context).run {
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
                    setBackgroundResource(getRippleBackground(context))
                    addView(languageFlag)
                    addView(languageText)
                    onClick {
                        AppPreferences.selectedLanguage = language.name
                        it?.postDelayed({ dismiss() }, 150)
                    }
                }
                rootLayout.addView(rowLayout)
            }
            setContentView(rootLayout)
            show()
        }
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

    private fun getRippleBackground(context: Context) =
            TypedValue().also {
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, it, true)
            }.resourceId
}