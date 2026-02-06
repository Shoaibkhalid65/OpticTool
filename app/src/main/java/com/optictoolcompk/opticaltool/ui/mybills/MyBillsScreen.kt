package com.optictoolcompk.opticaltool.ui.mybills


import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.optictoolcompk.opticaltool.data.models.Bill
import com.optictoolcompk.opticaltool.data.models.BillSortOption
import com.optictoolcompk.opticaltool.data.models.BillStatistics
import com.optictoolcompk.opticaltool.utils.BillPrintingUtils
import com.optictoolcompk.opticaltool.utils.BillReceiptGenerator
import com.optictoolcompk.opticaltool.utils.PdfCenteringUtil
import com.optictoolcompk.opticaltool.utils.ReceiptSharingHelper
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBillsScreen(
    onNavigateToBillCreation: () -> Unit,
    onNavigateToEditBill: (Long) -> Unit,
    onNavigateToShopDashboard: () -> Unit,
    viewModel: MyBillsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val shopSettings by viewModel.shopSettings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf<DatePickerType?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    var showImageDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    // State to track which bill is currently being prepared for editing
    var loadingBillId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Saved Bills") },
//                actions = {
//                    IconButton(onClick = onNavigateToShopDashboard) {
//                        Icon(Icons.Default.Settings, "Shop Settings")
//                    }
//                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToBillCreation) {
                Icon(Icons.Default.Add, "Create New Bill")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Statistics Dashboard
            BillStatisticsCard(
                statistics = uiState.statistics,
                currency = shopSettings.currency,
                modifier = Modifier.padding(16.dp)
            )

            // Search and Filters
            SearchAndFiltersCard(
                searchQuery = filterState.searchQuery,
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onStartDateClick = { showDatePicker = DatePickerType.START },
                onEndDateClick = { showDatePicker = DatePickerType.END },
                onSortClick = { showSortMenu = true },
                startDate = filterState.startDate,
                endDate = filterState.endDate
            )

            // Bill count
            Text(
                text = "Showing ${uiState.bills.size} bills",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Bills list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.bills.isEmpty()) {
                EmptyBillsState(onCreateBill = onNavigateToBillCreation)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.bills, key = { it.id }) { bill ->
                        BillCard(
                            bill = bill,
                            currency = shopSettings.currency,
                            isLoading = loadingBillId == bill.id,
                            onEdit = { 
                                loadingBillId = bill.id
                                onNavigateToEditBill(bill.id) 
                            },
                            onDelete = { showDeleteDialog = bill.id },
                            onView = {
                                scope.launch {
                                    val result = BillReceiptGenerator.generateReceipt(
                                        context,
                                        bill,
                                        shopSettings
                                    )
                                    if (result.imageUri != null) {
                                        imageUri = result.imageUri
                                        showImageDialog = true
                                    }
                                }
                            },
                            onShare = {
                                scope.launch {
                                    val receiptResult = BillReceiptGenerator.generateReceipt(
                                        context,
                                        bill,
                                        shopSettings
                                    )
                                    if (receiptResult.imageUri != null) {
                                        val imageUri = receiptResult.imageUri
                                        ReceiptSharingHelper.shareViaWhatsApp(
                                            context,
                                            imageUri,
                                            billNumber = bill.invoiceNumber,
                                            bill.customerPhone,
                                        )
                                    }
                                }
                            },
                            onDownloadPdf = {
                                scope.launch {
                                    val receiptResult = BillReceiptGenerator.generateReceipt(
                                        context,
                                        bill,
                                        shopSettings
                                    )
                                    if (receiptResult.imageUri != null) {
                                        val imageUri = receiptResult.imageUri
                                        ReceiptSharingHelper.saveToGallery(
                                            context,
                                            imageUri,
                                            billNumber = bill.invoiceNumber,
                                        )
                                        Toast.makeText(
                                            context,
                                            "Receipt Saved in Gallery",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onPrint = {
                                scope.launch {
                                    val receiptResult = BillReceiptGenerator.generateReceipt(
                                        context,
                                        bill,
                                        shopSettings
                                    )
                                    if (receiptResult.pdfUri != null) {
                                        val pdfUri = receiptResult.pdfUri
                                        val centeredPdfUri =
                                            PdfCenteringUtil.createCenteredPdf(context, pdfUri)
                                        BillPrintingUtils.printExistingPdf(
                                            context,
                                            centeredPdfUri,
                                            billNumber = bill.invoiceNumber,
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }


    if (showImageDialog) {
        ModalBottomSheet(
            onDismissRequest = { showImageDialog = false },
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            showImageDialog = false
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close dialog"
                        )
                    }
                }
                AsyncImage(
                    model = imageUri!!,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentDescription = "image of receipt"
                )
            }
        }
    }

    // Sort menu dropdown
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false }
    ) {
        BillSortOption.entries.forEach { option ->
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
                    viewModel.onSortOptionChanged(option)
                    showSortMenu = false
                }
            )
        }
    }

    // Date picker dialog
    showDatePicker?.let { type ->
        SimpleDatePickerDialog(
            onDateSelected = { date ->
                when (type) {
                    DatePickerType.START -> viewModel.onStartDateChanged(date)
                    DatePickerType.END -> viewModel.onEndDateChanged(date)
                }
                showDatePicker = null
            },
            onDismiss = { showDatePicker = null }
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { billId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Bill") },
            text = { Text("Are you sure you want to delete this bill? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBill(billId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BillStatisticsCard(
    statistics: BillStatistics,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Sales",
                    value = "$currency ${
                        String.format(
                            Locale.getDefault(),
                            "%.0f",
                            statistics.totalSalesAmount
                        )
                    }",
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    label = "Unpaid Amount",
                    value = "$currency ${
                        String.format(
                            Locale.getDefault(),
                            "%.0f",
                            statistics.totalUnpaidAmount
                        )
                    }",
                    valueColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Bills",
                    value = "${statistics.totalBillsCount}",
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    label = "Unpaid Bills",
                    value = "${statistics.unpaidBillsCount}",
                    valueColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    label = "Paid Bills",
                    value = "${statistics.paidBillsCount}",
                    valueColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFiltersCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onSortClick: () -> Unit,
    startDate: String?,
    endDate: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search invoice, name, phone") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Date filters and Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Start Date
                OutlinedButton(
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        startDate ?: "Start Date",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

                // End Date
                OutlinedButton(
                    onClick = onEndDateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        endDate ?: "End Date",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

                // Sort Button
                OutlinedButton(onClick = onSortClick) {
                    Icon(Icons.AutoMirrored.Default.Sort, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun BillCard(
    bill: Bill,
    currency: String,
    isLoading: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit,
    onShare: () -> Unit,
    onDownloadPdf: () -> Unit,
    onPrint: () -> Unit
) {
    var showMoreOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isUnpaid) {
                Color(0xFFFFEBEE) // Light red for unpaid
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (bill.isUnpaid) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350))
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Invoice number and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Invoice # : ${bill.invoiceNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "${bill.invoiceDate}   ${bill.invoiceTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Name and Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Name : ${bill.customerName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }


                Column(horizontalAlignment = Alignment.End) {
                    if (bill.isUnpaid) {
                        Text(
                            "Remaining:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "$currency ${
                                String.format(
                                    Locale.getDefault(),
                                    "%.0f",
                                    bill.remainingAmount
                                )
                            }",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (bill.remainingNote != null) {
                        Text(
                            bill.remainingNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (bill.isPaid) Color.Green else Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }

                }
            }

            Spacer(Modifier.height(4.dp))

            // Phone
            Text(
                "Phone : ${bill.customerPhone}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Edit")
                    }
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }

                Button(
                    onClick = onView,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("View")
                }

                // More options button
                Box {
                    IconButton(onClick = { showMoreOptions = true }) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }

                    DropdownMenu(
                        expanded = showMoreOptions,
                        onDismissRequest = { showMoreOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share on WhatsApp") },
                            onClick = {
                                onShare()
                                showMoreOptions = false
                            },
                            leadingIcon = { Icon(Icons.Default.Share, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Save in Gallery") },
                            onClick = {
                                onDownloadPdf()
                                showMoreOptions = false
                            },
                            leadingIcon = { Icon(Icons.Default.Download, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Print") },
                            onClick = {
                                onPrint()
                                showMoreOptions = false
                            },
                            leadingIcon = { Icon(Icons.Default.Print, null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBillsState(onCreateBill: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No bills found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCreateBill) {
                Text("Create your first bill")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        val formatted = date.format(
                            java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")
                        )
                        onDateSelected(formatted)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

enum class DatePickerType {
    START, END
}