package com.optictoolcompk.opticaltool.ui.mybills

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class DatePickerType {
    START, END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBillsScreen(
    onNavigateToBillCreation: () -> Unit,
    onNavigateToEditBill: (Long) -> Unit,
    onNavigateBack: () -> Unit,
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
    var loadingBillId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bills",
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
            ExtendedFloatingActionButton(
                onClick = onNavigateToBillCreation,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Bill", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
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
                ),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Statistics Dashboard
            item {
                BillStatisticsCard(
                    statistics = uiState.statistics,
                    currency = shopSettings.currency,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Sticky Search & Sort Header
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 3.dp
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))

                        SearchAndSortSection(
                            searchQuery = filterState.searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onSortClick = { showSortMenu = true },
                            showSortMenu = showSortMenu,
                            onDismissSort = { showSortMenu = false },
                            currentSortOption = filterState.sortBy,
                            onSortOptionSelected = { option ->
                                viewModel.onSortOptionChanged(option)
                            },
                            onStartDateClick = { showDatePicker = DatePickerType.START },
                            onEndDateClick = { showDatePicker = DatePickerType.END },
                            startDate = filterState.startDate,
                            endDate = filterState.endDate
                        )

                        // Transaction History Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaction History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${uiState.bills.size} Bills",
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Bills List or Empty State
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }
            } else if (uiState.bills.isEmpty()) {
                item {
                    EmptyBillsState(onCreateBill = onNavigateToBillCreation)
                }
            } else {
                items(uiState.bills, key = { it.id }) { bill ->
                    BillListItem(
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
                                receiptResult.imageUri?.let { uri ->
                                    ReceiptSharingHelper.shareReceiptImage(
                                        context,
                                        uri,
                                        bill.invoiceNumber,
                                    )
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
                                receiptResult.pdfUri?.let { pdfUri ->
                                    val centeredPdfUri =
                                        PdfCenteringUtil.createCenteredPdf(context, pdfUri)
                                    BillPrintingUtils.printExistingPdf(
                                        context,
                                        centeredPdfUri,
                                        bill.invoiceNumber
                                    )
                                }
                            }
                        },
                        onDownload = {
                            scope.launch {
                                val receiptResult = BillReceiptGenerator.generateReceipt(
                                    context,
                                    bill,
                                    shopSettings
                                )
                                receiptResult.imageUri?.let { uri ->
                                    ReceiptSharingHelper.saveToGallery(
                                        context,
                                        uri,
                                        bill.invoiceNumber
                                    )
                                    Toast.makeText(
                                        context,
                                        "Receipt Saved in Gallery",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Date Picker Dialog
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

    // Delete Confirmation Dialog
    showDeleteDialog?.let { billId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            title = {
                Text(
                    "Delete Bill?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This action cannot be undone. The bill will be permanently deleted.")
            },
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
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Image Preview Dialog
    if (showImageDialog && imageUri != null) {
        ModalBottomSheet(
            onDismissRequest = { showImageDialog = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Receipt Preview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showImageDialog = false }) {
                        Icon(Icons.Default.Close, "Share")
                    }
                }

                AsyncImage(
                    model = imageUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentDescription = "Receipt Preview"
                )
            }
        }
    }
}

@Composable
fun SearchAndSortSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSortClick: () -> Unit,
    showSortMenu: Boolean,
    onDismissSort: () -> Unit,
    currentSortOption: BillSortOption,
    onSortOptionSelected: (BillSortOption) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    startDate: String?,
    endDate: String?
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        // Search and Sort Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Field
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
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

            // Sort Button with Dropdown
            Box {
                Surface(
                    onClick = onSortClick,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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
                    onDismissRequest = onDismissSort,
                    modifier = Modifier.width(220.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    BillSortOption.entries.forEach { option ->
                        val isSelected = option == currentSortOption

                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortOptionSelected(option)
                                onDismissSort()
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.4f
                                    )
                                    else Color.Transparent
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateChip(
                label = startDate ?: "From Date",
                onClick = onStartDateClick,
                modifier = Modifier.weight(1f),
                isActive = startDate != null
            )
            DateChip(
                label = endDate ?: "To Date",
                onClick = onEndDateClick,
                modifier = Modifier.weight(1f),
                isActive = endDate != null
            )
        }
    }
}

@Composable
fun DateChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Financial Overview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Revenue
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Total Revenue",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.7f)
                    )
                    Text(
                        "$currency ${
                            String.format(
                                Locale.getDefault(),
                                "%,.0f",
                                statistics.totalSalesAmount
                            )
                        }",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Pending Dues
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "Pending Dues",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Text(
                        "$currency ${
                            String.format(
                                Locale.getDefault(),
                                "%,.0f",
                                statistics.totalUnpaidAmount
                            )
                        }",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Bill Counts Row
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCountItem(
                        "Total",
                        statistics.totalBillsCount.toString(),
                        Icons.Default.Receipt,
                        MaterialTheme.colorScheme.onPrimary
                    )
                    StatCountItem(
                        "Paid",
                        statistics.paidBillsCount.toString(),
                        Icons.Default.CheckCircle,
                        MaterialTheme.colorScheme.onPrimary
                    )
                    StatCountItem(
                        "Unpaid",
                        statistics.unpaidBillsCount.toString(),
                        Icons.Default.Pending,
                        MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatCountItem(
    label: String,
    value: String,
    icon: ImageVector,
    tintColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = tintColor.copy(alpha = 0.8f)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = tintColor
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = tintColor.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun BillListItem(
    bill: Bill,
    currency: String,
    isLoading: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit,
    onDownload: () -> Unit
) {
    var showMoreOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onView() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            // Status Indicator Bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(
                        if (bill.isUnpaid) MaterialTheme.colorScheme.error
                        else Color(0xFF4CAF50)
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Invoice ID and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "INV-${bill.invoiceNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = if (bill.isUnpaid) MaterialTheme.colorScheme.errorContainer
                        else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (bill.isUnpaid) "PENDING" else "PAID",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (bill.isUnpaid) MaterialTheme.colorScheme.error
                            else Color(0xFF2E7D32),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Customer Info
                Text(
                    bill.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    bill.customerPhone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Remaining Note
                if (!bill.remainingNote.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = bill.remainingNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Date and Financials
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${bill.invoiceDate} • ${bill.invoiceTime}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (bill.isUnpaid) {
                            Text(
                                "Due Balance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
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
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                "Paid",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                "Full Payment",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

                    IconButton(onClick = onView) {
                        Icon(
                            Icons.Default.Visibility,
                            null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Default.Share,
                            null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onEdit) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMoreOptions = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreOptions,
                            onDismissRequest = { showMoreOptions = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Print Receipt") },
                                onClick = {
                                    onPrint()
                                    showMoreOptions = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Print,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save to Gallery") },
                                onClick = {
                                    onDownload()
                                    showMoreOptions = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Download,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 0.5.dp
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete Bill",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    onDelete()
                                    showMoreOptions = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBillsState(onCreateBill: () -> Unit) {
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
                    Icons.AutoMirrored.Filled.ReceiptLong,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "No Bills Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Create your first bill to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            ExtendedFloatingActionButton(
                onClick = onCreateBill,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create First Bill")
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
        },
        shape = RoundedCornerShape(24.dp)
    ) {
        DatePicker(state = datePickerState)
    }
}