package com.arathort.localinteraction.presentation.profile

sealed interface ProfileEvent {
    data object Refresh : ProfileEvent
    data class SelectTab(val tab: ProfileTab) : ProfileEvent
    data object EditProfileClicked : ProfileEvent
    data class EditFullNameChanged(val value: String) : ProfileEvent
    data class EditPhoneChanged(val value: String) : ProfileEvent
    data class EditBioChanged(val value: String) : ProfileEvent
    data object SaveProfileClicked : ProfileEvent
    data object DismissEditProfileDialog : ProfileEvent
    data object LogoutClicked : ProfileEvent
    data object ConfirmLogoutClicked : ProfileEvent
    data object DismissLogoutDialog : ProfileEvent
    data class WithdrawTaskClicked(val applicationId: String) : ProfileEvent
    data object ConfirmWithdrawTaskClicked : ProfileEvent
    data object DismissWithdrawDialog : ProfileEvent
    data class EventClicked(val eventId: String) : ProfileEvent
    data object LoginClicked : ProfileEvent
    data object BackClicked : ProfileEvent
}
