package com.optictoolcompk.opticaltool.ui.billcreation

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.optictoolcompk.opticaltool.data.models.Bill
import com.optictoolcompk.opticaltool.data.models.BillDisplaySettings
import com.optictoolcompk.opticaltool.data.models.BillItem
import com.optictoolcompk.opticaltool.data.models.PrescriptionEntity
import com.optictoolcompk.opticaltool.data.models.PrescriptionFormDataForBill
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.PrescriptionHeader
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.PrescriptionTable
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.SmallField
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.addPaddingToImage
import com.optictoolcompk.opticaltool.ui.prescriptioncreation.getCurrentDate
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
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) "Edit Bill" else "Create Bill",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveBill() },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("SAVE", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
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
            SectionHeader(
                title = "Items",
                subtitle = "${uiState.items.size} item(s)",
                icon = Icons.Default.Receipt
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Item")
            }

            // Prescription Sections
            if (displaySettings.showPrescription) {
                SectionHeader(
                    title = "Add New Prescription",
                    icon = Icons.Default.Edit
                )
                AddNewPrescriptionSection(
                    prescriptionFormData = uiState.prescriptionFormData,
                    onCreateNewClick = { viewModel.onShowPrescriptionOptionsDialog() },
                    onRemovePrescription = { viewModel.onClearPrescriptionForm() },
                    isFormVisible = uiState.showPrescriptionFormCard
                )

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

            // Search Saved Prescriptions
            SectionHeader(
                title = "Search Saved Prescriptions",
                subtitle = "${uiState.prescriptionImagesPaths.size}/3 added",
                icon = Icons.Default.Search
            )
            SearchSavedPrescriptionsSection(
                prescriptionImages = uiState.prescriptionImagesPaths,
                onSearchSavedClick = { viewModel.onShowPrescriptionSearchDialog() },
                onRemoveImage = { index -> viewModel.onRemovePrescriptionImage(index) }
            )

            // Previous Unpaid Bills
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

            // Pickup Date
            PickupDateCard(
                pickupDate = uiState.pickupDate,
                onChange = { viewModel.onPickupDateChanged(it) }
            )

            // Images
            if (displaySettings.showUploadCaptureImages) {
                ImagesCard(
                    imagePaths = uiState.imagePaths,
                    isUploading = uiState.isUploadingImages,
                    onAdd = { imagePickerLauncher.launch("image/*") },
                    onRemove = { viewModel.onImageRemoved(it) }
                )
            }

            // Display Settings
            DisplaySettingsCard(
                settings = displaySettings,
                onChange = { viewModel.onDisplaySettingsChanged(it) }
            )
        }
    }

    // Dialogs
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
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
}

// ==================== SECTION HEADER ====================

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== COMPOSABLE COMPONENTS ====================

@Composable
fun InvoiceInfoCard(
    invoiceNumber: String,
    invoiceDate: String,
    invoiceTime: String,
    shopName: String,
    shopAddress: String,
    shopPhone: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Invoice #$invoiceNumber",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        shopName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (shopAddress.isNotBlank()) {
                        Text(
                            shopAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    if (shopPhone.isNotBlank()) {
                        Text(
                            shopPhone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                invoiceDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                invoiceTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Customer Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Customer Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("City") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                ) {
                    Icon(
                        Icons.Default.Close,
                        "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = item.itemName,
                onValueChange = { onItemChange(item.copy(itemName = it)) },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (item.quantity == 0) "" else item.quantity.toString(),
                    onValueChange = { onItemChange(item.copy(quantity = it.toIntOrNull() ?: 0)) },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = if (item.price == 0.0) "" else item.price.toString(),
                    onValueChange = { onItemChange(item.copy(price = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("$currency ") }
                )
                OutlinedTextField(
                    value = String.format(Locale.getDefault(), "%.0f", item.total),
                    onValueChange = { },
                    label = { Text("Total") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("$currency ") }
                )
            }
        }
    }
}

@Composable
fun AddNewPrescriptionSection(
    prescriptionFormData: PrescriptionFormDataForBill?,
    onCreateNewClick: () -> Unit,
    onRemovePrescription: () -> Unit,
    isFormVisible: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedButton(
                onClick = onCreateNewClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = prescriptionFormData == null && !isFormVisible,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Prescription")
            }

            if (prescriptionFormData != null && prescriptionFormData.patientName.isBlank()) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Image Prescription Added",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onRemovePrescription) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Rx Number: ${prescriptionFormData.prescriptionNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (prescriptionFormData.prescriptionImagePath.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            AsyncImage(
                                model = File(prescriptionFormData.prescriptionImagePath),
                                contentDescription = "New Prescription Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedButton(
                onClick = onSearchSavedClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !maxReached,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Search Saved Prescriptions")
            }

            if (maxReached) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Maximum 3 saved prescription images added",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (prescriptionImages.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Selected Prescriptions (${prescriptionImages.size}/3)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                prescriptionImages.forEachIndexed { index, imagePath ->
                    PrescriptionImageItem(
                        imagePath = imagePath,
                        index = index + 1,
                        onRemove = { onRemoveImage(index) }
                    )
                    if (index < prescriptionImages.size - 1) Spacer(Modifier.height(12.dp))
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
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
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Prescription #$index",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Database",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
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
            delay(100)
            if (graphicsLayer.size.width > 0 && graphicsLayer.size.height > 0) {
                val bitmap = graphicsLayer.toImageBitmap()
                val imageWithPadding = addPaddingToImage(bitmap, paddingPx = 24)
                onCaptureComplete(imageWithPadding.asAndroidBitmap())
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Prescription Form",
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

            Spacer(Modifier.height(12.dp))

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
                Text("I.P.D", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Spacer(Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("D ", fontSize = 14.sp, color = Color.Black)
                        SmallField(ipdD, onIpdDChange)
                        Text("mm", fontSize = 13.sp, color = Color.Black)
                    }
                    HorizontalDivider(
                        modifier = Modifier.width(80.dp),
                        color = Color.Black,
                        thickness = 1.5.dp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("N ", fontSize = 14.sp, color = Color.Black)
                        SmallField(ipdN, onIpdNChange)
                        Text("mm", fontSize = 13.sp, color = Color.Black)
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
                Text(
                    "Checked by :",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = checkedBy,
                    onValueChange = onCheckedByChange,
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    textStyle = TextStyle(fontSize = 15.sp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun SearchUnpaidBillsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Previous Unpaid Bill?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "Search and add to previous amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Search")
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Payment Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Total Amount
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$currency ${String.format(Locale.getDefault(), "%.0f", totalAmount)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (previousAmount.isNotBlank() && (previousAmount.toDoubleOrNull() ?: 0.0) > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Previous:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$currency $previousAmount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedTextField(
                value = discount,
                onValueChange = onDiscountChange,
                label = { Text("Discount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("- $currency ") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = advance,
                onValueChange = onAdvanceChange,
                label = { Text("Advance") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("- $currency ") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = advance2,
                onValueChange = onAdvance2Change,
                label = { Text("2nd Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("- $currency ") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
                supportingText = if (advance3.isNotBlank() && advance3Date != null) {
                    { Text(advance3Date) }
                } else null
            )

            HorizontalDivider(thickness = 1.dp)

            // Remaining Amount
            Surface(
                color = if (remainingAmount > 0)
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else
                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Remaining:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$currency ${String.format(Locale.getDefault(), "%.0f", remainingAmount)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (remainingAmount > 0) MaterialTheme.colorScheme.error
                        else Color(0xFF4CAF50)
                    )
                }
            }

            if (remainingNote != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        remainingNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (remainingAmount <= 0 && totalAmount > 0) {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "FULLY PAID",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PickupDateCard(pickupDate: String?, onChange: (String?) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Pickup Date (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pickupDate ?: "",
                onValueChange = { onChange(it.ifBlank { null }) },
                label = { Text("Pickup Date") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 15 January 2026") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Panorama, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Images (${imagePaths.size}/4)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (imagePaths.size < 4) {
                    Button(
                        onClick = onAdd,
                        enabled = !isUploading,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Add")
                        }
                    }
                }
            }

            if (imagePaths.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    imagePaths.forEach { path ->
                        ImagePreview(imagePath = path, onRemove = { onRemove(path) })
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
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    .padding(8.dp)
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DisplaySettingsCard(
    settings: BillDisplaySettings,
    onChange: (BillDisplaySettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Display Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            SettingRow("Show Prescription", settings.showPrescription) {
                onChange(settings.copy(showPrescription = it))
            }
            if (settings.showPrescription) {
                SettingRow("Show I.P.D", settings.showIpd) {
                    onChange(settings.copy(showIpd = it))
                }
                SettingRow("Show Checked BY", settings.showCheckedBy) {
                    onChange(settings.copy(showCheckedBy = it))
                }
            }
            SettingRow("Auto-save Prescriptions", settings.autoSavePrescriptions) {
                onChange(settings.copy(autoSavePrescriptions = it))
            }
            SettingRow("Show Upload/Capture Images", settings.showUploadCaptureImages) {
                onChange(settings.copy(showUploadCaptureImages = it))
            }
        }
    }
}

@Composable
fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

// ==================== DIALOGS ====================

@Composable
fun PrescriptionOptionsDialog(
    onDismiss: () -> Unit,
    onAddByImage: () -> Unit,
    onFillForm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Create New Prescription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Choose how you want to add the prescription:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onAddByImage,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(Icons.Default.Camera, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Add by Image")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onFillForm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Fill the Form")
                }

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
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
            shape = RoundedCornerShape(24.dp)
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
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                if (maxImagesReached) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Maximum 3 prescription images reached",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search by name, phone, or Rx number") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    enabled = !maxImagesReached,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                if (!maxImagesReached && searchResults.isNotEmpty()) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults.size) { index ->
                            val prescription = searchResults[index]
                            PrescriptionSearchItem(
                                prescription = prescription,
                                onClick = { onPrescriptionSelected(prescription) }
                            )
                        }
                    }
                } else if (!maxImagesReached && searchQuery.length >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
}

@Composable
fun PrescriptionSearchItem(
    prescription: PrescriptionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    prescription.patientName.ifBlank { "Image Only" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        prescription.prescriptionNumber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (prescription.phone.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    prescription.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Search Unpaid Bills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search by name or phone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                if (searchQuery.length < 2) {
                    Text(
                        "Type at least 2 characters to search",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (searchResults.isEmpty()) {
                    Text(
                        "No unpaid bills found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults.size) { index ->
                            val bill = searchResults[index]
                            UnpaidBillItem(
                                bill = bill,
                                currency = currency,
                                onSelect = { onBillSelected(bill) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnpaidBillItem(bill: Bill, currency: String, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    bill.customerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "# ${bill.invoiceNumber}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                bill.customerPhone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

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
