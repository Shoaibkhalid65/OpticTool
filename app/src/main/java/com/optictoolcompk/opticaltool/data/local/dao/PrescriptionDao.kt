package com.optictoolcompk.opticaltool.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.optictoolcompk.opticaltool.data.models.PrescriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {

    @Insert
    suspend fun insert(prescription: PrescriptionEntity): Long

    @Update
    suspend fun update(prescription: PrescriptionEntity)

    @Delete
    suspend fun delete(prescription: PrescriptionEntity)

    @Query("SELECT * FROM prescriptions ORDER BY id DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionById(id: Long): PrescriptionEntity?

    @Query("SELECT * FROM prescriptions WHERE prescriptionNumber = :prescriptionNumber")
    suspend fun getPrescriptionByNumber(prescriptionNumber: String): PrescriptionEntity?

    @Query("SELECT MAX(id) FROM prescriptions")
    suspend fun getLastPrescriptionId(): Long?

    @Query("SELECT COUNT(*) FROM prescriptions")
    suspend fun getPrescriptionCount(): Int

    @Query("SELECT * FROM prescriptions WHERE patientName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchPrescriptions(query: String): Flow<List<PrescriptionEntity>>
}