package com.optictoolcompk.opticaltool.data.local.dao



import androidx.room.*
import com.optictoolcompk.opticaltool.data.models.NotebookRowEntity
import com.optictoolcompk.opticaltool.data.models.NotebookSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookSectionDao {

    // ==================== SECTION OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: NotebookSectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<NotebookSectionEntity>): List<Long>

    @Update
    suspend fun updateSection(section: NotebookSectionEntity)

    @Delete
    suspend fun deleteSection(section: NotebookSectionEntity)

    @Query("DELETE FROM notebook_sections WHERE id = :sectionId")
    suspend fun deleteSectionById(sectionId: Long)

    @Query("SELECT * FROM notebook_sections ORDER BY orderIndex ASC")
    fun getAllSections(): Flow<List<NotebookSectionEntity>>

    @Query("SELECT * FROM notebook_sections WHERE id = :id")
    suspend fun getSectionById(id: Long): NotebookSectionEntity?

    @Query("SELECT * FROM notebook_sections WHERE name = :name LIMIT 1")
    suspend fun getSectionByName(name: String): NotebookSectionEntity?

    @Query("SELECT COUNT(*) FROM notebook_sections")
    suspend fun getSectionCount(): Int

    @Query("SELECT MAX(orderIndex) FROM notebook_sections")
    suspend fun getMaxSectionOrderIndex(): Int?

    @Query("DELETE FROM notebook_sections")
    suspend fun deleteAllSections()

    // ==================== ROW OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRow(row: NotebookRowEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<NotebookRowEntity>): List<Long>

    @Update
    suspend fun updateRow(row: NotebookRowEntity)

    @Update
    suspend fun updateRows(rows: List<NotebookRowEntity>)

    @Delete
    suspend fun deleteRow(row: NotebookRowEntity)

    @Query("DELETE FROM notebook_rows WHERE id = :rowId")
    suspend fun deleteRowById(rowId: Long)

    @Query("DELETE FROM notebook_rows WHERE id IN (:rowIds)")
    suspend fun deleteRowsByIds(rowIds: List<Long>)

    @Query("SELECT * FROM notebook_rows WHERE sectionId = :sectionId ORDER BY orderIndex ASC")
    fun getRowsBySection(sectionId: Long): Flow<List<NotebookRowEntity>>

    @Query("SELECT * FROM notebook_rows WHERE sectionId = :sectionId ORDER BY orderIndex ASC")
    suspend fun getRowsBySectionSync(sectionId: Long): List<NotebookRowEntity>

    @Query("SELECT * FROM notebook_rows WHERE id = :id")
    suspend fun getRowById(id: Long): NotebookRowEntity?

    @Query("SELECT COUNT(*) FROM notebook_rows WHERE sectionId = :sectionId")
    suspend fun getRowCountBySection(sectionId: Long): Int

    @Query("SELECT COUNT(*) FROM notebook_rows")
    suspend fun getTotalRowCount(): Int

    @Query("SELECT MAX(orderIndex) FROM notebook_rows WHERE sectionId = :sectionId")
    suspend fun getMaxRowOrderIndex(sectionId: Long): Int?

    @Query("DELETE FROM notebook_rows WHERE sectionId = :sectionId")
    suspend fun deleteAllRowsInSection(sectionId: Long)

    @Query("DELETE FROM notebook_rows")
    suspend fun deleteAllRows()

    // ==================== CLIPBOARD (COPY) OPERATIONS ====================

    @Query("""
        SELECT * FROM notebook_rows 
        WHERE isCopy = 1 
        ORDER BY 
            CASE WHEN sphValue != '0.00' AND sphValue != '' AND (cylValue = '0.00' OR cylValue = '') THEN 0
                 WHEN (sphValue = '0.00' OR sphValue = '') AND cylValue != '0.00' AND cylValue != '' THEN 1
                 ELSE 2
            END,
            CAST(sphValue AS REAL) ASC,
            CAST(cylValue AS REAL) ASC
    """)
    fun getAllCopiedRows(): Flow<List<NotebookRowEntity>>

    @Query("SELECT COUNT(*) FROM notebook_rows WHERE isCopy = 1")
    suspend fun getCopiedRowCount(): Int

    @Query("UPDATE notebook_rows SET isCopy = 0")
    suspend fun clearAllCopyFlags()

    @Query("UPDATE notebook_rows SET isCopy = 0 WHERE sectionId = :sectionId")
    suspend fun clearCopyFlagsInSection(sectionId: Long)

    // ==================== ORDERED OPERATIONS ====================

    @Query("SELECT COUNT(*) FROM notebook_rows WHERE isOrdered = 1")
    suspend fun getOrderedRowCount(): Int

    @Query("UPDATE notebook_rows SET isOrdered = 1 WHERE isCopy = 1")
    suspend fun markCopiedRowsAsOrdered()

    @Query("UPDATE notebook_rows SET isOrdered = 0")
    suspend fun clearAllOrderedFlags()

    @Query("UPDATE notebook_rows SET isOrdered = 1 WHERE sectionId = :sectionId")
    suspend fun markAllRowsAsOrdered(sectionId: Long)

    @Query("UPDATE notebook_rows SET isOrdered = 0 WHERE sectionId = :sectionId")
    suspend fun clearOrderedFlagsInSection(sectionId: Long)

    // ==================== DELETE MARK OPERATIONS ====================

    @Query("SELECT COUNT(*) FROM notebook_rows WHERE isDelete = 1")
    suspend fun getMarkedForDeleteCount(): Int

    @Query("DELETE FROM notebook_rows WHERE isDelete = 1")
    suspend fun deleteMarkedRows()

    @Query("DELETE FROM notebook_rows WHERE isDelete = 1 AND sectionId = :sectionId")
    suspend fun deleteMarkedRowsInSection(sectionId: Long)

    @Query("UPDATE notebook_rows SET isDelete = 0")
    suspend fun clearAllDeleteFlags()

    @Query("UPDATE notebook_rows SET isDelete = 1 WHERE sectionId = :sectionId")
    suspend fun markAllRowsForDelete(sectionId: Long)

    @Query("UPDATE notebook_rows SET isDelete = 0 WHERE sectionId = :sectionId")
    suspend fun clearDeleteFlagsInSection(sectionId: Long)

    // ==================== BATCH OPERATIONS ====================

    @Transaction
    suspend fun replaceSectionWithRows(section: NotebookSectionEntity, rows: List<NotebookRowEntity>) {
        // Delete existing section and rows (cascade will handle rows)
        section.id.let { sectionId ->
            if (sectionId > 0) {
                deleteSectionById(sectionId)
            }
        }

        // Insert new section
        val newSectionId = insertSection(section)

        // Insert rows with new section ID
        val updatedRows = rows.map { it.copy(sectionId = newSectionId) }
        insertRows(updatedRows)
    }

    @Transaction
    suspend fun resetToDefaultSections(sections: List<NotebookSectionEntity>) {
        deleteAllRows()
        deleteAllSections()
        insertSections(sections)
    }

    // ==================== COMBINED QUERIES ====================

    /**
     * Get section with its rows (one-to-many relationship)
     */
    @Transaction
    @Query("SELECT * FROM notebook_sections WHERE id = :sectionId")
    suspend fun getSectionWithRows(sectionId: Long): SectionWithRows?

    @Transaction
    @Query("SELECT * FROM notebook_sections ORDER BY orderIndex ASC")
    fun getAllSectionsWithRows(): Flow<List<SectionWithRows>>

    @Transaction
    @Query("SELECT * FROM notebook_sections ORDER BY orderIndex ASC")
    suspend fun getAllSectionsWithRowsSync(): List<SectionWithRows>
}

/**
 * Data class representing a section with its rows
 * Used for @Transaction queries
 */
data class SectionWithRows(
    @Embedded val section: NotebookSectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sectionId"
    )
    val rows: List<NotebookRowEntity>
)