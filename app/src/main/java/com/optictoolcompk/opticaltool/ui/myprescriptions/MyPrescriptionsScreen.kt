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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onCalculateTranspose: (PrescriptionEntity) -> Unit,
    onNavigateBack: () -> Unit
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
    var fabMenuExpanded by remember { mutableStateOf(false) }

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
                            "Prescription saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                            "Prescription saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prescriptions",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                        containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                            initialColor = MaterialTheme.colorScheme.primary,
                            finalColor = MaterialTheme.colorScheme.secondary
                        ),
                    ) {
                        val color= MaterialTheme.colorScheme.surface
                        Icon(
                            imageVector = if (fabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress }, color = {color }),
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        onCreateNewPrescription()
                    },
                    icon = { Icon(Icons.Default.Edit, "Create Form") },
                    text = { Text("Fill Form") }
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        imagePickerLauncher.launch("image/*")
                    },
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
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            // Search and Sort Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = filterState.searchQuery,
                            onValueChange = { listViewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            placeholder = {
                                Text(
                                    "Search by name, phone, #...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        Box {
                            Surface(
                                onClick = { showSortMenu = true },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = "Sort",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                shape = RoundedCornerShape(12.dp),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                PrescriptionSortOption.entries.forEach { option ->

                                    val isSelected = option == filterState.sortBy

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                option.displayName,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            listViewModel.updateSortOption(option)
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = MaterialTheme.colorScheme.onSurface,
                                            trailingIconColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected)
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.35f
                                                    )
                                                else
                                                    Color.Transparent
                                            )
                                    )
                                }
                            }

                        }
                    }

                    // Stats row
                    Text(
                        text = "Total Prescriptions: ${prescriptions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                    )
                }
            }

            // Prescription List
            if (prescriptions.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = prescriptions, key = { it.id }) { prescription ->
                        CompactPrescriptionCard(
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
        ImagePreviewDialog(
            imagePath = selectedImagePath,
            onDismiss = { showImageDialog = false }
        )
    }

    if (showDeleteDialog && prescriptionToDelete != null) {
        DeleteConfirmationDialog(
            prescription = prescriptionToDelete!!,
            deleteState = deleteState,
            onConfirm = { listViewModel.deletePrescription(prescriptionToDelete!!) },
            onDismiss = {
                showDeleteDialog = false
                prescriptionToDelete = null
            }
        )
    }
}

@Composable
fun CompactPrescriptionCard(
    prescription: PrescriptionEntity,
    onViewImage: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: (PrescriptionEntity) -> Unit,
    onCalculateTranspose: (PrescriptionEntity) -> Unit
) {
    var showMoreOptions by remember { mutableStateOf(false) }

    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            /* ---------------- Header ---------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = prescription.prescriptionNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDate(prescription.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val iconTint= MaterialTheme.colorScheme.onSurfaceVariant
                    if (prescription.prescriptionImagePath.isNotEmpty()) {
                        IconButton(
                            onClick = onViewImage,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = iconTint,
                            )
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = iconTint,
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMoreOptions = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = iconTint,
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreOptions,
                            onDismissRequest = { showMoreOptions = false },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {

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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(4.dp))

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete",
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    showMoreOptions = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error,
                                    leadingIconColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }

                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            /* ---------------- Patient Info (Conditional) ---------------- */
            if (prescription.patientName.isNotBlank()) {

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prescription.patientName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (prescription.phone.isNotEmpty()) {
                            Text(
                                text = prescription.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            /* ---------------- Power Section ---------------- */
            if (prescription.rightSph.isNotEmpty() || prescription.leftSph.isNotEmpty()) {

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    if (prescription.rightSph.isNotEmpty()) {
                        CompactPowerColumn(
                            label = "OD",
                            sph = prescription.rightSph,
                            cyl = prescription.rightCyl
                        )
                    }

                    if (
                        prescription.rightSph.isNotEmpty() &&
                        prescription.leftSph.isNotEmpty()
                    ) {
                        VerticalDivider(
                            modifier = Modifier.height(36.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    if (prescription.leftSph.isNotEmpty()) {
                        CompactPowerColumn(
                            label = "OS",
                            sph = prescription.leftSph,
                            cyl = prescription.leftCyl
                        )
                    }
                }
            }

            /* ---------------- Extra Info ---------------- */
            if (prescription.age.isNotEmpty() || prescription.city.isNotEmpty()) {

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    if (prescription.age.isNotEmpty()) {
                        CompactInfoChip(
                            icon = Icons.Default.CalendarToday,
                            text = "${prescription.age} yrs"
                        )
                    }

                    if (prescription.city.isNotEmpty()) {
                        CompactInfoChip(
                            icon = Icons.Default.LocationOn,
                            text = prescription.city
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CompactPowerColumn(
    label: String,
    sph: String,
    cyl: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = formatValue(sph),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        if (cyl.isNotEmpty()) {
            Text(
                text = "CYL: ${formatValue(cyl)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CompactInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun ImagePreviewDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                /* -------- Header -------- */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Prescription Image",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                /* -------- Image -------- */
                val file = File(imagePath)
                if (file.exists()) {
                    val bitmap = remember(imagePath) {
                        BitmapFactory.decodeFile(imagePath)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Prescription Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        ErrorImageView()
                    }
                } else {
                    ErrorImageView()
                }

                Spacer(modifier = Modifier.height(12.dp))

                /* -------- Actions -------- */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close")
                    }
                }
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
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.BrokenImage,
                null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Image not available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
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
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        title = {
            Text(
                "Delete Prescription?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    "This action cannot be undone. The prescription will be permanently deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Rx #${prescription.prescriptionNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            prescription.patientName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (deleteState is DeleteState.Error) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        deleteState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = deleteState !is DeleteState.Deleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                if (deleteState is DeleteState.Deleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
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
                enabled = deleteState !is DeleteState.Deleting,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "No Prescriptions Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tap the + button to create your first prescription",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
