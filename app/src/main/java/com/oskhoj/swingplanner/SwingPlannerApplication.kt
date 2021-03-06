package com.oskhoj.swingplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.chibatching.kotpref.Kotpref
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.Kodein.Module
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.conf.ConfigurableKodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.notifications.NotificationType
import io.fabric.sdk.android.Fabric
import org.jetbrains.anko.notificationManager
import saschpe.android.customtabs.CustomTabsActivityLifecycleCallbacks
import timber.log.Timber
import java.io.File

class SwingPlannerApplication : Application(), KodeinAware {

    override val kodein = ConfigurableKodein(mutable = true)
    var overrideModule: Module? = null

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())
        Kotpref.init(this)
        setupNotificationChannels()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        AnalyticsHelper.setupFirebaseAnalytics(this)
        resetInjection()
        registerActivityLifecycleCallbacks(CustomTabsActivityLifecycleCallbacks())
        setupRemoteConfig()
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationType.values().forEach {
                notificationManager.createNotificationChannel(
                        NotificationChannel(it.channelName, getString(it.stringRes), NotificationManager.IMPORTANCE_DEFAULT))
            }
        }
    }

    private fun resetInjection() {
        kodein.clear()
        kodein.addImport(appDependencies(), true)
    }

    private fun appDependencies(): Module {
        return Module(allowSilentOverride = true) {
            bind<Context>() with singleton { this@SwingPlannerApplication }
            bind<File>(tag = "cache") with singleton { instance<Context>().cacheDir }
        }
    }

    fun addModule(activityModules: Module) {
        kodein.addImport(activityModules, true)
        overrideModule?.let {
            kodein.addImport(it, true)
        }
    }

    private fun setupRemoteConfig() {
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettings(FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build())
        }.setDefaults(R.xml.remote_config_defaults)
    }
}

fun Context.asApp() = this.applicationContext as SwingPlannerApplication