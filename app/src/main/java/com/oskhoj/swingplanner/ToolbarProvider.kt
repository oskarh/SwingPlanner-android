package com.oskhoj.swingplanner

import com.oskhoj.swingplanner.ui.base.ViewType

interface ToolbarProvider {

    fun updateToolbar(viewType: ViewType)
}