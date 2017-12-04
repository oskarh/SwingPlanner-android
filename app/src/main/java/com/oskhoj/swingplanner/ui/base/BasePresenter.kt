package com.oskhoj.swingplanner.ui.base

import android.support.annotation.CallSuper
import timber.log.Timber

open class BasePresenter<V : BaseView> : Attachable<V> {

    protected var view: V? = null
        private set

    @CallSuper
    override fun onAttach(attachedView: V) {
        Timber.d("Attaching...")
        view = attachedView
    }

    @CallSuper
    override fun onDetach() {
        Timber.d("Detaching...")
        view = null
    }
}