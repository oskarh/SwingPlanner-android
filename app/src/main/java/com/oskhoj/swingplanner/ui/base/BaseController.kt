package com.oskhoj.swingplanner.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Controller
import com.github.salomonbrys.kodein.Kodein.Module
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.oskhoj.swingplanner.asApp
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import timber.log.Timber

abstract class BaseController<in V : BaseView, out T : Attachable<V>> protected constructor(args: Bundle = Bundle.EMPTY) : Controller(args), BaseView, KodeinInjected {

    protected abstract val presenter: T

    abstract val controllerModule: Module

    @get:LayoutRes
    protected abstract val layoutRes: Int

    protected open val screenType: ScreenType? = null

    override val injector = KodeinInjector()

    private fun inflateView(inflater: LayoutInflater, container: ViewGroup): View =
            inflater.inflate(layoutRes, container, false)

    @CallSuper
    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.onAttach(this as V)
        updateHomeArrow()
        activity?.let { activity ->
            screenType?.let { screenType ->
                Timber.d("Setting screen type ${screenType.screenName}")
                AnalyticsHelper.setCurrentScreen(activity, screenType)
            }
        }
    }

    @CallSuper
    override fun onDetach(view: View) {
        super.onDetach(view)
        Timber.d("onDetach...")
        presenter.onDetach()
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup) =
            inflateView(inflater, container).apply {
                onViewBound(this)
                context.asApp().addModule(controllerModule)
                injector.inject(appKodein())
            }

    private fun updateHomeArrow() {
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            setDisplayHomeAsUpEnabled(router.backstackSize > 1)
            setDisplayShowHomeEnabled(router.backstackSize > 1)
        }
    }

    protected open fun onViewBound(view: View) {}
}
