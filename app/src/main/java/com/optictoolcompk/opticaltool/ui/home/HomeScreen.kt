package com.optictoolcompk.opticaltool.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToPrescriptions: () -> Unit,
    onNavigateToBillBook: () -> Unit,
    onNavigateToNotebook: () -> Unit,
    onNavigateToShopSetting: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Optical Tool",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 16.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section - Spans full width (2 columns)
            item(span = { GridItemSpan(2) }) {
                WelcomeSection()
            }

            // Main Tool Items
            items(getToolItems()) { tool ->
                ToolCard(
                    tool = tool,
                    onClick = {
                        when (tool.type) {
                            ToolType.CALCULATOR -> onNavigateToCalculator()
                            ToolType.PRESCRIPTIONS -> onNavigateToPrescriptions()
                            ToolType.BILL_BOOK -> onNavigateToBillBook()
                            ToolType.NOTEBOOK -> onNavigateToNotebook()
                        }
                    }
                )
            }

            // Enhanced Shop Setting Card - Spans full width (2 columns)
            item(span = { GridItemSpan(2) }) {
                ShopSettingCard(onClick = onNavigateToShopSetting)
            }
        }
    }
}

@Composable
fun WelcomeSection() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("eye.json")
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colorScheme.primary.toArgb(),
            keyPath = arrayOf("**") // The "**" targets ALL layers in the animation
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lottie Animation Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(56.dp),
                    dynamicProperties = dynamicProperties
                )
            }

            // Welcome Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Welcome to Optical Tool",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Your complete optical management solution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ToolCard(
//    tool: ToolItem,
//    onClick: () -> Unit
//) {
//    Card(
//        onClick = onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .aspectRatio(1f),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        shape = RoundedCornerShape(24.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Icon with colored background
//            Box(
//                modifier = Modifier
//                    .size(72.dp)
//                    .clip(CircleShape)
//                    .background(tool.backgroundColor),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    tool.icon,
//                    contentDescription = null,
//                    modifier = Modifier.size(36.dp),
//                    tint = tool.iconColor
//                )
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            // Title
//            Text(
//                tool.title,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onSurface,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(Modifier.height(4.dp))
//
//            // Subtitle
//            Text(
//                tool.subtitle,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            with(LocalDensity.current) {
                val isSmallScreen = maxWidth < 160.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isSmallScreen) 12.dp else 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon with colored background
                    Box(
                        modifier = Modifier
                            .size(if (isSmallScreen) 48.dp else 72.dp)
                            .clip(CircleShape)
                            .background(tool.backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            tool.icon,
                            contentDescription = null,
                            modifier = Modifier.size(if (isSmallScreen) 24.dp else 36.dp),
                            tint = tool.iconColor
                        )
                    }

                    Spacer(Modifier.height(if (isSmallScreen) 6.dp else 12.dp))

                    // Title
                    Text(
                        tool.title,
                        style = if (isSmallScreen)
                            MaterialTheme.typography.titleSmall
                        else
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    // Subtitle
                    Text(
                        tool.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = if (isSmallScreen) 10.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==================== DATA MODELS ====================

enum class ToolType {
    CALCULATOR,
    PRESCRIPTIONS,
    BILL_BOOK,
    NOTEBOOK
}

data class ToolItem(
    val type: ToolType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color
)

fun getToolItems(): List<ToolItem> {
    return listOf(
        ToolItem(
            type = ToolType.CALCULATOR,
            title = "Vision Calculator",
            subtitle = "Transpose & Calculate",
            icon = Icons.Default.Calculate,
            iconColor = Color(0xFF2196F3),
            backgroundColor = Color(0xFF2196F3).copy(alpha = 0.15f)
        ),
        ToolItem(
            type = ToolType.PRESCRIPTIONS,
            title = "Prescriptions",
            subtitle = "Manage Rx Records",
            icon = Icons.Default.Description,
            iconColor = Color(0xFF4CAF50),
            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
        ),
        ToolItem(
            type = ToolType.BILL_BOOK,
            title = "Bill Book",
            subtitle = "Invoices & Payments",
            icon = Icons.Default.Receipt,
            iconColor = Color(0xFFFF9800),
            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.15f)
        ),
        ToolItem(
            type = ToolType.NOTEBOOK,
            title = "Notebook",
            subtitle = "Track Inventory",
            icon = Icons.AutoMirrored.Default.MenuBook,
            iconColor = Color(0xFF9C27B0),
            backgroundColor = Color(0xFF9C27B0).copy(alpha = 0.15f)
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopSettingCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Manage Shop Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Configure store details & preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow Icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
