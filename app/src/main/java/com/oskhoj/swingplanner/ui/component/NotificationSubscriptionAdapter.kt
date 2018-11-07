package com.oskhoj.swingplanner.ui.component

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.util.inflateView
import kotlinx.android.synthetic.main.notification_subscription_row.view.*
import org.jetbrains.anko.sdk21.listeners.onClick

class NotificationSubscriptionAdapter(private var subscriptions: MutableList<String>, private val onClick: (String) -> Unit)
    : RecyclerView.Adapter<NotificationSubscriptionAdapter.ViewHolder>() {

    init {
        subscriptions.sortBy { it.toLowerCase() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflateView(R.layout.notification_subscription_row))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(subscriptions[position], onClick)
    }

    override fun getItemCount() = subscriptions.size

    fun addSubscription(subscription: String) {
        subscriptions.add(subscription)
        subscriptions.sortBy { it.toLowerCase() }
        notifyDataSetChanged()
    }

    fun removeSubscription(subscription: String) {
        subscriptions.remove(subscription)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(subscription: String, listener: (String) -> Unit) = with(itemView) {
            subscription_text.text = subscription
            clear_icon.onClick { listener(subscription) }
        }
    }
}
