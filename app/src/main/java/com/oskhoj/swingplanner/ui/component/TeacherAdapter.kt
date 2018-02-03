package com.oskhoj.swingplanner.ui.component

import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.youtube.player.YouTubeIntents
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.util.ViewHolderList
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.inflateView
import com.oskhoj.swingplanner.util.visible
import com.oskhoj.swingplanner.util.visibleGiven
import kotlinx.android.synthetic.main.teacher_row.view.*
import net.cachapa.expandablelayout.ExpandableLayout
import org.jetbrains.anko.design.snackbar
import timber.log.Timber

// TODO: Add a presenter that handles expand events and loading / displaying new events from the list
class TeacherAdapter(var teachers: List<Teacher>, private val viewHolderList: ViewHolderList,
                     private val onTeacherClick: (Teacher) -> Unit, private val onEventClick: (EventSummary) -> Unit) : RecyclerView.Adapter<TeacherAdapter.ViewHolder>() {

    private val noItemSelected = -1

    private var selectedItem = noItemSelected

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflateView(R.layout.teacher_row))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(teachers[position], onTeacherClick)

    override fun getItemCount() = teachers.size

    // TODO: Diffutil this in a better way
    fun loadTeachers(newTeachers: List<Teacher>) {
        Timber.d("Loading new teachers with size ${newTeachers.size}")
        closeExpandedRow()
        teachers = newTeachers.sorted()
        notifyDataSetChanged()
    }

    fun showTeacherEvents(teacherEventsResponse: TeacherEventsResponse) {
        viewHolderList.findViewHolderForPosition(selectedItem)?.run {
            displayEvents(teacherEventsResponse)
        }
    }

    private fun closeExpandedRow() {
        viewHolderList.findViewHolderForPosition(selectedItem)?.run {
            teacherNameView.isSelected = false
            expandableLayout.collapse()
        }
        selectedItem = noItemSelected
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, ExpandableLayout.OnExpansionUpdateListener {

        private val favoriteImage: AppCompatImageView = itemView.favorite_icon
        private val arrowImage: AppCompatImageView = itemView.arrow_icon
        private val favoriteButton: AppCompatImageView = itemView.teacher_favorite_button
        private val youTubeButton: AppCompatImageView = itemView.teacher_youtube_button
        private val teacherHeader: LinearLayout = itemView.teacher_item_header
        private val progressBar: ProgressBar = itemView.teacher_events_progressbar
        private val teacherEvents: RecyclerView = itemView.teacher_events_recycler
        private lateinit var teacher: Teacher
        private val teacherEventsAdapter: EventAdapter = EventAdapter(emptyList(), {
            Timber.d("Clicked on event with id ${it.id}")
            onEventClick(it)
        })

        val teacherNameView: TextView = itemView.teacher_name
        val expandableLayout: ExpandableLayout = itemView.expanded_teacher_layout

        fun bind(teacher: Teacher, listener: (Teacher) -> Unit) = with(itemView) {
            this@ViewHolder.teacher = teacher
            teacher_name.text = teacher.name
            teacherHeader.setOnClickListener(this@ViewHolder)
            expandableLayout.setInterpolator(OvershootInterpolator())
            expandableLayout.setOnExpansionUpdateListener(this@ViewHolder)
            favoriteImage.visibleGiven { AppPreferences.hasFavoriteTeacher(teacher.id) }
            favoriteButton.setOnClickListener {
                AppPreferences.toggleFavoriteTeacher(teacher.id)
                favoriteImage.visibleGiven { AppPreferences.hasFavoriteTeacher(teacher.id) }
            }
            youTubeButton.setOnClickListener {
                if (YouTubeIntents.canResolveSearchIntent(context)) {
                    Timber.d("Redirecting to YouTube for [${teacher.name}]")
                    startActivity(context, YouTubeIntents.createSearchIntent(context, teacher.name), Bundle.EMPTY)
                } else {
                    Timber.d("YouTube not installed on device")
                    snackbar(this, context.getString(R.string.youtube_not_available))
                }
            }
            teacherEvents.adapter = teacherEventsAdapter
        }

        override fun onClick(view: View?) {
            viewHolderList.findViewHolderForPosition(selectedItem)?.run {
                teacherNameView.isSelected = false
                expandableLayout.collapse()
            }

            selectedItem =
                    if (layoutPosition == selectedItem) {
                        noItemSelected
                    } else {
                        this@TeacherAdapter.onTeacherClick(teacher)
                        teacherNameView.isSelected = true
                        expandableLayout.expand()
                        layoutPosition
                    }
        }

        fun displayEvents(teacherEventsResponse: TeacherEventsResponse) {
            Timber.d("Teacher events now visible ${teacherEventsResponse.events.firstOrNull()}")
            teacherEventsAdapter.loadEvents(teacherEventsResponse.events)
            progressBar.gone()
            teacherEvents.visible()
        }

        override fun onExpansionUpdate(expansionFraction: Float, state: Int) {
            when (state) {
                ExpandableLayout.State.COLLAPSED -> arrowImage.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                ExpandableLayout.State.EXPANDED -> arrowImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            }
        }
    }
}