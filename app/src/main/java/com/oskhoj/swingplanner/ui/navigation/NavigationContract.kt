package com.oskhoj.swingplanner.ui.navigation

import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object NavigationContract {
    interface View : BaseView {
        fun onItemSelected(id: Int)
    }

    interface Presenter : Attachable<View> {
        fun selectItem(id: Int)
    }
}