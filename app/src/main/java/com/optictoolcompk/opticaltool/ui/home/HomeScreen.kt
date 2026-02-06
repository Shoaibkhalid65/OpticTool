package com.optictoolcompk.opticaltool.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToPrescriptions: () -> Unit,
    onNavigateToBillBook: () -> Unit,
    onNavigateToNotebook: () -> Unit,
    onNavigateToShopSetting: ()-> Unit,
    onNavigateToProfile:()-> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Optic Tool",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(
                        onClick = onNavigateToProfile
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "profile icon"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            // Welcome Section
            WelcomeSection()

            // Tools Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
            }

            Card(
                modifier = Modifier.padding(24.dp).fillMaxWidth().height(200.dp),
                onClick = onNavigateToShopSetting
            ) {
                Text(
                    text = "Shop Setting"
                )
            }
        }
    }
}

@Composable
fun WelcomeSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    "Welcome to Optic Tool",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Your complete optical management solution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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