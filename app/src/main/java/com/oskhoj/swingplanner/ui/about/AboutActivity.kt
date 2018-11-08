package com.oskhoj.swingplanner.ui.about

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ABOUT_OPEN_SOURCE_CLICK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ABOUT_RATE_CLICK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ABOUT_SHARE_ABORT
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ABOUT_SHARE_SUCCESSFUL
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_OPENED_PRIVACY_POLICY
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_INVITED_COUNT
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.ui.component.canHandleRatingIntent
import com.oskhoj.swingplanner.ui.component.redirectToPlayStore
import com.oskhoj.swingplanner.util.clipboardManager
import com.oskhoj.swingplanner.util.getCompatColor
import com.oskhoj.swingplanner.util.loadAnimation
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.startActivity

class AboutActivity : MaterialAboutActivity() {

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val textSize = resources.getInteger(R.integer.about_screen_text_size)
        val iconColor = android.R.color.black

        val appInfoBuilder = MaterialAboutCard.Builder()
        appInfoBuilder.addItem(
            MaterialAboutTitleItem.Builder()
                .text(R.string.app_name)
                .icon(R.mipmap.swingplanner_icon)
                .build()
        )

        appInfoBuilder.addItem(
            ConvenienceBuilder.createVersionActionItem(
                this,
                IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_information_outline)
                    .color(getCompatColor(iconColor))
                    .sizeDp(textSize),
                getString(R.string.version),
                true
            )
        )

        appInfoBuilder.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.changelog))
            .icon(
                IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_history)
                    .color(getCompatColor(iconColor))
                    .sizeDp(textSize)
            )
                // TODO: Fix this as dependency does not support Jetpack
//            .setOnClickAction {
//                AnalyticsHelper.logEvent(ANALYTICS_ABOUT_CHANGELOG_CLICK)
//                alert(Appcompat, getString(R.string.changelog)) {
//                    customView = LayoutInflater.from(this@AboutActivity)
//                        .inflate(R.layout.changelog_content, null) as ChangeLogRecyclerView
//                    positiveButton(getString(R.string.dialog_ok)) { it.dismiss() }
//                }.show()
//            }
            .build())

        appInfoBuilder.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.open_source_licenses))
            .icon(
                IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_book)
                    .color(getCompatColor(iconColor))
                    .sizeDp(textSize)
            )
            .setOnClickAction {
                AnalyticsHelper.logEvent(ANALYTICS_ABOUT_OPEN_SOURCE_CLICK)
                startActivity<OssLicensesMenuActivity>()
            }
            .build())

        val appActionsBuilder = MaterialAboutCard.Builder()

        appActionsBuilder.addItem(ConvenienceBuilder.createRateActionItem(
            this,
            IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_star)
                .color(getCompatColor(iconColor))
                .sizeDp(textSize),
            getString(R.string.rate_app), null
        )
            .setOnClickAction {
                AnalyticsHelper.logEvent(ANALYTICS_ABOUT_RATE_CLICK)
                if (canHandleRatingIntent(this)) {
                    redirectToPlayStore()
                }
            })

        appActionsBuilder.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.share_app))
            .icon(
                IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_share_variant)
                    .color(getCompatColor(iconColor))
                    .sizeDp(textSize)
            )
            .setOnClickAction {
                shareApp()
            }
            .subText(getString(R.string.share_app_subtext))
            .build())

        appActionsBuilder.addItem(MaterialAboutTitleItem.Builder()
            .text(R.string.privacy_policy)
            .icon(
                IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_file_document)
                    .color(getCompatColor(iconColor))
                    .sizeDp(textSize)
            )
            .setOnClickAction {
                AnalyticsHelper.logEvent(ANALYTICS_OPENED_PRIVACY_POLICY)
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        getString(R.string.privacy_policy_url).toUri()
                    )
                )
            }
            .build())

        appActionsBuilder.addItem(ConvenienceBuilder.createEmailItem(
            this,
            IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_email)
                .color(getCompatColor(iconColor))
                .sizeDp(textSize),
            getString(R.string.contact_me),
            true,
            getString(R.string.swing_planner_email),
            getString(R.string.about_swing_planner),
            getString(R.string.contact_me_description)
        )
            .setOnLongClickAction { copyEmailToClipboard() })

        runOnUiThread {
            supportActionBar?.setHomeAsUpIndicator(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_arrow_back
                )
            )
        }
        return MaterialAboutList(appInfoBuilder.build(), appActionsBuilder.build())
    }

    private fun shareApp() {
        val shareIntent = AppInviteInvitation.IntentBuilder(getString(R.string.app_invite_title))
            .setCallToActionText(getString(R.string.app_invite_call_to_action))
            .setCustomImage(Uri.parse(getString(R.string.app_invite_image_url)))
            .setMessage(getString(R.string.app_invite_message))
            .build()
        startActivityForResult(shareIntent, REQUEST_INVITE)
    }

    private fun copyEmailToClipboard() {
        clipboardManager.primaryClip = ClipData.newPlainText(
            getString(R.string.swing_planner_label),
            getString(R.string.swing_planner_email)
        )
        findViewById<CoordinatorLayout>(R.id.mal_material_about_activity_coordinator_layout)?.let {
            snackbar(it, getString(R.string.email_copied))
        }
    }

    override fun onStart() {
        super.onStart()
        findViewById<RecyclerView>(R.id.mal_recyclerview)
            .startAnimation(loadAnimation(R.anim.about_activity_enter_animation))
    }

    override fun onResume() {
        super.onResume()
        AnalyticsHelper.setCurrentScreen(this, ScreenType.ABOUT)
    }

    override fun getActivityTitle(): String = getString(R.string.about)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val ids = AppInviteInvitation.getInvitationIds(resultCode, data)
                AnalyticsHelper.logEvent(
                    ANALYTICS_ABOUT_SHARE_SUCCESSFUL,
                    PROPERTY_INVITED_COUNT to ids.size
                )
            } else {
                AnalyticsHelper.logEvent(ANALYTICS_ABOUT_SHARE_ABORT)
            }
        }
    }

    companion object {
        const val REQUEST_INVITE = 1001
    }
}