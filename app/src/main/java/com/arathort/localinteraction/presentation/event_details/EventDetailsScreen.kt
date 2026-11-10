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
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.presentation.components.AppAsyncImage
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun EventDetailsRoute(
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToManageApplications: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToEditEvent: (String) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToWriteReview: ((String, String, String, String) -> Unit)? = null,
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
                is EventDetailsEffect.NavigateToEditEvent -> onNavigateToEditEvent(effect.eventId)
                is EventDetailsEffect.NavigateToProfile -> onNavigateToProfile(effect.userId)
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
        onRateOrganizer = {
            state.event?.let { ev ->
                onNavigateToWriteReview?.invoke(ev.id, ev.title, ev.organizer.id, ev.organizer.fullName.orEmpty())
            }
        },
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
    onRateOrganizer: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val canManageEvent = state.event != null &&
        ((state.currentUser.role == UserRole.ORGANIZER && state.event.organizer.id == state.currentUser.id) ||
            state.currentUser.role == UserRole.ADMIN)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.event_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (canManageEvent) {
                        IconButton(onClick = { onEvent(EventDetailsEvent.EditEventClicked) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = stringResource(R.string.action_edit)
                            )
                        }
                        IconButton(onClick = { onEvent(EventDetailsEvent.DeleteEventClicked) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.event != null) {
                EventDetailsBottomBar(
                    state = state,
                    onEvent = onEvent,
                    onRateOrganizer = onRateOrganizer
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
                    state = state,
                    event = state.event,
                    onEvent = onEvent,
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

        if (state.deletingCommentId != null) {
            DeleteCommentConfirmDialog(
                isDeleting = state.isActionLoading,
                onConfirm = { onEvent(EventDetailsEvent.ConfirmDeleteComment) },
                onDismiss = { onEvent(EventDetailsEvent.DismissDeleteCommentDialog) }
            )
        }

        if (state.showDeleteEventDialog) {
            DeleteEventConfirmDialog(
                isDeleting = state.isActionLoading,
                onConfirm = { onEvent(EventDetailsEvent.ConfirmDeleteEvent) },
                onDismiss = { onEvent(EventDetailsEvent.DismissDeleteEventDialog) }
            )
        }

        if (state.selectedPhotoUrlForPreview != null) {
            FullScreenPhotoPreviewDialog(
                photoUrl = state.selectedPhotoUrlForPreview,
                onDismiss = { onEvent(EventDetailsEvent.DismissPhotoPreview) }
            )
        }
    }
}

@Composable
private fun EventDetailsContent(
    state: EventDetailsState,
    event: Event,
    onEvent: (EventDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onOrganizerClicked: () -> Unit = {}
) {
    val isOrganizerOwner = state.currentUser.role == UserRole.ORGANIZER &&
        event.organizer.id == state.currentUser.id

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge)
    ) {
        if (!event.rejectionReason.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                ) {
                    Text(
                        text = stringResource(R.string.event_details_rejection_reason_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(
                            R.string.event_details_rejection_reason_format,
                            event.rejectionReason
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    if (isOrganizerOwner) {
                        OutlinedButton(
                            onClick = { onEvent(EventDetailsEvent.EditEventClicked) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spacingExtraSmall))
                            Text(text = stringResource(R.string.event_details_edit_rejected_action))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
        }

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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                modifier = Modifier.size(Dimensions.iconSmall),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = event.location.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
                        onClick = { onEvent(EventDetailsEvent.PhotoClicked(photoUrl)) },
                        modifier = Modifier.size(Dimensions.eventDetailsPhotoSize),
                        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
                    ) {
                        AppAsyncImage(
                            model = photoUrl,
                            contentDescription = event.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        EventCommentsSection(
            state = state,
            organizerId = event.organizer.id,
            onEvent = onEvent
        )

        Spacer(modifier = Modifier.height(Dimensions.authHeaderSpacing))
    }
}

@Composable
private fun EventCommentsSection(
    state: EventDetailsState,
    organizerId: String,
    onEvent: (EventDetailsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val commentsById = remember(state.comments) {
        state.comments.associateBy { it.id }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
    ) {
        Text(
            text = stringResource(R.string.event_details_comments_section, state.comments.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (state.currentUser.role == UserRole.GUEST) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                ) {
                    Text(
                        text = stringResource(R.string.event_details_comment_guest_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = { onEvent(EventDetailsEvent.SubmitCommentClicked) }) {
                        Text(text = stringResource(R.string.auth_login_button))
                    }
                }
            }
        } else if (state.event?.status != EventStatus.CANCELLED) {
            CommentInputForm(
                commentText = state.commentText,
                replyingToComment = state.replyingToComment,
                errorResId = state.commentErrorResId,
                isSubmitting = state.isSubmittingComment,
                onTextChanged = { onEvent(EventDetailsEvent.CommentTextChanged(it)) },
                onCancelReply = { onEvent(EventDetailsEvent.CancelReplyClicked) },
                onSubmit = { onEvent(EventDetailsEvent.SubmitCommentClicked) }
            )
        }

        if (state.isCommentsLoading && state.comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.paddingLarge),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.progressIndicatorSize),
                    strokeWidth = Dimensions.progressIndicatorStrokeWidth
                )
            }
        } else if (state.comments.isEmpty()) {
            Text(
                text = stringResource(R.string.event_details_empty_comments),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                state.comments.forEach { comment ->
                    val parentComment = comment.replyToCommentId?.let { commentsById[it] }
                    val canDelete = comment.authorId == state.currentUser.id ||
                        state.currentUser.role == UserRole.ADMIN
                    CommentCard(
                        comment = comment,
                        parentComment = parentComment,
                        isOrganizerAuthor = comment.authorId.isNotBlank() && comment.authorId == organizerId,
                        canReply = state.event?.status != EventStatus.CANCELLED,
                        canDelete = canDelete,
                        onAuthorClick = { onEvent(EventDetailsEvent.CommentAuthorClicked(comment.authorId)) },
                        onReplyClick = { onEvent(EventDetailsEvent.ReplyToCommentClicked(comment)) },
                        onDeleteClick = { onEvent(EventDetailsEvent.DeleteCommentClicked(comment.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInputForm(
    commentText: String,
    replyingToComment: Comment?,
    errorResId: Int?,
    isSubmitting: Boolean,
    onTextChanged: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        if (replyingToComment != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = Dimensions.paddingMedium,
                            vertical = Dimensions.paddingSmall
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_reply),
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSmall),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    R.string.event_details_comment_replying_to,
                                    replyingToComment.authorName
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = replyingToComment.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onCancelReply,
                        modifier = Modifier.size(Dimensions.iconMedium)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.action_cancel),
                            modifier = Modifier.size(Dimensions.iconSmall),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = commentText,
            onValueChange = onTextChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.event_details_comment_hint)) },
            minLines = 2,
            maxLines = 5,
            isError = errorResId != null,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = errorResId?.let { stringResource(it) }.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.review_char_counter, commentText.length, 1000),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (commentText.length > 1000) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        )

        Button(
            onClick = onSubmit,
            enabled = !isSubmitting && commentText.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.progressIndicatorSize),
                    strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                Text(text = stringResource(R.string.event_details_comment_send))
            }
        }
    }
}

@Composable
private fun CommentCard(
    comment: Comment,
    parentComment: Comment?,
    isOrganizerAuthor: Boolean,
    canReply: Boolean,
    canDelete: Boolean,
    onAuthorClick: () -> Unit,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReply = comment.replyToCommentId != null
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (isReply) Dimensions.commentReplyIndent else Dimensions.paddingExtraSmall),
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
                        .size(Dimensions.avatarSmall)
                        .clickable(onClick = onAuthorClick),
                    shape = RoundedCornerShape(Dimensions.avatarSmall),
                    color = if (isOrganizerAuthor) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    AppAsyncImage(
                        model = comment.authorAvatarUrl,
                        contentDescription = comment.authorName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        fallback = {
                            Text(
                                text = comment.authorName.trim().take(1).uppercase().ifBlank { "U" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOrganizerAuthor) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAuthorClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Text(
                            text = comment.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isOrganizerAuthor) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = stringResource(R.string.event_details_comment_organizer_badge),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    val formattedTime = comment.createdAt.take(16).replace("T", " ")
                    if (formattedTime.isNotBlank()) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(Dimensions.iconMedium)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.action_delete),
                            modifier = Modifier.size(Dimensions.iconSmall),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (parentComment != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(Dimensions.paddingSmall)) {
                        Text(
                            text = parentComment.authorName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = parentComment.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )

            if (canReply) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onReplyClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_reply),
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spacingExtraSmall))
                        Text(text = stringResource(R.string.event_details_comment_reply_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteCommentConfirmDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.event_details_comment_delete_title)) },
        text = { Text(text = stringResource(R.string.event_details_comment_delete_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun DeleteEventConfirmDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.event_delete_title)) },
        text = { Text(text = stringResource(R.string.event_delete_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text(text = stringResource(R.string.action_delete))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun FullScreenPhotoPreviewDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.event_details_photo_preview_title)) },
        text = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.photoPreviewHeight),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius)
            ) {
                AppAsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_back))
            }
        }
    )
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
                AppAsyncImage(
                    model = organizer.avatarUrl,
                    contentDescription = organizer.fullName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallback = {
                        Text(
                            text = organizer.fullName?.take(1).orEmpty().ifBlank { "O" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.width(Dimensions.spacingMedium))

            Column {
                Text(
                    text = organizer.fullName.orEmpty().ifBlank { stringResource(R.string.role_organizer) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
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
                        text = stringResource(R.string.event_details_rating_format, organizer.rating),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailsBottomBar(
    state: EventDetailsState,
    onEvent: (EventDetailsEvent) -> Unit,
    onRateOrganizer: () -> Unit = {},
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
                            } else if (myApp.status == ApplicationStatus.APPROVED &&
                                (event.status == EventStatus.IN_PROGRESS || event.status == EventStatus.COMPLETED)
                            ) {
                                Button(
                                    onClick = onRateOrganizer,
                                    enabled = !state.isActionLoading
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_star),
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSmall)
                                    )
                                    Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                                    Text(text = stringResource(R.string.review_action_rate_organizer))
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
                comments = listOf(
                    Comment(
                        id = "c-1",
                        eventId = "event-1",
                        authorId = "user-vol-1",
                        authorName = "Олена Коваленко",
                        authorRole = "Volunteer",
                        text = "Підкажіть, будь ласка, де саме біля входу в парк збираємося?",
                        createdAt = "2026-10-30T14:20:00Z"
                    ),
                    Comment(
                        id = "c-2",
                        eventId = "event-1",
                        authorId = "user-organizer-1",
                        authorName = "Тарас Мельник",
                        authorRole = "Organizer",
                        text = "Зустрічаємося біля центральної ротонди о 09:45!",
                        replyToCommentId = "c-1",
                        createdAt = "2026-10-30T14:35:00Z"
                    )
                ),
                currentUser = User(id = "user-vol-1", role = UserRole.VOLUNTEER),
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
