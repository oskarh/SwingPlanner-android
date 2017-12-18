package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.util.DanceStyle
import java.util.Locale

data class EventSearchParams(val query: String = "", val styles: Set<DanceStyle> = AppPreferences.filterOptions, val page: Int = 0) {

    fun danceStyles() = styles.joinToString(",")

    override fun equals(other: Any?): Boolean {
        val eventParams = (other as? EventSearchParams) ?: return false
        return query.equals(eventParams.query, true) && styles. equals(eventParams.styles) && page == eventParams.page
    }

    override fun hashCode() = query.toLowerCase(Locale.ENGLISH).hashCode() + 7 * styles.hashCode() + 23 * page.hashCode()

    override fun toString() = "${query.toLowerCase(Locale.ENGLISH)}${styles.joinToString(",")}$page"
}