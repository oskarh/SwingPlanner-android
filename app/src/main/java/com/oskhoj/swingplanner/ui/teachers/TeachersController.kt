package com.oskhoj.swingplanner.ui.teachers

import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.OrientationHelper.VERTICAL
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ViewType
import com.oskhoj.swingplanner.ViewType.TEACHERS_VIEW
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.component.TeacherAdapter
import com.oskhoj.swingplanner.ui.component.TextChangedListener
import com.oskhoj.swingplanner.util.KEY_STATE_SEARCH_TEXT
import com.oskhoj.swingplanner.util.ViewHolderList
import com.oskhoj.swingplanner.util.closeKeyboard
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.invisible
import com.oskhoj.swingplanner.util.loadLayoutAnimation
import com.oskhoj.swingplanner.util.visible
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_teachers.view.*
import org.jetbrains.anko.sdk21.listeners.onClick
import timber.log.Timber

class TeachersController(args: Bundle = Bundle.EMPTY) : ToolbarController<TeachersContract.View, TeachersContract.Presenter>(args), TeachersContract.View, ViewHolderList {
    override val presenter: TeachersContract.Presenter by instance()

    override val layoutRes = R.layout.controller_teachers

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<TeachersContract.Presenter>() with provider { TeachersPresenter(instance(), instance(), instance()) }
    }

    override val viewType: ViewType = TEACHERS_VIEW

    private lateinit var teacherRecyclerView: RecyclerView
    private var searchText: EditText? = null
    private lateinit var backIcon: AppCompatImageView
    private lateinit var clearIcon: AppCompatImageView

    private var storedText: String = ""

    private val textListener = TextChangedListener {
        clearIcon.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
        presenter.loadTeachers(it.toString())
    }

    private val teacherAdapter: TeacherAdapter = TeacherAdapter(emptyList(), this, {
        Timber.d("Clicked on teacher with id ${it.id}")
        presenter.openTeacherDetails(it)
    })

    override fun displayTeachers(teachers: List<Teacher>) {
        teacherAdapter.loadTeachers(teachers)
    }

    override fun displayTeacherEvents(teacherEventsResponse: TeacherEventsResponse) {
        Timber.d("Showing teacher events $teacherEventsResponse")
    }

    override fun displayEmptyView() {
        Timber.d("Displaying empty view...")
    }

    override fun displayErrorView() {
        Timber.d("Displaying error view...")
    }

    override fun onFavoriteClicked(isFavorite: Boolean) {
        Timber.d("Adding teacher to favorites...")
    }

    override fun openEventDetails(eventSummary: EventSummary, eventDetails: EventDetails) {
        Timber.d("Opening event details...")
    }

    override fun showLoading() {
        view?.teachers_progressbar?.visible()
    }

    override fun hideLoading() {
        view?.teachers_progressbar?.gone()
    }

    private fun setUpRecyclerView(view: View) {
        teacherRecyclerView = view.teachersRecyclerView.apply {
            layoutAnimation = view.loadLayoutAnimation(R.anim.layout_recycler_animation_new_dataset)
            val dividerItemDecoration = DividerItemDecoration(context, VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = teacherAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy != 0) {
                        activity?.closeKeyboard()
                    }
                }
            })
        }
    }

    override fun abortSearch() {
        clearText()
        searchText?.clearFocus()
        activity?.closeKeyboard()
        backIcon.invisible()
    }

    override fun clearText() {
        searchText?.text?.clear()
        clearIcon.invisible()
    }

    override fun findViewHolderForPosition(position: Int) =
            teacherRecyclerView.findViewHolderForAdapterPosition(position) as? TeacherAdapter.ViewHolder

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setUpRecyclerView(view)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.loadTeachers(storedText)
        activity?.run {
            backIcon = search_back
            backIcon.onClick { presenter.onSearchBack() }
            clearIcon = search_clear
            clearIcon.setOnClickListener { presenter.onSearchClear() }

            searchText = search_text?.apply {
                setText(storedText)
                setSelection(storedText.length)
                addTextChangedListener(textListener)
                backIcon.visibility = if (isFocused) View.VISIBLE else View.INVISIBLE
                clearIcon.visibility = if (isFocused && text.isNotBlank()) View.VISIBLE else View.INVISIBLE
                setOnFocusChangeListener { _, hasFocus ->
                    backIcon.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                    clearIcon.visibility = if (hasFocus && text.isNotBlank()) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        backIcon.setOnClickListener(null)
        clearIcon.setOnClickListener(null)
        searchText?.removeTextChangedListener(textListener)
        searchText?.onFocusChangeListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("Saving instance state... ${(searchText?.text ?: "")}")
        outState.putString(KEY_STATE_SEARCH_TEXT, (searchText?.text ?: "").toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        storedText = savedInstanceState.getString(KEY_STATE_SEARCH_TEXT)
        Timber.d("Restored $storedText")
    }
}