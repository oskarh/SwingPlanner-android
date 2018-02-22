package com.oskhoj.swingplanner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.widget.LinearLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.google.gson.Gson
import com.nytimes.android.external.fs3.FileSystemPersisterFactory
import com.nytimes.android.external.store3.base.impl.BarCode
import com.nytimes.android.external.store3.base.impl.MemoryPolicy
import com.nytimes.android.external.store3.base.impl.Store
import com.nytimes.android.external.store3.base.impl.StoreBuilder
import com.nytimes.android.external.store3.middleware.GsonParserFactory
import com.oskhoj.swingplanner.AppPreferences.appStartedCount
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventsPage
import com.oskhoj.swingplanner.model.FavoritesResponse
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.model.TeachersResponse
import com.oskhoj.swingplanner.network.ApiManagerFactory
import com.oskhoj.swingplanner.network.EventApiManager
import com.oskhoj.swingplanner.network.EventSearchBarcode
import com.oskhoj.swingplanner.network.FavoritesBarcode
import com.oskhoj.swingplanner.network.SubscriptionApiManager
import com.oskhoj.swingplanner.network.TeacherApiManager
import com.oskhoj.swingplanner.network.service.EventService
import com.oskhoj.swingplanner.network.service.SubscriptionService
import com.oskhoj.swingplanner.network.service.TeacherService
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.navigation.BottomNavigationController
import com.oskhoj.swingplanner.ui.onboarding.OnboardingActivity
import com.oskhoj.swingplanner.util.USER_PROPERTY_NUMBER_CUSTOM_SUBSCRIPTIONS
import com.oskhoj.swingplanner.util.USER_PROPERTY_NUMBER_FAVORITE_EVENTS
import com.oskhoj.swingplanner.util.USER_PROPERTY_NUMBER_FAVORITE_TEACHERS
import com.oskhoj.swingplanner.util.find
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.visible
import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import okio.BufferedSource
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.startActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ToolbarProvider {

    private lateinit var router: Router

    private val toolbarLayout by lazy { find<LinearLayout>(R.id.toolbar_layout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isOnboardingNeeded()) {
            startActivity<OnboardingActivity>()
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        invalidateOptionsMenu()
        asApp().addModule(Kodein.Module(allowSilentOverride = true) {
            bind<ToolbarProvider>() with instance(this@MainActivity)
            bind<EventService>() with singleton { ApiManagerFactory.eventService }
            bind<TeacherService>() with singleton { ApiManagerFactory.teacherService }
            bind<SubscriptionService>() with singleton { ApiManagerFactory.subscriptionService }
            bind<EventApiManager>() with singleton { EventApiManager(instance()) }
            bind<TeacherApiManager>() with singleton { TeacherApiManager(instance()) }
            bind<SubscriptionApiManager>() with singleton { SubscriptionApiManager(instance()) }
            bind<Store<EventsPage, EventSearchBarcode>>() with singleton { eventSummariesStore(instance()) }
            bind<Store<FavoritesResponse, BarCode>>() with singleton { eventSummaryStore(instance()) }
            bind<Store<EventDetails, BarCode>>() with singleton { eventDetailsStore(instance()) }
            bind<Store<FavoritesResponse, FavoritesBarcode>>() with singleton { favoritesStore(instance()) }
            bind<Store<TeachersResponse, BarCode>>() with singleton { teacherStore(instance()) }
            bind<Store<TeacherEventsResponse, BarCode>>() with singleton { teacherEventsStore(instance()) }
        })

        router = Conductor.attachRouter(this, controller_container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(BottomNavigationController()))
        }
        handleNewAppVersion()
        appStartedCount = Math.max(appStartedCount + 1, appStartedCount)
    }

    private fun handleNewAppVersion() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        when {
            isNewInstall() -> handleNewInstall(versionName)
            isAppUpdate(versionName) -> {
                showUpdateInformation()
                AppPreferences.currentVersion = versionName
            }
        }
    }

    private fun handleNewInstall(versionName: String) {
        AppPreferences.currentVersion = versionName
        AnalyticsHelper.setUserProperty(USER_PROPERTY_NUMBER_FAVORITE_EVENTS, 0)
        AnalyticsHelper.setUserProperty(USER_PROPERTY_NUMBER_FAVORITE_TEACHERS, 0)
        AnalyticsHelper.setUserProperty(USER_PROPERTY_NUMBER_CUSTOM_SUBSCRIPTIONS, 0)
    }

    private fun isNewInstall() = AppPreferences.currentVersion.isBlank()

    private fun isAppUpdate(versionName: String?) = AppPreferences.currentVersion != versionName

    private fun showUpdateInformation() {
        alert(Appcompat, getString(R.string.swingplanner_updated)) {
            customView = LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.updatelog_content, null) as ChangeLogRecyclerView
            positiveButton(getString(R.string.dialog_ok)) { it.dismiss() }
        }.show()
    }

    private fun isOnboardingNeeded() = !AppPreferences.hasShownOnboarding

    // TODO: Rename this
    private fun eventSummariesStore(eventApiManager: EventApiManager): Store<EventsPage, EventSearchBarcode> {
        return StoreBuilder
                .parsedWithKey<EventSearchBarcode, BufferedSource, EventsPage>()
                .fetcher { eventApiManager.searchEvents(it.params).map { it.source() } }
                .persister(FileSystemPersisterFactory.create(cacheDir, { it.toString() }))
                .refreshOnStale()
                .parser(GsonParserFactory.createSourceParser(Gson(), EventsPage::class.java))
                .open()
    }

    // TODO: Replace this with favorites store. Rename it
    private fun eventSummaryStore(eventApiManager: EventApiManager): Store<FavoritesResponse, BarCode> {
        return StoreBuilder
                .parsedWithKey<BarCode, BufferedSource, FavoritesResponse>()
                .fetcher { eventApiManager.eventsByIds(listOf(it.key.toInt())).map { it.source() } }
                .persister(FileSystemPersisterFactory.create(cacheDir, { it.toString() }))
                .parser(GsonParserFactory.createSourceParser(Gson(), FavoritesResponse::class.java))
                .open()
    }

    private fun eventDetailsStore(eventApiManager: EventApiManager): Store<EventDetails, BarCode> {
        return StoreBuilder
                .parsedWithKey<BarCode, BufferedSource, EventDetails>()
                .fetcher { eventApiManager.eventDetailsById(it.key.toInt()).map { it.source() } }
                .persister(FileSystemPersisterFactory.create(cacheDir, { it.toString() }))
                .parser(GsonParserFactory.createSourceParser(Gson(), EventDetails::class.java))
                .open()
    }

    private fun favoritesStore(eventApiManager: EventApiManager): Store<FavoritesResponse, FavoritesBarcode> {
        return StoreBuilder
                .parsedWithKey<FavoritesBarcode, BufferedSource, FavoritesResponse>()
                .fetcher { eventApiManager.eventsByIds(it.favoritesParameters.ids).map { it.source() } }
                .persister(FileSystemPersisterFactory.create(cacheDir, { it.toString() }))
                .parser(GsonParserFactory.createSourceParser(Gson(), FavoritesResponse::class.java))
                .open()
    }

    private fun teacherStore(teacherApiManager: TeacherApiManager): Store<TeachersResponse, BarCode> {
        return StoreBuilder
                .parsedWithKey<BarCode, BufferedSource, TeachersResponse>()
                .fetcher { teacherApiManager.allTeachers().map { it.source() } }
                .persister(FileSystemPersisterFactory.create(cacheDir, { it.toString() }))
                .parser(GsonParserFactory.createSourceParser(Gson(), TeachersResponse::class.java))
                .networkBeforeStale()
                .memoryPolicy(MemoryPolicy.MemoryPolicyBuilder()
                        .setExpireAfterWrite(TimeUnit.DAYS.toSeconds(7))
                        .setExpireAfterTimeUnit(TimeUnit.SECONDS)
                        .build())
                .open()
    }

    private fun teacherEventsStore(teacherApiManager: TeacherApiManager): Store<TeacherEventsResponse, BarCode> {
        return StoreBuilder
                .parsedWithKey<BarCode, BufferedSource, TeacherEventsResponse>()
                .fetcher { teacherApiManager.eventsByTeacher(it.key.toInt()).map { it.source() } }
                .persister(FileSystemPersisterFactory.create(cacheDir, { it.toString() }))
                .parser(GsonParserFactory.createSourceParser(Gson(), TeacherEventsResponse::class.java))
                .open()
    }

    override fun updateToolbar(viewType: ViewType) =
            when (viewType) {
                ViewType.SEARCH_VIEW -> {
                    toolbarLayout.visible()
                }
                ViewType.FAVORITES_VIEW -> {
                    supportActionBar?.title = getString(R.string.favorites)
//            toolbar.title = getString(R.string.favorites)
                    toolbarLayout.gone()
                }
                ViewType.TEACHERS_VIEW -> {
//            toolbar.title = getString(R.string.teachers)
                    toolbarLayout.visible()
                }
                ViewType.SETTINGS_VIEW -> {
                    toolbarLayout.gone()
                    supportActionBar?.title = getString(R.string.settings)
//            toolbar.title = getString(R.string.settings)
                }
                ViewType.DETAILS_VIEW -> {
                    supportActionBar?.title = ""
//            toolbar.title = ""
                    toolbarLayout.gone()
                }
            }

    override fun onCreateOptionsMenu(newMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, newMenu)
        return true
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }
}