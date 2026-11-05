package com.arathort.localinteraction.presentation.event_details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun EventDetailsRoute(
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToManageApplications: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EventDetailsEffect.NavigateBack -> onBack()
                EventDetailsEffect.NavigateToAuth -> onNavigateToAuth()
                is EventDetailsEffect.NavigateToManageApplications -> onNavigateToManageApplications(effect.eventId)
                is EventDetailsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageResId)
                    )
                }
                is EventDetailsEffect.ShowSnackbarText -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
            }
        }
    }

    EventDetailsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClicked = onBack,
        onOrganizerClicked = { state.event?.organizer?.id?.let(onNavigateToProfile) },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    state: EventDetailsState,
    onEvent: (EventDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = { onEvent(EventDetailsEvent.BackClicked) },
    onOrganizerClicked: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.event_details_title)) },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) {
                        Text(text = stringResource(R.string.action_back))
                    }
                }
            )
        },
        bottomBar = {
            if (state.event != null) {
                EventDetailsBottomBar(
                    state = state,
                    onEvent = onEvent
                )
            }
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
                    CircularProgressIndicator()
                }
            }
            state.event == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(
                                state.errorMessageResId ?: R.string.event_details_not_found
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                        Button(onClick = { onEvent(EventDetailsEvent.Refresh) }) {
                            Text(text = stringResource(R.string.action_retry))
                        }
                    }
                }
            }
            else -> {
                EventDetailsContent(
                    event = state.event,
                    onOrganizerClicked = onOrganizerClicked,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }

        if (state.showApplyDialog) {
            ApplyDialog(state = state, onEvent = onEvent)
        }

        if (state.showStatusDialog) {
            ChangeStatusDialog(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun EventDetailsContent(
    event: Event,
    modifier: Modifier = Modifier,
    onOrganizerClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = {},
                label = { Text(text = event.categoryName.ifBlank { stringResource(R.string.home_all_categories) }) }
            )
            AssistChip(
                onClick = {},
                label = { Text(text = event.status.apiValue) }
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        Text(
            text = event.location.address,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        QuotaProgressCard(
            approvedCount = event.approvedVolunteersCount,
            quota = event.volunteerQuota
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        OrganizerCard(
            organizer = event.organizer,
            onClick = onOrganizerClicked
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

        Text(
            text = stringResource(R.string.event_details_about_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = Dimensions.lineHeightBodyLarge
        )

        if (event.photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            Text(
                text = stringResource(R.string.event_details_photos_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)) {
                items(event.photos) { photoUrl ->
                    Card(
                        modifier = Modifier.size(Dimensions.eventDetailsPhotoSize),
                        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = photoUrl.takeLast(10),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        Text(
            text = stringResource(R.string.event_details_comments_section, 0),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
        Text(
            text = stringResource(R.string.event_details_empty_comments),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimensions.authHeaderSpacing))
    }
}

@Composable
private fun QuotaProgressCard(
    approvedCount: Int,
    quota: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(Dimensions.paddingMedium)) {
            Text(
                text = stringResource(R.string.event_details_quota_format, approvedCount, quota),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
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
private fun OrganizerCard(
    organizer: OrganizerShortInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(Dimensions.avatarMedium),
                shape = RoundedCornerShape(Dimensions.avatarMedium),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = organizer.fullName?.take(1).orEmpty().ifBlank { "O" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimensions.spacingMedium))

            Column {
                Text(
                    text = organizer.fullName.orEmpty().ifBlank { stringResource(R.string.role_organizer) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.event_details_rating_format, organizer.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EventDetailsBottomBar(
    state: EventDetailsState,
    onEvent: (EventDetailsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val event = state.event ?: return
    val isOrganizerOwner = state.currentUser.role == UserRole.ORGANIZER &&
        event.organizer.id == state.currentUser.id

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = Dimensions.paddingSmall
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge)
        ) {
            when {
                state.currentUser.role == UserRole.GUEST -> {
                    Button(
                        onClick = { onEvent(EventDetailsEvent.ApplyClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.buttonHeight)
                    ) {
                        Text(text = stringResource(R.string.event_details_action_join))
                    }
                }
                isOrganizerOwner -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                    ) {
                        Button(
                            onClick = { onEvent(EventDetailsEvent.ManageApplicationsClicked) },
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimensions.buttonHeight)
                        ) {
                            Text(text = stringResource(R.string.event_details_action_manage_applications))
                        }
                        OutlinedButton(
                            onClick = { onEvent(EventDetailsEvent.ChangeStatusClicked) },
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimensions.buttonHeight)
                        ) {
                            Text(text = stringResource(R.string.event_details_action_change_status))
                        }
                    }
                }
                else -> {
                    val myApp = state.userApplication
                    if (myApp == null) {
                        Button(
                            onClick = { onEvent(EventDetailsEvent.ApplyClicked) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimensions.buttonHeight),
                            enabled = event.status == EventStatus.OPEN && !state.isActionLoading
                        ) {
                            if (state.isActionLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(Dimensions.progressIndicatorSize),
                                    strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(text = stringResource(R.string.event_details_action_join))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.event_details_my_application_status,
                                    myApp.status.apiValue
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (myApp.canWithdraw) {
                                Button(
                                    onClick = { onEvent(EventDetailsEvent.WithdrawApplicationClicked) },
                                    enabled = !state.isActionLoading
                                ) {
                                    if (state.isActionLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(Dimensions.progressIndicatorSize),
                                            strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text(text = stringResource(R.string.event_details_action_withdraw))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplyDialog(
    state: EventDetailsState,
    onEvent: (EventDetailsEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(EventDetailsEvent.DismissApplyDialog) },
        title = { Text(text = stringResource(R.string.event_details_apply_dialog_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.motivationLetter,
                    onValueChange = { onEvent(EventDetailsEvent.MotivationLetterChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.event_details_motivation_hint)) },
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEvent(EventDetailsEvent.AgreedToShareContactsChanged(!state.agreedToShareContacts))
                        }
                ) {
                    Checkbox(
                        checked = state.agreedToShareContacts,
                        onCheckedChange = { onEvent(EventDetailsEvent.AgreedToShareContactsChanged(it)) }
                    )
                    Text(
                        text = stringResource(R.string.event_details_share_contacts_consent),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = Dimensions.paddingExtraSmall)
                    )
                }

                state.applyErrorResId?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = Dimensions.paddingSmall, top = Dimensions.paddingExtraSmall)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(EventDetailsEvent.SubmitApplication) },
                enabled = !state.isActionLoading
            ) {
                if (state.isActionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(R.string.event_details_submit_application))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(EventDetailsEvent.DismissApplyDialog) }) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun ChangeStatusDialog(
    state: EventDetailsState,
    onEvent: (EventDetailsEvent) -> Unit
) {
    val allowedStatuses = listOf(
        EventStatus.IN_PROGRESS,
        EventStatus.COMPLETED,
        EventStatus.CANCELLED
    )

    AlertDialog(
        onDismissRequest = { onEvent(EventDetailsEvent.DismissStatusDialog) },
        title = { Text(text = stringResource(R.string.event_details_select_status)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                allowedStatuses.forEach { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEvent(EventDetailsEvent.NewStatusSelected(status)) }
                            .padding(vertical = Dimensions.paddingExtraSmall)
                    ) {
                        RadioButton(
                            selected = state.selectedNewStatus == status,
                            onClick = { onEvent(EventDetailsEvent.NewStatusSelected(status)) }
                        )
                        Text(
                            text = status.apiValue,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimensions.paddingSmall)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(EventDetailsEvent.SubmitStatusChange) },
                enabled = state.selectedNewStatus != null && !state.isActionLoading
            ) {
                if (state.isActionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(R.string.action_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(EventDetailsEvent.DismissStatusDialog) }) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun EventDetailsScreenPreview() {
    LocalInteractionTheme {
        EventDetailsScreen(
            state = EventDetailsState(
                event = Event(
                    id = "event-1",
                    title = "Весняне прибирання парку «Наталка»",
                    description = "Запрошуємо небайдужих мешканців міста долучитися до великої толоки. Інвентар та рукавички надаються організатором заходу.",
                    categoryId = "cat-eco",
                    categoryName = "Екологія",
                    status = EventStatus.OPEN,
                    eventDate = "2026-10-31T10:00:00Z",
                    location = GeoPoint(
                        latitude = 50.5010,
                        longitude = 30.5194,
                        address = "м. Київ, Оболонська набережна, 9"
                    ),
                    volunteerQuota = 25,
                    approvedVolunteersCount = 8,
                    organizer = OrganizerShortInfo(
                        id = "user-organizer-1",
                        fullName = "Тарас Мельник",
                        rating = 4.8
                    )
                ),
                currentUser = User(id = "user-guest", role = UserRole.GUEST),
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
