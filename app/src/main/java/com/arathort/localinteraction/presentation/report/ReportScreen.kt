package com.arathort.localinteraction.presentation.report

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.ComplaintTargetType
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun ReportRoute(
    viewModel: ReportViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageResId)
                    )
                }
                is ReportEffect.ShowSnackbarText -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
                ReportEffect.NavigateBack -> onBack()
            }
        }
    }

    ReportScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClicked = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    state: ReportState,
    onEvent: (ReportEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = { onEvent(ReportEvent.BackClicked) },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val categoryOptions = listOf(
        stringResource(R.string.complaint_category_fraud),
        stringResource(R.string.complaint_category_spam),
        stringResource(R.string.complaint_category_harassment),
        stringResource(R.string.complaint_category_misinformation),
        stringResource(R.string.complaint_category_harmful),
        stringResource(R.string.complaint_category_other)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.complaint_dialog_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimensions.notificationIconContainerSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_flag),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(Dimensions.notificationIconSize)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (state.targetType) {
                                ComplaintTargetType.USER -> stringResource(R.string.report_user_action)
                                ComplaintTargetType.EVENT -> stringResource(R.string.report_event_action)
                                ComplaintTargetType.COMMENT -> stringResource(R.string.report_comment_action)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (state.targetTitle.isNotBlank()) {
                            Text(
                                text = state.targetTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))

                        Text(
                            text = stringResource(R.string.complaint_dialog_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Text(
                    text = stringResource(R.string.complaint_category_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spacingExtraSmall)
                ) {
                    categoryOptions.forEach { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { onEvent(ReportEvent.CategorySelected(category)) },
                            label = { Text(text = category) }
                        )
                    }
                }

                state.categoryErrorResId?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Text(
                    text = stringResource(R.string.complaint_description_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { onEvent(ReportEvent.DescriptionChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimensions.reportDescriptionMinHeight),
                    placeholder = { Text(text = stringResource(R.string.complaint_description_hint)) },
                    minLines = 4,
                    maxLines = 8,
                    isError = state.descriptionErrorResId != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (state.descriptionErrorResId != null) {
                                Text(
                                    text = stringResource(state.descriptionErrorResId),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Spacer(modifier = Modifier.size(Dimensions.spacingSmall))
                            }
                            Text(
                                text = stringResource(
                                    R.string.complaint_description_counter,
                                    state.description.length
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.description.length >= 10) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            Button(
                onClick = { onEvent(ReportEvent.SubmitClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight),
                enabled = !state.isSubmitting
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(R.string.complaint_submit_button))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportScreenPreview() {
    LocalInteractionTheme {
        ReportScreen(
            state = ReportState(
                targetType = ComplaintTargetType.EVENT,
                targetId = "event-1",
                targetTitle = "Прибирання міського парку",
                selectedCategory = "Шахрайство або обман",
                description = "Захід містить неправдиві дані щодо збору коштів."
            ),
            onEvent = {}
        )
    }
}
