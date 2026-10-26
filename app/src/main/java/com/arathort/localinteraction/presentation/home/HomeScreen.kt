package com.arathort.localinteraction.presentation.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    if (state.currentUser.role == UserRole.GUEST) {
                        TextButton(onClick = { onEvent(HomeEvent.NavigateToAuthClicked) }) {
                            Text(text = stringResource(R.string.auth_tab_login))
                        }
                    } else {
                        TextButton(onClick = { onEvent(HomeEvent.NavigateToProfileClicked) }) {
                            Text(
                                text = state.currentUser.fullName
                                    .orEmpty()
                                    .ifBlank { stringResource(R.string.profile_title) }
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
                .padding(horizontal = Dimensions.paddingLarge)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                UserRole.entries.forEach { role ->
                    val roleLabel = when (role) {
                        UserRole.GUEST -> stringResource(R.string.role_guest)
                        UserRole.VOLUNTEER -> stringResource(R.string.role_volunteer)
                        UserRole.ORGANIZER -> stringResource(R.string.role_organizer)
                        UserRole.ADMIN -> stringResource(R.string.role_admin)
                    }
                    FilterChip(
                        selected = state.currentUser.role == role,
                        onClick = { onEvent(HomeEvent.RoleSwitched(role)) },
                        label = { Text(text = roleLabel) }
                    )
                }
            }

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
            Text(
                text = event.location.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
