package com.oskhoj.swingplanner.ui.settings.notificationmanager

import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object NotificationManagerContract {

    interface View : BaseView {

    }

    interface Presenter : Attachable<View> {
        fun addSubscription(query: String)

        fun removeSubscription(query: String)

        fun aboutAction()
    }
}