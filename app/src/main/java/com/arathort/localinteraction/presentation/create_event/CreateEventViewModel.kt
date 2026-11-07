package com.arathort.localinteraction.presentation.create_event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.usecase.CreateEventUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEventState(isLoadingCategories = true))
    val uiState: StateFlow<CreateEventState> = _uiState.asStateFlow()

    private val effectChannel = Channel<CreateEventEffect>(Channel.BUFFERED)
    val effect: Flow<CreateEventEffect> = effectChannel.receiveAsFlow()

    init {
        checkUserRole()
        loadCategories()
    }

    fun onEvent(event: CreateEventEvent) {
        when (event) {
            is CreateEventEvent.TitleChanged -> _uiState.update { it.copy(title = event.value, titleErrorResId = null) }
            is CreateEventEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value, descriptionErrorResId = null) }
            is CreateEventEvent.CategorySelected -> _uiState.update { it.copy(selectedCategoryId = event.categoryId, categoryErrorResId = null) }
            is CreateEventEvent.AddressChanged -> _uiState.update { it.copy(address = event.value, addressErrorResId = null) }
            CreateEventEvent.OpenMapPickerClicked -> _uiState.update {
                it.copy(
                    tempMapLatitude = it.latitude,
                    tempMapLongitude = it.longitude,
                    showMapPickerDialog = true
                )
            }
            is CreateEventEvent.MapPointClicked -> _uiState.update {
                it.copy(tempMapLatitude = event.latitude, tempMapLongitude = event.longitude)
            }
            CreateEventEvent.ConfirmMapCoordinatesClicked -> _uiState.update {
                it.copy(
                    latitude = it.tempMapLatitude,
                    longitude = it.tempMapLongitude,
                    showMapPickerDialog = false
                )
            }
            CreateEventEvent.DismissMapPicker -> _uiState.update { it.copy(showMapPickerDialog = false) }
            is CreateEventEvent.DateTimeSelected -> _uiState.update {
                it.copy(dateTimeMillis = event.millis, dateErrorResId = null, showDatePicker = false)
            }
            CreateEventEvent.ShowDatePickerClicked -> _uiState.update { it.copy(showDatePicker = true) }
            CreateEventEvent.DismissDatePicker -> _uiState.update { it.copy(showDatePicker = false) }
            is CreateEventEvent.QuotaChanged -> _uiState.update { it.copy(volunteerQuotaText = event.value, quotaErrorResId = null) }
            is CreateEventEvent.PhotoUrlInputChanged -> _uiState.update { it.copy(photoUrlInput = event.value) }
            CreateEventEvent.AddPhotoUrlClicked -> addPhotoUrl()
            is CreateEventEvent.RemovePhotoClicked -> removePhotoUrl(event.photoUrl)
            CreateEventEvent.SubmitClicked -> submitEvent()
            CreateEventEvent.BackClicked -> sendEffect(CreateEventEffect.NavigateBack)
        }
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            val user = observeCurrentUserUseCase().first()
            val isOrganizer = user.role == UserRole.ORGANIZER
            _uiState.update { it.copy(isOrganizer = isOrganizer) }
            if (!isOrganizer) {
                effectChannel.send(CreateEventEffect.ShowSnackbar(R.string.error_only_organizers_can_create_events))
                effectChannel.send(CreateEventEffect.NavigateBack)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            observeCategoriesUseCase().collect { categories ->
                _uiState.update { state ->
                    state.copy(
                        categories = categories,
                        isLoadingCategories = false,
                        selectedCategoryId = state.selectedCategoryId ?: categories.firstOrNull()?.id
                    )
                }
            }
        }
    }

    private fun addPhotoUrl() {
        val trimmed = _uiState.value.photoUrlInput.trim()
        if (trimmed.isNotBlank() && trimmed !in _uiState.value.photoUrls) {
            _uiState.update {
                it.copy(
                    photoUrls = it.photoUrls + trimmed,
                    photoUrlInput = ""
                )
            }
        }
    }

    private fun removePhotoUrl(url: String) {
        _uiState.update {
            it.copy(photoUrls = it.photoUrls - url)
        }
    }

    private fun submitEvent() {
        val state = _uiState.value
        val title = state.title.trim()
        val description = state.description.trim()
        val categoryId = state.selectedCategoryId.orEmpty()
        val address = state.address.trim()
        val quota = state.volunteerQuotaText.toIntOrNull() ?: 0
        val dateTimeMillis = state.dateTimeMillis

        var hasError = false
        var titleError: Int? = null
        var descriptionError: Int? = null
        var categoryError: Int? = null
        var addressError: Int? = null
        var dateError: Int? = null
        var quotaError: Int? = null

        if (title.length !in 5..150) {
            titleError = R.string.error_event_title_length
            hasError = true
        }

        if (description.length < 50) {
            descriptionError = R.string.error_event_description_too_short
            hasError = true
        }

        if (categoryId.isBlank()) {
            categoryError = R.string.error_event_category_required
            hasError = true
        }

        if (dateTimeMillis == null || dateTimeMillis <= System.currentTimeMillis()) {
            dateError = R.string.error_event_date_in_past
            hasError = true
        }

        if (address.length < 5) {
            addressError = R.string.error_event_address_too_short
            hasError = true
        }

        if (quota <= 0) {
            quotaError = R.string.error_event_quota_invalid
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    titleErrorResId = titleError,
                    descriptionErrorResId = descriptionError,
                    categoryErrorResId = categoryError,
                    addressErrorResId = addressError,
                    dateErrorResId = dateError,
                    quotaErrorResId = quotaError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalErrorResId = null) }
            val result = createEventUseCase(
                title = title,
                description = description,
                categoryId = categoryId,
                dateTimeMillis = dateTimeMillis!!,
                address = address,
                latitude = state.latitude,
                longitude = state.longitude,
                volunteerQuota = quota,
                photoUrls = state.photoUrls
            )

            result.onSuccess {
                _uiState.update { it.copy(isSubmitting = false) }
                effectChannel.send(CreateEventEffect.ShowSnackbar(R.string.event_create_success))
                effectChannel.send(CreateEventEffect.NavigateBack)
            }.onFailure {
                _uiState.update { it.copy(isSubmitting = false, generalErrorResId = R.string.error_network) }
                effectChannel.send(CreateEventEffect.ShowSnackbar(R.string.error_network))
            }
        }
    }

    private fun sendEffect(effect: CreateEventEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }
}
