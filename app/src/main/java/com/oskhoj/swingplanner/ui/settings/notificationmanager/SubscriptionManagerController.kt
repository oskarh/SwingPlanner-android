package com.oskhoj.swingplanner.ui.settings.notificationmanager

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_SUBSCRIPTIONS_ADD
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_SUBSCRIPTIONS_REMOVE
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_SUBSCRIPTION
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.component.NotificationSubscriptionAdapter
import com.oskhoj.swingplanner.util.SUBSCRIPTION_MIN_LENGTH
import com.oskhoj.swingplanner.util.showTapTarget
import kotlinx.android.synthetic.main.notification_manager_settings.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.customView
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.horizontalMargin
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk21.listeners.onClick
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import timber.log.Timber

class SubscriptionManagerController(args: Bundle = Bundle.EMPTY) :
        ToolbarController<SubscriptionManagerContract.View, SubscriptionManagerContract.Presenter>(args), SubscriptionManagerContract.View {

    override val presenter: SubscriptionManagerContract.Presenter by instance()

    override val layoutRes = R.layout.notification_manager_settings

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<SubscriptionManagerContract.Presenter>() with provider { SubscriptionManagerPresenter(instance()) }
    }

    override val viewType: ViewType = ViewType.SETTINGS_VIEW

    override val screenType: ScreenType = ScreenType.MANAGE_SUBSCRIPTIONS

    private val subscriptionAdapter: NotificationSubscriptionAdapter = NotificationSubscriptionAdapter(
            AppPreferences.subscriptions.iterator().asSequence().toMutableList(), {
        Timber.d("Clicked on subscription with name $it")
        presenter.removeSubscription(it)
    })

    private fun setUpRecyclerView(view: View) {
        view.notificationSubscriptionsRecyclerView.apply {
            val dividerItemDecoration = DividerItemDecoration(
                context,
                OrientationHelper.VERTICAL
            )
            addItemDecoration(dividerItemDecoration)
            adapter = subscriptionAdapter
        }
    }

    override fun subscriptionAdded(query: String) {
        if (query.trim().length < SUBSCRIPTION_MIN_LENGTH) {
            view?.let {
                longSnackbar(it, it.context.getString(R.string.subscription_validation_failed_message, SUBSCRIPTION_MIN_LENGTH))
            }
        } else {
            AnalyticsHelper.logEvent(ANALYTICS_SUBSCRIPTIONS_ADD, PROPERTY_SUBSCRIPTION to query)
            subscriptionAdapter.addSubscription(query)
        }
    }

    override fun subscriptionRemoved(query: String) {
        AnalyticsHelper.logEvent(ANALYTICS_SUBSCRIPTIONS_REMOVE, PROPERTY_SUBSCRIPTION to query)
        subscriptionAdapter.removeSubscription(query)
        view?.let {
            longSnackbar(it, it.context.getString(R.string.deleted_message, query),
                    it.context.getString(R.string.undo_action), { presenter.addSubscription(query) })
        }
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setUpRecyclerView(view)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        activity?.run {
            view.notificationsFab.onClick {
                alert {
                    var subscriptionEditText: EditText? = null
                    title = getString(R.string.subscribe_query_title)
                    customView {
                        verticalLayout {
                            linearLayout {
                                subscriptionEditText = editText {
                                    hint = context.getString(R.string.subscribe_query_hint)
                                }.lparams(matchParent, wrapContent) {
                                    horizontalMargin = dip(16)
                                    topMargin = dip(8)
                                }
                            }
                            cancelButton { }
                            okButton {
                                val subscription = subscriptionEditText?.text.toString()
                                if (!AppPreferences.hasSubscription(subscription)) {
                                    presenter.addSubscription(subscription)
                                } else {
                                    snackbar(view, context.getString(R.string.subscription_already_present, subscription))
                                }
                            }
                        }
                    }
                }.show()
            }
            if (!AppPreferences.hasShownAddSubscriptionTapTarget) {
                activity?.run {
                    showTapTarget(R.id.notificationsFab, R.string.tap_target_add_subscription_title, R.string.tap_target_add_subscription_message)
                    AppPreferences.hasShownAddSubscriptionTapTarget = true
                }
            }
        }
    }
}
