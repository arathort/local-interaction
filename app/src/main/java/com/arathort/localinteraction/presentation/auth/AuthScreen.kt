package com.arathort.localinteraction.presentation.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arathort.localinteraction.R
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
                    TextButton(onClick = onBackClicked) {
                        Text(text = stringResource(R.string.action_back))
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
        }
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
                TextButton(onClick = { onEvent(AuthEvent.ToggleLoginPasswordVisibility) }) {
                    Text(
                        text = stringResource(
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

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

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

        Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onEvent(AuthEvent.RoleSelected(UserRole.VOLUNTEER)) }
            ) {
                RadioButton(
                    selected = state.selectedRole == UserRole.VOLUNTEER,
                    onClick = { onEvent(AuthEvent.RoleSelected(UserRole.VOLUNTEER)) }
                )
                Text(
                    text = stringResource(R.string.role_volunteer),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Dimensions.paddingExtraSmall)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onEvent(AuthEvent.RoleSelected(UserRole.ORGANIZER)) }
            ) {
                RadioButton(
                    selected = state.selectedRole == UserRole.ORGANIZER,
                    onClick = { onEvent(AuthEvent.RoleSelected(UserRole.ORGANIZER)) }
                )
                Text(
                    text = stringResource(R.string.role_organizer),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Dimensions.paddingExtraSmall)
                )
            }
        }

        state.roleErrorResId?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = Dimensions.paddingExtraSmall)
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

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
                TextButton(onClick = { onEvent(AuthEvent.ToggleRegisterPasswordVisibility) }) {
                    Text(
                        text = stringResource(
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
                state.registerPasswordErrorResId?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error
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
                TextButton(onClick = { onEvent(AuthEvent.ToggleRegisterConfirmPasswordVisibility) }) {
                    Text(
                        text = stringResource(
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
                state.registerConfirmPasswordErrorResId?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error
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
