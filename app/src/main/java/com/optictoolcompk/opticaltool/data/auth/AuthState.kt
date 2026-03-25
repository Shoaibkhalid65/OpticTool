package com.optictoolcompk.opticaltool.data.auth

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data object SessionExpired : AuthState()
}