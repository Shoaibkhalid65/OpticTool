package com.optictoolcompk.opticaltool.ui.profile


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.optictoolcompk.opticaltool.ui.auth.components.AuthLoadingButton
import com.optictoolcompk.opticaltool.ui.auth.models.AuthAction
import com.optictoolcompk.opticaltool.ui.auth.viewmodel.AuthViewModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Instant


//@OptIn(ExperimentalMaterial3ExpressiveApi::class)
//@Composable
//fun ProfileScreen(authViewModel: AuthViewModel = hiltViewModel()) {
//
//    val scope = rememberCoroutineScope()
//    var userInfo by remember { mutableStateOf<UserInfo?>(null) }
//
//    val uiState by authViewModel.uiState.collectAsState()
//
//    LaunchedEffect(Unit) {
//        scope.launch {
//            userInfo = authViewModel.getCurrentUser()
//        }
//    }
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//            .padding(24.dp),
//        verticalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterVertically),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Home Screen",
//            fontWeight = FontWeight.SemiBold,
//            style = MaterialTheme.typography.titleMedium
//        )
//
//        AdaptiveImage(
//            imageUrl = userInfo?.userMetadata?.get("avatar_url")?.jsonPrimitive?.content,
//            fallbackIcon = Icons.Default.Person,
//            contentDescription = "profile image",
//            modifier = Modifier
//                .size(64.dp)
//                .clip(CircleShape)
//        )
//
//        Text(
//            text = userInfo?.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "name"
//        )
//
//        Text(
//            text = userInfo?.email ?: "email"
//        )
//
//        AuthLoadingButton(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            text = "Sign Out",
//            action = AuthAction.SIGN_OUT,
//            uiState = uiState,
//            onClick = { authViewModel.signOut() }
//        )
//    }
//}
//
@Composable
fun AdaptiveImage(
    imageUrl: String?,
    fallbackIcon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        Image(
            imageVector = fallbackIcon,
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = ColorFilter.tint(Color.Gray)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    padding: PaddingValues
) {
    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<UserInfo?>(null) }
    val uiState by authViewModel.uiState.collectAsState()

    // Extracting Metadata safely
    val fullName = userInfo?.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "Optical Pro"
    val avatarUrl = userInfo?.userMetadata?.get("avatar_url")?.jsonPrimitive?.content
    val email = userInfo?.email ?: "Not Available"

    val joinedDate = remember(userInfo?.createdAt) {
        formatKotlinInstant(userInfo?.createdAt)
    }

    val lastActive = remember(userInfo?.lastSignInAt) {
        formatKotlinInstant(userInfo?.lastSignInAt)
    }



    LaunchedEffect(Unit) {
        userInfo = authViewModel.getCurrentUser()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // --- Profile Header Section ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    4.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            AdaptiveImage(
                imageUrl = avatarUrl,
                fallbackIcon = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Info Cards Section ---
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileInfoCard(
                title = "Account Information",
                items = listOf(
                    ProfileItem(
                        Icons.Default.Badge,
                        "Role",
                        userInfo?.role?.uppercase() ?: "USER"
                    ),
                    ProfileItem(Icons.Default.CalendarToday, "Member Since", joinedDate),
                    ProfileItem(Icons.Default.History, "Last Active", lastActive)
                )
            )

            ProfileInfoCard(
                title = "Support & Settings",
                items = listOf(
                    ProfileItem(Icons.Default.Info, "About Optic Tool", "v1.0.2"),
                    ProfileItem(Icons.Default.Lock, "Privacy Policy", "")
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Logout Button ---
            AuthLoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                text = "Sign Out",
                action = AuthAction.SIGN_OUT,
                uiState = uiState,
                onClick = { authViewModel.signOut() }
            )

            Text(
                text = "User ID: ${userInfo?.id ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProfileInfoCard(title: String, items: List<ProfileItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.value.isNotEmpty()) {
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

data class ProfileItem(val icon: ImageVector, val label: String, val value: String)

fun formatKotlinInstant(instant: Instant?): String {
    if (instant == null) return "N/A"

    // Convert Instant to a local date-time based on the user's system timezone
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    // Format manually or using a simple string template for a clean look
    val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val day = localDateTime.dayOfMonth
    val year = localDateTime.year

    return "$month $day, $year"
}