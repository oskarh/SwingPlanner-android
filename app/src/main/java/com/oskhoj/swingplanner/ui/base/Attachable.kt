package com.oskhoj.swingplanner.ui.base

interface Attachable<in V : BaseView> {

    fun onAttach(attachedView: V)

    fun onDetach()
}