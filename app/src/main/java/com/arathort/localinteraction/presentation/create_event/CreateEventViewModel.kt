package com.arathort.localinteraction.presentation.create_event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.usecase.CreateEventUseCase
import com.arathort.localinteraction.domain.usecase.GetEventDetailsUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.UpdateEventUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@HiltViewModel(assistedFactory = CreateEventViewModel.Factory::class)
class CreateEventViewModel @AssistedInject constructor(
    @Assisted private val eventId: String?,
    private val createEventUseCase: CreateEventUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val getEventDetailsUseCase: GetEventDetailsUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(eventId: String? = null): CreateEventViewModel
    }

    private val _uiState = MutableStateFlow(
        CreateEventState(
            eventId = eventId,
            isLoadingCategories = true,
            isLoadingEvent = !eventId.isNullOrBlank()
        )
    )
    val uiState: StateFlow<CreateEventState> = _uiState.asStateFlow()

    private val effectChannel = Channel<CreateEventEffect>(Channel.BUFFERED)
    val effect: Flow<CreateEventEffect> = effectChannel.receiveAsFlow()

    init {
        checkUserRole()
        loadCategories()
        if (!eventId.isNullOrBlank()) {
            loadExistingEvent(eventId)
        }
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
            is CreateEventEvent.PhotosSelected -> {
                _uiState.update { state ->
                    state.copy(photoUrls = (state.photoUrls + event.uris).distinct().take(10))
                }
            }
            is CreateEventEvent.RemovePhotoClicked -> removePhotoUrl(event.photoUrl)
            CreateEventEvent.SubmitClicked -> submitEvent()
            CreateEventEvent.BackClicked -> sendEffect(CreateEventEffect.NavigateBack)
        }
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            val user = observeCurrentUserUseCase().first()
            val isOrganizer = user.role == UserRole.ORGANIZER || user.role == UserRole.ADMIN
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

    private fun loadExistingEvent(targetEventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEvent = true) }
            getEventDetailsUseCase(targetEventId)
                .onSuccess { existingEvent ->
                    val parsedDateMillis = parseEventDateMillis(existingEvent.eventDate)
                    _uiState.update { state ->
                        val matchedCategoryId = existingEvent.categoryId
                            ?: state.categories.find { it.name.equals(existingEvent.categoryName, ignoreCase = true) }?.id
                            ?: state.selectedCategoryId
                        val resolvedLat = existingEvent.location.latitude ?: state.latitude
                        val resolvedLng = existingEvent.location.longitude ?: state.longitude
                        state.copy(
                            isLoadingEvent = false,
                            title = existingEvent.title,
                            description = existingEvent.description,
                            selectedCategoryId = matchedCategoryId,
                            dateTimeMillis = parsedDateMillis,
                            address = existingEvent.location.address,
                            latitude = resolvedLat,
                            longitude = resolvedLng,
                            tempMapLatitude = resolvedLat,
                            tempMapLongitude = resolvedLng,
                            volunteerQuotaText = existingEvent.volunteerQuota.toString(),
                            photoUrls = existingEvent.photos
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingEvent = false, generalErrorResId = R.string.error_network) }
                    effectChannel.send(CreateEventEffect.ShowSnackbar(R.string.error_network))
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

        if (dateTimeMillis == null || (!state.isEditMode && dateTimeMillis <= System.currentTimeMillis())) {
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
            val result = if (state.isEditMode && !state.eventId.isNullOrBlank()) {
                updateEventUseCase(
                    eventId = state.eventId,
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
            } else {
                createEventUseCase(
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
            }

            result.onSuccess {
                _uiState.update { it.copy(isSubmitting = false) }
                val messageRes = if (state.isEditMode) {
                    R.string.event_update_success
                } else {
                    R.string.event_create_success
                }
                effectChannel.send(CreateEventEffect.ShowSnackbar(messageRes))
                effectChannel.send(CreateEventEffect.NavigateBack)
            }.onFailure {
                _uiState.update { it.copy(isSubmitting = false, generalErrorResId = R.string.error_network) }
                effectChannel.send(CreateEventEffect.ShowSnackbar(R.string.error_network))
            }
        }
    }

    private fun parseEventDateMillis(eventDate: String): Long {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(eventDate)?.time
            }.getOrNull()
            if (parsed != null && parsed > 0L) {
                return parsed
            }
        }
        return System.currentTimeMillis() + 86_400_000L
    }

    private fun sendEffect(effect: CreateEventEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }
}
