package com.optictoolcompk.opticaltool.ui.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.optictoolcompk.opticaltool.data.models.ClipboardData
import com.optictoolcompk.opticaltool.data.models.ClipboardRow
import com.optictoolcompk.opticaltool.data.models.NotebookSection
import com.optictoolcompk.opticaltool.utils.ClipboardImageGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassesNotebookScreen(
    viewModel: GlassesNotebookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val selectedSectionId by viewModel.selectedSectionId.collectAsState()
    val clipboardData by viewModel.clipboardData.collectAsState()
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
                title = { Text("Glasses Notebook", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Fixed Bottom Actions
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showAddSectionDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Section")
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All Sections")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Clipboard Section at Top
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
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }

                // 2. Section Selector
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

                // 3. Main Sections List
                if (selectedSectionId == -1L) {
                    items(sections, key = { it.id }) { section ->
                        SectionCard(
                            section = section,
                            viewModel = viewModel,
                            isExpanded = true,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    val selectedSection = sections.find { it.id == selectedSectionId }
                    if (selectedSection != null) {
                        item(key = selectedSection.id) {
                            SectionCard(
                                section = selectedSection,
                                viewModel = viewModel,
                                isExpanded = true,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs...
    if (showAddSectionDialog) {
        AddSectionDialog(
            onDismiss = { showAddSectionDialog = false },
            onConfirm = { name, mode ->
                viewModel.createSection(name, mode)
                showAddSectionDialog = false
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Clear All Sections?") },
            text = { Text("This will delete all sections and data, and reset to default sections. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefault()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clipboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onScreenshot,
                        enabled = clipboardData.totalRows > 0,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Screenshot",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    FilledTonalButton(
                        onClick = { showMarkDialog = true },
                        enabled = clipboardData.totalRows > 0,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF27AE60), contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Text("Mark Order", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        enabled = clipboardData.totalRows > 0,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Text("Clear All", fontSize = 11.sp)
                    }
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sr.", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f))
                Text("Quality", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.35f), textAlign = TextAlign.Center)
                Text("Number", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.35f), textAlign = TextAlign.Center)
                Text("Pairs", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f), textAlign = TextAlign.End)
            }

            if (clipboardData.rows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No items in clipboard", color = Color.Gray)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousPage, enabled = clipboardData.currentPage > 1) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                    Text(
                        text = "Page ${clipboardData.currentPage} of ${clipboardData.totalPages}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onNextPage, enabled = clipboardData.currentPage < clipboardData.totalPages) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Clipboard?") },
            text = { Text("Clear all ${clipboardData.totalRows} items from clipboard?") },
            confirmButton = {
                TextButton(onClick = { onClearClipboard(); showClearDialog = false }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showMarkDialog) {
        AlertDialog(
            onDismissRequest = { showMarkDialog = false },
            title = { Text("Mark as Ordered?") },
            text = { Text("Mark all ${clipboardData.totalRows} clipboard items as ordered?") },
            confirmButton = {
                TextButton(onClick = { onMarkAsOrdered(); showMarkDialog = false }) { Text("Mark") }
            },
            dismissButton = {
                TextButton(onClick = { showMarkDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ClipboardRow(row: ClipboardRow) {
    val backgroundColor = if (row.globalIndex % 2 == 0) Color(0xFFF2F2F2) else Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.globalIndex.toString(),
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.bodyMedium
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
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EmptyClipboardRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = " ", modifier = Modifier.weight(0.15f), style = MaterialTheme.typography.bodyMedium)
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
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable, true),
            colors = OutlinedTextFieldDefaults.colors()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sections.forEach { section ->
                DropdownMenuItem(text = { Text(section.name) }, onClick = { onSectionSelected(section.id); expanded = false })
            }
            HorizontalDivider()
            DropdownMenuItem(text = {
                Text(
                    "View All Sections",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }, onClick = { onViewAllSelected(); expanded = false })
        }
    }
}
