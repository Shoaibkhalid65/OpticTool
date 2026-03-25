package com.optictoolcompk.opticaltool.ui.shopdashboard


import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShopDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCurrencyMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Inside ShopDashboardScreen.kt

// Handle save success and navigate back
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            // 1. Show feedback to user
            Toast.makeText(context, "Settings updated!", Toast.LENGTH_SHORT).show()

            // 2. Clear the state in ViewModel (optional but good practice)
            viewModel.clearSaveSuccess()

            // 3. NAVIGATE BACK
            onNavigateBack()
        }
    }

    // Add this at the top of ShopDashboardScreen
    var showExitConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.hasChanges) {
        showExitConfirmation = true
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have modified your shop settings. Do you want to discard changes and leave?") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shop Profile", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (uiState.hasChanges) {
                        TextButton(onClick = { viewModel.saveSettings() }) {
                            Text("SAVE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Branding Header
                    item {
                        ShopHeaderSection(uiState.shopName)
                    }


                    // 2. Help/Tip Section (Re-integrated and Styled)
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        "Public Profile Info",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "These details appear on your PDFs, Prints, and WhatsApp shares. Tap any field to edit.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 2. Business Identity Group
                    item {
                        SettingsGroup(title = "Business Identity") {
                            DashboardTextField(
                                value = uiState.shopName,
                                onValueChange = { viewModel.onShopNameChanged(it) },
                                label = "Shop Name",
                                icon = Icons.Default.Store
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                            DashboardTextField(
                                value = uiState.shopPhone,
                                onValueChange = { viewModel.onShopPhoneChanged(it) },
                                label = "Business Contact",
                                icon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }

                    // 3. Location & Localization
                    item {
                        SettingsGroup(title = "Location & Currency") {
                            DashboardTextField(
                                value = uiState.shopAddress,
                                onValueChange = { viewModel.onShopAddressChanged(it) },
                                label = "Physical Address",
                                icon = Icons.Default.LocationOn,
                                singleLine = false
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))

                            // Currency Selector
                            CurrencySelectorRow(
                                selectedCurrency = uiState.currency,
                                onClick = { showCurrencyMenu = true }
                            )
                        }
                    }

                    // 4. Billing Footer Settings
                    item {
                        SettingsGroup(title = "Receipt Footer") {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Terms & Conditions",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = uiState.termsAndConditions,
                                    onValueChange = { viewModel.onTermsAndConditionsChanged(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. No refund without receipt", fontSize = 14.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    // 5. Save Action
                    item {
                        Button(
                            onClick = { viewModel.saveSettings() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = uiState.hasChanges && !uiState.isSaving,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Update Shop Settings", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Currency Dropdown
    if (showCurrencyMenu) {
        CurrencyPickerDialog(
            onDismiss = { showCurrencyMenu = false },
            onSelect = {
                viewModel.onCurrencyChanged(it)
                showCurrencyMenu = false
            },
            current = uiState.currency
        )
    }
}

@Composable
fun CurrencyPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    current: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Select Currency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            // Using a Column to list out the common currencies
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                listOf("Rs", "₹", "$", "€", "£", "¥", "AED", "SAR").forEach { currency ->
                    val isSelected = currency == current

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(currency) },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            letterSpacing = 1.sp
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            content = content
        )
    }
}

@Composable
fun DashboardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
        // Added Trailing Icon to signal "Editability"
        trailingIcon = {
            Icon(
                Icons.Default.Edit,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun ShopHeaderSection(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Store, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(name.ifBlank { "Your Shop" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Settings & Customization", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun CurrencySelectorRow(selectedCurrency: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Default Currency", style = MaterialTheme.typography.bodyLarge)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(selectedCurrency, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
        }
    }
}