package com.optictoolcompk.opticaltool.ui.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.optictoolcompk.opticaltool.data.models.NotebookMode
import com.optictoolcompk.opticaltool.data.models.NotebookRow
import com.optictoolcompk.opticaltool.data.models.NotebookSection
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionCard(
    section: NotebookSection,
    viewModel: GlassesNotebookViewModel,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    var showDeleteMenu by remember { mutableStateOf(false) }
    var showMarkMenu by remember { mutableStateOf(false) }
    var showDeleteSectionDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header with Name
            var sectionName by remember(section.name) { mutableStateOf(section.name) }


            OutlinedTextField(
                value = sectionName,
                onValueChange = { value ->
                    sectionName = value
                    viewModel.updateSectionName(section.id, value)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Enter Quality Name",
                        textAlign = TextAlign.Center
                    )
                }
            )


            Spacer(Modifier.height(12.dp))

            // Mode Selection (SPH/CYL or KT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = section.mode == NotebookMode.SPH_CYL,
                        onClick = { viewModel.updateSectionMode(section.id, NotebookMode.SPH_CYL) }
                    )
                    Text("SPH / CYL", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.width(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = section.mode == NotebookMode.KT,
                        onClick = { viewModel.updateSectionMode(section.id, NotebookMode.KT) }
                    )
                    Text("KT", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Add Row Form
            if (isExpanded) {
                AddRowForm(
                    section = section,
                    onAddRow = { sph, cyl, pairs ->
                        viewModel.addRow(section.id, sph, cyl, pairs, section.mode)
                    }
                )

                Spacer(Modifier.height(8.dp))


                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Number",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Pairs",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Copy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Order",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                // Rows
                section.rows.forEach { row ->
                    NotebookRowItem(
                        row = row,
                        onToggleCopy = { viewModel.toggleCopyFlag(row.id) },
                        onToggleOrdered = { viewModel.toggleOrderedFlag(row.id) },
                        onToggleDelete = { viewModel.toggleDeleteFlag(row.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Delete Dropdown Button
                Box {
                    Button(
                        onClick = { showDeleteMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC3545)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Delete ▼", fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = showDeleteMenu,
                        onDismissRequest = { showDeleteMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Mark Copy", color = Color(0xFF1976D2)) },
                            onClick = {
                                viewModel.deleteCopiedRowsInSection(section.id)
                                showDeleteMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Mark Order", color = Color(0xFF388E3C)) },
                            onClick = {
                                viewModel.deleteOrderedRowsInSection(section.id)
                                showDeleteMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Mark Delete", color = Color(0xFFD32F2F)) },
                            onClick = {
                                viewModel.deleteMarkedRowsInSection(section.id)
                                showDeleteMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete This Section",
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                showDeleteSectionDialog = true
                                showDeleteMenu = false
                            }
                        )
                    }
                }

                // Move Arrows
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.moveSectionUp(section.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { viewModel.moveSectionDown(section.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = Color.Gray
                        )
                    }
                }

                // Mark/Unmark Dropdown Button
                Box {
                    Button(
                        onClick = { showMarkMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C757D)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Mark/Unmark ▼", fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = showMarkMenu,
                        onDismissRequest = { showMarkMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mark All Copy", color = Color(0xFF1976D2)) },
                            onClick = {
                                viewModel.markAllCopyInSection(section.id, true)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark All Order", color = Color(0xFF388E3C)) },
                            onClick = {
                                viewModel.markAllOrderedInSection(section.id, true)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark All Delete", color = Color(0xFFD32F2F)) },
                            onClick = {
                                viewModel.markAllDeleteInSection(section.id, true)
                                showMarkMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Unmark All Copy", color = Color(0xFF1976D2)) },
                            onClick = {
                                viewModel.markAllCopyInSection(section.id, false)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Unmark All Order", color = Color(0xFF388E3C)) },
                            onClick = {
                                viewModel.markAllOrderedInSection(section.id, false)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Unmark All Delete", color = Color(0xFFD32F2F)) },
                            onClick = {
                                viewModel.markAllDeleteInSection(section.id, false)
                                showMarkMenu = false
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Section Confirmation Dialog
    if (showDeleteSectionDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSectionDialog = false },
            title = { Text("Delete Section?") },
            text = { Text("Delete section '${section.name}' and all its ${section.rowCount} rows?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSection(section.id)
                        showDeleteSectionDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRowForm(
    section: NotebookSection,
    onAddRow: (String, String, Int) -> Unit
) {
    var sphValue by remember { mutableStateOf("") }
    var cylValue by remember { mutableStateOf("") }
    var pairs by remember { mutableStateOf("") }
    var showValidationError by remember { mutableStateOf(false) }

    val sphLabel = if (section.mode == NotebookMode.KT) "Dist" else "SPH"
    val cylLabel = if (section.mode == NotebookMode.KT) "ADD" else "CYL"

    // Generate dropdown options based on mode
    val sphOptions = remember(section.mode) {
        generateSphOptions(section.mode)
    }

    val cylOptions = remember(section.mode) {
        generateCylOptions(section.mode)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // SPH Dropdown
        var sphExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = sphExpanded,
            onExpandedChange = { sphExpanded = !sphExpanded },
            modifier = Modifier.weight(1.3f)
        ) {
            OutlinedTextField(
                value = sphValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(sphLabel, fontSize = 11.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sphExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable),
                isError = showValidationError && sphValue.isEmpty(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            ExposedDropdownMenu(
                expanded = sphExpanded,
                onDismissRequest = { sphExpanded = false },
                modifier = Modifier
                    .height(250.dp)
            ) {
                sphOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = {
                            sphValue = option
                            sphExpanded = false
                            showValidationError = false
                        }
                    )
                }
            }
        }

        // CYL Dropdown
        var cylExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = cylExpanded,
            onExpandedChange = { cylExpanded = !cylExpanded },
            modifier = Modifier.weight(1.3f)
        ) {
            OutlinedTextField(
                value = cylValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(cylLabel, fontSize = 11.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cylExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable),
                isError = showValidationError && cylValue.isEmpty(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            ExposedDropdownMenu(
                expanded = cylExpanded,
                onDismissRequest = { cylExpanded = false },
                modifier = Modifier
                    .height(250.dp)
            ) {
                cylOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = {
                            cylValue = option
                            cylExpanded = false
                            showValidationError = false
                        }
                    )
                }
            }
        }

        // Pairs Input
        OutlinedTextField(
            value = pairs,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    pairs = it
                    showValidationError = false
                }
            },
            label = { Text("Pairs", fontSize = 11.sp) },
            modifier = Modifier.weight(0.8f),
            isError = showValidationError && pairs.isEmpty(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    Spacer(Modifier.height(8.dp))

    // Add Button
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "${section.rowCount} rows",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
        )
        Button(
            onClick = {
                // Validation
                val isValid = when (section.mode) {
                    NotebookMode.KT -> cylValue.isNotEmpty() && pairs.isNotEmpty()
                    NotebookMode.SPH_CYL -> (sphValue.isNotEmpty() || cylValue.isNotEmpty()) && pairs.isNotEmpty()
                }

                if (isValid) {
                    val finalSph = sphValue.ifEmpty { "0.00" }
                    val finalCyl = cylValue.ifEmpty { "0.00" }

                    onAddRow(finalSph, finalCyl, pairs.toIntOrNull() ?: 0)

                    // Clear form
                    sphValue = ""
                    cylValue = ""
                    pairs = ""
                    showValidationError = false
                } else {
                    showValidationError = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Add Row", fontSize = 12.sp)
        }
    }
}

@Composable
fun NotebookRowItem(
    row: NotebookRow,
    onToggleCopy: () -> Unit,
    onToggleOrdered: () -> Unit,
    onToggleDelete: () -> Unit
) {
    val backgroundColor = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number
        Text(
            text = row.getFormattedNumber(),
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        // Pairs
        Text(
            text = row.pairs.toString(),
            modifier = Modifier.weight(0.7f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        // Copy Checkbox (Blue)
        CustomCheckbox(
            checked = row.isCopy,
            onCheckedChange = { onToggleCopy() },
            color = Color(0xFF3498DB),
            modifier = Modifier.weight(0.7f)
        )

        // Ordered Checkbox (Green)
        CustomCheckbox(
            checked = row.isOrdered,
            onCheckedChange = { onToggleOrdered() },
            color = Color(0xFF27AE60),
            modifier = Modifier.weight(0.7f)
        )

        // Delete Checkbox (Red)
        CustomCheckbox(
            checked = row.isDelete,
            onCheckedChange = { onToggleDelete() },
            color = Color(0xFFD32F2F),
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .size(20.dp)
            .border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(2.dp)
            )
            .background(
                color = if (checked) color.copy(alpha = 0.1f) else Color.White,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Text(
                text = "✓",
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddSectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, NotebookMode) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(NotebookMode.SPH_CYL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Section") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Section Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Mode:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedMode == NotebookMode.SPH_CYL,
                        onClick = { selectedMode = NotebookMode.SPH_CYL }
                    )
                    Text("SPH / CYL")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedMode == NotebookMode.KT,
                        onClick = { selectedMode = NotebookMode.KT }
                    )
                    Text("KT")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedMode)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper functions for generating dropdown options
private fun generateSphOptions(mode: NotebookMode): List<String> {
    val options = mutableListOf<String>()

    when (mode) {
        NotebookMode.KT -> {
            // KT Mode: -3.00 to +3.00
            for (i in 12 downTo 1) {
                val value = (i * 0.25).toString()
                options.add("-$value")
            }
            options.add("0.00")
            for (i in 1..12) {
                val value = (i * 0.25).toString()
                options.add("+$value")
            }
        }

        NotebookMode.SPH_CYL -> {
            // SPH/CYL Mode: -24.00 to +24.00
            for (i in 96 downTo 1) {
                val value = String.format(Locale.getDefault(), "%.2f", i * 0.25)
                options.add("+$value")
            }
            options.add("0.00")
            for (i in 1..96) {
                val value = String.format(Locale.getDefault(), "%.2f", i * 0.25)
                options.add("-$value")
            }
        }
    }

    return options
}

private fun generateCylOptions(mode: NotebookMode): List<String> {
    val options = mutableListOf<String>()

    when (mode) {
        NotebookMode.KT -> {
            // KT Mode: 0.00 to +3.00 only
            options.add("0.00")
            for (i in 1..12) {
                val value = String.format(Locale.getDefault(), "%.2f", i * 0.25)
                options.add("+$value")
            }
        }

        NotebookMode.SPH_CYL -> {
            // SPH/CYL Mode: -6.00 to +6.00
            for (i in 24 downTo 1) {
                val value = String.format(Locale.getDefault(), "%.2f", i * 0.25)
                options.add("+$value")
            }
            options.add("0.00")
            for (i in 1..24) {
                val value = String.format(Locale.getDefault(), "%.2f", i * 0.25)
                options.add("-$value")
            }
        }
    }

    return options
}

