package com.arathort.localinteraction.presentation.admin

sealed interface AdminPanelEffect {
    data class ShowMessage(val message: String) : AdminPanelEffect
    data class ShowMessageRes(val stringResId: Int, val formatArgs: List<Any> = emptyList()) : AdminPanelEffect
    data object AccessDenied : AdminPanelEffect
    data class NavigateToProfile(val userId: String) : AdminPanelEffect
    data object NavigateBack : AdminPanelEffect
}
