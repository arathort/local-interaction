package com.arathort.localinteraction.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.UserStatus
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun AdminPanelRoute(
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminPanelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminPanelEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AdminPanelEffect.ShowMessageRes -> {
                    val resolvedMessage = if (effect.formatArgs.isEmpty()) {
                        context.getString(effect.stringResId)
                    } else {
                        context.getString(effect.stringResId, *effect.formatArgs.toTypedArray())
                    }
                    snackbarHostState.showSnackbar(resolvedMessage)
                }
                AdminPanelEffect.AccessDenied -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.admin_error_access_denied))
                    onBack()
                }
                is AdminPanelEffect.NavigateToProfile -> {
                    onNavigateToProfile(effect.userId)
                }
                AdminPanelEffect.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    AdminPanelScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    state: AdminPanelState,
    onEvent: (AdminPanelEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.admin_panel_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AdminPanelEvent.NavigateBackClicked) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                            modifier = Modifier.size(Dimensions.iconMedium)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(AdminPanelEvent.Refresh) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_admin_panel),
                            contentDescription = stringResource(R.string.admin_panel_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimensions.iconMedium)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (state.selectedTab == AdminTab.CATEGORIES) {
                FloatingActionButton(
                    onClick = { onEvent(AdminPanelEvent.OpenCreateCategoryDialog) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.admin_action_add_category),
                        modifier = Modifier.size(Dimensions.iconMedium)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = state.selectedTab == AdminTab.MODERATION,
                    onClick = { onEvent(AdminPanelEvent.SelectTab(AdminTab.MODERATION)) },
                    text = { Text(text = stringResource(R.string.admin_tab_moderation)) }
                )
                Tab(
                    selected = state.selectedTab == AdminTab.CATEGORIES,
                    onClick = { onEvent(AdminPanelEvent.SelectTab(AdminTab.CATEGORIES)) },
                    text = { Text(text = stringResource(R.string.admin_tab_categories)) }
                )
                Tab(
                    selected = state.selectedTab == AdminTab.COMPLAINTS_AND_USERS,
                    onClick = { onEvent(AdminPanelEvent.SelectTab(AdminTab.COMPLAINTS_AND_USERS)) },
                    text = { Text(text = stringResource(R.string.admin_tab_complaints)) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (state.selectedTab) {
                    AdminTab.MODERATION -> ModerationTabContent(
                        state = state,
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize()
                    )
                    AdminTab.CATEGORIES -> CategoriesTabContent(
                        state = state,
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize()
                    )
                    AdminTab.COMPLAINTS_AND_USERS -> ComplaintsAndUsersTabContent(
                        state = state,
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (state.isActionLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.progressIndicatorSize),
                            strokeWidth = Dimensions.progressIndicatorStrokeWidth
                        )
                    }
                }
            }
        }
    }

    state.eventToReject?.let { event ->
        RejectEventDialog(
            eventTitle = event.title,
            onDismiss = { onEvent(AdminPanelEvent.DismissRejectEventDialog) },
            onConfirm = { reason -> onEvent(AdminPanelEvent.ConfirmRejectEvent(reason)) }
        )
    }

    state.categoryDialogMode?.let { mode ->
        CategoryDialog(
            mode = mode,
            onDismiss = { onEvent(AdminPanelEvent.DismissCategoryDialog) },
            onConfirm = { name -> onEvent(AdminPanelEvent.ConfirmSaveCategory(name)) }
        )
    }

    state.categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { onEvent(AdminPanelEvent.DismissCategoryDialog) },
            title = { Text(text = stringResource(R.string.admin_dialog_delete_category_title)) },
            text = { Text(text = stringResource(R.string.admin_dialog_delete_category_message, category.name)) },
            confirmButton = {
                Button(
                    onClick = { onEvent(AdminPanelEvent.ConfirmDeleteCategory) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(R.string.admin_action_delete_category))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AdminPanelEvent.DismissCategoryDialog) }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    state.complaintToDeleteContent?.let {
        AlertDialog(
            onDismissRequest = { onEvent(AdminPanelEvent.DismissDeleteComplaintContentDialog) },
            title = { Text(text = stringResource(R.string.admin_dialog_delete_content_title)) },
            text = { Text(text = stringResource(R.string.admin_dialog_delete_content_message)) },
            confirmButton = {
                Button(
                    onClick = { onEvent(AdminPanelEvent.ConfirmDeleteComplaintContent) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(R.string.admin_action_delete_content))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AdminPanelEvent.DismissDeleteComplaintContentDialog) }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    state.complaintToReject?.let {
        RejectComplaintDialog(
            onDismiss = { onEvent(AdminPanelEvent.DismissRejectComplaintDialog) },
            onConfirm = { note -> onEvent(AdminPanelEvent.ConfirmRejectComplaint(note)) }
        )
    }

    state.userToBlock?.let { user ->
        BlockUserDialog(
            userName = user.fullName ?: user.email,
            onDismiss = { onEvent(AdminPanelEvent.DismissUserDialog) },
            onConfirm = { reason -> onEvent(AdminPanelEvent.ConfirmBlockUser(reason)) }
        )
    }

    state.userToUnblock?.let { user ->
        AlertDialog(
            onDismissRequest = { onEvent(AdminPanelEvent.DismissUserDialog) },
            title = { Text(text = stringResource(R.string.admin_dialog_unblock_user_title)) },
            text = { Text(text = stringResource(R.string.admin_dialog_unblock_user_message, user.fullName ?: user.email)) },
            confirmButton = {
                Button(onClick = { onEvent(AdminPanelEvent.ConfirmUnblockUser) }) {
                    Text(text = stringResource(R.string.admin_action_unblock_user))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AdminPanelEvent.DismissUserDialog) }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun ModerationTabContent(
    state: AdminPanelState,
    onEvent: (AdminPanelEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.progressIndicatorSize),
                strokeWidth = Dimensions.progressIndicatorStrokeWidth
            )
        }
    } else if (state.pendingEvents.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.admin_empty_pending_events),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            items(state.pendingEvents, key = { it.id }) { event ->
                PendingEventCard(
                    event = event,
                    isExpanded = state.expandedEventId == event.id,
                    onToggleExpand = { onEvent(AdminPanelEvent.ToggleEventDetails(event.id)) },
                    onApprove = { onEvent(AdminPanelEvent.ApproveEvent(event.id)) },
                    onReject = { onEvent(AdminPanelEvent.OpenRejectEventDialog(event)) }
                )
            }
        }
    }
}

@Composable
private fun PendingEventCard(
    event: Event,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                text = event.eventDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                text = event.location.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.categoryName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = event.progressText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (event.organizer.fullName?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                    Text(
                        text = stringResource(R.string.event_details_organizer_section) + ": " + event.organizer.fullName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onToggleExpand) {
                    Text(
                        text = if (isExpanded) {
                            stringResource(R.string.action_cancel)
                        } else {
                            stringResource(R.string.admin_action_view_details)
                        }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(R.string.admin_action_reject_event))
                }
                Button(onClick = onApprove) {
                    Text(text = stringResource(R.string.admin_action_publish))
                }
            }
        }
    }
}

@Composable
private fun CategoriesTabContent(
    state: AdminPanelState,
    onEvent: (AdminPanelEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.progressIndicatorSize),
                strokeWidth = Dimensions.progressIndicatorStrokeWidth
            )
        }
    } else if (state.categories.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.admin_empty_categories),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            items(state.categories, key = { it.id }) { category ->
                CategoryItemCard(
                    category = category,
                    onEdit = { onEvent(AdminPanelEvent.OpenEditCategoryDialog(category)) },
                    onDelete = { onEvent(AdminPanelEvent.OpenDeleteCategoryDialog(category)) }
                )
            }
        }
    }
}

@Composable
private fun CategoryItemCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                    Text(
                        text = if (category.isArchived) {
                            stringResource(R.string.admin_category_status_archived)
                        } else {
                            stringResource(R.string.admin_category_status_active)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (category.isArchived) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
                Text(
                    text = stringResource(R.string.admin_category_bound_events, category.boundEventsCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (category.totalEventsCount > 0) {
                    Text(
                        text = stringResource(R.string.admin_category_total_events, category.totalEventsCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.admin_action_edit_category),
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.admin_action_delete_category),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplaintsAndUsersTabContent(
    state: AdminPanelState,
    onEvent: (AdminPanelEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            FilterChip(
                selected = state.complaintsUsersSubTab == ComplaintsUsersSubTab.COMPLAINTS,
                onClick = { onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.COMPLAINTS)) },
                label = { Text(text = stringResource(R.string.admin_subtab_complaints)) }
            )
            FilterChip(
                selected = state.complaintsUsersSubTab == ComplaintsUsersSubTab.USERS,
                onClick = { onEvent(AdminPanelEvent.SelectComplaintsUsersSubTab(ComplaintsUsersSubTab.USERS)) },
                label = { Text(text = stringResource(R.string.admin_subtab_users)) }
            )
        }

        when (state.complaintsUsersSubTab) {
            ComplaintsUsersSubTab.COMPLAINTS -> ComplaintsSubTabContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize()
            )
            ComplaintsUsersSubTab.USERS -> UsersSubTabContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ComplaintsSubTabContent(
    state: AdminPanelState,
    onEvent: (AdminPanelEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingExtraSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            FilterChip(
                selected = state.complaintFilter == ComplaintFilter.ALL,
                onClick = { onEvent(AdminPanelEvent.FilterComplaints(ComplaintFilter.ALL)) },
                label = { Text(text = stringResource(R.string.admin_filter_all)) }
            )
            FilterChip(
                selected = state.complaintFilter == ComplaintFilter.PENDING,
                onClick = { onEvent(AdminPanelEvent.FilterComplaints(ComplaintFilter.PENDING)) },
                label = { Text(text = stringResource(R.string.admin_filter_pending)) }
            )
            FilterChip(
                selected = state.complaintFilter == ComplaintFilter.REJECTED,
                onClick = { onEvent(AdminPanelEvent.FilterComplaints(ComplaintFilter.REJECTED)) },
                label = { Text(text = stringResource(R.string.admin_filter_rejected)) }
            )
            FilterChip(
                selected = state.complaintFilter == ComplaintFilter.RESOLVED,
                onClick = { onEvent(AdminPanelEvent.FilterComplaints(ComplaintFilter.RESOLVED)) },
                label = { Text(text = stringResource(R.string.admin_filter_resolved)) }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.progressIndicatorSize),
                    strokeWidth = Dimensions.progressIndicatorStrokeWidth
                )
            }
        } else if (state.complaints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.admin_empty_complaints),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimensions.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                items(state.complaints, key = { it.id }) { complaint ->
                    ComplaintCard(
                        complaint = complaint,
                        onDeleteContent = { onEvent(AdminPanelEvent.OpenDeleteComplaintContentDialog(complaint)) },
                        onRejectComplaint = { onEvent(AdminPanelEvent.OpenRejectComplaintDialog(complaint)) },
                        onBlockUser = {
                            val userToBlock = User(
                                id = complaint.violatorId,
                                fullName = complaint.violatorName,
                                email = ""
                            )
                            onEvent(AdminPanelEvent.OpenBlockUserDialog(userToBlock, complaint.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplaintCard(
    complaint: Complaint,
    onDeleteContent: () -> Unit,
    onRejectComplaint: () -> Unit,
    onBlockUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.admin_complaint_target, complaint.targetSummary, complaint.targetType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = complaint.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (complaint.isResolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            if (complaint.violationCategory.isNotBlank()) {
                Text(
                    text = stringResource(R.string.admin_complaint_category, complaint.violationCategory),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                text = complaint.reason,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
            Text(
                text = stringResource(R.string.admin_complaint_reporter, complaint.reporterName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (complaint.violatorName.isNotBlank()) {
                Text(
                    text = stringResource(R.string.admin_complaint_violator, complaint.violatorName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (complaint.adminNote?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
                Text(
                    text = stringResource(R.string.admin_complaint_note, complaint.adminNote),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!complaint.isResolved && !complaint.status.equals("Rejected", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onRejectComplaint) {
                        Text(text = stringResource(R.string.admin_action_reject_complaint))
                    }
                    Button(
                        onClick = onDeleteContent,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(R.string.admin_action_delete_content))
                    }
                    if (complaint.violatorId.isNotBlank()) {
                        IconButton(onClick = onBlockUser) {
                            Icon(
                                painter = painterResource(R.drawable.ic_block),
                                contentDescription = stringResource(R.string.admin_action_block_user),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UsersSubTabContent(
    state: AdminPanelState,
    onEvent: (AdminPanelEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.userSearchQuery,
            onValueChange = { onEvent(AdminPanelEvent.SearchUsers(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingSmall),
            placeholder = { Text(text = stringResource(R.string.admin_user_search_hint)) },
            singleLine = true
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            FilterChip(
                selected = state.userRoleFilter == UserRoleFilter.ALL,
                onClick = { onEvent(AdminPanelEvent.FilterUsersByRole(UserRoleFilter.ALL)) },
                label = { Text(text = stringResource(R.string.admin_filter_all)) }
            )
            FilterChip(
                selected = state.userRoleFilter == UserRoleFilter.VOLUNTEER,
                onClick = { onEvent(AdminPanelEvent.FilterUsersByRole(UserRoleFilter.VOLUNTEER)) },
                label = { Text(text = stringResource(R.string.admin_filter_volunteer)) }
            )
            FilterChip(
                selected = state.userRoleFilter == UserRoleFilter.ORGANIZER,
                onClick = { onEvent(AdminPanelEvent.FilterUsersByRole(UserRoleFilter.ORGANIZER)) },
                label = { Text(text = stringResource(R.string.admin_filter_organizer)) }
            )
            FilterChip(
                selected = state.userStatusFilter == UserStatusFilter.ACTIVE,
                onClick = {
                    val newFilter = if (state.userStatusFilter == UserStatusFilter.ACTIVE) UserStatusFilter.ALL else UserStatusFilter.ACTIVE
                    onEvent(AdminPanelEvent.FilterUsersByStatus(newFilter))
                },
                label = { Text(text = stringResource(R.string.admin_filter_active)) }
            )
            FilterChip(
                selected = state.userStatusFilter == UserStatusFilter.BLOCKED,
                onClick = {
                    val newFilter = if (state.userStatusFilter == UserStatusFilter.BLOCKED) UserStatusFilter.ALL else UserStatusFilter.BLOCKED
                    onEvent(AdminPanelEvent.FilterUsersByStatus(newFilter))
                },
                label = { Text(text = stringResource(R.string.admin_filter_blocked)) }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.progressIndicatorSize),
                    strokeWidth = Dimensions.progressIndicatorStrokeWidth
                )
            }
        } else if (state.filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.admin_empty_users),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimensions.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                items(state.filteredUsers, key = { it.id }) { user ->
                    AdminUserCard(
                        user = user,
                        onViewProfile = { onEvent(AdminPanelEvent.ViewUserProfile(user.id)) },
                        onBlock = { onEvent(AdminPanelEvent.OpenBlockUserDialog(user)) },
                        onUnblock = { onEvent(AdminPanelEvent.OpenUnblockUserDialog(user)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: User,
    onViewProfile: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.fullName ?: user.email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (user.status) {
                        UserStatus.ACTIVE -> stringResource(R.string.admin_filter_active)
                        UserStatus.BLOCKED -> stringResource(R.string.admin_filter_blocked)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.status == UserStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (user.role) {
                        UserRole.VOLUNTEER -> stringResource(R.string.role_volunteer)
                        UserRole.ORGANIZER -> stringResource(R.string.role_organizer)
                        UserRole.ADMIN -> stringResource(R.string.role_admin)
                        UserRole.GUEST -> stringResource(R.string.role_guest)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (user.rating > 0.0) {
                    Text(
                        text = "★ ${user.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (user.status == UserStatus.BLOCKED && user.blockReason?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                Text(
                    text = stringResource(R.string.admin_user_block_reason, user.blockReason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onViewProfile) {
                    Text(text = stringResource(R.string.admin_action_view_profile))
                }
                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                if (user.status == UserStatus.ACTIVE) {
                    OutlinedButton(
                        onClick = onBlock,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = stringResource(R.string.admin_action_block_user))
                    }
                } else {
                    Button(onClick = onUnblock) {
                        Text(text = stringResource(R.string.admin_action_unblock_user))
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectEventDialog(
    eventTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.admin_dialog_reject_event_title)) },
        text = {
            Column {
                Text(
                    text = eventTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(text = stringResource(R.string.admin_reject_reason_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.admin_action_reject_event))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun CategoryDialog(
    mode: CategoryDialogMode,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialName = when (mode) {
        CategoryDialogMode.Create -> ""
        is CategoryDialogMode.Edit -> mode.category.name
    }
    var name by remember { mutableStateOf(initialName) }

    val title = when (mode) {
        CategoryDialogMode.Create -> stringResource(R.string.admin_dialog_create_category_title)
        is CategoryDialogMode.Edit -> stringResource(R.string.admin_dialog_edit_category_title)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.admin_category_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text(text = stringResource(R.string.admin_action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun RejectComplaintDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.admin_dialog_reject_complaint_title)) },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(text = stringResource(R.string.admin_dialog_reject_complaint_note_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(note.takeIf { it.isNotBlank() }) }) {
                Text(text = stringResource(R.string.admin_action_reject_complaint))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun BlockUserDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.admin_dialog_block_user_title)) },
        text = {
            Column {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(text = stringResource(R.string.admin_block_reason_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.admin_action_block_user))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview
@Composable
private fun AdminPanelScreenPreview() {
    LocalInteractionTheme {
        AdminPanelScreen(
            state = AdminPanelState(
                selectedTab = AdminTab.MODERATION,
                pendingEvents = listOf(
                    Event(
                        id = "1",
                        title = "Прибирання парку",
                        eventDate = "15.11.2026 10:00",
                        location = GeoPoint(address = "м. Київ", latitude = 50.45, longitude = 30.52),
                        volunteerQuota = 10,
                        description = "Масштабне прибирання території центрального міського парку.",
                        categoryName = "Екологія",
                        progressText = "Підтверджено: 3 з 10"
                    )
                )
            ),
            onEvent = {}
        )
    }
}
