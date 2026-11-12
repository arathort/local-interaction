package com.arathort.localinteraction.presentation.create_event

sealed interface CreateEventEvent {
    data class TitleChanged(val value: String) : CreateEventEvent
    data class DescriptionChanged(val value: String) : CreateEventEvent
    data class CategorySelected(val categoryId: String) : CreateEventEvent
    data class AddressChanged(val value: String) : CreateEventEvent
    data object OpenMapPickerClicked : CreateEventEvent
    data class MapPointClicked(val latitude: Double, val longitude: Double) : CreateEventEvent
    data object ConfirmMapCoordinatesClicked : CreateEventEvent
    data object DismissMapPicker : CreateEventEvent
    data class DateTimeSelected(val millis: Long) : CreateEventEvent
    data object ShowDatePickerClicked : CreateEventEvent
    data object DismissDatePicker : CreateEventEvent
    data class QuotaChanged(val value: String) : CreateEventEvent
    data class PhotosSelected(val uris: List<String>) : CreateEventEvent
    data class RemovePhotoClicked(val photoUrl: String) : CreateEventEvent
    data object SubmitClicked : CreateEventEvent
    data object BackClicked : CreateEventEvent
}
