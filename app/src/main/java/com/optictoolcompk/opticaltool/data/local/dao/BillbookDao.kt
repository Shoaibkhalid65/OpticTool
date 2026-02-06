package com.optictoolcompk.opticaltool.data.local.dao



import androidx.room.*
import com.optictoolcompk.opticaltool.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    // ==================== CREATE ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillItems(items: List<BillItemEntity>)

    @Transaction
    suspend fun insertBillWithItems(bill: BillEntity, items: List<BillItemEntity>): Long {
        val billId = insertBill(bill)
        val itemsWithBillId = items.map { it.copy(billId = billId) }
        insertBillItems(itemsWithBillId)
        return billId
    }

    // ==================== READ ====================

    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): BillEntity?

    @Query("SELECT * FROM bills WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getBillByInvoiceNumber(invoiceNumber: String): BillEntity?

    @Query("SELECT * FROM bill_items WHERE billId = :billId ORDER BY orderIndex ASC")
    suspend fun getBillItems(billId: Long): List<BillItemEntity>

    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    fun getAllBillsFlow(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    suspend fun getAllBills(): List<BillEntity>

    // ==================== UPDATE ====================

    @Update
    suspend fun updateBill(bill: BillEntity)

    // method to settle amount of the previous bill when we add it into the current bill
    @Query("""
        UPDATE bills
        SET remainingAmount = 0,
            remainingNote = :note
        WHERE id = :billId
    """)
    suspend fun settleBillById(
        billId: Long,
        note: String
    )


    @Query("DELETE FROM bill_items WHERE billId = :billId")
    suspend fun deleteBillItems(billId: Long)

    @Transaction
    suspend fun updateBillWithItems(bill: BillEntity, items: List<BillItemEntity>) {
        updateBill(bill)
        deleteBillItems(bill.id)
        insertBillItems(items.map { it.copy(billId = bill.id) })
    }

    // ==================== DELETE ====================

    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBillById(billId: Long)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    // ==================== SEARCH & FILTER ====================

    @Query("""
        SELECT * FROM bills 
        WHERE (customerName LIKE '%' || :query || '%' 
            OR customerPhone LIKE '%' || :query || '%'
            OR invoiceNumber LIKE '%' || :query || '%'
            OR invoiceDate LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchBills(query: String): Flow<List<BillEntity>>

    @Query("""
        SELECT * FROM bills 
        WHERE remainingAmount > 0
        ORDER BY createdAt DESC
    """)
    fun getUnpaidBills(): Flow<List<BillEntity>>

    @Query("""
        SELECT * FROM bills 
        WHERE remainingAmount > 0
        AND (customerName LIKE '%' || :query || '%' 
            OR customerPhone LIKE '%' || :query || '%'
            OR invoiceNumber LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchUnpaidBills(query: String): Flow<List<BillEntity>>

    @Query("""
        SELECT * FROM bills 
        WHERE remainingAmount > 0
        AND (customerName LIKE '%' || :query || '%' 
            OR customerPhone LIKE '%' || :query || '%')
        LIMIT 20
    """)
    suspend fun searchUnpaidBillsForPreviousAmount(query: String): List<BillEntity>

    // ==================== STATISTICS ====================

    @Query("SELECT SUM(totalAmount) FROM bills")
    suspend fun getTotalSalesAmount(): Double?

    @Query("SELECT SUM(remainingAmount) FROM bills WHERE remainingAmount > 0")
    suspend fun getTotalUnpaidAmount(): Double?

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun getTotalBillsCount(): Int

    @Query("SELECT COUNT(*) FROM bills WHERE remainingAmount > 0")
    suspend fun getUnpaidBillsCount(): Int

    @Query("SELECT COUNT(*) FROM bills WHERE remainingAmount <= 0 AND totalAmount > 0")
    suspend fun getPaidBillsCount(): Int

    // ==================== INVOICE NUMBER ====================

    @Query("""
        SELECT MAX(CAST(invoiceNumber AS INTEGER)) 
        FROM bills 
        WHERE invoiceNumber GLOB '[0-9]*'
    """)
    suspend fun getMaxInvoiceNumber(): Int?

    // ==================== CLOUD SYNC (Future) ====================

    @Query("SELECT * FROM bills WHERE needsSync = 1 AND syncedWithCloud = 0")
    suspend fun getUnsyncedBills(): List<BillEntity>

    @Query("UPDATE bills SET syncedWithCloud = 1, cloudId = :cloudId, needsSync = 0 WHERE id = :billId")
    suspend fun markAsSynced(billId: Long, cloudId: String)
}


@Dao
interface ShopSettingsDao {

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    suspend fun getShopSettings(): ShopSettingsEntity?

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    fun getShopSettingsFlow(): Flow<ShopSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopSettings(settings: ShopSettingsEntity)

    @Update
    suspend fun updateShopSettings(settings: ShopSettingsEntity)

    @Transaction
    suspend fun saveShopSettings(settings: ShopSettingsEntity) {
        val existing = getShopSettings()
        if (existing == null) {
            insertShopSettings(settings)
        } else {
            updateShopSettings(settings.copy(id = 1))
        }
    }
}