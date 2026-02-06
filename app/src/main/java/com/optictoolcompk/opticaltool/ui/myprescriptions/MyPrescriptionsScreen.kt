package com.optictoolcompk.opticaltool.ui.myprescriptions

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.optictoolcompk.opticaltool.data.models.PrescriptionEntity
import com.optictoolcompk.opticaltool.data.models.PrescriptionSortOption
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.PrescriptionViewModel
import com.optictoolcompk.opticaltool.utils.ImageCompressionUtil
import com.optictoolcompk.opticaltool.utils.ShareUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionListScreen(
    listViewModel: PrescriptionListViewModel = hiltViewModel(),
    prescriptionViewModel: PrescriptionViewModel = hiltViewModel(),
    onCreateNewPrescription: () -> Unit,
    onEditPrescription: (PrescriptionEntity) -> Unit,
    onCalculateTranspose: (PrescriptionEntity) -> Unit
) {
    val prescriptions by listViewModel.prescriptions.collectAsState()
    val filterState by listViewModel.filterState.collectAsState()
    val deleteState by listViewModel.deleteState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImagePath by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var prescriptionToDelete by remember { mutableStateOf<PrescriptionEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Handle delete success
    LaunchedEffect(deleteState) {
        if (deleteState is DeleteState.Success) {
            showDeleteDialog = false
            prescriptionToDelete = null
            listViewModel.resetDeleteState()
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    Toast.makeText(context, "Compressing image...", Toast.LENGTH_SHORT).show()
                    val compressedFile = ImageCompressionUtil.compressImage(context, it)
                    if (compressedFile != null) {
                        prescriptionViewModel.saveImageOnlyPrescription(compressedFile)
                        Toast.makeText(
                            context,
                            "Prescription saved successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Camera launcher
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            scope.launch {
                try {
                    Toast.makeText(context, "Compressing image...", Toast.LENGTH_SHORT).show()
                    val compressedFile =
                        ImageCompressionUtil.compressImage(context, capturedImageUri!!)
                    if (compressedFile != null) {
                        prescriptionViewModel.saveImageOnlyPrescription(compressedFile)
                        Toast.makeText(
                            context,
                            "Prescription saved successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "prescription_${System.currentTimeMillis()}.jpg")
            capturedImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(capturedImageUri!!)
        }
    }

    var fabMenuExpanded by remember { mutableStateOf(false) }
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Prescriptions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                    ) {
                        Icon(
                            imageVector = if (fabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = { fabMenuExpanded = false; onCreateNewPrescription() },
                    icon = { Icon(Icons.Default.Edit, "Create Form") },
                    text = { Text("Fill Form") }
                )
                FloatingActionButtonMenuItem(
                    onClick = { fabMenuExpanded = false; imagePickerLauncher.launch("image/*") },
                    icon = { Icon(Icons.Default.Photo, "Upload Image") },
                    text = { Text("Upload from Gallery") }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val photoFile = File(
                                context.cacheDir,
                                "prescription_${System.currentTimeMillis()}.jpg"
                            )
                            capturedImageUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            cameraLauncher.launch(capturedImageUri!!)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    icon = { Icon(Icons.Default.CameraAlt, "Capture Photo") },
                    text = { Text("Capture Photo") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Search and Sort Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = { listViewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search name, phone, #") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            PrescriptionSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (option == filterState.sortBy) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            Text(option.displayName)
                                        }
                                    },
                                    onClick = {
                                        listViewModel.updateSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Status Bar
            Text(
                text = "Showing ${prescriptions.size} prescriptions",
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp, end = 20.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Prescription List
            if (prescriptions.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = prescriptions, key = { it.id }) { prescription ->
                        PrescriptionCard(
                            prescription = prescription,
                            onViewImage = {
                                selectedImagePath = prescription.prescriptionImagePath
                                showImageDialog = true
                            },
                            onEdit = { onEditPrescription(prescription) },
                            onDelete = {
                                prescriptionToDelete = prescription
                                showDeleteDialog = true
                            },
                            onShare = {
                                ShareUtils.sharePrescriptionImage(
                                    context,
                                    it.prescriptionImagePath
                                )
                            },
                            onCalculateTranspose = { onCalculateTranspose(it) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showImageDialog && selectedImagePath.isNotEmpty()) {
        ImagePreviewDialog(imagePath = selectedImagePath, onDismiss = { showImageDialog = false })
    }

    if (showDeleteDialog && prescriptionToDelete != null) {
        DeleteConfirmationDialog(
            prescription = prescriptionToDelete!!,
            deleteState = deleteState,
            onConfirm = { listViewModel.deletePrescription(prescriptionToDelete!!) },
            onDismiss = { showDeleteDialog = false; prescriptionToDelete = null }
        )
    }
}

@Composable
fun PrescriptionCard(
    prescription: PrescriptionEntity,
    onViewImage: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: (PrescriptionEntity) -> Unit,
    onCalculateTranspose: (PrescriptionEntity) -> Unit
) {
    var showMoreOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prescription.prescriptionNumber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDate(prescription.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (prescription.prescriptionImagePath.isNotEmpty()) {
                        IconButton(onClick = onViewImage, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.Default.Visibility,
                                "View Image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFF44336))
                    }

                    Box {
                        IconButton(
                            onClick = { showMoreOptions = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, "More options")
                        }

                        DropdownMenu(
                            expanded = showMoreOptions,
                            onDismissRequest = { showMoreOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    showMoreOptions = false
                                    onShare(prescription)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Share,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Calculate Transpose") },
                                onClick = {
                                    showMoreOptions = false
                                    onCalculateTranspose(prescription)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Calculate,
                                        null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Name", value = prescription.patientName)
            InfoRow(label = "Phone", value = prescription.phone)
            if (prescription.age.isNotEmpty()) InfoRow(label = "Age", value = prescription.age)
            if (prescription.city.isNotEmpty()) InfoRow(label = "City", value = prescription.city)

            if (prescription.rightSph.isNotEmpty() || prescription.leftSph.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (prescription.rightSph.isNotEmpty()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Right Eye",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Text(
                                "SPH: ${formatValue(prescription.rightSph)}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                    if (prescription.leftSph.isNotEmpty()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Left Eye",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Text(
                                "SPH: ${formatValue(prescription.leftSph)}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)) {
            Text(
                "$label:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.width(80.dp)
            )
            Text(
                value,
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ImagePreviewDialog(imagePath: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prescription Image", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val file = File(imagePath)
                if (file.exists()) {
                    val bitmap = remember(imagePath) { BitmapFactory.decodeFile(imagePath) }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Prescription Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else ErrorImageView()
                } else ErrorImageView()
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Close") }
            }
        }
    }
}

@Composable
fun ErrorImageView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Image not available", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun DeleteConfirmationDialog(
    prescription: PrescriptionEntity,
    deleteState: DeleteState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (deleteState !is DeleteState.Deleting) onDismiss() },
        title = { Text("Delete Prescription", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Are you sure you want to delete this prescription?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${prescription.prescriptionNumber} - ${prescription.patientName}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (deleteState is DeleteState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        deleteState.message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = deleteState !is DeleteState.Deleting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                if (deleteState is DeleteState.Deleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (deleteState is DeleteState.Deleting) "Deleting..." else "Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = deleteState !is DeleteState.Deleting
            ) { Text("Cancel") }
        }
    )
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "No Prescriptions Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap the + button to create your first prescription",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun formatValue(value: String): String {
    val num = value.toDoubleOrNull() ?: return value
    return if (num >= 0) "+$value" else value
}
