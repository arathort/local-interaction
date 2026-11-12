package com.arathort.localinteraction.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.OAuthProvider
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.presentation.util.Dimensions
import com.arathort.localinteraction.ui.theme.LocalInteractionTheme

@Composable
fun AuthRoute(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AuthEffect.NavigateBack -> onSuccess()
                is AuthEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageResId)
                    )
                }
                is AuthEffect.ShowSnackbarText -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
            }
        }
    }

    AuthScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClicked = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = { onEvent(AuthEvent.BackClicked) },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    if (state.showPasswordRecoveryDialog) {
        PasswordRecoveryDialog(
            state = state,
            onEvent = onEvent
        )
    }

    state.activeOAuthProvider?.let { provider ->
        OAuthLoginDialog(
            provider = provider,
            state = state,
            onEvent = onEvent
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (state.currentTab) {
                            AuthTab.LOGIN -> stringResource(R.string.auth_tab_login)
                            AuthTab.REGISTER -> stringResource(R.string.auth_tab_register)
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            AuthHeroHeader(currentTab = state.currentTab)

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = Dimensions.formMaxWidth),
                shape = RoundedCornerShape(Dimensions.cardCornerRadius),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimensions.cardElevation)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = state.currentTab.ordinal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = state.currentTab == AuthTab.LOGIN,
                            onClick = { onEvent(AuthEvent.TabSelected(AuthTab.LOGIN)) },
                            text = { Text(text = stringResource(R.string.auth_tab_login)) }
                        )
                        Tab(
                            selected = state.currentTab == AuthTab.REGISTER,
                            onClick = { onEvent(AuthEvent.TabSelected(AuthTab.REGISTER)) },
                            text = { Text(text = stringResource(R.string.auth_tab_register)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

                    when (state.currentTab) {
                        AuthTab.LOGIN -> LoginForm(state = state, onEvent = onEvent)
                        AuthTab.REGISTER -> RegisterForm(state = state, onEvent = onEvent)
                    }

                    Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

//                    SocialOAuthSection(
//                        enabled = !state.isLoading && !state.isOAuthLoading,
//                        onProviderClick = { onEvent(AuthEvent.OAuthProviderClicked(it)) }
//                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingExtraLarge))
        }
    }
}

@Composable
private fun AuthHeroHeader(
    currentTab: AuthTab,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = Dimensions.formMaxWidth),
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
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                modifier = Modifier.size(Dimensions.authHeroIconSize),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        Text(
            text = when (currentTab) {
                AuthTab.LOGIN -> stringResource(R.string.auth_hero_login_title)
                AuthTab.REGISTER -> stringResource(R.string.auth_hero_register_title)
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))

        Text(
            text = when (currentTab) {
                AuthTab.LOGIN -> stringResource(R.string.auth_hero_login_subtitle)
                AuthTab.REGISTER -> stringResource(R.string.auth_hero_register_subtitle)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoginForm(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = state.loginEmail,
            onValueChange = { onEvent(AuthEvent.LoginEmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.auth_email_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = state.loginEmailErrorResId != null,
            supportingText = {
                state.loginEmailErrorResId?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        OutlinedTextField(
            value = state.loginPassword,
            onValueChange = { onEvent(AuthEvent.LoginPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.auth_password_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            visualTransformation = if (state.isLoginPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { onEvent(AuthEvent.ToggleLoginPasswordVisibility) }) {
                    Icon(
                        painter = painterResource(
                            if (state.isLoginPasswordVisible) {
                                R.drawable.ic_visibility_off
                            } else {
                                R.drawable.ic_visibility
                            }
                        ),
                        contentDescription = stringResource(
                            if (state.isLoginPasswordVisible) {
                                R.string.action_hide_password
                            } else {
                                R.string.action_show_password
                            }
                        )
                    )
                }
            },
            isError = state.loginPasswordErrorResId != null,
            supportingText = {
                state.loginPasswordErrorResId?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(onClick = { onEvent(AuthEvent.OpenPasswordRecoveryClicked) }) {
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        Button(
            onClick = { onEvent(AuthEvent.LoginSubmitted) },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.buttonHeight),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.progressIndicatorSize),
                    strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(R.string.auth_login_button))
            }
        }
    }
}

@Composable
private fun RegisterForm(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.auth_select_role_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            RoleSelectionCard(
                title = stringResource(R.string.role_volunteer),
                description = stringResource(R.string.auth_role_volunteer_desc),
                isSelected = state.selectedRole == UserRole.VOLUNTEER,
                onClick = { onEvent(AuthEvent.RoleSelected(UserRole.VOLUNTEER)) }
            )

            RoleSelectionCard(
                title = stringResource(R.string.role_organizer),
                description = stringResource(R.string.auth_role_organizer_desc),
                isSelected = state.selectedRole == UserRole.ORGANIZER,
                onClick = { onEvent(AuthEvent.RoleSelected(UserRole.ORGANIZER)) }
            )
        }

        state.roleErrorResId?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = Dimensions.paddingExtraSmall)
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        OutlinedTextField(
            value = state.registerEmail,
            onValueChange = { onEvent(AuthEvent.RegisterEmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.auth_email_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = state.registerEmailErrorResId != null,
            supportingText = {
                state.registerEmailErrorResId?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        OutlinedTextField(
            value = state.registerPassword,
            onValueChange = { onEvent(AuthEvent.RegisterPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.auth_password_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            visualTransformation = if (state.isRegisterPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { onEvent(AuthEvent.ToggleRegisterPasswordVisibility) }) {
                    Icon(
                        painter = painterResource(
                            if (state.isRegisterPasswordVisible) {
                                R.drawable.ic_visibility_off
                            } else {
                                R.drawable.ic_visibility
                            }
                        ),
                        contentDescription = stringResource(
                            if (state.isRegisterPasswordVisible) {
                                R.string.action_hide_password
                            } else {
                                R.string.action_show_password
                            }
                        )
                    )
                }
            },
            isError = state.registerPasswordErrorResId != null,
            supportingText = {
                val errorRes = state.registerPasswordErrorResId
                if (errorRes != null) {
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (state.registerPassword.isNotEmpty()) {
                    val isEnough = state.registerPassword.length >= 8
                    Text(
                        text = stringResource(
                            R.string.auth_password_length_indicator,
                            state.registerPassword.length
                        ),
                        color = if (isEnough) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        OutlinedTextField(
            value = state.registerConfirmPassword,
            onValueChange = { onEvent(AuthEvent.RegisterConfirmPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.auth_confirm_password_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            visualTransformation = if (state.isRegisterConfirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { onEvent(AuthEvent.ToggleRegisterConfirmPasswordVisibility) }) {
                    Icon(
                        painter = painterResource(
                            if (state.isRegisterConfirmPasswordVisible) {
                                R.drawable.ic_visibility_off
                            } else {
                                R.drawable.ic_visibility
                            }
                        ),
                        contentDescription = stringResource(
                            if (state.isRegisterConfirmPasswordVisible) {
                                R.string.action_hide_password
                            } else {
                                R.string.action_show_password
                            }
                        )
                    )
                }
            },
            isError = state.registerConfirmPasswordErrorResId != null,
            supportingText = {
                val errorRes = state.registerConfirmPasswordErrorResId
                if (errorRes != null) {
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (
                    state.registerConfirmPassword.isNotEmpty() &&
                    state.registerConfirmPassword == state.registerPassword
                ) {
                    Text(
                        text = stringResource(R.string.auth_passwords_match_indicator),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onEvent(AuthEvent.PrivacyPolicyConsentChanged(!state.acceptedPrivacyPolicy))
                }
        ) {
            Checkbox(
                checked = state.acceptedPrivacyPolicy,
                onCheckedChange = { onEvent(AuthEvent.PrivacyPolicyConsentChanged(it)) }
            )
            Text(
                text = stringResource(R.string.auth_privacy_policy_consent),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = Dimensions.paddingExtraSmall)
            )
        }

        state.privacyPolicyErrorResId?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = Dimensions.paddingSmall, top = Dimensions.paddingExtraSmall)
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

        Button(
            onClick = { onEvent(AuthEvent.RegisterSubmitted) },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.buttonHeight),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.progressIndicatorSize),
                    strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(R.string.auth_register_button))
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val borderWidth = if (isSelected) {
        Dimensions.roleCardBorderSelected
    } else {
        Dimensions.roleCardBorderDefault
    }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimensions.paddingSmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SocialOAuthSection(
    enabled: Boolean,
    onProviderClick: (OAuthProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.auth_or_continue_with),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
        ) {
            OutlinedButton(
                onClick = { onProviderClick(OAuthProvider.GOOGLE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight),
                enabled = enabled
            ) {
                Text(text = stringResource(R.string.auth_google_sign_in))
            }

            OutlinedButton(
                onClick = { onProviderClick(OAuthProvider.FACEBOOK) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.buttonHeight),
                enabled = enabled
            ) {
                Text(text = stringResource(R.string.auth_facebook_sign_in))
            }
        }
    }
}

@Composable
private fun PasswordRecoveryDialog(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(AuthEvent.DismissPasswordRecoveryDialog) },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_lock_reset),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = when (state.recoveryStep) {
                    PasswordRecoveryStep.REQUEST_EMAIL -> stringResource(R.string.auth_recovery_dialog_title_step1)
                    PasswordRecoveryStep.CONFIRM_RESET -> stringResource(R.string.auth_recovery_dialog_title_step2)
                }
            )
        },
        text = {
            when (state.recoveryStep) {
                PasswordRecoveryStep.REQUEST_EMAIL -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_recovery_dialog_subtitle_step1),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = state.recoveryEmail,
                            onValueChange = { onEvent(AuthEvent.RecoveryEmailChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = stringResource(R.string.auth_email_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            isError = state.recoveryEmailErrorResId != null,
                            supportingText = {
                                state.recoveryEmailErrorResId?.let {
                                    Text(
                                        text = stringResource(it),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        TextButton(
                            onClick = {
                                onEvent(AuthEvent.RecoveryStepChanged(PasswordRecoveryStep.CONFIRM_RESET))
                            }
                        ) {
                            Text(text = stringResource(R.string.auth_recovery_already_have_code))
                        }
                    }
                }
                PasswordRecoveryStep.CONFIRM_RESET -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_recovery_dialog_subtitle_step2),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = state.resetToken,
                            onValueChange = { onEvent(AuthEvent.ResetTokenChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = stringResource(R.string.auth_recovery_token_label)) },
                            singleLine = true,
                            isError = state.resetTokenErrorResId != null,
                            supportingText = {
                                state.resetTokenErrorResId?.let {
                                    Text(
                                        text = stringResource(it),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = state.resetNewPassword,
                            onValueChange = { onEvent(AuthEvent.ResetNewPasswordChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = stringResource(R.string.auth_recovery_new_password_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            visualTransformation = if (state.isResetNewPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { onEvent(AuthEvent.ToggleResetNewPasswordVisibility) }) {
                                    Icon(
                                        painter = painterResource(
                                            if (state.isResetNewPasswordVisible) {
                                                R.drawable.ic_visibility_off
                                            } else {
                                                R.drawable.ic_visibility
                                            }
                                        ),
                                        contentDescription = null
                                    )
                                }
                            },
                            isError = state.resetNewPasswordErrorResId != null,
                            supportingText = {
                                state.resetNewPasswordErrorResId?.let {
                                    Text(
                                        text = stringResource(it),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = state.resetConfirmPassword,
                            onValueChange = { onEvent(AuthEvent.ResetConfirmPasswordChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = stringResource(R.string.auth_recovery_confirm_password_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            visualTransformation = if (state.isResetConfirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { onEvent(AuthEvent.ToggleResetConfirmPasswordVisibility) }) {
                                    Icon(
                                        painter = painterResource(
                                            if (state.isResetConfirmPasswordVisible) {
                                                R.drawable.ic_visibility_off
                                            } else {
                                                R.drawable.ic_visibility
                                            }
                                        ),
                                        contentDescription = null
                                    )
                                }
                            },
                            isError = state.resetConfirmPasswordErrorResId != null,
                            supportingText = {
                                state.resetConfirmPasswordErrorResId?.let {
                                    Text(
                                        text = stringResource(it),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        TextButton(
                            onClick = {
                                onEvent(AuthEvent.RecoveryStepChanged(PasswordRecoveryStep.REQUEST_EMAIL))
                            }
                        ) {
                            Text(text = stringResource(R.string.auth_recovery_back_to_email))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (state.recoveryStep) {
                        PasswordRecoveryStep.REQUEST_EMAIL -> onEvent(AuthEvent.ForgotPasswordSubmitted)
                        PasswordRecoveryStep.CONFIRM_RESET -> onEvent(AuthEvent.ResetPasswordSubmitted)
                    }
                },
                enabled = !state.isRecoveryLoading
            ) {
                if (state.isRecoveryLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = when (state.recoveryStep) {
                            PasswordRecoveryStep.REQUEST_EMAIL -> stringResource(R.string.auth_recovery_send_code_button)
                            PasswordRecoveryStep.CONFIRM_RESET -> stringResource(R.string.auth_recovery_submit_reset_button)
                        }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(AuthEvent.DismissPasswordRecoveryDialog) },
                enabled = !state.isRecoveryLoading
            ) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun OAuthLoginDialog(
    provider: OAuthProvider,
    state: AuthState,
    onEvent: (AuthEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(AuthEvent.DismissOAuthDialog) },
        title = {
            Text(
                text = when (provider) {
                    OAuthProvider.GOOGLE -> stringResource(R.string.auth_oauth_dialog_title_google)
                    OAuthProvider.FACEBOOK -> stringResource(R.string.auth_oauth_dialog_title_facebook)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall)
            ) {
                Text(
                    text = stringResource(R.string.auth_oauth_dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = state.oauthTokenInput,
                    onValueChange = { onEvent(AuthEvent.OAuthTokenChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.auth_oauth_token_label)) },
                    singleLine = true,
                    isError = state.oauthTokenErrorResId != null,
                    supportingText = {
                        state.oauthTokenErrorResId?.let {
                            Text(
                                text = stringResource(it),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Text(
                    text = stringResource(R.string.auth_select_role_label),
                    style = MaterialTheme.typography.labelLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onEvent(AuthEvent.OAuthRoleSelected(UserRole.VOLUNTEER))
                        }
                    ) {
                        RadioButton(
                            selected = state.oauthSelectedRole == UserRole.VOLUNTEER,
                            onClick = { onEvent(AuthEvent.OAuthRoleSelected(UserRole.VOLUNTEER)) }
                        )
                        Text(
                            text = stringResource(R.string.role_volunteer),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onEvent(AuthEvent.OAuthRoleSelected(UserRole.ORGANIZER))
                        }
                    ) {
                        RadioButton(
                            selected = state.oauthSelectedRole == UserRole.ORGANIZER,
                            onClick = { onEvent(AuthEvent.OAuthRoleSelected(UserRole.ORGANIZER)) }
                        )
                        Text(
                            text = stringResource(R.string.role_organizer),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEvent(AuthEvent.OAuthPrivacyConsentChanged(!state.oauthAcceptedPrivacyPolicy))
                        }
                ) {
                    Checkbox(
                        checked = state.oauthAcceptedPrivacyPolicy,
                        onCheckedChange = { onEvent(AuthEvent.OAuthPrivacyConsentChanged(it)) }
                    )
                    Text(
                        text = stringResource(R.string.auth_privacy_policy_consent),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                state.oauthPrivacyErrorResId?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(AuthEvent.OAuthLoginSubmitted) },
                enabled = !state.isOAuthLoading
            ) {
                if (state.isOAuthLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(R.string.auth_oauth_continue_button))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(AuthEvent.DismissOAuthDialog) },
                enabled = !state.isOAuthLoading
            ) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenLoginPreview() {
    LocalInteractionTheme {
        AuthScreen(
            state = AuthState(currentTab = AuthTab.LOGIN),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenRegisterPreview() {
    LocalInteractionTheme {
        AuthScreen(
            state = AuthState(
                currentTab = AuthTab.REGISTER,
                selectedRole = UserRole.VOLUNTEER
            ),
            onEvent = {}
        )
    }
}
