package com.oskhoj.swingplanner.ui.navigation

import com.oskhoj.swingplanner.ui.base.BasePresenter

class NavigationPresenter : BasePresenter<NavigationContract.View>(), NavigationContract.Presenter {

    override fun selectItem(id: Int) {
        view?.onItemSelected(id)
    }
}