package com.oskhoj.swingplanner.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.MainActivity
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ONBOARDING_FINISH
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ONBOARDING_SKIP
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ONBOARDING_START
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.util.getCompatColor
import org.jetbrains.anko.startActivity

class OnboardingActivity : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsHelper.logEvent(ANALYTICS_ONBOARDING_START)
        setFlowAnimation()
        arrayOf(createPage(getString(R.string.onboarding_title_1), getString(R.string.onboarding_message_1), R.drawable.globe, Color.DKGRAY),
                createPage(getString(R.string.onboarding_title_2), getString(R.string.onboarding_message_2), R.drawable.annie_happy, getCompatColor(R.color.blue_grey_300)),
                createPage(getString(R.string.onboarding_title_3), getString(R.string.onboarding_message_3), R.drawable.annie_dancing, getCompatColor(R.color.light_blue_300)),
                createPage(getString(R.string.onboarding_title_4), getString(R.string.onboarding_message_4), R.drawable.dancing_couple, getCompatColor(R.color.brown_300)))
                .forEach { addSlide(AppIntroFragment.newInstance(it)) }
    }

    private fun createPage(title: String, description: String, @DrawableRes imageDrawable: Int, @ColorInt bgColor: Int) =
            SliderPage().apply {
                this.title = title
                this.description = description
                this.imageDrawable = imageDrawable
                this.bgColor = bgColor
            }

    override fun onResume() {
        super.onResume()
        AnalyticsHelper.setCurrentScreen(this, ScreenType.ONBOARDING)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        AnalyticsHelper.logEvent(ANALYTICS_ONBOARDING_SKIP)
        AppPreferences.hasShownOnboarding = true
        startActivity<MainActivity>()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        AnalyticsHelper.logEvent(ANALYTICS_ONBOARDING_FINISH)
        AppPreferences.hasShownOnboarding = true
        startActivity<MainActivity>()
    }
}