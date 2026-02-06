package com.optictoolcompk.opticaltool.ui.billcreation

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.optictoolcompk.opticaltool.data.models.*
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillCreationScreen(
    billId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: BillCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val displaySettings by viewModel.displaySettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(billId) {
        if (billId != null && billId > 0) {
            viewModel.loadBillForEdit(billId)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
            viewModel.clearSaveSuccess()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                viewModel.onImagesSelected(context, uris)
            }
        }
    }

    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                viewModel.onPrescriptionImageSelected(context, it)
            }
        }
    }

    if (uiState.initialLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Loading bill details...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (uiState.isEditMode) "Edit Bill" else "Create Bill") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { viewModel.saveBill() },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("SAVE")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Invoice Info
                InvoiceInfoCard(
                    invoiceNumber = uiState.invoiceNumber,
                    invoiceDate = uiState.invoiceDate,
                    invoiceTime = uiState.invoiceTime,
                    shopName = shopSettings.shopName,
                    shopAddress = shopSettings.shopAddress,
                    shopPhone = shopSettings.shopPhone
                )

                // Customer Info
                CustomerInfoCard(
                    name = uiState.customerName,
                    onNameChange = { viewModel.onCustomerNameChanged(it) },
                    phone = uiState.customerPhone,
                    onPhoneChange = { viewModel.onCustomerPhoneChanged(it) },
                    city = uiState.customerCity,
                    onCityChange = { viewModel.onCustomerCityChanged(it) }
                )

                // Items Section
                Text(
                    "Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                uiState.items.forEachIndexed { index, item ->
                    BillItemCard(
                        item = item,
                        currency = shopSettings.currency,
                        onItemChange = { viewModel.onItemChanged(index, it) },
                        onRemove = { viewModel.onRemoveItem(index) }
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.onAddItem() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item")
                }

                // --- SEPARATED PRESCRIPTION SECTIONS ---

                // 1. Add New Prescription Section (Conditional)
                if (displaySettings.showPrescription) {
                    AddNewPrescriptionSection(
                        prescriptionFormData = uiState.prescriptionFormData,
                        onCreateNewClick = { viewModel.onShowPrescriptionOptionsDialog() },
                        onRemovePrescription = { viewModel.onClearPrescriptionForm() },
                        isFormVisible = uiState.showPrescriptionFormCard
                    )

                    // Inline Prescription Form Card (if visible)
                    if (uiState.showPrescriptionFormCard) {
                        PrescriptionFormCard(
                            formData = uiState.prescriptionFormInputs,
                            onFormDataChange = { viewModel.onPrescriptionFormInputChanged(it) },
                            onDismiss = { viewModel.onHidePrescriptionForm() },
                            displaySettings = displaySettings,
                            shouldTriggerCapture = uiState.shouldTriggerCapture,
                            onCaptureComplete = { bitmap ->
                                viewModel.onSavePrescriptionForm(context, bitmap)
                            }
                        )
                    }
                }

                // 2. Search Saved Prescriptions Section (Permanent)
                SearchSavedPrescriptionsSection(
                    prescriptionImages = uiState.prescriptionImagesPaths,
                    onSearchSavedClick = { viewModel.onShowPrescriptionSearchDialog() },
                    onRemoveImage = { index -> viewModel.onRemovePrescriptionImage(index) }
                )

                SearchUnpaidBillsCard(onClick = { viewModel.onShowUnpaidBillsDialog() })

                // Totals
                TotalsCard(
                    totalAmount = uiState.totalAmount,
                    discount = uiState.discount,
                    onDiscountChange = { viewModel.onDiscountChanged(it) },
                    advance = uiState.advance,
                    onAdvanceChange = { viewModel.onAdvanceChanged(it) },
                    advance2 = uiState.advance2,
                    onAdvance2Change = { viewModel.onAdvance2Changed(it) },
                    advance2Date = uiState.advance2Date,
                    advance3 = uiState.advance3,
                    onAdvance3Change = { viewModel.onAdvance3Changed(it) },
                    advance3Date = uiState.advance3Date,
                    previousAmount = uiState.previousAmount.toString(),
                    remainingAmount = uiState.remainingAmount,
                    remainingNote = uiState.remainingNote,
                    currency = shopSettings.currency
                )

                PickupDateCard(
                    pickupDate = uiState.pickupDate,
                    onChange = { viewModel.onPickupDateChanged(it) }
                )

                if (displaySettings.showUploadCaptureImages) {
                    ImagesCard(
                        imagePaths = uiState.imagePaths,
                        isUploading = uiState.isUploadingImages,
                        onAdd = { imagePickerLauncher.launch("image/*") },
                        onRemove = { viewModel.onImageRemoved(it) }
                    )
                }

                DisplaySettingsCard(
                    settings = displaySettings,
                    onChange = { viewModel.onDisplaySettingsChanged(it) }
                )
            }
        }
    }

    // Unpaid Bills Dialog
    val showUnpaidDialog by viewModel.showUnpaidBillsDialog.collectAsStateWithLifecycle()
    if (showUnpaidDialog) {
        SearchUnpaidBillsDialog(
            searchQuery = viewModel.unpaidBillsSearchQuery.collectAsState().value,
            onSearchQueryChange = { viewModel.onUnpaidBillsSearchQueryChanged(it) },
            searchResults = viewModel.unpaidBillsSearchResults.collectAsState().value,
            onBillSelected = { viewModel.onUnpaidBillSelected(it) },
            onDismiss = { viewModel.onHideUnpaidBillsDialog() },
            currency = shopSettings.currency
        )
    }

    // Search Prescription Dialog
    val showPrescriptionDialog by viewModel.showPrescriptionSearchDialog.collectAsState()
    if (showPrescriptionDialog) {
        SearchPrescriptionDialog(
            searchQuery = viewModel.prescriptionSearchQuery.collectAsState().value,
            onSearchQueryChange = { viewModel.onPrescriptionSearchQueryChanged(it) },
            searchResults = viewModel.prescriptionSearchResults.collectAsState().value,
            onPrescriptionSelected = { prescription ->
                if (prescription.prescriptionImagePath.isNotBlank()) {
                    viewModel.onAddSavedPrescriptionImage(prescription.prescriptionImagePath)
                }
            },
            onDismiss = { viewModel.onHidePrescriptionSearchDialog() },
            maxImagesReached = uiState.prescriptionImagesPaths.size >= 3
        )
    }

    // Prescription Options Dialog
    if (uiState.showPrescriptionOptionsDialog) {
        PrescriptionOptionsDialog(
            onDismiss = { viewModel.onHidePrescriptionOptionsDialog() },
            onAddByImage = {
                viewModel.onHidePrescriptionOptionsDialog()
                singleImagePickerLauncher.launch("image/*")
            },
            onFillForm = {
                viewModel.onShowPrescriptionForm()
            }
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
}

// ==================== COMPOSABLE COMPONENTS ====================

@Composable
fun AddNewPrescriptionSection(
    prescriptionFormData: PrescriptionFormDataForBill?,
    onCreateNewClick: () -> Unit,
    onRemovePrescription: () -> Unit,
    isFormVisible: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Add New Prescription",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // Add New button (Disabled if already added)
            OutlinedButton(
                onClick = onCreateNewClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = prescriptionFormData == null && !isFormVisible
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Prescription")
            }

            // Preview Section
            if (prescriptionFormData != null && prescriptionFormData.patientName.isBlank()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (prescriptionFormData.patientName.isBlank()) "Image Prescription" else "Form Prescription",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = onRemovePrescription) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Text(
                            "Rx Number: ${prescriptionFormData.prescriptionNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(8.dp))

                        if (prescriptionFormData.prescriptionImagePath.isNotBlank()) {
                            AsyncImage(
                                model = File(prescriptionFormData.prescriptionImagePath),
                                contentDescription = "New Prescription Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSavedPrescriptionsSection(
    prescriptionImages: List<String>,
    onSearchSavedClick: () -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    val maxReached = prescriptionImages.size >= 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Search Saved Prescriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSearchSavedClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !maxReached
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Search Saved Prescriptions")
            }

            if (maxReached) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Maximum 3 saved prescription images added",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontStyle = FontStyle.Italic
                )
            }

            // Display added images below the card
            if (prescriptionImages.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Selected Saved Prescriptions (${prescriptionImages.size}/3)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                prescriptionImages.forEachIndexed { index, imagePath ->
                    PrescriptionImageItem(
                        imagePath = imagePath,
                        index = index + 1,
                        onRemove = { onRemoveImage(index) }
                    )
                    if (index < prescriptionImages.size - 1) Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PrescriptionFormCard(
    formData: PrescriptionFormDataForBill,
    onFormDataChange: (PrescriptionFormDataForBill) -> Unit,
    onDismiss: () -> Unit,
    displaySettings: BillDisplaySettings,
    shouldTriggerCapture: Boolean,
    onCaptureComplete: (Bitmap) -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(shouldTriggerCapture) {
        if (shouldTriggerCapture) {
            delay(100) // Ensure layout is settled
            if (graphicsLayer.size.width > 0 && graphicsLayer.size.height > 0) {
                val bitmap = graphicsLayer.toImageBitmap()
                val imageWithPadding=addPaddingToImage(bitmap, paddingPx = 24)
                onCaptureComplete(imageWithPadding.asAndroidBitmap())
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Fill Prescription Form",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        "Dismiss Form",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PrescriptionHeader(
                        prescriptionNo = formData.prescriptionNumber,
                        date = getCurrentDate(),
                        name = formData.patientName,
                        onNameChange = { onFormDataChange(formData.copy(patientName = it)) },
                        phone = formData.phone,
                        onPhoneChange = { onFormDataChange(formData.copy(phone = it)) },
                        age = formData.age,
                        onAgeChange = { onFormDataChange(formData.copy(age = it)) },
                        city = formData.city,
                        onCityChange = { onFormDataChange(formData.copy(city = it)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrescriptionTable(
                        rightSph = formData.rightSph,
                        onRightSphChange = { onFormDataChange(formData.copy(rightSph = it)) },
                        rightCyl = formData.rightCyl,
                        onRightCylChange = { onFormDataChange(formData.copy(rightCyl = it)) },
                        rightAxis = formData.rightAxis,
                        onRightAxisChange = { onFormDataChange(formData.copy(rightAxis = it)) },
                        rightVa = formData.rightVa,
                        onRightVaChange = { onFormDataChange(formData.copy(rightVa = it)) },
                        leftSph = formData.leftSph,
                        onLeftSphChange = { onFormDataChange(formData.copy(leftSph = it)) },
                        leftCyl = formData.leftCyl,
                        onLeftCylChange = { onFormDataChange(formData.copy(leftCyl = it)) },
                        leftAxis = formData.leftAxis,
                        onLeftAxisChange = { onFormDataChange(formData.copy(leftAxis = it)) },
                        leftVa = formData.leftVa,
                        onLeftVaChange = { onFormDataChange(formData.copy(leftVa = it)) },
                        add = formData.addPower,
                        onAddChange = { onFormDataChange(formData.copy(addPower = it)) },
                        isShortWidth = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrescriptionFooter(
                        ipdN = formData.ipdNear,
                        onIpdNChange = { onFormDataChange(formData.copy(ipdNear = it)) },
                        ipdD = formData.ipdDistance,
                        onIpdDChange = { onFormDataChange(formData.copy(ipdDistance = it)) },
                        checkedBy = formData.checkedBy,
                        onCheckedByChange = { onFormDataChange(formData.copy(checkedBy = it)) },
                        showIpd = displaySettings.showIpd,
                        showCheckedBy = displaySettings.showCheckedBy
                    )
                }
            }
        }
    }
}

@Composable
fun PrescriptionImageItem(
    imagePath: String,
    index: Int,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "Prescription Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Saved Prescription #$index",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Imported from database",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PrescriptionOptionsDialog(
    onDismiss: () -> Unit,
    onAddByImage: () -> Unit,
    onFillForm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Create New Prescription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Choose how you want to add the prescription:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = onAddByImage, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AddPhotoAlternate, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add by Image")
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onFillForm, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("By Filling the Form")
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Cancel") }
            }
        }
    }
}

// Helper Prescription Footer for Bill Creation Form
@Composable
fun PrescriptionFooter(
    ipdN: String, onIpdNChange: (String) -> Unit,
    ipdD: String, onIpdDChange: (String) -> Unit,
    checkedBy: String, onCheckedByChange: (String) -> Unit,
    showIpd: Boolean,
    showCheckedBy: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIpd) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("I.P.D", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("D ", fontSize = 14.sp)
                        SmallField(ipdD, onIpdDChange)
                        Text("mm", fontSize = 13.sp)
                    }
                    HorizontalDivider(
                        modifier = Modifier.width(80.dp),
                        color = Color.Black,
                        thickness = 1.5.dp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("N ", fontSize = 14.sp)
                        SmallField(ipdN, onIpdNChange)
                        Text("mm", fontSize = 13.sp)
                    }
                }
            }
        }

        if (showCheckedBy) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                Text("Checked by :", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = checkedBy,
                    onValueChange = onCheckedByChange,
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    textStyle = TextStyle(fontSize = 16.sp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun SearchPrescriptionDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<PrescriptionEntity>,
    onPrescriptionSelected: (PrescriptionEntity) -> Unit,
    onDismiss: () -> Unit,
    maxImagesReached: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Search Prescriptions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close") }
                }
                if (maxImagesReached) {
                    Text(
                        "Maximum 3 prescription images reached",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search by name, phone, or Rx number") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    enabled = !maxImagesReached
                )
                Spacer(Modifier.height(16.dp))
                if (!maxImagesReached && searchResults.isNotEmpty()) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults.size) { index ->
                            val prescription = searchResults[index]
                            PrescriptionSearchItem(
                                prescription = prescription,
                                onClick = { onPrescriptionSelected(prescription) })
                        }
                    }
                } else if (!maxImagesReached && searchQuery.length >= 2) {
                    Text(
                        "No prescriptions found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PrescriptionSearchItem(prescription: PrescriptionEntity, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    prescription.patientName.ifBlank { "Image Only" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    prescription.prescriptionNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (prescription.phone.isNotBlank()) Text(
                prescription.phone,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun InvoiceInfoCard(
    invoiceNumber: String,
    invoiceDate: String,
    invoiceTime: String,
    shopName: String,
    shopAddress: String,
    shopPhone: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Invoice No : $invoiceNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(invoiceDate, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        invoiceTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                shopName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                shopAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (shopPhone.isNotBlank()) Text(
                shopPhone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomerInfoCard(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Customer Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Customer Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("City") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun BillItemCard(
    item: BillItem,
    currency: String,
    onItemChange: (BillItem) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Item",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) { Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.error) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = item.itemName,
                onValueChange = { onItemChange(item.copy(itemName = it)) },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (item.quantity == 0) "" else item.quantity.toString(),
                    onValueChange = { onItemChange(item.copy(quantity = it.toIntOrNull() ?: 0)) },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = if (item.price == 0.0) "" else item.price.toString(),
                    onValueChange = { onItemChange(item.copy(price = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    prefix = { Text("$currency ") })
                OutlinedTextField(
                    value = String.format(Locale.getDefault(), "%.0f", item.total),
                    onValueChange = { },
                    label = { Text("Total") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    enabled = false,
                    prefix = { Text("$currency ") })
            }
        }
    }
}

@Composable
fun SearchUnpaidBillsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Previous Unpaid Bill?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Search and add to previous amount",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(onClick = onClick) { Text("Search") }
        }
    }
}

@Composable
fun TotalsCard(
    totalAmount: Double,
    discount: String,
    onDiscountChange: (String) -> Unit,
    advance: String,
    onAdvanceChange: (String) -> Unit,
    advance2: String,
    onAdvance2Change: (String) -> Unit,
    advance2Date: String?,
    advance3: String,
    onAdvance3Change: (String) -> Unit,
    advance3Date: String?,
    previousAmount: String,
    remainingAmount: Double,
    remainingNote: String?,
    currency: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Payment Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "$currency ${String.format(Locale.getDefault(), "%.0f", totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (previousAmount.isNotBlank() && (previousAmount.toDoubleOrNull() ?: 0.0) > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Previous:", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "$currency $previousAmount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            OutlinedTextField(
                value = discount,
                onValueChange = onDiscountChange,
                label = { Text("Discount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("- $currency ") },
                singleLine = true
            )
            OutlinedTextField(
                value = advance,
                onValueChange = onAdvanceChange,
                label = { Text("Advance") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("- $currency ") },
                singleLine = true
            )
            OutlinedTextField(
                value = advance2,
                onValueChange = onAdvance2Change,
                label = { Text("2nd Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("- $currency ") },
                singleLine = true,
                supportingText = if (advance2.isNotBlank() && advance2Date != null) {
                    { Text(advance2Date) }
                } else null
            )
            OutlinedTextField(
                value = advance3,
                onValueChange = onAdvance3Change,
                label = { Text("3rd Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("- $currency ") },
                singleLine = true,
                supportingText = if (advance3.isNotBlank() && advance3Date != null) {
                    { Text(advance3Date) }
                } else null
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Remaining:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$currency ${String.format(Locale.getDefault(), "%.0f", remainingAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingAmount > 0) MaterialTheme.colorScheme.error else Color(
                        0xFF4CAF50
                    )
                )
            }
            if (remainingNote != null) Text(
                remainingNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (remainingAmount <= 0 && totalAmount > 0) {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "✓ FULLY PAID",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun PickupDateCard(pickupDate: String?, onChange: (String?) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Pickup Date (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = pickupDate ?: "",
                onValueChange = { onChange(it.ifBlank { null }) },
                label = { Text("Pickup Date") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 15 January 2026") },
                singleLine = true
            )
        }
    }
}

@Composable
fun ImagesCard(
    imagePaths: List<String>,
    isUploading: Boolean,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Images (${imagePaths.size}/4)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (imagePaths.size < 4) {
                    Button(onClick = onAdd, enabled = !isUploading) {
                        if (isUploading) CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        else {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                null
                            ); Spacer(Modifier.width(4.dp)); Text("Add")
                        }
                    }
                }
            }
            if (imagePaths.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    imagePaths.forEach { path ->
                        ImagePreview(
                            imagePath = path,
                            onRemove = { onRemove(path) })
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePreview(imagePath: String, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Box {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Bill Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        Icons.Default.Close,
                        "Remove",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DisplaySettingsCard(settings: BillDisplaySettings, onChange: (BillDisplaySettings) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Display Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            SettingRow("Show Prescription", settings.showPrescription) {
                onChange(
                    settings.copy(
                        showPrescription = it
                    )
                )
            }
            if (settings.showPrescription) {
                SettingRow("Show I.P.D", settings.showIpd) { onChange(settings.copy(showIpd = it)) }
                SettingRow("Show Checked BY", settings.showCheckedBy) {
                    onChange(
                        settings.copy(
                            showCheckedBy = it
                        )
                    )
                }
            }
            SettingRow("Auto-save Prescriptions", settings.autoSavePrescriptions) {
                onChange(
                    settings.copy(autoSavePrescriptions = it)
                )
            }
            SettingRow("Show Upload/Capture Images", settings.showUploadCaptureImages) {
                onChange(
                    settings.copy(showUploadCaptureImages = it)
                )
            }
        }
    }
}

@Composable
fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label); Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SearchUnpaidBillsDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<Bill>,
    onBillSelected: (Bill) -> Unit,
    onDismiss: () -> Unit,
    currency: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Search Unpaid Bills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search by name or phone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                if (searchQuery.length < 2) Text(
                    "Type at least 2 characters to search",
                    style = MaterialTheme.typography.bodySmall
                )
                else if (searchResults.isEmpty()) Text(
                    "No unpaid bills found",
                    style = MaterialTheme.typography.bodyMedium
                )
                else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults.size) { index ->
                            val bill = searchResults[index]
                            UnpaidBillItem(
                                bill = bill,
                                currency = currency,
                                onSelect = { onBillSelected(bill) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnpaidBillItem(bill: Bill, currency: String, onSelect: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onSelect) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    bill.customerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "# ${bill.invoiceNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(bill.customerPhone, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    bill.invoiceDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Remaining: $currency ${
                        String.format(
                            Locale.getDefault(),
                            "%.0f",
                            bill.remainingAmount
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
