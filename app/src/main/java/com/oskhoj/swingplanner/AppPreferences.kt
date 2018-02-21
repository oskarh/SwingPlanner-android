package com.oskhoj.swingplanner

import com.chibatching.kotpref.KotprefModel
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.util.ANALYTICS_INITIALIZED_LANGUAGE
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.Language
import com.oskhoj.swingplanner.util.PROPERTY_APP_LANGUAGE
import com.oskhoj.swingplanner.util.PROPERTY_DEVICE_LANGUAGE
import com.oskhoj.swingplanner.util.compareToIgnoreWhitespace
import timber.log.Timber
import java.util.Locale

object AppPreferences : KotprefModel() {
    var currentVersion by stringPref()
    var hasShownAddSubscriptionTapTarget by booleanPref(false)
    var hasShownCalendarTapTarget by booleanPref(false)
    var hasShownLikeEventTapTarget by booleanPref(false)
    var hasShownLikeTeacherTapTarget by booleanPref(false)
    var hasShownManageSubscriptionsTapTarget by booleanPref(false)
    var hasShownOnboarding by booleanPref(false)
    var hasShownSearchEventsTapTarget by booleanPref(false)
    var hasShownSearchTeachersTapTarget by booleanPref(false)
    var hasShownWebsiteTapTarget by booleanPref(false)
    var hasShownYouTubeTapTarget by booleanPref(false)
    var isAnimationsEnabled by booleanPref(true)
    var isShowingCardList by booleanPref(true)
    var selectedLanguage by stringPref()
    var firebaseToken by stringPref()
    val subscriptions by stringSetPref {
        return@stringSetPref mutableSetOf("Stockholm", "Berlin", "New York")
    }

    init {
        if (selectedLanguage.isBlank()) {
            selectedLanguage = when (Locale.getDefault().language) {
                Language.ENGLISH.isoCodeLanguage -> Language.ENGLISH.name
                Language.FRENCH.isoCodeLanguage -> Language.FRENCH.name
                Language.GERMAN.isoCodeLanguage -> Language.GERMAN.name
                Language.ITALIAN.isoCodeLanguage -> Language.ITALIAN.name
                Language.SPANISH.isoCodeLanguage -> Language.SPANISH.name
                else -> Language.defaultLanguage.name
            }
            AnalyticsHelper.logEvent(ANALYTICS_INITIALIZED_LANGUAGE, PROPERTY_DEVICE_LANGUAGE to Locale.getDefault().language,
                    PROPERTY_APP_LANGUAGE to selectedLanguage)
        }
    }

    fun addSubscription(subscription: String) = subscriptions.add(subscription)

    fun removeSubscription(subscription: String) = subscriptions.remove(subscription)

    fun hasSubscription(subscription: String) = subscriptions.asSequence().any { it.compareToIgnoreWhitespace(subscription, true) == 0 }

    fun numberCustomSubscriptions() = subscriptions.size

    private var favoriteEventIdsString by stringPref()

    var favoriteEventIds: Set<Int> = sortedSetOf()
        get() = favoriteEventIdsString
                .split(",")
                .filter { it.isNotBlank() }
                .map { it.toInt() }
                .toSortedSet()
        private set

    fun numberFavoriteEvents() = favoriteEventIds.size

    fun toggleFavoriteEvent(eventId: Int): Boolean {
        val isNowFavorite: Boolean
        favoriteEventIdsString =
                if (favoriteEventIds.contains(eventId)) {
                    isNowFavorite = false
                    (favoriteEventIds - eventId).joinToString(",")
                } else {
                    isNowFavorite = true
                    (favoriteEventIds + eventId).joinToString(",")
                }
        return isNowFavorite
    }

    fun hasFavoriteEvent(eventId: Int) = favoriteEventIds.contains(eventId)

    private var favoriteTeacherIdsString by stringPref()

    private var favoriteTeacherIds: Set<Int> = setOf()
        get() = favoriteTeacherIdsString
                .split(",")
                .filter { it.isNotBlank() }
                .map { it.toInt() }
                .toSet()

    fun toggleFavoriteTeacher(teacherId: Int): Boolean {
        val isNowFavorite: Boolean
        favoriteTeacherIdsString =
                if (favoriteTeacherIds.contains(teacherId)) {
                    isNowFavorite = false
                    (favoriteTeacherIds - teacherId).joinToString(",")
                } else {
                    isNowFavorite = true
                    (favoriteTeacherIds + teacherId).joinToString(",")
                }
        return isNowFavorite
    }

    fun hasFavoriteTeacher(teacherId: Int) = favoriteTeacherIds.contains(teacherId)

    fun numberFavoriteTeachers() = favoriteTeacherIds.size

    private var filterOptionsString by stringPref(DanceStyle.values().joinToString(","))

    var filterOptions: Set<DanceStyle> = setOf()
        get() = filterOptionsString
                .split(",")
                .filter { it.isNotBlank() }
                .map { enumValueOf<DanceStyle>(it) }
                .toSet()

    fun toggleFilterOption(danceStyle: DanceStyle) {
        filterOptionsString =
                if (filterOptions.contains(danceStyle)) {
                    Timber.d("Removing ${danceStyle.name}")
                    (filterOptions - danceStyle).joinToString(",")
                } else {
                    Timber.d("Adding ${danceStyle.name}")
                    (filterOptions + danceStyle).joinToString(",")
                }
    }

    fun hasFavoriteDanceStyle(danceStyle: DanceStyle) = filterOptions.contains(danceStyle)

    var filteredDanceStyles = filterOptions.joinToString(",")
        get() = filterOptions.joinToString(",")
        private set
}