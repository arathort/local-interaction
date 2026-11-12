package com.arathort.localinteraction.presentation.report

sealed interface ReportEvent {
    data class CategorySelected(val category: String) : ReportEvent
    data class DescriptionChanged(val description: String) : ReportEvent
    data object SubmitClicked : ReportEvent
    data object BackClicked : ReportEvent
}
