package com.optictoolcompk.opticaltool.data.auth


import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.optictoolcompk.opticaltool.R
import com.optictoolcompk.opticaltool.di.ApplicationScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.auth.user.UserInfo

class AuthRepository @Inject constructor(
    private val auth: Auth,
    @param:ApplicationScope private val appScope: CoroutineScope
) {

    val authState: StateFlow<AuthState> =
        auth.sessionStatus
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated ->
                        AuthState.Authenticated(
                            status.session.user!!.id
                        )

                    is SessionStatus.NotAuthenticated ->
                        AuthState.Unauthenticated

                    is SessionStatus.RefreshFailure ->
                        AuthState.SessionExpired

                    SessionStatus.Initializing ->
                        AuthState.Loading
                }
            }
            .stateIn(
                scope = appScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AuthState.Loading
            )

    // we use it when we have to create the new account for user it will send the otp to the given email
    suspend fun signUpWithEmail(
        emailValue: String,
        passwordValue: String,
        nameValue: String
    ) {
        try {
            auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
                data = buildJsonObject { put("full_name", JsonPrimitive(nameValue)) }
            }
        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    //    we use it when we have to verify the otp send to the email to verify the sign up
    suspend fun verifyEmailOtp(otp: String, emailValue: String){
        try {
            auth.verifyEmailOtp(
                type = OtpType.Email.SIGNUP,
                email = emailValue,
                token = otp
            )
        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    // when we have to resend the opt for the sign up
    suspend fun resendEmailOtp(emailValue: String) {
        try {
            auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = emailValue,
            )
        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    // we use it when we want to sign in with the given email and  password
    suspend fun signInWithEmail(emailValue: String, passwordValue: String) {
        try {
            auth.signInWith(Email) {
                email = emailValue
                password = passwordValue

            }
        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    //   we use it when we have to send the reset password request for the given email
//    it will send an opt to the given email
    suspend fun resetPasswordForEmail(emailValue: String){
        try {
            auth.resetPasswordForEmail(
                email = emailValue
            )
        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    // we will use this function when we want to verify the otp that we is used to request to reset the password
    suspend fun verifyEmailOtpForPasswordRecovery(emailValue: String, otp: String){
        try {
            auth.verifyEmailOtp(
                type = OtpType.Email.RECOVERY,
                email = emailValue,
                token = otp
            )
        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    //    we use it when we want to update the password of the user
    suspend fun updateTheUserEmailPassword(passwordValue: String) {
        try {
            auth.updateUser {
                password = passwordValue
            }

        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    suspend fun loginWithGoogle(activityContext: Context) {
        val hashNonce = createNonce()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activityContext.getString(R.string.web_clint_id))
            .setAutoSelectEnabled(false)
            .setNonce(hashNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activityContext)

        try {
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            val googleIdToken = googleIdTokenCredential.idToken

            auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }

        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }

    fun getUser(): UserInfo? {
        return auth.currentUserOrNull()
    }

    suspend fun signOut(){
        try {
            auth.signOut()
        } catch (e: Exception) {
            throw mapSupabaseAuthException(e)
        }
    }


    private fun mapSupabaseAuthException(throwable: Throwable): Exception {

        val message = when (throwable) {

            is AuthWeakPasswordException -> {
                "Password is too weak. Requirements: ${throwable.reasons.joinToString(", ")}"
            }

            is AuthRestException -> {

                when (throwable.errorCode?.name) {

                    "invalid_credentials" ->
                        "Incorrect email or password"

                    "user_not_found" ->
                        "No account found with this email"

                    "email_not_confirmed" ->
                        "Please verify your email before signing in"

                    "email_already_in_use" ->
                        "This email is already registered"

                    "phone_already_in_use" ->
                        "This phone number is already registered"

                    "otp_expired" ->
                        "OTP has expired. Please request a new one"

                    "invalid_otp" ->
                        "Invalid OTP. Please try again"

                    "over_request_rate_limit" ->
                        "Too many attempts. Please try again later"

                    "signup_disabled" ->
                        "Sign up is currently disabled"

                    "provider_disabled" ->
                        "This sign-in method is not available"

                    "session_not_found" ->
                        "Your session has expired. Please sign in again"

                    "invalid_grant" ->
                        "Authentication session is no longer valid"

                    else -> when (throwable.statusCode) {

                        400 ->
                            "Invalid request. Please check your information"

                        401 ->
                            "Authentication failed. Please sign in again"

                        403 ->
                            "Access denied"

                        404 ->
                            "Resource not found"

                        422 ->
                            "Invalid input. Please check your information"

                        in 500..599 ->
                            "Server error. Please try again later"

                        else ->
                            throwable.errorDescription
                    }
                }
            }

            is RestException ->
                "Server error. Please try again later"

            is HttpRequestTimeoutException ->
                "Request timed out. Please check your internet connection"

            is HttpRequestException ->
                "Network error. Please check your internet connection"

            else ->
                throwable.message ?: "Something went wrong. Please try again"
        }
        return Exception(message)
    }
}