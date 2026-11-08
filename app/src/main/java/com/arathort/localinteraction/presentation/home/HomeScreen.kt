package com.arathort.localinteraction.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun HomeRoute(
    onNavigateToEventDetails: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToEventDetails -> onNavigateToEventDetails(effect.eventId)
                HomeEffect.NavigateToAuth -> onNavigateToAuth()
                HomeEffect.NavigateToProfile -> onNavigateToProfile()
                HomeEffect.NavigateToCreateEvent -> onNavigateToCreateEvent()
                HomeEffect.NavigateToAdminPanel -> onNavigateToAdminPanel()
            }
        }
    }

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.home_title)) },
                actions = {
                    if (state.currentUser.role == UserRole.ADMIN) {
                        IconButton(onClick = { onEvent(HomeEvent.NavigateToAdminClicked) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_admin_panel),
                                contentDescription = stringResource(R.string.admin_panel_title)
                            )
                        }
                    }
                    if (state.currentUser.role == UserRole.GUEST) {
                        TextButton(onClick = { onEvent(HomeEvent.NavigateToAuthClicked) }) {
                            Text(text = stringResource(R.string.auth_tab_login))
                        }
                    } else {
                        IconButton(onClick = { onEvent(HomeEvent.NavigateToProfileClicked) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_person),
                                contentDescription = state.currentUser.fullName
                                    .orEmpty()
                                    .ifBlank { stringResource(R.string.profile_title) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.currentUser.role == UserRole.ORGANIZER && (!state.isMapView || state.selectedMapEvent == null)) {
                ExtendedFloatingActionButton(
                    onClick = { onEvent(HomeEvent.NavigateToCreateEventClicked) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(text = stringResource(R.string.home_create_event_fab))
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimensions.paddingLarge)
        ) {
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.action_search)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                state.categories.forEach { category ->
                    FilterChip(
                        selected = category.id in state.selectedCategoryIds,
                        onClick = { onEvent(HomeEvent.CategoryToggled(category.id)) },
                        label = { Text(text = category.name) }
                    )
                }
                if (state.selectedCategoryIds.isNotEmpty() || state.searchQuery.isNotBlank()) {
                    AssistChip(
                        onClick = { onEvent(HomeEvent.ClearFilters) },
                        label = { Text(text = stringResource(R.string.action_clear_filters)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !state.isMapView,
                    onClick = { onEvent(HomeEvent.ViewModeChanged(false)) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(text = stringResource(R.string.home_view_list))
                }
                SegmentedButton(
                    selected = state.isMapView,
                    onClick = { onEvent(HomeEvent.ViewModeChanged(true)) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(text = stringResource(R.string.home_view_map))
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            if (state.isMapView) {
                HomeEventsMap(
                    events = state.events,
                    selectedEvent = state.selectedMapEvent,
                    onMarkerClick = { onEvent(HomeEvent.MapMarkerClicked(it)) },
                    onMapClick = { onEvent(HomeEvent.MapDismissPreviewClicked) },
                    onEventClick = { onEvent(HomeEvent.EventClicked(it)) },
                    onClearFilters = { onEvent(HomeEvent.ClearFilters) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                if (state.events.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.home_empty_events),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = Dimensions.paddingLarge),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                    ) {
                        items(state.events, key = { it.id }) { event ->
                            EventSummaryCard(
                                event = event,
                                onClick = { onEvent(HomeEvent.EventClicked(event.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventSummaryCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.categoryName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.event_status_open),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
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
            if (event.eventDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.eventDate.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
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
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                text = stringResource(
                    R.string.home_quota_progress,
                    event.approvedVolunteersCount,
                    event.volunteerQuota
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun HomeEventsMap(
    events: List<Event>,
    selectedEvent: Event?,
    onMarkerClick: (String) -> Unit,
    onMapClick: () -> Unit,
    onEventClick: (String) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialLatLng = remember(events) {
        val firstWithCoords = events.firstOrNull { it.location.latitude != null && it.location.longitude != null }
        if (firstWithCoords != null) {
            LatLng(firstWithCoords.location.latitude ?: 50.4501, firstWithCoords.location.longitude ?: 30.5234)
        } else {
            LatLng(50.4501, 30.5234)
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 12f)
    }

    LaunchedEffect(selectedEvent) {
        selectedEvent?.let {
            val lat = it.location.latitude
            val lng = it.location.longitude
            if (lat != null && lng != null) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lat, lng),
                        14f
                    )
                )
            }
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { onMapClick() }
        ) {
            events.forEach { event ->
                val lat = event.location.latitude
                val lng = event.location.longitude
                if (lat != null && lng != null) {
                    val markerState = rememberUpdatedMarkerState(
                        position = LatLng(lat, lng)
                    )
                    Marker(
                        state = markerState,
                        title = event.title,
                        snippet = event.categoryName,
                        onClick = {
                            onMarkerClick(event.id)
                            true
                        }
                    )
                }
            }
        }

        if (events.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(Dimensions.paddingLarge),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.paddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                ) {
                    Text(
                        text = stringResource(R.string.home_map_empty_events),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onClearFilters) {
                        Text(text = stringResource(R.string.action_clear_filters))
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedEvent != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium)
        ) {
            if (selectedEvent != null) {
                MapEventPreviewCard(
                    event = selectedEvent,
                    onOpenDetails = { onEventClick(selectedEvent.id) },
                    onDismiss = onMapClick
                )
            }
        }
    }
}

@Composable
private fun MapEventPreviewCard(
    event: Event,
    onOpenDetails: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.mapPreviewCardElevation),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.categoryName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(Dimensions.iconMedium)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.home_map_close_preview_cd),
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                }
            }

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (event.eventDate.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.eventDate.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.home_quota_progress,
                        event.approvedVolunteersCount,
                        event.volunteerQuota
                    ),
                    style = MaterialTheme.typography.labelSmall
                )

                Button(
                    onClick = onOpenDetails,
                    modifier = Modifier.height(Dimensions.buttonHeight)
                ) {
                    Text(text = stringResource(R.string.home_map_event_details_action))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    LocalInteractionTheme {
        HomeScreen(
            state = HomeState(
                currentUser = User(
                    id = "user-volunteer-1",
                    fullName = "Олена Коваленко",
                    email = "olena@ukr.net",
                    role = UserRole.VOLUNTEER
                ),
                categories = listOf(
                    Category(id = "cat-eco", name = "Екологія та довкілля"),
                    Category(id = "cat-defense", name = "Допомога ЗСУ та ВПО")
                ),
                events = listOf(
                    Event(
                        id = "event-1",
                        title = "Весняне прибирання парку «Наталка»",
                        description = "Запрошуємо мешканців Києва долучитися до великої толоки.",
                        categoryId = "cat-eco",
                        categoryName = "Екологія та довкілля",
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
                    )
                ),
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
