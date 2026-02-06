package com.optictoolcompk.opticaltool.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*



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
                    // Refined Profile Action
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle, // Changed to AccountCircle for a better look
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
//    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.eye_animation))
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("eye.json")
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Replaced Icon with Lottie
            Box(
                modifier = Modifier
                    .size(80.dp) // Increased size slightly to show detail
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    "Welcome to Optical Tool",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Your complete optical management solution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

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
            .aspectRatio(1f)
//            .dropShadow(CardDefaults.shape, shadow = Shadow(radius = 10.dp, color = tool.iconColor, alpha = 0.4f))
        ,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Circle Background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(tool.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    tool.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = tool.iconColor
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                tool.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            Text(
                tool.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            title = "Glasses Notebook",
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
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Shop Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure store details & preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun Modifier.rippleClickable(
    enabled: Boolean=true,
    onClick:()-> Unit,
): Modifier{
    val interactionSource= remember{ MutableInteractionSource() }
    return this.clickable (
        interactionSource=interactionSource,
        indication = ripple(
            bounded = true,
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
            radius = 40.dp
        ),
        enabled=enabled,
        onClick=onClick
    )
}

//
//package com.optictoolcompk.opticaltool.ui.home
//
//import androidx.compose.animation.animateContentSize
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.spring
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.dropShadow
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.shadow.Shadow
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//
//data class ToolCard(
//    val title: String,
//    val subtitle: String,
//    val icon: ImageVector,
//    val gradientColors: List<Color>,
//    val onClick: () -> Unit
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(
//    shopName: String = "Vision Care Opticals",
//    onNavigateToCalculator: () -> Unit,
//    onNavigateToPrescriptions: () -> Unit,
//    onNavigateToBillBook: () -> Unit,
//    onNavigateToNotebook: () -> Unit,
//    onNavigateToShopSetting: ()-> Unit,
//    onNavigateToProfile:()-> Unit
//) {
//    val scrollState = rememberScrollState()
//
//    Scaffold(
//        topBar = {
//            OpticianTopBar(
//                shopName = shopName,
//                onProfileClick = onNavigateToProfile
//            )
//        },
//        containerColor = Color(0xFFF5F7FA)
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .verticalScroll(scrollState)
//                .padding(bottom = 24.dp)
//        ) {
//            // Welcome Section with Greeting
//            WelcomeSection()
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Quick Stats Cards
//            QuickStatsRow()
//
//            Spacer(modifier = Modifier.height(28.dp))
//
//            // Main Tools Section
//            Text(
//                text = "Quick Tools",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1A1A1A),
//                modifier = Modifier.padding(horizontal = 20.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Tool Cards Grid
//            ToolsGrid(
//                onNavigateToCalculator = onNavigateToCalculator,
//                onNavigateToPrescriptions = onNavigateToPrescriptions,
//                onNavigateToBillbook = onNavigateToBillBook,
//                onNavigateToNotebook = onNavigateToNotebook
//            )
//
//            Spacer(modifier = Modifier.height(28.dp))
//
//            // Shop Dashboard Settings - Full Width Card
//            ShopDashboardSettingsCard(
//                onClick = onNavigateToShopSetting
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OpticianTopBar(
//    shopName: String,
//    onProfileClick: () -> Unit
//) {
//    var notificationCount by remember { mutableStateOf(3) }
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .shadow(
//                elevation = 4.dp,
//                spotColor = Color.Black.copy(alpha = 0.1f),
//                ambientColor = Color.Black.copy(alpha = 0.05f)
//            ),
//        color = Color.White
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp, vertical = 16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Shop Logo & Name
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.weight(1f)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(44.dp)
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(
//                            Brush.linearGradient(
//                                colors = listOf(
//                                    Color(0xFF4C63D2),
//                                    Color(0xFF6B7FE8)
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Visibility,
//                        contentDescription = "Shop Logo",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Column {
//                    Text(
//                        text = shopName,
//                        fontSize = 17.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF1A1A1A),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Text(
//                        text = "Optical Management",
//                        fontSize = 12.sp,
//                        color = Color(0xFF6B7280),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//
//            // Action Icons
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Notification Bell
////                Box(
////                    modifier = Modifier
////                        .size(44.dp)
////                        .clip(CircleShape)
////                        .background(Color(0xFFF3F4F6))
////                        .clickable { onNotificationClick() },
////                    contentAlignment = Alignment.Center
////                ) {
////                    Icon(
////                        imageVector = Icons.Outlined.Notifications,
////                        contentDescription = "Notifications",
////                        tint = Color(0xFF374151),
////                        modifier = Modifier.size(22.dp)
////                    )
////
////                    if (notificationCount > 0) {
////                        Box(
////                            modifier = Modifier
////                                .size(18.dp)
////                                .align(Alignment.TopEnd)
////                                .offset(x = 2.dp, y = (-2).dp)
////                                .clip(CircleShape)
////                                .background(Color(0xFFEF4444)),
////                            contentAlignment = Alignment.Center
////                        ) {
////                            Text(
////                                text = if (notificationCount > 9) "9+" else notificationCount.toString(),
////                                color = Color.White,
////                                fontSize = 9.sp,
////                                fontWeight = FontWeight.Bold
////                            )
////                        }
////                    }
////                }
//
//                // Profile Avatar
//                Box(
//                    modifier = Modifier
//                        .size(44.dp)
//                        .clip(CircleShape)
//                        .background(
//                            Brush.linearGradient(
//                                colors = listOf(
//                                    Color(0xFF10B981),
//                                    Color(0xFF059669)
//                                )
//                            )
//                        )
//                        .clickable { onProfileClick() },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Person,
//                        contentDescription = "Profile",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun WelcomeSection() {
//    val currentHour = remember { LocalDateTime.now().hour }
//    val greeting = when (currentHour) {
//        in 0..11 -> "Good Morning"
//        in 12..16 -> "Good Afternoon"
//        else -> "Good Evening"
//    }
//
//    val currentDate = remember {
//        LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd"))
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp, vertical = 20.dp)
//    ) {
//        Text(
//            text = greeting,
//            fontSize = 28.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF1A1A1A)
//        )
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Text(
//            text = currentDate,
//            fontSize = 15.sp,
//            color = Color(0xFF6B7280),
//            fontWeight = FontWeight.Medium
//        )
//    }
//}
//
//@Composable
//fun QuickStatsRow() {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        QuickStatCard(
//            title = "Today's Sales",
//            value = "₹12,450",
//            icon = Icons.Outlined.TrendingUp,
//            bgColor = Color(0xFFECFDF5),
//            iconColor = Color(0xFF10B981),
//            modifier = Modifier.weight(1f)
//        )
//
//        QuickStatCard(
//            title = "Appointments",
//            value = "8",
//            icon = Icons.Outlined.CalendarToday,
//            bgColor = Color(0xFFFEF3C7),
//            iconColor = Color(0xFFF59E0B),
//            modifier = Modifier.weight(1f)
//        )
//    }
//}
//
//@Composable
//fun QuickStatCard(
//    title: String,
//    value: String,
//    icon: ImageVector,
//    bgColor: Color,
//    iconColor: Color,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier
//            .height(100.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 0.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(bgColor),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = null,
//                    tint = iconColor,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            Column {
//                Text(
//                    text = value,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF1A1A1A)
//                )
//                Text(
//                    text = title,
//                    fontSize = 12.sp,
//                    color = Color(0xFF6B7280),
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun ToolsGrid(
//    onNavigateToCalculator: () -> Unit,
//    onNavigateToPrescriptions: () -> Unit,
//    onNavigateToBillbook: () -> Unit,
//    onNavigateToNotebook: () -> Unit
//) {
//    val tools = listOf(
//        ToolCard(
//            title = "Calculator",
//            subtitle = "Lens & Power",
//            icon = Icons.Outlined.Calculate,
//            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
//            onClick = onNavigateToCalculator
//        ),
//        ToolCard(
//            title = "Prescriptions",
//            subtitle = "View & Manage",
//            icon = Icons.Outlined.Assignment,
//            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
//            onClick = onNavigateToPrescriptions
//        ),
//        ToolCard(
//            title = "Billbook",
//            subtitle = "Billing & Invoices",
//            icon = Icons.Outlined.Receipt,
//            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
//            onClick = onNavigateToBillbook
//        ),
//        ToolCard(
//            title = "Notebook",
//            subtitle = "Notes & Records",
//            icon = Icons.Outlined.Book,
//            gradientColors = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
//            onClick = onNavigateToNotebook
//        )
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        tools.chunked(2).forEach { rowTools ->
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                rowTools.forEach { tool ->
//                    ToolCardItem(
//                        tool = tool,
//                        modifier = Modifier.weight(1f)
//                    )
//                }
//
//                // Add spacer if odd number of items in last row
//                if (rowTools.size == 1) {
//                    Spacer(modifier = Modifier.weight(1f))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ToolCardItem(
//    tool: ToolCard,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier
//            .height(140.dp)
//            .clickable { tool.onClick() }
//            .animateContentSize(
//                animationSpec = spring(
//                    dampingRatio = Spring.DampingRatioMediumBouncy,
//                    stiffness = Spring.StiffnessLow
//                )
//            )
////            .dropShadow(
////                CardDefaults.shape, shadow = Shadow(radius = 10.dp, brush = Brush.radialGradient(tool.gradientColors))
////            )
//        ,
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 0.dp
//        )
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(18.dp),
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(52.dp)
//                        .clip(RoundedCornerShape(14.dp))
//                        .background(
//                            Brush.linearGradient(
//                                colors = tool.gradientColors
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = tool.icon,
//                        contentDescription = tool.title,
//                        tint = Color.White,
//                        modifier = Modifier.size(26.dp)
//                    )
//                }
//
//                Column {
//                    Text(
//                        text = tool.title,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF1A1A1A)
//                    )
//                    Text(
//                        text = tool.subtitle,
//                        fontSize = 12.sp,
//                        color = Color(0xFF6B7280),
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.padding(top = 2.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ShopDashboardSettingsCard(
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 2.dp
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    Brush.horizontalGradient(
//                        colors = listOf(
//                            Color(0xFF1E293B),
//                            Color(0xFF334155)
//                        )
//                    )
//                )
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(24.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(48.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(Color.White.copy(alpha = 0.15f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.Dashboard,
//                                contentDescription = "Dashboard",
//                                tint = Color.White,
//                                modifier = Modifier.size(26.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(16.dp))
//
//                        Column {
//                            Text(
//                                text = "Shop Dashboard",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.White
//                            )
//                            Text(
//                                text = "Settings & Configuration",
//                                fontSize = 13.sp,
//                                color = Color.White.copy(alpha = 0.7f),
//                                fontWeight = FontWeight.Medium,
//                                modifier = Modifier.padding(top = 2.dp)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(20.dp))
//
//                    // Quick Settings Options
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        SettingQuickOption(
//                            icon = Icons.Outlined.Store,
//                            label = "Store Info",
//                            modifier = Modifier.weight(1f)
//                        )
//                        SettingQuickOption(
//                            icon = Icons.Outlined.Inventory,
//                            label = "Inventory",
//                            modifier = Modifier.weight(1f)
//                        )
//                        SettingQuickOption(
//                            icon = Icons.Outlined.Group,
//                            label = "Staff",
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                // Arrow Icon
//                Box(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .background(Color.White.copy(alpha = 0.15f)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                        contentDescription = "Navigate",
//                        tint = Color.White,
//                        modifier = Modifier.size(22.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SettingQuickOption(
//    icon: ImageVector,
//    label: String,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .clip(RoundedCornerShape(10.dp))
//                .background(Color.White.copy(alpha = 0.1f)),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = label,
//                tint = Color.White.copy(alpha = 0.9f),
//                modifier = Modifier.size(20.dp)
//            )
//        }
//
//        Text(
//            text = label,
//            fontSize = 11.sp,
//            color = Color.White.copy(alpha = 0.8f),
//            fontWeight = FontWeight.Medium,
//            modifier = Modifier.padding(top = 6.dp),
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//    }
//}