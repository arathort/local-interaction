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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.arathort.localinteraction.R
import com.arathort.localinteraction.presentation.home.HomeRoute
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
                        backStack.add(AppDestination.Profile)
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
                DestinationPlaceholderScreen(
                    title = stringResource(R.string.event_details_title),
                    subtitle = key.eventId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AppDestination.Auth> {
                DestinationPlaceholderScreen(
                    title = stringResource(R.string.auth_tab_login),
                    subtitle = stringResource(R.string.auth_select_role_label),
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AppDestination.Profile> {
                DestinationPlaceholderScreen(
                    title = stringResource(R.string.profile_title),
                    subtitle = stringResource(R.string.profile_tab_my_tasks),
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AppDestination.CreateEditEvent> {
                DestinationPlaceholderScreen(
                    title = stringResource(R.string.create_event_title),
                    subtitle = stringResource(R.string.event_form_submit_moderation),
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AppDestination.ManageApplications> { key ->
                DestinationPlaceholderScreen(
                    title = stringResource(R.string.manage_applications_title),
                    subtitle = key.eventId,
                    onBack = { backStack.removeLastOrNull() }
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
                DestinationPlaceholderScreen(
                    title = stringResource(R.string.admin_panel_title),
                    subtitle = stringResource(R.string.admin_tab_moderation),
                    onBack = { backStack.removeLastOrNull() }
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
