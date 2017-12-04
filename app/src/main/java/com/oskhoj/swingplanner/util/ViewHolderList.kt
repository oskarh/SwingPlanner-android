package com.oskhoj.swingplanner.util

import com.oskhoj.swingplanner.ui.component.TeacherAdapter

interface ViewHolderList {
    fun findViewHolderForPosition(position: Int): TeacherAdapter.ViewHolder?
}