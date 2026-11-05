package com.arathort.localinteraction.presentation.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.MyTaskHistoryItem
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.UserStatus
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun ProfileRoute(
    userId: String? = null,
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToEventDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageResId)
                    )
                }
                ProfileEffect.NavigateToAuth -> onNavigateToAuth()
                is ProfileEffect.NavigateToEventDetails -> onNavigateToEventDetails(effect.eventId)
                ProfileEffect.NavigateBack -> onBack()
            }
        }
    }

    ProfileScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        onBackClicked = onBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = { onEvent(ProfileEvent.BackClicked) },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.profile_title)) },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) {
                        Text(text = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (state.isCurrentUser && !state.isGuest && state.user != null) {
                        TextButton(onClick = { onEvent(ProfileEvent.EditProfileClicked) }) {
                            Text(text = stringResource(R.string.profile_edit_action))
                        }
                        TextButton(onClick = { onEvent(ProfileEvent.LogoutClicked) }) {
                            Text(
                                text = stringResource(R.string.profile_logout_action),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
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
            state.isGuest -> {
                GuestProfileContent(
                    onLoginClicked = { onEvent(ProfileEvent.LoginClicked) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            state.user != null -> {
                val user = state.user
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = Dimensions.paddingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                ) {
                    item {
                        ProfileHeader(user = user)
                    }

                    item {
                        val availableTabs = buildList {
                            add(ProfileTab.INFO)
                            add(ProfileTab.REVIEWS)
                            if (state.isCurrentUser && user.role == UserRole.VOLUNTEER) {
                                add(ProfileTab.TASKS)
                            } else if (user.role == UserRole.ORGANIZER) {
                                add(ProfileTab.TASKS)
                            }
                        }

                        val selectedTabIndex = availableTabs.indexOf(state.selectedTab).coerceAtLeast(0)

                        PrimaryTabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableTabs.forEach { tab ->
                                val tabTitle = when (tab) {
                                    ProfileTab.INFO -> stringResource(R.string.profile_tab_info)
                                    ProfileTab.REVIEWS -> stringResource(R.string.profile_reviews_section)
                                    ProfileTab.TASKS -> {
                                        if (user.role == UserRole.ORGANIZER) {
                                            stringResource(R.string.profile_tab_my_events)
                                        } else {
                                            stringResource(R.string.profile_tab_my_tasks)
                                        }
                                    }
                                }
                                Tab(
                                    selected = state.selectedTab == tab,
                                    onClick = { onEvent(ProfileEvent.SelectTab(tab)) },
                                    text = { Text(text = tabTitle) }
                                )
                            }
                        }
                    }

                    when (state.selectedTab) {
                        ProfileTab.INFO -> {
                            item {
                                ProfileInfoSection(
                                    user = user,
                                    isCurrentUser = state.isCurrentUser
                                )
                            }
                        }
                        ProfileTab.REVIEWS -> {
                            if (user.reviews.isEmpty()) {
                                item {
                                    EmptyStateCard(message = stringResource(R.string.profile_no_reviews))
                                }
                            } else {
                                items(user.reviews) { review ->
                                    ProfileReviewCard(review = review)
                                }
                            }
                        }
                        ProfileTab.TASKS -> {
                            if (user.role == UserRole.ORGANIZER) {
                                if (state.organizerEvents.isEmpty()) {
                                    item {
                                        EmptyStateCard(message = stringResource(R.string.profile_my_events_empty))
                                    }
                                } else {
                                    items(state.organizerEvents) { event ->
                                        OrganizerEventCard(
                                            event = event,
                                            onClick = { onEvent(ProfileEvent.EventClicked(event.id)) }
                                        )
                                    }
                                }
                            } else {
                                if (user.myTasks.isEmpty()) {
                                    item {
                                        EmptyStateCard(message = stringResource(R.string.profile_my_applications_empty))
                                    }
                                } else {
                                    items(user.myTasks) { task ->
                                        VolunteerTaskCard(
                                            task = task,
                                            onCardClick = { onEvent(ProfileEvent.EventClicked(task.eventId)) },
                                            onWithdrawClick = { onEvent(ProfileEvent.WithdrawTaskClicked(task.applicationId)) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
                    }
                }
            }
        }
    }

    if (state.isEditProfileDialogVisible) {
        EditProfileDialog(
            fullName = state.editFullName,
            phone = state.editPhone,
            bio = state.editBio,
            isSaving = state.isSavingProfile,
            errorMessageResId = state.errorMessageResId,
            onFullNameChanged = { onEvent(ProfileEvent.EditFullNameChanged(it)) },
            onPhoneChanged = { onEvent(ProfileEvent.EditPhoneChanged(it)) },
            onBioChanged = { onEvent(ProfileEvent.EditBioChanged(it)) },
            onSave = { onEvent(ProfileEvent.SaveProfileClicked) },
            onDismiss = { onEvent(ProfileEvent.DismissEditProfileDialog) }
        )
    }

    if (state.isLogoutDialogVisible) {
        LogoutConfirmDialog(
            onConfirm = { onEvent(ProfileEvent.ConfirmLogoutClicked) },
            onDismiss = { onEvent(ProfileEvent.DismissLogoutDialog) }
        )
    }

    if (state.withdrawConfirmationApplicationId != null) {
        WithdrawConfirmDialog(
            isWithdrawing = state.isWithdrawing,
            onConfirm = { onEvent(ProfileEvent.ConfirmWithdrawTaskClicked) },
            onDismiss = { onEvent(ProfileEvent.DismissWithdrawDialog) }
        )
    }
}

@Composable
private fun GuestProfileContent(
    onLoginClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Dimensions.paddingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile_guest_prompt_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
        Text(
            text = stringResource(R.string.profile_guest_prompt_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingExtraLarge))
        Button(
            onClick = onLoginClicked,
            modifier = Modifier.height(Dimensions.buttonHeight)
        ) {
            Text(text = stringResource(R.string.auth_login_button))
        }
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(Dimensions.avatarExtraLarge),
                shape = RoundedCornerShape(Dimensions.avatarExtraLarge),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = user.fullName?.trim()?.take(1).orEmpty().ifBlank {
                        user.email.take(1).uppercase()
                    }
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            Text(
                text = user.fullName?.ifBlank { null } ?: user.email,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = user.role.apiValue) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = user.status.apiValue) }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                val ratingFormatted = if (user.rating > 0.0) {
                    stringResource(R.string.event_details_rating_format, user.rating)
                } else {
                    "—"
                }
                Text(
                    text = ratingFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.profile_rating_count_format, user.reviews.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoSection(
    user: User,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            if (isCurrentUser && user.email.isNotBlank()) {
                InfoItem(
                    label = stringResource(R.string.profile_email_label),
                    value = user.email
                )
            }

            InfoItem(
                label = stringResource(R.string.profile_phone_label),
                value = user.phone?.ifBlank { null } ?: stringResource(R.string.profile_empty_phone)
            )

            InfoItem(
                label = stringResource(R.string.profile_bio_label),
                value = user.description?.ifBlank { null } ?: stringResource(R.string.profile_empty_bio)
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProfileReviewCard(
    review: Review,
    modifier: Modifier = Modifier
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
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.event_details_rating_format, review.rating.toDouble()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!review.comment.isNullOrBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = review.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VolunteerTaskCard(
    task: MyTaskHistoryItem,
    onCardClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCardClick,
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
            Text(
                text = task.eventTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = task.eventStatus.apiValue) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = task.applicationStatus.apiValue) }
                )
            }

            Text(
                text = stringResource(R.string.profile_applied_at_format, task.appliedAt.take(10)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (task.canWithdraw && task.applicationStatus != ApplicationStatus.WITHDRAWN) {
                OutlinedButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(R.string.profile_withdraw_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizerEventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
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
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = event.categoryName) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = event.status.apiValue) }
                )
            }

            Text(
                text = stringResource(
                    R.string.event_details_quota_format,
                    event.approvedVolunteersCount,
                    event.volunteerQuota
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = {
                    if (event.volunteerQuota > 0) {
                        (event.approvedVolunteersCount.toFloat() / event.volunteerQuota.toFloat()).coerceIn(0f, 1f)
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
private fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingExtraLarge),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    fullName: String,
    phone: String,
    bio: String,
    isSaving: Boolean,
    errorMessageResId: Int?,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.profile_edit_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChanged,
                    label = { Text(text = stringResource(R.string.profile_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChanged,
                    label = { Text(text = stringResource(R.string.profile_phone_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = onBioChanged,
                    label = { Text(text = stringResource(R.string.profile_bio_label)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessageResId != null) {
                    Text(
                        text = stringResource(errorMessageResId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth
                    )
                } else {
                    Text(text = stringResource(R.string.profile_save_action))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(text = stringResource(R.string.profile_cancel_action))
            }
        }
    )
}

@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.profile_logout_confirm_title)) },
        text = { Text(text = stringResource(R.string.profile_logout_confirm_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(
                    text = stringResource(R.string.profile_logout_action),
                    color = MaterialTheme.colorScheme.onError
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_cancel_action))
            }
        }
    )
}

@Composable
private fun WithdrawConfirmDialog(
    isWithdrawing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.profile_withdraw_task_title)) },
        text = { Text(text = stringResource(R.string.profile_withdraw_task_confirm)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isWithdrawing
            ) {
                if (isWithdrawing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth
                    )
                } else {
                    Text(text = stringResource(R.string.profile_withdraw_action))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isWithdrawing
            ) {
                Text(text = stringResource(R.string.profile_cancel_action))
            }
        }
    )
}

@Preview
@Composable
private fun ProfileScreenVolunteerPreview() {
    LocalInteractionTheme {
        ProfileScreen(
            state = ProfileState(
                user = User(
                    id = "u-vol",
                    role = UserRole.VOLUNTEER,
                    fullName = "Оксана Петренко",
                    email = "oxana@example.com",
                    phone = "+380671234567",
                    description = "Активна волонтерка з досвідом у притулках.",
                    rating = 4.9,
                    status = UserStatus.ACTIVE,
                    reviews = listOf(
                        Review(
                            id = "rev-1",
                            eventId = "ev-1",
                            authorId = "u-org",
                            authorName = "Благодійний фонд",
                            rating = 5,
                            comment = "Чудова допомога, дуже відповідальна!",
                            createdAt = "2026-11-01T10:00:00Z"
                        )
                    ),
                    myTasks = listOf(
                        MyTaskHistoryItem(
                            applicationId = "app-1",
                            eventId = "ev-1",
                            eventTitle = "Прибирання міського парку",
                            eventStatus = EventStatus.OPEN,
                            applicationStatus = ApplicationStatus.APPROVED,
                            canWithdraw = true,
                            appliedAt = "2026-11-02T12:00:00Z"
                        )
                    )
                ),
                isCurrentUser = true,
                selectedTab = ProfileTab.INFO
            ),
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun ProfileScreenGuestPreview() {
    LocalInteractionTheme {
        ProfileScreen(
            state = ProfileState(
                isGuest = true,
                isCurrentUser = true
            ),
            onEvent = {}
        )
    }
}
