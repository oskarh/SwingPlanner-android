package com.oskhoj.swingplanner.util

enum class Language(val nativeName: String, val isoCodeLanguage: String, val isoCodeFlag: String = isoCodeLanguage) {
    GERMAN("Deutsch", "de"),
    ENGLISH("English", "en", "gb"),
    SPANISH("Español", "es"),
    FRENCH("Français", "fr"),
    ITALIAN("Italiano", "it");

    companion object {
        val defaultLanguage = ENGLISH
    }
}