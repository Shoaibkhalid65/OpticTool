package com.optictoolcompk.opticaltool.di

import android.content.Context
import androidx.room.Room
import com.optictoolcompk.opticaltool.data.local.dao.BillDao
import com.optictoolcompk.opticaltool.data.local.dao.NotebookSectionDao
import com.optictoolcompk.opticaltool.data.local.dao.PrescriptionDao
import com.optictoolcompk.opticaltool.data.local.dao.ShopSettingsDao
import com.optictoolcompk.opticaltool.data.local.database.AppDatabase
import com.optictoolcompk.opticaltool.data.repository.BillRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "optician_app_database_4"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun providePrescriptionDao(database: AppDatabase): PrescriptionDao {
        return database.prescriptionDao()
    }

    @Provides
    @Singleton
    fun provideBillDao(database: AppDatabase): BillDao {
        return database.billDao()
    }

    @Provides
    @Singleton
    fun provideShopSettingsDao(database: AppDatabase): ShopSettingsDao {
        return database.shopSettingsDao()
    }

    @Provides
    @Singleton
    fun provideBillRepository(
        billDao: BillDao,
        shopSettingsDao: ShopSettingsDao
    ): BillRepository {
        return BillRepository(billDao, shopSettingsDao)
    }

    @Provides
    @Singleton
    fun provideNotebookSectionDao(
        database: AppDatabase
    ): NotebookSectionDao {
        return database.notebookSectionDao()
    }
}