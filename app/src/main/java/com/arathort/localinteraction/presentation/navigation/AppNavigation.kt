package com.arathort.localinteraction.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.arathort.localinteraction.R
import com.arathort.localinteraction.presentation.admin.AdminPanelRoute
import com.arathort.localinteraction.presentation.auth.AuthRoute
import com.arathort.localinteraction.presentation.create_event.CreateEventRoute
import com.arathort.localinteraction.presentation.create_event.CreateEventViewModel
import com.arathort.localinteraction.presentation.event_details.EventDetailsRoute
import com.arathort.localinteraction.presentation.event_details.EventDetailsViewModel
import com.arathort.localinteraction.presentation.home.HomeRoute
import com.arathort.localinteraction.presentation.manage_applications.ManageApplicationsRoute
import com.arathort.localinteraction.presentation.manage_applications.ManageApplicationsViewModel
import com.arathort.localinteraction.presentation.notifications.NotificationsRoute
import com.arathort.localinteraction.presentation.profile.ProfileRoute
import com.arathort.localinteraction.presentation.profile.ProfileViewModel
import com.arathort.localinteraction.presentation.review.WriteReviewRoute
import com.arathort.localinteraction.presentation.review.WriteReviewViewModel

private enum class TopLevelDestination(
    @param:StringRes val labelResId: Int,
    @param:DrawableRes val iconResId: Int
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    NOTIFICATIONS(R.string.nav_notifications, R.drawable.ic_notifications),
    PROFILE(R.string.nav_profile, R.drawable.ic_person)
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navViewModel: MainNavigationViewModel = hiltViewModel()
) {
    val backStack = rememberNavBackStack(AppDestination.Home)
    val unreadCount by navViewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    val currentDestination = backStack.lastOrNull()
    val isTopLevelDestination = currentDestination is AppDestination.Home ||
        currentDestination is AppDestination.Notifications ||
        (currentDestination is AppDestination.Profile && currentDestination.userId == null)

    val selectedTopLevelDestination = when {
        currentDestination is AppDestination.Home -> TopLevelDestination.HOME
        currentDestination is AppDestination.Notifications -> TopLevelDestination.NOTIFICATIONS
        currentDestination is AppDestination.Profile && currentDestination.userId == null -> TopLevelDestination.PROFILE
        else -> null
    }

    val onSelectTopLevelDestination: (TopLevelDestination) -> Unit = { target ->
        when (target) {
            TopLevelDestination.HOME -> {
                while (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
                if (backStack.isEmpty() || backStack.lastOrNull() !is AppDestination.Home) {
                    backStack.add(AppDestination.Home)
                }
            }
            TopLevelDestination.NOTIFICATIONS -> {
                if (backStack.lastOrNull() !is AppDestination.Notifications) {
                    while (backStack.size > 1 && backStack.lastOrNull() !is AppDestination.Home) {
                        backStack.removeLastOrNull()
                    }
                    backStack.add(AppDestination.Notifications)
                }
            }
            TopLevelDestination.PROFILE -> {
                val current = backStack.lastOrNull()
                if (current !is AppDestination.Profile || current.userId != null) {
                    while (backStack.size > 1 && backStack.lastOrNull() !is AppDestination.Home) {
                        backStack.removeLastOrNull()
                    }
                    backStack.add(AppDestination.Profile(userId = null))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val isSelected = destination == selectedTopLevelDestination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onSelectTopLevelDestination(destination) },
                            icon = {
                                if (destination == TopLevelDestination.NOTIFICATIONS && unreadCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge {
                                                Text(
                                                    text = if (unreadCount > 99) "99+" else unreadCount.toString()
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(destination.iconResId),
                                            contentDescription = stringResource(destination.labelResId)
                                        )
                                    }
                                } else {
                                    Icon(
                                        painter = painterResource(destination.iconResId),
                                        contentDescription = stringResource(destination.labelResId)
                                    )
                                }
                            },
                            label = {
                                Text(text = stringResource(destination.labelResId))
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<AppDestination.Home> {
                    HomeRoute(
                        onNavigateToEventDetails = { eventId ->
                            backStack.add(AppDestination.EventDetails(eventId))
                        },
                        onNavigateToAuth = {
                            backStack.add(AppDestination.Auth)
                        },
                        onNavigateToProfile = {
                            onSelectTopLevelDestination(TopLevelDestination.PROFILE)
                        },
                        onNavigateToCreateEvent = {
                            backStack.add(AppDestination.CreateEditEvent())
                        },
                        onNavigateToAdminPanel = {
                            backStack.add(AppDestination.AdminPanel)
                        }
                    )
                }

                entry<AppDestination.Notifications> {
                    NotificationsRoute(
                        onNavigateToEventDetails = { eventId ->
                            backStack.add(AppDestination.EventDetails(eventId))
                        },
                        onNavigateToAuth = {
                            backStack.add(AppDestination.Auth)
                        }
                    )
                }

                entry<AppDestination.EventDetails> { key ->
                    val viewModel = hiltViewModel<EventDetailsViewModel, EventDetailsViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.eventId) }
                    )
                    EventDetailsRoute(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToAuth = { backStack.add(AppDestination.Auth) },
                        onNavigateToManageApplications = { eventId ->
                            backStack.add(AppDestination.ManageApplications(eventId))
                        },
                        onNavigateToEditEvent = { eventId ->
                            backStack.add(AppDestination.CreateEditEvent(eventId))
                        },
                        onNavigateToProfile = { userId ->
                            backStack.add(AppDestination.Profile(userId))
                        },
                        onNavigateToWriteReview = { eventId, eventTitle, targetUserId, targetUserName ->
                            backStack.add(
                                AppDestination.WriteReview(
                                    eventId = eventId,
                                    eventTitle = eventTitle,
                                    targetUserId = targetUserId,
                                    targetUserName = targetUserName
                                )
                            )
                        }
                    )
                }

                entry<AppDestination.Auth> {
                    AuthRoute(
                        onSuccess = { backStack.removeLastOrNull() },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<AppDestination.Profile> { key ->
                    val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.userId) }
                    )
                    ProfileRoute(
                        viewModel = viewModel,
                        userId = key.userId,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToAuth = { backStack.add(AppDestination.Auth) },
                        onNavigateToEventDetails = { eventId ->
                            backStack.add(AppDestination.EventDetails(eventId))
                        },
                        onNavigateToAdminPanel = {
                            backStack.add(AppDestination.AdminPanel)
                        }
                    )
                }

                entry<AppDestination.CreateEditEvent> { key ->
                    val viewModel = hiltViewModel<CreateEventViewModel, CreateEventViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.eventId) }
                    )
                    CreateEventRoute(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToEventDetails = { eventId ->
                            backStack.add(AppDestination.EventDetails(eventId))
                        }
                    )
                }

                entry<AppDestination.ManageApplications> { key ->
                    val viewModel = hiltViewModel<ManageApplicationsViewModel, ManageApplicationsViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.eventId) }
                    )
                    ManageApplicationsRoute(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToVolunteerProfile = { volunteerId ->
                            backStack.add(AppDestination.Profile(volunteerId))
                        },
                        onNavigateToWriteReview = { eventId, eventTitle, volunteerId, volunteerName ->
                            backStack.add(
                                AppDestination.WriteReview(
                                    eventId = eventId,
                                    eventTitle = eventTitle,
                                    targetUserId = volunteerId,
                                    targetUserName = volunteerName
                                )
                            )
                        }
                    )
                }

                entry<AppDestination.WriteReview> { key ->
                    val viewModel = hiltViewModel<WriteReviewViewModel, WriteReviewViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(
                                eventId = key.eventId,
                                eventTitle = key.eventTitle,
                                targetUserId = key.targetUserId,
                                targetUserName = key.targetUserName
                            )
                        }
                    )
                    WriteReviewRoute(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<AppDestination.AdminPanel> {
                    AdminPanelRoute(
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToProfile = { userId ->
                            backStack.add(AppDestination.Profile(userId))
                        }
                    )
                }
            }
        )
    }
}
