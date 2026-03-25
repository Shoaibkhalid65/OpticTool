package com.optictoolcompk.opticaltool.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.optictoolcompk.opticaltool.ui.auth.components.AuthLoadingButton
import com.optictoolcompk.opticaltool.ui.auth.components.OtpInputField
import com.optictoolcompk.opticaltool.ui.auth.models.AuthAction
import com.optictoolcompk.opticaltool.ui.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OtpVerificationScreen(
    email: String,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(0) }
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
//            .verticalScroll(rememberScrollState())
//            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Main Content - Centered
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Verify Your Email",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle with email
            Text(
                text = "We've sent a verification code to",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please check your inbox and enter the 6-digit code below",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

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

            // Verify Code Button
            AuthLoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                text = "Verify Code",
                action = AuthAction.VERIFY_RECOVERY_OTP,
                uiState = uiState,
                enabled = otpValue.length == 6,
                onClick = {
                    authViewModel.verifyRecoveryOtp(email, otpValue)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resend Code Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Didn't receive the code?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        authViewModel.resetPassword(email)
                        timeLeft = 30
                        otpValue = "" // Clear OTP on resend
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
                        fontWeight = FontWeight.SemiBold,
                        color = if (timeLeft > 0)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
