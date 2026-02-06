package com.optictoolcompk.opticaltool.ui.profile


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.optictoolcompk.opticaltool.ui.auth.components.AuthLoadingButton
import com.optictoolcompk.opticaltool.ui.auth.models.AuthAction
import com.optictoolcompk.opticaltool.ui.auth.viewmodel.AuthViewModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel = hiltViewModel()) {

    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<UserInfo?>(null) }

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        scope.launch {
            userInfo = authViewModel.getCurrentUser()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Home Screen",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )

        AdaptiveImage(
            imageUrl = userInfo?.userMetadata?.get("avatar_url")?.jsonPrimitive?.content,
            fallbackIcon = Icons.Default.Person,
            contentDescription = "profile image",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )

        Text(
            text = userInfo?.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "name"
        )

        Text(
            text = userInfo?.email ?: "email"
        )

        AuthLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            text = "Sign Out",
            action = AuthAction.SIGN_OUT,
            uiState = uiState,
            onClick = { authViewModel.signOut() }
        )
    }
}

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