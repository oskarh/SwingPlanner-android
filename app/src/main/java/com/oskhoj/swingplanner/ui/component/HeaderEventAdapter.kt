package com.oskhoj.swingplanner.ui.component

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.EventsPage
import com.oskhoj.swingplanner.util.getLong
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.inflateView
import com.oskhoj.swingplanner.util.visible
import kotlinx.android.synthetic.main.event_header_row.view.*
import timber.log.Timber

class HeaderEventAdapter(events: List<EventSummary>, onClick: (EventSummary) -> Unit) : EventAdapter(events, onClick) {

    private val headerItemView = 2

    private var totalNumberEvents: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                headerItemView -> HeaderHolder(parent.inflateView(R.layout.event_header_row))
                else -> super.onCreateViewHolder(parent, viewType)
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when (holder) {
                is HeaderHolder -> holder.bind()
                else -> super.onBindViewHolder(holder, position - 1)
            }

    override fun getItemCount() = events.size + 1

    override fun getItemViewType(position: Int) = if (position == 0) headerItemView else itemViewType

    fun loadEventsPage(eventsPage: EventsPage) {
        totalNumberEvents = eventsPage.totalEvents
        loadEvents(eventsPage.events)
    }

    fun addEvents(addedEvents: List<EventSummary>) {
        Timber.d("Adding events $addedEvents")
        events += addedEvents
        notifyDataSetChanged()
    }

    inner class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() = with(itemView) {
            if (events.isEmpty()) {
                message_header.gone()
            } else {
                message_header.run {
                    text = itemView.context.getString(R.string.search_header_found, totalNumberEvents)
                    alpha = 0f
                    visible()
                    animate().alpha(1f).duration = itemView.context.getLong(R.integer.anim_duration_medium)
                }
            }
        }
    }
}
