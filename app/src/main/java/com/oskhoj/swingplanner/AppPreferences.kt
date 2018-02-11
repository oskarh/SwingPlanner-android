package com.oskhoj.swingplanner

import com.chibatching.kotpref.KotprefModel
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.Language
import timber.log.Timber
import java.util.Locale

object AppPreferences : KotprefModel() {
    var isShowingCardList by booleanPref(true)
    var hasShownOnboarding by booleanPref(false)
    var hasShownSearchEventsTapTarget by booleanPref(false)
    var hasShownSearchTeachersTapTarget by booleanPref(false)
    var hasShownLikeEventTapTarget by booleanPref(false)
    var hasShownWebsiteTapTarget by booleanPref(false)
    var hasShownCalendarTapTarget by booleanPref(false)
    var hasShownManageSubscriptionsTapTarget by booleanPref(false)
    var hasShownAddSubscriptionTapTarget by booleanPref(false)
    var hasShownYouTubeTapTarget by booleanPref(false)
    var isAnimationsEnabled by booleanPref(true)
    var selectedLanguage by stringPref()
    val subscriptions by stringSetPref {
        return@stringSetPref mutableSetOf("Stockholm", "Berlin", "New York")
    }
    var currentVersion by stringPref()

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
        }
    }

    fun addSubscription(subscription: String) = subscriptions.add(subscription)

    fun removeSubscription(subscription: String) = subscriptions.remove(subscription)

    fun hasSubscription(subscription: String) = subscriptions.asSequence().any { it.equals(subscription, true) }

    private var favoriteEventIdsString by stringPref()

    var favoriteEventIds: Set<Int> = sortedSetOf()
        get() = favoriteEventIdsString
                .split(",")
                .filter { it.isNotBlank() }
                .map { it.toInt() }
                .toSortedSet()
        private set

    fun toggleFavoriteEvent(eventId: Int) {
        favoriteEventIdsString =
                if (favoriteEventIds.contains(eventId)) {
                    (favoriteEventIds - eventId).joinToString(",")
                } else {
                    (favoriteEventIds + eventId).joinToString(",")
                }
    }

    fun hasFavoriteEvent(eventId: Int) = favoriteEventIds.contains(eventId)

    private var favoriteTeacherIdsString by stringPref()

    private var favoriteTeacherIds: Set<Int> = setOf()
        get() = favoriteTeacherIdsString
                .split(",")
                .filter { it.isNotBlank() }
                .map { it.toInt() }
                .toSet()

    fun toggleFavoriteTeacher(teacherId: Int) {
        favoriteTeacherIdsString =
                if (favoriteTeacherIds.contains(teacherId)) {
                    (favoriteTeacherIds - teacherId).joinToString(",")
                } else {
                    (favoriteTeacherIds + teacherId).joinToString(",")
                }
    }

    fun hasFavoriteTeacher(teacherId: Int) = favoriteTeacherIds.contains(teacherId)

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