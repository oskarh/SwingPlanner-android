package com.oskhoj.swingplanner.ui.component

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.util.Month
import com.oskhoj.swingplanner.util.inflateView
import com.oskhoj.swingplanner.util.loadAnimation
import com.oskhoj.swingplanner.util.loadFlagIconOrDisappear
import com.oskhoj.swingplanner.util.loadImageOrDisappear
import kotlinx.android.synthetic.main.event_row_card.view.*
import kotlinx.android.synthetic.main.event_row_small.view.*
import timber.log.Timber

open class EventAdapter(var events: List<EventSummary>, protected val onClick: (EventSummary) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val smallItemView = 0
    private val largeItemView = 1

    protected var itemViewType = if (AppPreferences.isShowingCardList) largeItemView else smallItemView

    private var lastAnimatedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            smallItemView -> EventHolder(parent.inflateView(R.layout.event_row_small))
            largeItemView -> EventHolder(parent.inflateView(R.layout.event_row_card))
            else -> throw IllegalStateException("Unknown view type [$viewType]")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EventHolder) {
            holder.bind(events[position], onClick)
            setAnimation(holder.itemView, position)
        } else {
            throw IllegalStateException("Illegal type for holder [${holder.javaClass.simpleName}]")
        }
    }

    override fun getItemCount() = events.size

    override fun getItemViewType(position: Int) = itemViewType

    fun toggleItemViewType() {
        itemViewType = if (itemViewType == largeItemView) smallItemView else largeItemView
    }

    // TODO: Diffutil this in a better way. Also add animation
    fun loadEvents(newEvents: List<EventSummary>) {
        Timber.d("Loading new events with size ${newEvents.size}")
        events = newEvents.sorted()
        notifyDataSetChanged()
    }

    fun isEmpty() = events.isEmpty()

    inner class EventHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(event: EventSummary, listener: (EventSummary) -> Unit) = with(itemView) {
            if (itemViewType == largeItemView) {
                card_event_name.text = event.name
                card_event_city.text = event.city
                card_event_country.text = event.country.name
                month_text.text = Month.getMonth(event.startDate.month, itemView.context).take(3)
                day_text.text = event.dayOfMonth
                card_event_image.loadImageOrDisappear(event.imageUrl, context)
                card_country_flag.loadFlagIconOrDisappear(event.country.isoCode, context)
            } else {
                small_event_name.text = event.name
                small_event_country.text = "${event.city}, ${event.country.name}"
                month_small_text.text = Month.getMonth(event.startDate.month, itemView.context).take(3)
                day_small_text.text = event.dayOfMonth
                small_country_flag.loadFlagIconOrDisappear(event.country.isoCode, context)
            }
            setOnClickListener { listener(event) }
            setOnTouchListener(OnTouchAnimation())
        }
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastAnimatedPosition) {
            view.startAnimation(view.context.loadAnimation(R.anim.recycler_item_enter_animation))
            lastAnimatedPosition = position
        }
    }
}
