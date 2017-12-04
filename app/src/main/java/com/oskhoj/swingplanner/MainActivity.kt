package com.oskhoj.swingplanner

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.widget.LinearLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.oskhoj.swingplanner.network.ApiManagerFactory
import com.oskhoj.swingplanner.network.EventApiManager
import com.oskhoj.swingplanner.network.TeacherApiManager
import com.oskhoj.swingplanner.network.service.EventService
import com.oskhoj.swingplanner.network.service.TeacherService
import com.oskhoj.swingplanner.ui.navigation.BottomNavigationController
import com.oskhoj.swingplanner.util.find
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.visible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ToolbarProvider {

    private lateinit var router: Router

    private val toolbarLayout by lazy { find<LinearLayout>(R.id.toolbar_layout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        invalidateOptionsMenu()
        asApp().addModule(Kodein.Module(allowSilentOverride = true) {
            bind<ToolbarProvider>() with instance(this@MainActivity)
            bind<EventApiManager>() with singleton { EventApiManager(instance()) }
            bind<EventService>() with singleton { ApiManagerFactory.eventService }
            bind<TeacherApiManager>() with singleton { TeacherApiManager(instance()) }
            bind<TeacherService>() with singleton { ApiManagerFactory.teacherService }
        })

        router = Conductor.attachRouter(this, controller_container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(BottomNavigationController()))
        }
    }

    override fun updateToolbar(viewType: ViewType) = when (viewType) {
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