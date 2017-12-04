package com.oskhoj.swingplanner.ui.settings.notificationmanager

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.OrientationHelper
import android.view.View
import android.widget.EditText
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ViewType
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.component.NotificationSubscriptionAdapter
import com.oskhoj.swingplanner.util.SUBSCRIPTION_MIN_LENGTH
import kotlinx.android.synthetic.main.notification_manager_settings.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.customView
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

class NotificationManagerController(args: Bundle = Bundle.EMPTY) :
        ToolbarController<NotificationManagerContract.View, NotificationManagerContract.Presenter>(args), NotificationManagerContract.View {

    override val presenter: NotificationManagerContract.Presenter by instance()

    override val layoutRes = R.layout.notification_manager_settings

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<NotificationManagerContract.Presenter>() with instance(NotificationManagerPresenter())
    }

    override val viewType: ViewType = ViewType.SETTINGS_VIEW

    private val subscriptionAdapter: NotificationSubscriptionAdapter = NotificationSubscriptionAdapter(
            AppPreferences.subscriptions.iterator().asSequence().toMutableList(), {
        Timber.d("Clicked on subscription with name $it")
        removeSubscription(it)
    })

    private fun setUpRecyclerView(view: View) {
        view.notificationSubscriptionsRecyclerView.apply {
            val dividerItemDecoration = DividerItemDecoration(context, OrientationHelper.VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = subscriptionAdapter
        }
    }

    private fun addSubscription(subscription: String?) {
        subscription?.let {
            if (it.trim().length < SUBSCRIPTION_MIN_LENGTH) {
                view?.let {
                    snackbar(it, it.context.getString(R.string.subscription_validation_failed_message, SUBSCRIPTION_MIN_LENGTH))
                }
            } else {
                AppPreferences.addSubscription(it)
                subscriptionAdapter.addSubscription(it)
            }
        }
    }

    private fun removeSubscription(subscription: String) {
        AppPreferences.removeSubscription(subscription)
        subscriptionAdapter.removeSubscription(subscription)
        view?.let {
            snackbar(it, it.context.getString(R.string.deleted_message, subscription), it.context.getString(R.string.undo_action),
                    { addSubscription(subscription) })
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
                                // TODO: Fix this
                                addSubscription(subscriptionEditText?.text.toString())
                            }
                        }
                    }
                }.show()
            }
        }
    }
}
