package com.oskhoj.swingplanner

import android.app.Application
import android.content.Context
import com.chibatching.kotpref.Kotpref
import com.github.salomonbrys.kodein.Kodein.Module
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.conf.ConfigurableKodein
import com.github.salomonbrys.kodein.instance
import timber.log.Timber

class SwingPlannerApplication : Application(), KodeinAware {

    override val kodein = ConfigurableKodein(mutable = true)
    var overrideModule: Module? = null

    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        resetInjection()
    }

    // TODO: Do I need to clear kodein?
    private fun resetInjection() {
        kodein.clear()
        kodein.addImport(appDependencies(), true)
    }

    private fun appDependencies(): Module {
        return Module(allowSilentOverride = true) {
            bind<String>(tag = "tag") with instance("SwingPlannerApplication")
        }
    }

    fun addModule(activityModules: Module) {
        kodein.addImport(activityModules, true)
        overrideModule?.let {
            kodein.addImport(it, true)
        }
    }
}

fun Context.asApp() = this.applicationContext as SwingPlannerApplication