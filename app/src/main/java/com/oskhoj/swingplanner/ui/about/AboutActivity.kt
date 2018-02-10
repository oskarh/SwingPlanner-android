package com.oskhoj.swingplanner.ui.about

import android.content.ClipData
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.util.clipboardManager
import com.oskhoj.swingplanner.util.getCompatColor
import com.oskhoj.swingplanner.util.loadAnimation
import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.startActivity
import timber.log.Timber

class AboutActivity : MaterialAboutActivity() {

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val textSize = resources.getInteger(R.integer.about_screen_text_size)
        val iconColor = R.color.black

        val appInfoBuilder = MaterialAboutCard.Builder()
        appInfoBuilder.addItem(MaterialAboutTitleItem.Builder()
                .text(R.string.app_name)
                .icon(R.mipmap.ic_launcher)
                .build())

        appInfoBuilder.addItem(ConvenienceBuilder.createVersionActionItem(this,
                IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_information_outline)
                        .color(getCompatColor(iconColor))
                        .sizeDp(textSize),
                getString(R.string.version),
                false))

        appInfoBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.changelog))
                .icon(IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_history)
                        .color(getCompatColor(iconColor))
                        .sizeDp(textSize))
                .setOnClickAction {
                    alert(Appcompat, getString(R.string.changelog)) {
                        customView = LayoutInflater.from(this@AboutActivity)
                                .inflate(R.layout.changelog_content, null) as ChangeLogRecyclerView
                        positiveButton(getString(R.string.dialog_ok)) { it.dismiss() }
                    }.show()
                }
                .build())

        appInfoBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.open_source_licenses))
                .icon(IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(getCompatColor(iconColor))
                        .sizeDp(textSize))
                .setOnClickAction {
                    startActivity<OssLicensesMenuActivity>()
                }
                .build())

        val appActionsBuilder = MaterialAboutCard.Builder()

        appActionsBuilder.addItem(ConvenienceBuilder.createRateActionItem(this,
                IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_star)
                        .color(getCompatColor(iconColor))
                        .sizeDp(textSize),
                getString(R.string.rate_app),
                null))

        appActionsBuilder.addItem(MaterialAboutActionItem.Builder()
                .text(getString(R.string.share_app))
                .icon(IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_share_variant)
                        .color(getCompatColor(iconColor))
                        .sizeDp(textSize))
                .setOnClickAction { Timber.d("Sharing app...") }
                .build())

        appActionsBuilder.addItem(ConvenienceBuilder.createEmailItem(this,
                IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_email)
                        .color(getCompatColor(iconColor))
                        .sizeDp(textSize),
                getString(R.string.contact_me),
                true,
                getString(R.string.contact_me_description),
                getString(R.string.about_swing_planner))
                .setOnLongClickAction { copyEmailToClipboard() })

        val toolbar = findViewById<Toolbar>(com.danielstone.materialaboutlibrary.R.id.mal_toolbar)
//        toolbar.setTitleTextColor(getCompatColor(R.color.white))
        supportActionBar?.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back))
        return MaterialAboutList(appInfoBuilder.build(), appActionsBuilder.build())
    }

    private fun copyEmailToClipboard() {
        clipboardManager.primaryClip = ClipData.newPlainText(getString(R.string.swing_planner_label), getString(R.string.swing_planner_email))
        findViewById<CoordinatorLayout>(R.id.mal_material_about_activity_coordinator_layout)?.let {
            snackbar(it, getString(R.string.email_copied))
        }
    }

    override fun onStart() {
        super.onStart()
        findViewById<RecyclerView>(R.id.mal_recyclerview).startAnimation(loadAnimation(R.anim.about_activity_enter_animation))
    }

    override fun onResume() {
        super.onResume()
        AnalyticsHelper.setCurrentScreen(this, ScreenType.ABOUT)
    }

    override fun getActivityTitle(): String = getString(R.string.about)
}