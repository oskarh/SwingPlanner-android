package com.oskhoj.swingplanner.ui.settings.notificationmanager

import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object SubscriptionManagerContract {

    interface View : BaseView {
        fun subscriptionAdded(query: String)

        fun subscriptionRemoved(query: String)
    }

    interface Presenter : Attachable<View> {
        fun addSubscription(query: String)

        fun removeSubscription(query: String)

        fun aboutAction()
    }
}