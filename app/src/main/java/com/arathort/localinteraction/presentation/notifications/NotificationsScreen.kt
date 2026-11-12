package com.arathort.localinteraction.presentation.notifications

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.model.NotificationType
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun NotificationsRoute(
    onNavigateToEventDetails: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NotificationsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageResId)
                    )
                }
                is NotificationsEffect.ShowSnackbarText -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
                is NotificationsEffect.NavigateToEventDetails -> {
                    onNavigateToEventDetails(effect.eventId)
                }
                NotificationsEffect.NavigateToAuth -> {
                    onNavigateToAuth()
                }
            }
        }
    }

    NotificationsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsState,
    onEvent: (NotificationsEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.notifications_title)) },
                actions = {
                    if (state.currentUser.role != UserRole.GUEST && state.notifications.any { !it.isRead }) {
                        IconButton(
                            onClick = { onEvent(NotificationsEvent.MarkAllAsReadClicked) },
                            enabled = !state.isMarkingAllAsRead
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_done_all),
                                contentDescription = stringResource(R.string.notifications_mark_all_read),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.currentUser.role == UserRole.GUEST) {
                GuestNotificationsPlaceholder(
                    onLoginClick = { onEvent(NotificationsEvent.LoginClicked) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.paddingLarge)
                )
            } else {
                NotificationsFilterRow(
                    filter = state.filter,
                    unreadCount = state.unreadCount,
                    totalCount = state.notifications.size,
                    onFilterChange = { onEvent(NotificationsEvent.FilterChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingSmall)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.progressIndicatorSize),
                            strokeWidth = Dimensions.progressIndicatorStrokeWidth
                        )
                    } else if (state.notifications.isEmpty()) {
                        EmptyNotificationsPlaceholder(
                            filter = state.filter,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimensions.paddingExtraLarge)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = Dimensions.paddingLarge,
                                vertical = Dimensions.paddingSmall
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                        ) {
                            items(
                                items = state.notifications,
                                key = { it.id }
                            ) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onClick = { onEvent(NotificationsEvent.NotificationClicked(notification)) },
                                    onMarkAsRead = { onEvent(NotificationsEvent.MarkAsReadClicked(notification.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsFilterRow(
    filter: NotificationsFilter,
    unreadCount: Int,
    totalCount: Int,
    onFilterChange: (NotificationsFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
    ) {
        FilterChip(
            selected = filter == NotificationsFilter.ALL,
            onClick = { onFilterChange(NotificationsFilter.ALL) },
            label = {
                Text(
                    text = if (totalCount > 0 && filter == NotificationsFilter.ALL) {
                        stringResource(R.string.notifications_filter_all, totalCount)
                    } else {
                        stringResource(R.string.notifications_filter_all, totalCount)
                    }
                )
            }
        )

        FilterChip(
            selected = filter == NotificationsFilter.UNREAD_ONLY,
            onClick = { onFilterChange(NotificationsFilter.UNREAD_ONLY) },
            label = {
                Text(text = stringResource(R.string.notifications_filter_unread, unreadCount))
            }
        )
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (notification.isRead) Dimensions.cardElevation else Dimensions.cardElevation + Dimensions.cardElevation
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.notificationItemPadding),
            verticalAlignment = Alignment.Top
        ) {
            NotificationTypeIcon(
                type = notification.type,
                modifier = Modifier.padding(end = Dimensions.paddingMedium)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(Dimensions.notificationUnreadDotSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!notification.relatedEntityId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                    Text(
                        text = stringResource(R.string.notifications_action_details),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!notification.isRead) {
                IconButton(
                    onClick = onMarkAsRead,
                    modifier = Modifier.size(Dimensions.avatarSmall)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_done_all),
                        contentDescription = stringResource(R.string.notifications_mark_read),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationTypeIcon(
    type: NotificationType,
    modifier: Modifier = Modifier
) {
    val (iconRes, containerColor, iconTint) = when (type) {
        NotificationType.EVENT_MODERATION -> Triple(
            R.drawable.ic_admin_panel,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        NotificationType.NEW_APPLICATION -> Triple(
            R.drawable.ic_person,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        NotificationType.APPLICATION_STATUS -> Triple(
            R.drawable.ic_done_all,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        NotificationType.APPLICATION_WITHDRAWAL -> Triple(
            R.drawable.ic_close,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        NotificationType.EVENT_STATUS_CHANGE -> Triple(
            R.drawable.ic_calendar,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        NotificationType.NEW_REVIEW -> Triple(
            R.drawable.ic_star,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        NotificationType.NEW_COMMENT -> Triple(
            R.drawable.ic_comment,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        NotificationType.ACCOUNT_SECURITY -> Triple(
            R.drawable.ic_lock_reset,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    Box(
        modifier = modifier
            .size(Dimensions.notificationIconContainerSize)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(Dimensions.notificationIconSize)
        )
    }
}

@Composable
private fun EmptyNotificationsPlaceholder(
    filter: NotificationsFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_notifications),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(Dimensions.avatarExtraLarge)
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        Text(
            text = if (filter == NotificationsFilter.UNREAD_ONLY) {
                stringResource(R.string.notifications_empty_unread)
            } else {
                stringResource(R.string.notifications_empty_title)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))

        Text(
            text = stringResource(R.string.notifications_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GuestNotificationsPlaceholder(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.cardCornerRadius)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimensions.authHeroIconContainerSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.authHeroIconSize),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

                Text(
                    text = stringResource(R.string.notifications_guest_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

                Text(
                    text = stringResource(R.string.notifications_guest_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.buttonHeight)
                ) {
                    Text(text = stringResource(R.string.auth_tab_login))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    LocalInteractionTheme {
        NotificationsScreen(
            state = NotificationsState(
                currentUser = User(id = "user-1", role = UserRole.VOLUNTEER),
                unreadCount = 1,
                notifications = listOf(
                    AppNotification(
                        id = "notif-1",
                        userId = "user-1",
                        type = NotificationType.APPLICATION_STATUS,
                        title = "Заявку схвалено",
                        message = "Організатор заходу 'Прибирання парку' підтвердив вашу участь",
                        relatedEntityId = "event-1",
                        isRead = false,
                        createdAt = "2026-11-11T10:00:00"
                    ),
                    AppNotification(
                        id = "notif-2",
                        userId = "user-1",
                        type = NotificationType.NEW_COMMENT,
                        title = "Новий коментар",
                        message = "Організатор відповів на ваш коментар",
                        relatedEntityId = "event-1",
                        isRead = true,
                        createdAt = "2026-11-10T15:30:00"
                    )
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsGuestPreview() {
    LocalInteractionTheme {
        NotificationsScreen(
            state = NotificationsState(
                currentUser = User(id = "guest", role = UserRole.GUEST)
            ),
            onEvent = {}
        )
    }
}
