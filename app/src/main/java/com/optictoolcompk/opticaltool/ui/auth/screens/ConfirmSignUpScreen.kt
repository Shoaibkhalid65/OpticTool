package com.optictoolcompk.opticaltool.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.optictoolcompk.opticaltool.ui.auth.components.AuthLoadingButton
import com.optictoolcompk.opticaltool.ui.auth.components.OtpInputField
import com.optictoolcompk.opticaltool.ui.auth.models.AuthAction
import com.optictoolcompk.opticaltool.ui.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConfirmEmailScreen(
    onBackToLogin: () -> Unit,
    authViewModel: AuthViewModel,
    email: String,
) {
    var otpValue by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(0) }
    val uiState by authViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Confirm Your Email",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle with email
        Text(
            text = "Enter the 6-digit code sent to\n$email",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // OTP Input Boxes
        OtpInputField(
            otpValue = otpValue,
            onOtpChange = { newValue ->
                if (newValue.length <= 6) {
                    otpValue = newValue
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthLoadingButton(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            text = "Confirm Code",
            action = AuthAction.VERIFY_SIGNUP_OTP,
            uiState=uiState,
            enabled = otpValue.length==6,
            onClick = {
                authViewModel.verifySignupOtp(email,otpValue)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Resend Code Button with Timer
        TextButton(
            onClick = {
                authViewModel.resendSignupOtp(email)
                timeLeft = 30
                scope.launch {
                    while (timeLeft > 0) {
                        delay(1000)
                        timeLeft--
                    }
                }
            },
            enabled = timeLeft == 0
        ) {
            Text(
                text = if (timeLeft > 0) "Resend code in ${timeLeft}s" else "Resend Code",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (timeLeft > 0)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Login
        TextButton(
            onClick = onBackToLogin
        ) {
            Text(
                text = "Back to Login",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
