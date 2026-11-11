package com.arathort.localinteraction.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.ComplaintTargetType
import com.arathort.localinteraction.domain.usecase.SubmitComplaintUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ReportViewModel.Factory::class)
class ReportViewModel @AssistedInject constructor(
    @Assisted("targetType") private val targetTypeString: String,
    @Assisted("targetId") private val targetId: String,
    @Assisted("targetTitle") private val targetTitle: String,
    private val submitComplaintUseCase: SubmitComplaintUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("targetType") targetType: String,
            @Assisted("targetId") targetId: String,
            @Assisted("targetTitle") targetTitle: String
        ): ReportViewModel
    }

    private val targetType = ComplaintTargetType.fromApiValue(targetTypeString)

    private val _uiState = MutableStateFlow(
        ReportState(
            targetType = targetType,
            targetId = targetId,
            targetTitle = targetTitle
        )
    )
    val uiState: StateFlow<ReportState> = _uiState.asStateFlow()

    private val _effect = Channel<ReportEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: ReportEvent) {
        when (event) {
            is ReportEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category, categoryErrorResId = null) }
            }
            is ReportEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description, descriptionErrorResId = null) }
            }
            ReportEvent.SubmitClicked -> handleSubmit()
            ReportEvent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(ReportEffect.NavigateBack)
                }
            }
        }
    }

    private fun handleSubmit() {
        val state = _uiState.value
        val category = state.selectedCategory.trim()
        val description = state.description.trim()

        var hasError = false
        var categoryError: Int? = null
        var descriptionError: Int? = null

        if (category.isBlank()) {
            categoryError = R.string.error_complaint_category_required
            hasError = true
        }

        if (description.length < 10) {
            descriptionError = R.string.error_complaint_description_too_short
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    categoryErrorResId = categoryError,
                    descriptionErrorResId = descriptionError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            submitComplaintUseCase(
                targetType = state.targetType,
                targetId = state.targetId,
                violationCategory = category,
                description = description
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false) }
                _effect.send(ReportEffect.ShowSnackbar(R.string.complaint_submitted_success))
                _effect.send(ReportEffect.NavigateBack)
            }.onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false) }
                val msg = error.message.orEmpty()
                if (msg.isNotBlank()) {
                    _effect.send(ReportEffect.ShowSnackbarText(msg))
                } else {
                    _effect.send(ReportEffect.ShowSnackbar(R.string.error_network))
                }
            }
        }
    }
}
