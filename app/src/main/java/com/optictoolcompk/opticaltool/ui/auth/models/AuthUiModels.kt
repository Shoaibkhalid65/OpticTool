package com.optictoolcompk.opticaltool.ui.auth.models

enum class AuthAction {
    LOGIN,
    LOGIN_GOOGLE,
    SIGNUP,
    VERIFY_SIGNUP_OTP,
    RESEND_SIGNUP_OTP,
    REQUEST_PASSWORD_OTP,
    VERIFY_RECOVERY_OTP,
    UPDATE_PASSWORD,
    SIGN_OUT
}

sealed class AuthEvent {
    data class Navigate(
        val route: String,
        val popUpTo: String? = null,
        val inclusive: Boolean = false
    ) : AuthEvent()

    data class ShowSnackbar(val message: String) : AuthEvent()
}

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data class Loading(val action: AuthAction) : AuthUiState
}