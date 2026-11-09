package com.arathort.localinteraction.presentation.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun WriteReviewRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WriteReviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WriteReviewEffect.ShowSnackbar -> {
                    val message = if (effect.formatArgs.isEmpty()) {
                        context.getString(effect.stringResId)
                    } else {
                        context.getString(effect.stringResId, *effect.formatArgs.toTypedArray())
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is WriteReviewEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                WriteReviewEffect.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    WriteReviewScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    state: WriteReviewState,
    onEvent: (WriteReviewEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.review_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(WriteReviewEvent.CancelClicked) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                            modifier = Modifier.size(Dimensions.iconMedium)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                    Text(
                        text = stringResource(R.string.review_subtitle, state.eventTitle, state.targetUserName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
                    val roleLabel = when (state.targetRole) {
                        UserRole.ORGANIZER -> stringResource(R.string.role_organizer)
                        UserRole.VOLUNTEER -> stringResource(R.string.role_volunteer)
                        UserRole.ADMIN -> stringResource(R.string.role_admin)
                        UserRole.GUEST -> stringResource(R.string.role_guest)
                    }
                    Text(
                        text = stringResource(R.string.review_target_role_format, roleLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.alreadyReviewed) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = stringResource(R.string.review_already_submitted_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(Dimensions.paddingMedium)
                    )
                }
            } else if (!state.canReview) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = stringResource(R.string.review_cannot_review_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(Dimensions.paddingMedium)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.review_rating_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                InteractiveStarRatingBar(
                    rating = state.rating,
                    enabled = !state.alreadyReviewed && state.canReview && !state.isSubmitting,
                    onRatingChanged = { onEvent(WriteReviewEvent.RatingChanged(it)) }
                )
                Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
                val ratingDescription = when (state.rating) {
                    1 -> stringResource(R.string.review_rating_1)
                    2 -> stringResource(R.string.review_rating_2)
                    3 -> stringResource(R.string.review_rating_3)
                    4 -> stringResource(R.string.review_rating_4)
                    5 -> stringResource(R.string.review_rating_5)
                    else -> ""
                }
                if (ratingDescription.isNotBlank()) {
                    Text(
                        text = ratingDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                state.ratingErrorRes?.let { errorRes ->
                    Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
                    Text(
                        text = stringResource(errorRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.comment,
                    onValueChange = { onEvent(WriteReviewEvent.CommentChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.reviewCommentMinHeight),
                    label = { Text(text = stringResource(R.string.review_comment_hint)) },
                    enabled = !state.alreadyReviewed && state.canReview && !state.isSubmitting,
                    isError = state.commentErrorRes != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            state.commentErrorRes?.let { errorRes ->
                                Text(
                                    text = stringResource(errorRes),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } ?: Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = stringResource(
                                    R.string.review_char_counter,
                                    state.commentLength,
                                    1000
                                )
                            )
                        }
                    }
                )
            }

            state.generalError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onEvent(WriteReviewEvent.CancelClicked) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSubmitting
                ) {
                    Text(text = stringResource(R.string.action_cancel))
                }

                Button(
                    onClick = { onEvent(WriteReviewEvent.SubmitClicked) },
                    modifier = Modifier.weight(1f),
                    enabled = state.isSubmitEnabled
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.iconSmall),
                            strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = stringResource(R.string.review_action_rate))
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveStarRatingBar(
    rating: Int,
    enabled: Boolean,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (star in 1..5) {
            val isSelected = star <= rating
            val iconRes = if (isSelected) R.drawable.ic_star else R.drawable.ic_star_outline
            val tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }

            Box(
                modifier = Modifier
                    .size(Dimensions.starRatingLarge)
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onRatingChanged(star) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(R.string.review_star_cd, star),
                    tint = tint,
                    modifier = Modifier.size(Dimensions.starRatingLarge)
                )
            }
        }
    }
}

@Preview
@Composable
private fun WriteReviewScreenPreview() {
    LocalInteractionTheme {
        WriteReviewScreen(
            state = WriteReviewState(
                eventId = "event-1",
                eventTitle = "Прибирання парку",
                targetUserId = "user-1",
                targetUserName = "Олександр Коваленко",
                targetRole = UserRole.ORGANIZER,
                rating = 4,
                comment = "Чудова організація заходу, все було чітко скоординовано!"
            ),
            onEvent = {}
        )
    }
}
