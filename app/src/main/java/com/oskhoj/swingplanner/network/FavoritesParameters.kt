package com.oskhoj.swingplanner.network

data class FavoritesParameters(val ids: List<Int>) {
    override fun equals(other: Any?): Boolean {
        val favoritesResponse = (other as? FavoritesParameters) ?: return false
        return setOf(ids) == setOf(favoritesResponse.ids)
    }

    override fun hashCode() = setOf(ids).hashCode()

    override fun toString() = ids.joinToString()
}