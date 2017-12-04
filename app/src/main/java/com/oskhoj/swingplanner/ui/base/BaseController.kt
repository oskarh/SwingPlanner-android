package com.oskhoj.swingplanner.ui.base

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.github.salomonbrys.kodein.Kodein.Module
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.oskhoj.swingplanner.asApp
import timber.log.Timber

abstract class BaseController<in V : BaseView, out T : Attachable<V>> protected constructor(args: Bundle = Bundle.EMPTY) : Controller(args), BaseView, KodeinInjected {

    protected abstract val presenter: T

    abstract val controllerModule: Module

    @get:LayoutRes
    protected abstract val layoutRes: Int

    override val injector = KodeinInjector()

    private fun inflateView(inflater: LayoutInflater, container: ViewGroup): View =
            inflater.inflate(layoutRes, container, false)

    @CallSuper
    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.onAttach(this as V)
        updateHomeArrow()
    }

    @CallSuper
    override fun onDetach(view: View) {
        super.onDetach(view)
        Timber.d("onDetach...")
        presenter.onDetach()
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflateView(inflater, container)
        onViewBound(view)
        view.context.asApp().addModule(controllerModule)
        injector.inject(view.appKodein())
        return view
    }

    private fun updateHomeArrow() {
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            setDisplayHomeAsUpEnabled(router.backstackSize > 1)
            setDisplayShowHomeEnabled(router.backstackSize > 1)
        }
    }

    protected open fun onViewBound(view: View) {}
}
