package com.oskhoj.swingplanner.ui.teachers

import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.OrientationHelper.VERTICAL
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.base.ViewType.TEACHERS_VIEW
import com.oskhoj.swingplanner.ui.component.TeacherAdapter
import com.oskhoj.swingplanner.ui.component.TextChangedListener
import com.oskhoj.swingplanner.ui.details.DetailsController
import com.oskhoj.swingplanner.util.KEY_STATE_SEARCH_TEXT
import com.oskhoj.swingplanner.util.TeacherExpandedListener
import com.oskhoj.swingplanner.util.ViewHolderList
import com.oskhoj.swingplanner.util.animateToGone
import com.oskhoj.swingplanner.util.animateToVisible
import com.oskhoj.swingplanner.util.closeKeyboard
import com.oskhoj.swingplanner.util.invisible
import com.oskhoj.swingplanner.util.loadLayoutAnimation
import com.oskhoj.swingplanner.util.removeClickListener
import com.oskhoj.swingplanner.util.showTapTarget
import com.oskhoj.swingplanner.util.visibleIf
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

    override val screenType: ScreenType = ScreenType.TEACHER

    private var searchText: EditText? = null
    private lateinit var teacherRecyclerView: RecyclerView
    private lateinit var backIcon: AppCompatImageView
    private lateinit var clearIcon: AppCompatImageView

    private var storedText: String = ""

    private val textListener = TextChangedListener {
        clearIcon.visibleIf { it.isNotEmpty() }
        presenter.loadTeachers(it.toString())
    }

    private val teacherAdapter = TeacherAdapter(emptyList(), this, {
        presenter.openTeacherDetails(it)
    }, {
        openEvent(it)
    }, { teacherId, isLiked ->
        presenter.onTeacherLike(teacherId, isLiked)
    }, object : TeacherExpandedListener {
        override fun onTeacherExpanded(youTubeButton: AppCompatImageView, likeButton: AppCompatImageView) {
            if (!AppPreferences.hasShownYouTubeTapTarget) {
                activity?.showTapTarget(youTubeButton, R.string.tap_target_teacher_youtube_title, R.string.tap_target_teacher_youtube_message)
                AppPreferences.hasShownYouTubeTapTarget = true
            } else if (!AppPreferences.hasShownLikeTeacherTapTarget) {
                activity?.showTapTarget(likeButton, R.string.tap_target_like_teacher_title, R.string.tap_target_like_teacher_message)
                AppPreferences.hasShownLikeTeacherTapTarget = true
            }
        }
    })

    override fun displayTeachers(teachers: List<Teacher>) {
        teacherAdapter.loadTeachers(teachers)
    }

    override fun displayTeacherEvents(teacherEventsResponse: TeacherEventsResponse) {
        Timber.d("Showing teacher events $teacherEventsResponse")
        teacherAdapter.showTeacherEvents(teacherEventsResponse)
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

    private fun openEvent(eventSummary: EventSummary) {
        Timber.d("Opening event details...")
        activity?.closeKeyboard()
        router.pushController(RouterTransaction.with(DetailsController(eventSummary)))
    }

    override fun showLoading() {
        view?.teachers_progressbar?.animateToVisible()
    }

    override fun hideLoading() {
        view?.teachers_progressbar?.animateToGone()
    }

    private fun setUpRecyclerView(view: View) {
        teacherRecyclerView = view.teachersRecyclerView.apply {
            layoutAnimation = view.loadLayoutAnimation(R.anim.layout_recycler_animation_new_dataset)
            addItemDecoration(DividerItemDecoration(context, VERTICAL))
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
            backIcon = search_back.apply {
                onClick { presenter.onSearchBack() }
            }
            clearIcon = search_clear.apply {
                onClick { presenter.onSearchClear() }
            }

            searchText = search_text?.apply {
                setText(storedText)
                setSelection(storedText.length)
                addTextChangedListener(textListener)
                backIcon.visibleIf { isFocused }
                clearIcon.visibleIf { isFocused && text.isNotBlank() }
                setOnFocusChangeListener { _, hasFocus ->
                    backIcon.visibleIf { hasFocus }
                    clearIcon.visibleIf { hasFocus && text.isNotBlank() }
                }
            }
            if (!AppPreferences.hasShownSearchTeachersTapTarget) {
                showTapTarget(R.id.search_text, R.string.tap_target_search_teachers_title, R.string.tap_target_search_teachers_message)
                AppPreferences.hasShownSearchTeachersTapTarget = true
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        backIcon.removeClickListener()
        clearIcon.removeClickListener()
        searchText?.removeTextChangedListener(textListener)
        searchText?.onFocusChangeListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("Saving instance state... ${(searchText?.text)}")
        outState.putString(KEY_STATE_SEARCH_TEXT, (searchText?.text ?: "").toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Timber.d("Restored $storedText")
        storedText = savedInstanceState.getString(KEY_STATE_SEARCH_TEXT)
    }
}