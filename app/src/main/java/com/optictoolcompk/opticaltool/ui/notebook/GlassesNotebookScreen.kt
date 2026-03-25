package com.optictoolcompk.opticaltool.ui.notebook

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.optictoolcompk.opticaltool.data.models.ClipboardData
import com.optictoolcompk.opticaltool.data.models.ClipboardRow
import com.optictoolcompk.opticaltool.data.models.NotebookSection
import com.optictoolcompk.opticaltool.utils.ClipboardImageGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassesNotebookScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: GlassesNotebookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val selectedSectionId by viewModel.selectedSectionId.collectAsStateWithLifecycle()
    val clipboardData by viewModel.clipboardData.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Glasses Notebook",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clipboard Section
                item {
                    ClipboardSection(
                        clipboardData = clipboardData,
                        onClearClipboard = { viewModel.clearClipboard() },
                        onMarkAsOrdered = { viewModel.markClipboardAsOrdered() },
                        onPreviousPage = { viewModel.goToPreviousClipboardPage() },
                        onNextPage = { viewModel.goToNextClipboardPage() },
                        onScreenshot = {
                            scope.launch {
                                ClipboardImageGenerator.generateAndSaveImage(
                                    context = context,
                                    rows = clipboardData.rows,
                                    pageNumber = clipboardData.currentPage,
                                    totalPages = clipboardData.totalPages
                                )
                            }
                        }
                    )
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // Section Selector
                if (sections.isNotEmpty()) {
                    item {
                        SectionDropdown(
                            sections = sections,
                            selectedSectionId = selectedSectionId,
                            onSectionSelected = { viewModel.selectSection(it) },
                            onViewAllSelected = { viewModel.selectViewAllSections() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Sections List
                if (selectedSectionId == -1L) {
                    items(sections, key = { it.id }) { section ->
                        SectionCard(
                            snackbarHostState = snackbarHostState,
                            section = section,
                            viewModel = viewModel,
                            isExpanded = true,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    val selectedSection = sections.find { it.id == selectedSectionId }
                    if (selectedSection != null) {
                        item(key = selectedSection.id) {
                            SectionCard(
                                snackbarHostState = snackbarHostState,
                                section = selectedSection,
                                viewModel = viewModel,
                                isExpanded = true,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        tonalElevation = 8.dp,
                        shadowElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showAddSectionDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Add Section", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showResetDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.error
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Clear All", fontWeight = FontWeight.Bold)
                            }
                        }

                    }
                }
            }
        }
    }

    // Add Section Dialog
    if (showAddSectionDialog) {
        AddSectionDialog(
            onDismiss = { showAddSectionDialog = false },
            onConfirm = { name, mode ->
                viewModel.createSection(name, mode)
                showAddSectionDialog = false
            }
        )
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            title = {
                Text(
                    "Clear All Sections?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will delete all sections and data, and reset to default sections. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefault()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ClipboardSection(
    clipboardData: ClipboardData,
    onClearClipboard: () -> Unit,
    onMarkAsOrdered: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onScreenshot: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showMarkDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Clipboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${clipboardData.totalRows} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.7f
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onScreenshot,
                            enabled = clipboardData.totalRows > 0,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (clipboardData.totalRows > 0) MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.15f
                                    )
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                "Screenshot",
                                tint = if (clipboardData.totalRows > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Button(
                            onClick = { showMarkDialog = true },
                            enabled = clipboardData.totalRows > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Mark", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showClearDialog = true },
                            enabled = clipboardData.totalRows > 0,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (clipboardData.totalRows > 0) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.outlineVariant
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Table Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Sr.",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.15f),
                        fontSize = 14.sp
                    )
                    Text(
                        "Quality",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.35f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        "Number",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.35f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        "Pairs",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.15f),
                        textAlign = TextAlign.End,
                        fontSize = 14.sp
                    )
                }
            }

            // Table Rows
            if (clipboardData.rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Visibility,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No items in clipboard",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    clipboardData.rows.forEach { row ->
                        ClipboardRow(row)
                    }
                    val emptyRowCount = clipboardData.rowsPerPage - clipboardData.rows.size
                    repeat(emptyRowCount) {
                        EmptyClipboardRow()
                    }
                }
            }

            // Pagination
            if (clipboardData.totalRows > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPreviousPage,
                            enabled = clipboardData.currentPage > 1
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Previous",
                                tint = if (clipboardData.currentPage > 1) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Page ${clipboardData.currentPage} of ${clipboardData.totalPages}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        IconButton(
                            onClick = onNextPage,
                            enabled = clipboardData.currentPage < clipboardData.totalPages
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                "Next",
                                tint = if (clipboardData.currentPage < clipboardData.totalPages) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Clear Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            title = { Text("Clear Clipboard?", fontWeight = FontWeight.Bold) },
            text = { Text("Clear all ${clipboardData.totalRows} items from clipboard?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearClipboard()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Mark Dialog
    if (showMarkDialog) {
        AlertDialog(
            onDismissRequest = { showMarkDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF4CAF50)
                    )
                }
            },
            title = { Text("Mark as Ordered?", fontWeight = FontWeight.Bold) },
            text = { Text("Mark all ${clipboardData.totalRows} clipboard items as ordered?") },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkAsOrdered()
                        showMarkDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Mark")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ClipboardRow(row: ClipboardRow) {
    val backgroundColor =
        if (row.globalIndex % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.globalIndex.toString(),
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = row.sectionName,
            modifier = Modifier.weight(0.35f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = row.getFormattedNumber(),
            modifier = Modifier.weight(0.35f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = row.pairs.toString(),
            modifier = Modifier.weight(0.15f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyClipboardRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = " ",
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(text = " ", modifier = Modifier.weight(0.35f), textAlign = TextAlign.Center)
        Text(text = " ", modifier = Modifier.weight(0.35f), textAlign = TextAlign.Center)
        Text(text = " ", modifier = Modifier.weight(0.15f), textAlign = TextAlign.End)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionDropdown(
    sections: List<NotebookSection>,
    selectedSectionId: Long?,
    onSectionSelected: (Long) -> Unit,
    onViewAllSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = when (selectedSectionId) {
        -1L -> "View All Sections"
        null -> "Select Section"
        else -> sections.find { it.id == selectedSectionId }?.name ?: "Select Section"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text("View Section") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable, true),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            sections.forEach { section ->
                DropdownMenuItem(
                    text = { Text(section.name) },
                    onClick = {
                        onSectionSelected(section.id)
                        expanded = false
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = {
                    Text(
                        "View All Sections",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    onViewAllSelected()
                    expanded = false
                }
            )
        }
    }
}
