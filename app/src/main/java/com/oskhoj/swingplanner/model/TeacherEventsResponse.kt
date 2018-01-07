package com.oskhoj.swingplanner.model

data class TeacherEventsResponse(val teacherId: Long, val events: List<EventSummary> = emptyList())