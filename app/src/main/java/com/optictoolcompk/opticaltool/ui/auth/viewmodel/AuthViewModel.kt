package com.optictoolcompk.opticaltool.ui.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optictoolcompk.opticaltool.data.auth.AuthRepository
import com.optictoolcompk.opticaltool.navigation.Screen
import com.optictoolcompk.opticaltool.ui.auth.models.AuthAction
import com.optictoolcompk.opticaltool.ui.auth.models.AuthEvent
import com.optictoolcompk.opticaltool.ui.auth.models.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    val authState = repository.authState

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    // ----------------------------------
    // SIGN UP
    // ----------------------------------
    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.SIGNUP)

            runCatching {
                repository.signUpWithEmail(email, password, name)
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(
                        Screen.ConfirmEmail.createRoute(email)
                    )
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "Sign up failed")
                )
            }

            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // VERIFY SIGNUP OTP
    // ----------------------------------
    fun verifySignupOtp(email: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.VERIFY_SIGNUP_OTP)

            runCatching {
                repository.verifyEmailOtp(otp, email)
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(
                        Screen.HomeScreen.route,
                        popUpTo = Screen.SignIn.route,
                        inclusive = true
                    )
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "OTP verification failed")
                )
            }

            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // RESEND OTP
    // ----------------------------------
    fun resendSignupOtp(email: String) {
        _uiState.value = AuthUiState.Loading(AuthAction.RESEND_SIGNUP_OTP)
        viewModelScope.launch {
            runCatching {
                repository.resendEmailOtp(email)
            }.onSuccess {
                _events.emit(
                    AuthEvent.ShowSnackbar("OTP sent successfully")
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "Failed to resend OTP")
                )
            }
        }
    }

    // ----------------------------------
    // SIGN IN
    // ----------------------------------
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.LOGIN)

            runCatching {
                repository.signInWithEmail(email, password)
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(
                        Screen.HomeScreen.route,
                        popUpTo = Screen.SignIn.route,
                        inclusive = true
                    )
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "Sign in failed")
                )
            }

            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // FORGOT PASSWORD
    // ----------------------------------
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.REQUEST_PASSWORD_OTP)

            runCatching {
                repository.resetPasswordForEmail(email)
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(
                        Screen.RecoveryOtpVerf.createRoute(email)
                    )
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "Password reset failed")
                )
            }

            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // VERIFY RECOVERY OTP
    // ----------------------------------
    fun verifyRecoveryOtp(email: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.VERIFY_RECOVERY_OTP)

            runCatching {
                repository.verifyEmailOtpForPasswordRecovery(email, otp)
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(Screen.NewPassword.route)
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "OTP verification failed")
                )
            }

            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // UPDATE PASSWORD
    // ----------------------------------
    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.UPDATE_PASSWORD)

            runCatching {
                repository.updateTheUserEmailPassword(newPassword)
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(
                        Screen.HomeScreen.route,
                        popUpTo = Screen.SignIn.route,
                        inclusive = true
                    )
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "Password update failed")
                )
            }

            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // GOOGLE SIGN IN
    // ----------------------------------
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.LOGIN_GOOGLE)

            runCatching {
                repository.loginWithGoogle(activityContext)
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(
                        Screen.HomeScreen.route,
                        popUpTo = Screen.SignIn.route,
                        inclusive = true
                    )
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "Google sign in failed")
                )
            }

            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // SIGN OUT
    // ----------------------------------
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading(AuthAction.SIGN_OUT)
            runCatching {
                repository.signOut()
            }.onSuccess {
                _events.emit(
                    AuthEvent.Navigate(
                        Screen.SignIn.route,
                        popUpTo = Screen.HomeScreen.route,
                        inclusive = true
                    )
                )
            }.onFailure {
                _events.emit(
                    AuthEvent.ShowSnackbar(it.message ?: "Sign out failed")
                )
            }
            _uiState.value = AuthUiState.Idle
        }
    }

    // ----------------------------------
    // CURRENT USER
    // ----------------------------------
    fun getCurrentUser() = repository.getUser()
}