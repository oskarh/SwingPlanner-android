package com.oskhoj.swingplanner.ui.component

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
    private val footerItemView = 3

    private var totalNumberEvents: Int = 0
    private var hasMorePages: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                headerItemView -> HeaderHolder(parent.inflateView(R.layout.event_header_row))
                footerItemView -> FooterHolder(parent.inflateView(R.layout.event_footer_row))
                else -> super.onCreateViewHolder(parent, viewType)
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when (holder) {
                is HeaderHolder -> holder.bind()
                is FooterHolder -> holder.bind()
                else -> super.onBindViewHolder(holder, position - 1)
            }

    override fun getItemCount() = if (hasMorePages) events.size + 2 else events.size + 1

    override fun getItemViewType(position: Int) =
            when {
                position == 0 -> headerItemView
                hasMorePages && (position == itemCount - 1) -> footerItemView
                else -> itemViewType
            }

    fun loadEventsPage(eventsPage: EventsPage) {
        totalNumberEvents = eventsPage.totalEvents
        loadEvents(eventsPage.events)
        hasMorePages = !eventsPage.isLastPage
        Timber.d("Has more events $hasMorePages")
    }

    fun addEvents(addedEvents: List<EventSummary>) {
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
                    animate().alpha(1f).duration = itemView.getLong(R.integer.anim_duration_medium)
                }
            }
        }
    }

    inner class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            Timber.d("Created footer...")
        }
    }
}
