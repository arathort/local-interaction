package com.arathort.localinteraction.presentation.manage_applications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.presentation.components.AppAsyncImage
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun ManageApplicationsRoute(
    onBack: () -> Unit,
    onNavigateToVolunteerProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToWriteReview: ((String, String, String, String) -> Unit)? = null,
    viewModel: ManageApplicationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ManageApplicationsEffect.NavigateBack -> onBack()
                is ManageApplicationsEffect.NavigateToVolunteerProfile -> onNavigateToVolunteerProfile(effect.volunteerId)
                is ManageApplicationsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageResId)
                    )
                }
            }
        }
    }

    ManageApplicationsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClicked = onBack,
        onRateVolunteerClicked = { app ->
            onNavigateToWriteReview?.invoke(state.eventId, state.eventTitle, app.volunteerId, app.volunteerName)
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageApplicationsScreen(
    state: ManageApplicationsState,
    onEvent: (ManageApplicationsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = { onEvent(ManageApplicationsEvent.BackClicked) },
    onRateVolunteerClicked: ((VolunteerApplication) -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.manage_applications_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth
                    )
                }
            }
            state.errorMessageResId != null && state.applications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                    ) {
                        Text(
                            text = stringResource(state.errorMessageResId),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { onEvent(ManageApplicationsEvent.Refresh) }) {
                            Text(text = stringResource(R.string.action_retry))
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    HeaderQuotaCard(
                        eventTitle = state.eventTitle,
                        eventStatus = state.eventStatus,
                        approvedCount = state.approvedVolunteersCount,
                        quota = state.volunteerQuota,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.paddingLarge)
                            .padding(top = Dimensions.paddingSmall)
                    )

                    Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

                    StatusFilterChips(
                        selectedFilter = state.selectedFilter,
                        allCount = state.applications.size,
                        pendingCount = state.pendingCount,
                        approvedCount = state.approvedCount,
                        rejectedCount = state.rejectedCount,
                        withdrawnCount = state.withdrawnCount,
                        onFilterSelected = { onEvent(ManageApplicationsEvent.FilterSelected(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

                    val filteredList = state.filteredApplications
                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(
                                    if (state.applications.isEmpty()) {
                                        R.string.manage_applications_empty
                                    } else {
                                        R.string.manage_applications_empty_filter
                                    }
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = Dimensions.paddingLarge,
                                end = Dimensions.paddingLarge,
                                bottom = Dimensions.paddingLarge
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                        ) {
                            items(filteredList, key = { it.id }) { application ->
                                VolunteerApplicationCard(
                                    application = application,
                                    isProcessing = state.isProcessingAction,
                                    onVolunteerClick = { onEvent(ManageApplicationsEvent.VolunteerClicked(application.volunteerId)) },
                                    onApprove = { onEvent(ManageApplicationsEvent.ApproveClicked(application.id)) },
                                    onReject = { onEvent(ManageApplicationsEvent.RejectClicked(application.id)) },
                                    onRateVolunteer = if (state.eventStatus == EventStatus.IN_PROGRESS || state.eventStatus == EventStatus.COMPLETED) {
                                        { onRateVolunteerClicked?.invoke(application) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.rejectingApplicationId != null) {
        RejectApplicationDialog(
            reason = state.rejectionReasonText,
            errorResId = state.rejectionReasonErrorResId,
            isProcessing = state.isProcessingAction,
            onReasonChange = { onEvent(ManageApplicationsEvent.RejectionReasonChanged(it)) },
            onConfirm = { onEvent(ManageApplicationsEvent.ConfirmRejection) },
            onDismiss = { onEvent(ManageApplicationsEvent.DismissRejectDialog) }
        )
    }

    if (state.pendingApprovalApplicationId != null) {
        QuotaWarningConfirmDialog(
            approvedCount = state.approvedVolunteersCount,
            quota = state.volunteerQuota,
            onConfirm = { onEvent(ManageApplicationsEvent.ConfirmOverQuotaApproval) },
            onDismiss = { onEvent(ManageApplicationsEvent.DismissOverQuotaDialog) }
        )
    }
}

@Composable
private fun HeaderQuotaCard(
    eventTitle: String,
    eventStatus: EventStatus,
    approvedCount: Int,
    quota: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = eventTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = eventStatus.apiValue) }
                )
            }

            Text(
                text = stringResource(R.string.manage_applications_confirmed_count, approvedCount, quota),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            LinearProgressIndicator(
                progress = {
                    if (quota > 0) {
                        (approvedCount.toFloat() / quota.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusFilterChips(
    selectedFilter: ApplicationStatusFilter,
    allCount: Int,
    pendingCount: Int,
    approvedCount: Int,
    rejectedCount: Int,
    withdrawnCount: Int,
    onFilterSelected: (ApplicationStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Dimensions.paddingLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == ApplicationStatusFilter.ALL,
                onClick = { onFilterSelected(ApplicationStatusFilter.ALL) },
                label = { Text(text = stringResource(R.string.manage_applications_filter_all, allCount)) }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == ApplicationStatusFilter.PENDING,
                onClick = { onFilterSelected(ApplicationStatusFilter.PENDING) },
                label = { Text(text = stringResource(R.string.manage_applications_filter_pending, pendingCount)) }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == ApplicationStatusFilter.APPROVED,
                onClick = { onFilterSelected(ApplicationStatusFilter.APPROVED) },
                label = { Text(text = stringResource(R.string.manage_applications_filter_approved, approvedCount)) }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == ApplicationStatusFilter.REJECTED,
                onClick = { onFilterSelected(ApplicationStatusFilter.REJECTED) },
                label = { Text(text = stringResource(R.string.manage_applications_filter_rejected, rejectedCount)) }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == ApplicationStatusFilter.WITHDRAWN,
                onClick = { onFilterSelected(ApplicationStatusFilter.WITHDRAWN) },
                label = { Text(text = stringResource(R.string.manage_applications_filter_withdrawn, withdrawnCount)) }
            )
        }
    }
}

@Composable
private fun VolunteerApplicationCard(
    application: VolunteerApplication,
    isProcessing: Boolean,
    onVolunteerClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    onRateVolunteer: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(Dimensions.avatarMedium)
                        .clickable(onClick = onVolunteerClick),
                    shape = RoundedCornerShape(Dimensions.avatarMedium),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    AppAsyncImage(
                        model = application.volunteerAvatarUrl,
                        contentDescription = application.volunteerName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        fallback = {
                            Text(
                                text = application.volunteerName.trim().take(1).ifBlank { "V" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.width(Dimensions.spacingMedium))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onVolunteerClick)
                ) {
                    Text(
                        text = application.volunteerName.ifBlank { stringResource(R.string.role_volunteer) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.starRatingSmall),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.event_details_rating_format, application.volunteerRating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                ApplicationStatusBadge(status = application.status)
            }

            if (!application.createdAt.isBlank()) {
                Text(
                    text = stringResource(R.string.manage_applications_applied_date_format, application.createdAt.take(10)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!application.motivationLetter.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(Dimensions.paddingSmall)) {
                        Text(
                            text = stringResource(R.string.manage_applications_motivation_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
                        Text(
                            text = application.motivationLetter,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (application.status == ApplicationStatus.APPROVED && !application.volunteerPhone.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.manage_applications_phone_label, application.volunteerPhone),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (application.status == ApplicationStatus.REJECTED && !application.rejectionReason.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.manage_applications_rejection_reason_label, application.rejectionReason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (application.status == ApplicationStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimensions.buttonHeight),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = stringResource(R.string.manage_applications_reject))
                    }

                    Button(
                        onClick = onApprove,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimensions.buttonHeight)
                    ) {
                        Text(text = stringResource(R.string.manage_applications_approve))
                    }
                }
            }

            if (application.status == ApplicationStatus.APPROVED && onRateVolunteer != null) {
                OutlinedButton(
                    onClick = onRateVolunteer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                    Text(text = stringResource(R.string.review_action_rate_volunteer))
                }
            }
        }
    }
}

@Composable
private fun ApplicationStatusBadge(
    status: ApplicationStatus,
    modifier: Modifier = Modifier
) {
    val chipColors = when (status) {
        ApplicationStatus.PENDING -> AssistChipDefaults.assistChipColors()
        ApplicationStatus.APPROVED -> AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.primary
        )
        ApplicationStatus.REJECTED -> AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.error
        )
        ApplicationStatus.WITHDRAWN -> AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.outline
        )
    }

    AssistChip(
        onClick = {},
        label = { Text(text = status.apiValue) },
        colors = chipColors,
        modifier = modifier
    )
}

@Composable
private fun RejectApplicationDialog(
    reason: String,
    errorResId: Int?,
    isProcessing: Boolean,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.manage_applications_reject_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.manage_applications_reject_reason_hint)) },
                    minLines = 3,
                    maxLines = 5,
                    isError = errorResId != null,
                    supportingText = errorResId?.let {
                        { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.manage_applications_reject))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun QuotaWarningConfirmDialog(
    approvedCount: Int,
    quota: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.manage_applications_quota_warning_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.manage_applications_quota_warning_message,
                    approvedCount,
                    quota
                )
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.manage_applications_quota_warning_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ManageApplicationsScreenPreview() {
    LocalInteractionTheme {
        ManageApplicationsScreen(
            state = ManageApplicationsState(
                eventTitle = "Прибирання парку Шевченка",
                volunteerQuota = 10,
                approvedVolunteersCount = 4,
                applications = listOf(
                    VolunteerApplication(
                        id = "app-1",
                        eventId = "event-1",
                        volunteerId = "vol-1",
                        volunteerName = "Олена Коваленко",
                        volunteerRating = 4.9,
                        motivationLetter = "Маю досвід координації роздільного збору сміття.",
                        status = ApplicationStatus.PENDING,
                        createdAt = "2026-11-08T10:00:00Z"
                    ),
                    VolunteerApplication(
                        id = "app-2",
                        eventId = "event-1",
                        volunteerId = "vol-2",
                        volunteerName = "Максим Іванов",
                        volunteerRating = 4.7,
                        volunteerPhone = "+380501234567",
                        status = ApplicationStatus.APPROVED,
                        createdAt = "2026-11-07T12:00:00Z"
                    )
                )
            ),
            onEvent = {}
        )
    }
}
