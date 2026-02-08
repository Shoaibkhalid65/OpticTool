package com.optictoolcompk.opticaltool.data.models


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter

// ==================== ROOM ENTITIES ====================

@Entity(
    tableName = "bills",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerPhone"]),
        Index(value = ["remainingAmount"])
    ]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Invoice details
    val invoiceNumber: String,
    val invoiceDate: String,
    val invoiceTime: String,

    // Customer info
    val customerName: String,
    val customerPhone: String,
    val customerCity: String = "",

    // Amounts
    val totalAmount: Double,
    val discount: Double = 0.0,
    val advance: Double = 0.0,
    val advance2: Double = 0.0,
    val advance2Date: String? = null,
    val advance3: Double = 0.0,
    val advance3Date: String? = null,
    val previousAmount: Double = 0.0,
    val remainingAmount: Double,
    val remainingNote: String? = null,

    // Additional fields
    val pickupDate: String? = null,

    // Prescription reference (for backward compatibility)
    val prescriptionId: Long? = null,
    val prescriptionNumber: String? = null,

    // NEW: Store multiple prescription image paths (comma-separated)
    // This allows up to 3 prescription images
    val prescriptionImagesPaths: String? = null,

    // NEW: Store prescription form data as JSON string (for bills created with form)
    val prescriptionFormDataJson: String? = null,

    // Images stored as comma-separated local file paths
    val imagesPaths: String? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Cloud sync flags (for future use)
    val syncedWithCloud: Boolean = false,
    val cloudId: String? = null,
    val needsSync: Boolean = true
)

@Entity(
    tableName = "bill_items",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("billId")]
)
data class BillItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long,
    val itemName: String,
    val quantity: Int,
    val price: Double,
    val total: Double,
    val orderIndex: Int = 0
)

// Shop settings stored in SharedPreferences, but we'll create a data class for it
@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Always 1, only one row
    val shopName: String = "Ansari Optical Service",
    val shopAddress: String = "near Ahle Hadis Masjid Rajan Pur",
    val shopPhone: String = "03340064776",
    val termsAndConditions: String = "",
    val currency: String = "Rs",
    val updatedAt: Long = System.currentTimeMillis()
)

// ==================== UI MODELS ====================

data class Bill(
    val id: Long = 0,
    val invoiceNumber: String = "",
    val invoiceDate: String = "",
    val invoiceTime: String = "",

    val customerName: String = "",
    val customerPhone: String = "",
    val customerCity: String = "",

    val items: List<BillItem> = emptyList(),

    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val advance: Double = 0.0,
    val advance2: Double = 0.0,
    val advance2Date: String? = null,
    val advance3: Double = 0.0,
    val advance3Date: String? = null,
    val previousAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val remainingNote: String? = null,

    val pickupDate: String? = null,

    val prescriptionId: Long? = null,
    val prescriptionNumber: String? = null,
    val prescription: PrescriptionData? = null,

    // NEW: Multiple prescription images (up to 3)
    val prescriptionImagesPaths: List<String> = emptyList(),

    // NEW: Prescription form data (when created via form)
    val prescriptionFormData: PrescriptionFormDataForBill? = null,

    val imagesPaths: List<String> = emptyList(),

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val syncedWithCloud: Boolean = false,
    val cloudId: String? = null
) {
    val isUnpaid: Boolean
        get() = remainingAmount > 0.01 // Use small threshold for floating point

    val isPaid: Boolean
        get() = !isUnpaid && totalAmount > 0

    fun calculateRemaining(): Double {
        return totalAmount - discount - advance - advance2 - advance3 + previousAmount
    }
}

data class BillItem(
    val id: Long = 0,
    val itemName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
) {
    val total: Double
        get() = quantity * price
}

data class PrescriptionData(
    val id: Long = 0,
    val prescriptionNumber: String = "",
    val name: String = "",
    val age: String = "",
    val phone: String = "",
    val city: String = "",
    val rx: RxData = RxData(),
    val ipd: IpdData = IpdData(),
    val checkedBy: String = "",
    val images: List<String> = emptyList()
)

// NEW: Prescription form data that will be stored with bill
@Serializable
data class PrescriptionFormDataForBill(
    val prescriptionNumber: String = "",
    val patientName: String = "",
    val phone: String = "",
    val age: String = "",
    val city: String = "",
    val rightSph: String = "",
    val rightCyl: String = "",
    val rightAxis: String = "",
    val rightVa: String = "",
    val leftSph: String = "",
    val leftCyl: String = "",
    val leftAxis: String = "",
    val leftVa: String = "",
    val addPower: String = "",
    val ipdNear: String = "",
    val ipdDistance: String = "",
    val checkedBy: String = "",
    // Store the captured image path of the form
    val prescriptionImagePath: String = ""
)

data class RxData(
    val right: EyeData = EyeData(),
    val left: EyeData = EyeData(),
    val add: String = ""
)

data class EyeData(
    val sph: String = "",
    val cyl: String = "",
    val axis: String = "",
    val va: String = ""
)

data class IpdData(
    val d: String = "",
    val n: String = ""
)

data class ShopSettings(
    val shopName: String = "Ansari Optical Service",
    val shopAddress: String = "near Ahle Hadis Masjid Rajan Pur",
    val shopPhone: String = "03340064776",
    val termsAndConditions: String = "",
    val currency: String = "Rs"
)

// ==================== STATISTICS ====================

data class BillStatistics(
    val totalSalesAmount: Double = 0.0,
    val totalUnpaidAmount: Double = 0.0,
    val totalBillsCount: Int = 0,
    val unpaidBillsCount: Int = 0,
    val paidBillsCount: Int = 0
)

// ==================== FILTER & SORT ====================

data class BillFilter(
    val searchQuery: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val showOnlyUnpaid: Boolean = false,
    val sortBy: BillSortOption = BillSortOption.NEWEST_FIRST
)

enum class BillSortOption(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    AMOUNT_HIGH_TO_LOW("Amount: High to Low"),
    AMOUNT_LOW_TO_HIGH("Amount: Low to High"),
    UNPAID_ONLY("Due Bills Only")
}

// ==================== DISPLAY SETTINGS ====================

data class BillDisplaySettings(
    val showPrescription: Boolean = true,
    val showIpd: Boolean = true,
    val showCheckedBy: Boolean = true,
    val autoSavePrescriptions: Boolean = false,
    val showUploadCaptureImages: Boolean = true
)

// ==================== TYPE CONVERTERS ====================

class BillTypeConverters {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}

// ==================== EXTENSIONS ====================

fun BillEntity.toBill(items: List<BillItemEntity>): Bill {
    // Parse prescription form data from JSON if exists
    val prescriptionFormData = prescriptionFormDataJson?.let {
        if (it.isNotBlank()) {
            try {
                val jsonConfig = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true // Useful if the JSON has nulls where you have defaults
                }
                jsonConfig.decodeFromString<PrescriptionFormDataForBill>(it)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    return Bill(
        id = id,
        invoiceNumber = invoiceNumber,
        invoiceDate = invoiceDate,
        invoiceTime = invoiceTime,
        customerName = customerName,
        customerPhone = customerPhone,
        customerCity = customerCity,
        items = items.map { it.toBillItem() },
        totalAmount = totalAmount,
        discount = discount,
        advance = advance,
        advance2 = advance2,
        advance2Date = advance2Date,
        advance3 = advance3,
        advance3Date = advance3Date,
        previousAmount = previousAmount,
        remainingAmount = remainingAmount,
        remainingNote = remainingNote,
        pickupDate = pickupDate,
        prescriptionId = prescriptionId,
        prescriptionNumber = prescriptionNumber,
        prescriptionImagesPaths = prescriptionImagesPaths?.split(",")?.filter { it.isNotBlank() }
            ?: emptyList(),
        prescriptionFormData = prescriptionFormData,
        imagesPaths = imagesPaths?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncedWithCloud = syncedWithCloud,
        cloudId = cloudId
    )
}

fun BillItemEntity.toBillItem(): BillItem {
    return BillItem(
        id = id,
        itemName = itemName,
        quantity = quantity,
        price = price
    )
}

fun Bill.toEntity(): BillEntity {
    // Convert prescription form data to JSON
    val prescriptionFormDataJson = prescriptionFormData?.let {
        try {
            val prettyJson = Json {
                prettyPrint = true
                encodeDefaults = true // Includes fields even if they match the default value
            }
            prettyJson.encodeToString(it)
        } catch (e: Exception) {
            null
        }
    }

    return BillEntity(
        id = if (id == 0L) 0 else id,
        invoiceNumber = invoiceNumber,
        invoiceDate = invoiceDate,
        invoiceTime = invoiceTime,
        customerName = customerName,
        customerPhone = customerPhone,
        customerCity = customerCity,
        totalAmount = totalAmount,
        discount = discount,
        advance = advance,
        advance2 = advance2,
        advance2Date = advance2Date,
        advance3 = advance3,
        advance3Date = advance3Date,
        previousAmount = previousAmount,
        remainingAmount = remainingAmount,
        remainingNote = remainingNote,
        pickupDate = pickupDate,
        prescriptionId = prescriptionId,
        prescriptionNumber = prescriptionNumber,
        prescriptionImagesPaths = prescriptionImagesPaths.joinToString(","),
        prescriptionFormDataJson = prescriptionFormDataJson,
        imagesPaths = imagesPaths.joinToString(","),
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
        syncedWithCloud = syncedWithCloud,
        cloudId = cloudId,
        needsSync = true
    )
}

fun BillItem.toEntity(billId: Long, orderIndex: Int): BillItemEntity {
    return BillItemEntity(
        id = if (id == 0L) 0 else id,
        billId = billId,
        itemName = itemName,
        quantity = quantity,
        price = price,
        total = total,
        orderIndex = orderIndex
    )
}

fun ShopSettingsEntity.toShopSettings(): ShopSettings {
    return ShopSettings(
        shopName = shopName,
        shopAddress = shopAddress,
        shopPhone = shopPhone,
        termsAndConditions = termsAndConditions,
        currency = currency
    )
}

fun ShopSettings.toEntity(): ShopSettingsEntity {
    return ShopSettingsEntity(
        id = 1,
        shopName = shopName,
        shopAddress = shopAddress,
        shopPhone = shopPhone,
        termsAndConditions = termsAndConditions,
        currency = currency,
        updatedAt = System.currentTimeMillis()
    )
}