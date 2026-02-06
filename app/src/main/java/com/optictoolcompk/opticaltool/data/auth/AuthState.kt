package com.optictoolcompk.opticaltool.data.auth

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object SessionExpired : AuthState()
}