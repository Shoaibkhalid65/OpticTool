package com.optictoolcompk.opticaltool.data.local.database


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.optictoolcompk.opticaltool.data.local.dao.BillDao
import com.optictoolcompk.opticaltool.data.local.dao.NotebookSectionDao
import com.optictoolcompk.opticaltool.data.local.dao.PrescriptionDao
import com.optictoolcompk.opticaltool.data.local.dao.ShopSettingsDao
import com.optictoolcompk.opticaltool.data.models.BillEntity
import com.optictoolcompk.opticaltool.data.models.BillItemEntity
import com.optictoolcompk.opticaltool.data.models.BillTypeConverters
import com.optictoolcompk.opticaltool.data.models.NotebookRowEntity
import com.optictoolcompk.opticaltool.data.models.NotebookSectionEntity
import com.optictoolcompk.opticaltool.data.models.NotebookTypeConverters
import com.optictoolcompk.opticaltool.data.models.PrescriptionEntity
import com.optictoolcompk.opticaltool.data.models.ShopSettingsEntity

@Database(
    entities = [
        PrescriptionEntity::class,
        BillEntity::class,
        BillItemEntity::class,
        ShopSettingsEntity::class,
        NotebookSectionEntity::class,
        NotebookRowEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(BillTypeConverters::class, NotebookTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun billDao(): BillDao
    abstract fun shopSettingsDao(): ShopSettingsDao
    abstract fun notebookSectionDao(): NotebookSectionDao
}