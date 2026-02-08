package com.optictoolcompk.opticaltool.ui.notebook

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Section Header
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
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            // Mode Selection
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.updateSectionMode(section.id, NotebookMode.SPH_CYL)
                            }
                    ) {
                        RadioButton(
                            selected = section.mode == NotebookMode.SPH_CYL,
                            onClick = {
                                viewModel.updateSectionMode(section.id, NotebookMode.SPH_CYL)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "SPH / CYL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateSectionMode(section.id, NotebookMode.KT) }
                    ) {
                        RadioButton(
                            selected = section.mode == NotebookMode.KT,
                            onClick = { viewModel.updateSectionMode(section.id, NotebookMode.KT) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "KT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Add Row Form
            if (isExpanded) {
                AddRowForm(
                    section = section,
                    onAddRow = { sph, cyl, pairs ->
                        viewModel.addRow(section.id, sph, cyl, pairs, section.mode)
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Table Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Number",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Text(
                            "Pairs",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Text(
                            "Copy",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Text(
                            "Order",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Text(
                            "Del",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
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

            Spacer(Modifier.height(16.dp))

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Dropdown
                Box {
                    Button(
                        onClick = { showDeleteMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp))
                    }

                    DropdownMenu(
                        expanded = showDeleteMenu,
                        onDismissRequest = { showDeleteMenu = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Marked Copy",
                                    color = Color(0xFF2196F3)
                                )
                            },
                            onClick = {
                                viewModel.deleteCopiedRowsInSection(section.id)
                                showDeleteMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Marked Order",
                                    color = Color(0xFF4CAF50)
                                )
                            },
                            onClick = {
                                viewModel.deleteOrderedRowsInSection(section.id)
                                showDeleteMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Marked Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                viewModel.deleteMarkedRowsInSection(section.id)
                                showDeleteMenu = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete This Section",
                                    color = MaterialTheme.colorScheme.error,
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

                // Move Arrows (compact)
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.moveSectionUp(section.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.moveSectionDown(section.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }


                // Mark/Unmark Dropdown
                Box {
                    Button(
                        onClick = { showMarkMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp))
                    }

                    DropdownMenu(
                        expanded = showMarkMenu,
                        onDismissRequest = { showMarkMenu = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Mark All",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        DropdownMenuItem(
                            text = { Text("Mark All Copy", color = Color(0xFF2196F3)) },
                            onClick = {
                                viewModel.markAllCopyInSection(section.id, true)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark All Order", color = Color(0xFF4CAF50)) },
                            onClick = {
                                viewModel.markAllOrderedInSection(section.id, true)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Mark All Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                viewModel.markAllDeleteInSection(section.id, true)
                                showMarkMenu = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            "Unmark All",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        DropdownMenuItem(
                            text = { Text("Unmark All Copy", color = Color(0xFF2196F3)) },
                            onClick = {
                                viewModel.markAllCopyInSection(section.id, false)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Unmark All Order", color = Color(0xFF4CAF50)) },
                            onClick = {
                                viewModel.markAllOrderedInSection(section.id, false)
                                showMarkMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Unmark All Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
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

    // Delete Section Dialog
    if (showDeleteSectionDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSectionDialog = false },
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
            title = {
                Text(
                    "Delete Section?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Delete section '${section.name}' and all its ${section.rowCount} rows?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSection(section.id)
                        showDeleteSectionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSectionDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
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

    val sphOptions = remember(section.mode) { generateSphOptions(section.mode) }
    val cylOptions = remember(section.mode) { generateCylOptions(section.mode) }

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        label = { Text(sphLabel, fontSize = 10.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sphExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable),
                        isError = showValidationError && sphValue.isEmpty(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = sphExpanded,
                        onDismissRequest = { sphExpanded = false },
                        modifier = Modifier.height(250.dp)
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
                        label = { Text(cylLabel, fontSize = 10.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = cylExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.SecondaryEditable),
                        isError = showValidationError && cylValue.isEmpty(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = cylExpanded,
                        onDismissRequest = { cylExpanded = false },
                        modifier = Modifier.height(250.dp)
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
                    label = { Text("Pairs", fontSize = 12.sp) },
                    modifier = Modifier.weight(0.8f),
                    isError = showValidationError && pairs.isEmpty(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),

                    )
            }

            Spacer(Modifier.height(12.dp))

            // Add Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${section.rowCount} rows",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val isValid = when (section.mode) {
                            NotebookMode.KT -> cylValue.isNotEmpty() && pairs.isNotEmpty()
                            NotebookMode.SPH_CYL -> (sphValue.isNotEmpty() || cylValue.isNotEmpty()) && pairs.isNotEmpty()
                        }

                        if (isValid) {
                            val finalSph = sphValue.ifEmpty { "0.00" }
                            val finalCyl = cylValue.ifEmpty { "0.00" }

                            onAddRow(finalSph, finalCyl, pairs.toIntOrNull() ?: 0)

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
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Row", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
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
    val backgroundColor = MaterialTheme.colorScheme.surface

    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = row.getFormattedNumber(),
                modifier = Modifier.weight(1.5f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = row.pairs.toString(),
                modifier = Modifier.weight(0.7f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            CustomCheckbox(
                checked = row.isCopy,
                onCheckedChange = { onToggleCopy() },
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(0.7f)
            )

            CustomCheckbox(
                checked = row.isOrdered,
                onCheckedChange = { onToggleOrdered() },
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(0.7f)
            )

            CustomCheckbox(
                checked = row.isDelete,
                onCheckedChange = { onToggleDelete() },
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(0.7f)
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally)
    ) {
        Surface(
            onClick = onCheckedChange,
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = if (checked) color else MaterialTheme.colorScheme.outline
            ),
            color = if (checked) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
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
        title = {
            Text(
                "Add New Section",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Section Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Mode:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMode = NotebookMode.SPH_CYL }
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedMode == NotebookMode.SPH_CYL,
                                    onClick = { selectedMode = NotebookMode.SPH_CYL },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("SPH / CYL", fontWeight = FontWeight.Medium)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMode = NotebookMode.KT }
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedMode == NotebookMode.KT,
                                    onClick = { selectedMode = NotebookMode.KT },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("KT", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedMode)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add Section")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// Helper functions
private fun generateSphOptions(mode: NotebookMode): List<String> {
    val options = mutableListOf<String>()

    when (mode) {
        NotebookMode.KT -> {
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
            options.add("0.00")
            for (i in 1..12) {
                val value = String.format(Locale.getDefault(), "%.2f", i * 0.25)
                options.add("+$value")
            }
        }

        NotebookMode.SPH_CYL -> {
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
