package com.oskhoj.swingplanner.ui.component

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
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
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_LANGUAGE_CHANGE
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_NEW_LANGUAGE
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_OLD_LANGUAGE
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.Language
import com.oskhoj.swingplanner.util.getCompatColor
import com.oskhoj.swingplanner.util.loadFlagIconOrDisappear
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.textColor

object BottomSheetDialogHelper {

    private const val LARGE_MARGIN = 16
    private const val CARD_MARGIN = 8

    fun showDanceFilterDialog(context: Context, listener: DialogInterface.() -> Unit) {
        val rootLayout = LinearLayout(context).apply {
            padding = dip(LARGE_MARGIN)
            orientation = VERTICAL
            layoutParams = linearLayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        val titleText = TextView(context).apply {
            text = context.getString(R.string.filter_options_description)
            layoutParams = linearLayoutParams(bottomMargin = dip(LARGE_MARGIN))
        }
        rootLayout.addView(titleText)

        val rowLayoutParams = linearLayoutParams(width = MATCH_PARENT, gravity = Gravity.CENTER_VERTICAL or Gravity.START)

        DanceStyle.values().forEach { danceStyle ->
            val danceStyleCheckBox = CheckBox(context).apply {
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
                setBackgroundResource(getRippleBackground(context))
                addView(danceStyleCheckBox)
                addView(languageText)
                onClick { danceStyleCheckBox.performClick() }
            }
            rootLayout.addView(rowLayout)
        }

        BottomSheetDialog(context).run {
            val okButton = Button(context).apply {
                layoutParams = linearLayoutParams(gravity = Gravity.END)
                text = context.getString(R.string.dialog_ok)
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
                    loadFlagIconOrDisappear(language.isoCodeFlag, context)
                }
                val languageText = TextView(context).apply {
                    layoutParams = viewParams
                    text = language.nativeName
                }
                val rowLayout = LinearLayout(context).apply {
                    setBackgroundResource(getRippleBackground(context))
                    if (AppPreferences.selectedLanguage == language.name || AppPreferences.selectedLanguage.isBlank() && language == Language.defaultLanguage) {
                        backgroundColor = Color.LTGRAY
                    }
                    addView(languageFlag)
                    addView(languageText)
                    onClick {
                        AnalyticsHelper.logEvent(ANALYTICS_LANGUAGE_CHANGE, PROPERTY_OLD_LANGUAGE to AppPreferences.selectedLanguage,
                                PROPERTY_NEW_LANGUAGE to language.name)
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