package com.arathort.localinteraction.presentation.create_event

import androidx.annotation.StringRes
import com.arathort.localinteraction.domain.model.Category

data class CreateEventState(
    val isLoadingCategories: Boolean = false,
    val isSubmitting: Boolean = false,
    val isOrganizer: Boolean = false,
    val title: String = "",
    val description: String = "",
    val selectedCategoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val dateTimeMillis: Long? = null,
    val address: String = "",
    val latitude: Double = 50.4501,
    val longitude: Double = 30.5234,
    val tempMapLatitude: Double = 50.4501,
    val tempMapLongitude: Double = 30.5234,
    val volunteerQuotaText: String = "5",
    val photoUrlInput: String = "",
    val photoUrls: List<String> = emptyList(),
    val showDatePicker: Boolean = false,
    val showMapPickerDialog: Boolean = false,
    @param:StringRes val titleErrorResId: Int? = null,
    @param:StringRes val descriptionErrorResId: Int? = null,
    @param:StringRes val categoryErrorResId: Int? = null,
    @param:StringRes val addressErrorResId: Int? = null,
    @param:StringRes val dateErrorResId: Int? = null,
    @param:StringRes val quotaErrorResId: Int? = null,
    @param:StringRes val generalErrorResId: Int? = null
)
