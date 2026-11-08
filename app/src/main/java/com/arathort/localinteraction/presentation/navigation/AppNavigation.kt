package com.arathort.localinteraction.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.arathort.localinteraction.R
import com.arathort.localinteraction.presentation.admin.AdminPanelRoute
import com.arathort.localinteraction.presentation.auth.AuthRoute
import com.arathort.localinteraction.presentation.create_event.CreateEventRoute
import com.arathort.localinteraction.presentation.event_details.EventDetailsRoute
import com.arathort.localinteraction.presentation.event_details.EventDetailsViewModel
import com.arathort.localinteraction.presentation.home.HomeRoute
import com.arathort.localinteraction.presentation.manage_applications.ManageApplicationsRoute
import com.arathort.localinteraction.presentation.manage_applications.ManageApplicationsViewModel
import com.arathort.localinteraction.presentation.profile.ProfileRoute
import com.arathort.localinteraction.presentation.profile.ProfileViewModel
import com.arathort.localinteraction.presentation.util.Dimensions

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(AppDestination.Home)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
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
                        backStack.add(AppDestination.Profile())
                    },
                    onNavigateToCreateEvent = {
                        backStack.add(AppDestination.CreateEditEvent())
                    },
                    onNavigateToAdminPanel = {
                        backStack.add(AppDestination.AdminPanel)
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
                    onNavigateToProfile = { userId ->
                        backStack.add(AppDestination.Profile(userId))
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

            entry<AppDestination.CreateEditEvent> {
                CreateEventRoute(
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
                    }
                )
            }

            entry<AppDestination.WriteReview> { key ->
                DestinationPlaceholderScreen(
                    title = stringResource(R.string.review_title),
                    subtitle = stringResource(
                        R.string.review_subtitle,
                        key.eventTitle,
                        key.targetUserName
                    ),
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

@Composable
private fun DestinationPlaceholderScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.paddingExtraLarge),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            Button(onClick = onBack) {
                Text(text = stringResource(R.string.action_back))
            }
        }
    }
}
