package com.arathort.localinteraction.presentation.create_event

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.presentation.components.AppAsyncImage
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateEventRoute(
    onBack: () -> Unit,
    onNavigateToEventDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreateEventEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageResId)
                    )
                }
                is CreateEventEffect.NavigateToEventDetails -> onNavigateToEventDetails(effect.eventId)
                CreateEventEffect.NavigateBack -> onBack()
            }
        }
    }

    CreateEventScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        onBackClicked = onBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEventScreen(
    state: CreateEventState,
    onEvent: (CreateEventEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = { onEvent(CreateEventEvent.BackClicked) },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scrollState = rememberScrollState()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onEvent(CreateEventEvent.PhotosSelected(uris.map { it.toString() }))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.isEditMode) R.string.edit_event_title else R.string.create_event_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoadingEvent) {
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
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(CreateEventEvent.TitleChanged(it)) },
                label = { Text(text = stringResource(R.string.event_form_name_label)) },
                singleLine = true,
                isError = state.titleErrorResId != null,
                supportingText = state.titleErrorResId?.let {
                    { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.event_form_category_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            if (state.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
                ) {
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = state.selectedCategoryId == category.id,
                            onClick = { onEvent(CreateEventEvent.CategorySelected(category.id)) },
                            label = { Text(text = category.name) }
                        )
                    }
                }
            }

            if (state.categoryErrorResId != null) {
                Text(
                    text = stringResource(state.categoryErrorResId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(CreateEventEvent.DescriptionChanged(it)) },
                label = { Text(text = stringResource(R.string.event_form_description_label)) },
                minLines = 4,
                maxLines = 8,
                isError = state.descriptionErrorResId != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        state.descriptionErrorResId?.let {
                            Text(text = stringResource(it, state.description.length), color = MaterialTheme.colorScheme.error)
                        } ?: Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.event_form_desc_counter_format, state.description.length),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.event_form_date_time_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            val formattedDate = state.dateTimeMillis?.let {
                val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                formatter.format(Date(it))
            } ?: stringResource(R.string.event_form_select_date_placeholder)

            OutlinedButton(
                onClick = { onEvent(CreateEventEvent.ShowDatePickerClicked) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                Text(text = formattedDate)
            }

            if (state.dateErrorResId != null) {
                Text(
                    text = stringResource(state.dateErrorResId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = state.address,
                onValueChange = { onEvent(CreateEventEvent.AddressChanged(it)) },
                label = { Text(text = stringResource(R.string.event_form_address_label)) },
                singleLine = true,
                isError = state.addressErrorResId != null,
                supportingText = state.addressErrorResId?.let {
                    { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                ) {
                    Text(
                        text = stringResource(
                            R.string.event_form_coordinates_label,
                            state.latitude,
                            state.longitude
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = { onEvent(CreateEventEvent.OpenMapPickerClicked) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_location),
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                        Text(text = stringResource(R.string.event_form_pick_location))
                    }
                }
            }

            OutlinedTextField(
                value = state.volunteerQuotaText,
                onValueChange = { onEvent(CreateEventEvent.QuotaChanged(it)) },
                label = { Text(text = stringResource(R.string.event_form_quota_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.quotaErrorResId != null,
                supportingText = state.quotaErrorResId?.let {
                    { Text(text = stringResource(it), color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.event_details_photos_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_photo_library),
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(Dimensions.spacingSmall))
                Text(text = stringResource(R.string.event_form_pick_photos))
            }

            if (state.photoUrls.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)) {
                    items(state.photoUrls) { photoUrl ->
                        GalleryPhotoThumbnail(
                            photoUri = photoUrl,
                            onRemove = { onEvent(CreateEventEvent.RemovePhotoClicked(photoUrl)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
            ) {
                OutlinedButton(
                    onClick = onBackClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.buttonHeight)
                ) {
                    Text(text = stringResource(R.string.profile_cancel_action))
                }

                Button(
                    onClick = { onEvent(CreateEventEvent.SubmitClicked) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(Dimensions.buttonHeight)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.progressIndicatorSize),
                            strokeWidth = Dimensions.progressIndicatorStrokeWidth
                        )
                    } else {
                        Text(
                            text = stringResource(
                                if (state.isEditMode) {
                                    R.string.event_form_save_changes
                                } else {
                                    R.string.event_form_submit_moderation
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateTimeMillis ?: (System.currentTimeMillis() + 86400000L)
        )
        DatePickerDialog(
            onDismissRequest = { onEvent(CreateEventEvent.DismissDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onEvent(CreateEventEvent.DateTimeSelected(it))
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.profile_save_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CreateEventEvent.DismissDatePicker) }) {
                    Text(text = stringResource(R.string.profile_cancel_action))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (state.showMapPickerDialog) {
        EventLocationMapDialog(
            latitude = state.tempMapLatitude,
            longitude = state.tempMapLongitude,
            onMapClick = { lat, lng -> onEvent(CreateEventEvent.MapPointClicked(lat, lng)) },
            onConfirm = { onEvent(CreateEventEvent.ConfirmMapCoordinatesClicked) },
            onDismiss = { onEvent(CreateEventEvent.DismissMapPicker) }
        )
    }
}

@Composable
private fun GalleryPhotoThumbnail(
    photoUri: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        modifier = modifier.size(Dimensions.photoThumbnailSize)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppAsyncImage(
                model = photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(Dimensions.iconMedium)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimensions.iconSmall)
                )
            }
        }
    }
}

@Composable
private fun EventLocationMapDialog(
    latitude: Double,
    longitude: Double,
    onMapClick: (Double, Double) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val initialLatLng = remember(latitude, longitude) { LatLng(latitude, longitude) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 13f)
    }
    val markerState = rememberUpdatedMarkerState(position = LatLng(latitude, longitude))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.event_form_map_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Text(
                    text = stringResource(R.string.event_form_coordinates_label, latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.mapPickerHeight)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            onMapClick(latLng.latitude, latLng.longitude)
                        }
                    ) {
                        Marker(state = markerState)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.event_form_confirm_location))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_cancel_action))
            }
        }
    )
}

@Preview
@Composable
private fun CreateEventScreenPreview() {
    LocalInteractionTheme {
        CreateEventScreen(
            state = CreateEventState(
                categories = listOf(
                    Category(id = "cat-1", name = "Екологія"),
                    Category(id = "cat-2", name = "Тварини")
                ),
                selectedCategoryId = "cat-1",
                isOrganizer = true
            ),
            onEvent = {}
        )
    }
}
