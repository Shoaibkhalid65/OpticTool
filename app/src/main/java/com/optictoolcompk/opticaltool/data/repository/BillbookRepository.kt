package com.optictoolcompk.opticaltool.data.repository


import com.optictoolcompk.opticaltool.data.local.dao.BillDao
import com.optictoolcompk.opticaltool.data.local.dao.ShopSettingsDao
import com.optictoolcompk.opticaltool.data.models.Bill
import com.optictoolcompk.opticaltool.data.models.BillStatistics
import com.optictoolcompk.opticaltool.data.models.ShopSettings
import com.optictoolcompk.opticaltool.data.models.toBill
import com.optictoolcompk.opticaltool.data.models.toEntity
import com.optictoolcompk.opticaltool.data.models.toShopSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepository @Inject constructor(
    private val billDao: BillDao,
    private val shopSettingsDao: ShopSettingsDao
) {

    // ==================== CREATE ====================

    suspend fun createBill(bill: Bill): Result<Long> {
        return try {
            val entity = bill.toEntity()
            val items = bill.items.mapIndexed { index, item ->
                item.toEntity(billId = 0, orderIndex = index)
            }

            val billId = billDao.insertBillWithItems(entity, items)

            Result.success(billId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ==================== READ ====================

    fun getAllBillsFlow(): Flow<List<Bill>> {
        return billDao.getAllBillsFlow().map { entities ->
            entities.map { entity ->
                val items = billDao.getBillItems(entity.id)
                entity.toBill(items)
            }
        }
    }

    suspend fun getBillById(billId: Long): Bill? {
        return try {
            val entity = billDao.getBillById(billId) ?: return null
            val items = billDao.getBillItems(billId)
            entity.toBill(items)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getBillByInvoiceNumber(invoiceNumber: String): Bill? {
        return try {
            val entity = billDao.getBillByInvoiceNumber(invoiceNumber) ?: return null
            val items = billDao.getBillItems(entity.id)
            entity.toBill(items)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun searchBills(query: String): Flow<List<Bill>> {
        return billDao.searchBills(query).map { entities ->
            entities.map { entity ->
                val items = billDao.getBillItems(entity.id)
                entity.toBill(items)
            }
        }
    }

    fun getUnpaidBills(): Flow<List<Bill>> {
        return billDao.getUnpaidBills().map { entities ->
            entities.map { entity ->
                val items = billDao.getBillItems(entity.id)
                entity.toBill(items)
            }
        }
    }

    fun searchUnpaidBills(query: String): Flow<List<Bill>> {
        return billDao.searchUnpaidBills(query).map { entities ->
            entities.map { entity ->
                val items = billDao.getBillItems(entity.id)
                entity.toBill(items)
            }
        }
    }

    suspend fun searchUnpaidBillsForPreviousAmount(query: String): List<Bill> {
        return try {
            val entities = billDao.searchUnpaidBillsForPreviousAmount(query)
            entities.map { entity ->
                val items = billDao.getBillItems(entity.id)
                entity.toBill(items)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ==================== UPDATE ====================

    suspend fun updateBill(bill: Bill): Result<Unit> {
        return try {
            val entity = bill.toEntity()
            val items = bill.items.mapIndexed { index, item ->
                item.toEntity(billId = bill.id, orderIndex = index)
            }

            billDao.updateBillWithItems(entity, items)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ==================== DELETE ====================

    suspend fun deleteBill(billId: Long): Result<Unit> {
        return try {
            billDao.deleteBillById(billId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ==================== STATISTICS ====================

    suspend fun getBillStatistics(): BillStatistics {
        return try {
            BillStatistics(
                totalSalesAmount = billDao.getTotalSalesAmount() ?: 0.0,
                totalUnpaidAmount = billDao.getTotalUnpaidAmount() ?: 0.0,
                totalBillsCount = billDao.getTotalBillsCount(),
                unpaidBillsCount = billDao.getUnpaidBillsCount(),
                paidBillsCount = billDao.getPaidBillsCount()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BillStatistics()
        }
    }

    // ==================== INVOICE NUMBER ====================

    suspend fun getNextInvoiceNumber(): String {
        return try {
            val maxNumber = billDao.getMaxInvoiceNumber() ?: 0
            (maxNumber + 1).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "1"
        }
    }

    // ==================== SHOP SETTINGS ====================

    fun getShopSettingsFlow(): Flow<ShopSettings> {
        return shopSettingsDao.getShopSettingsFlow().map { entity ->
            entity?.toShopSettings() ?: ShopSettings()
        }
    }

    suspend fun getShopSettings(): ShopSettings {
        return try {
            shopSettingsDao.getShopSettings()?.toShopSettings() ?: ShopSettings()
        } catch (e: Exception) {
            e.printStackTrace()
            ShopSettings()
        }
    }

    suspend fun saveShopSettings(settings: ShopSettings): Result<Unit> {
        return try {
            shopSettingsDao.saveShopSettings(settings.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ==================== CLOUD SYNC (Future Implementation) ====================

    suspend fun getUnsyncedBills(): List<Bill> {
        return try {
            val entities = billDao.getUnsyncedBills()
            entities.map { entity ->
                val items = billDao.getBillItems(entity.id)
                entity.toBill(items)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun markAsSynced(billId: Long, cloudId: String): Result<Unit> {
        return try {
            billDao.markAsSynced(billId, cloudId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    //    settle the unpaid bills to new bill
    suspend fun settleBills(
        billIds: Set<Long>,
        newInvoiceNumber: String
    ) {
        billIds.forEach { billId ->
            billDao.settleBillById(
                billId = billId,
                note = "Added to Invoice #$newInvoiceNumber"
            )
        }
    }
}