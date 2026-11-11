package com.arathort.localinteraction.presentation.report

import androidx.annotation.StringRes

sealed interface ReportEffect {
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : ReportEffect
    data class ShowSnackbarText(val message: String) : ReportEffect
    data object NavigateBack : ReportEffect
}
