package com.optictoolcompk.opticaltool.data.repository

import com.optictoolcompk.opticaltool.data.local.dao.NotebookSectionDao
import android.content.Context
import com.optictoolcompk.opticaltool.data.local.dao.SectionWithRows
import com.optictoolcompk.opticaltool.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotebookRepository @Inject constructor(
    private val notebookDao: NotebookSectionDao,
    @param:ApplicationContext private val context: Context
) {

    // ==================== SECTION OPERATIONS ====================

    /**
     * Get all sections with their rows as Flow
     * Added flowOn to ensure mapping happens off the main thread
     */
    fun getAllSections(): Flow<List<NotebookSection>> {
        return notebookDao.getAllSectionsWithRows().map { sectionsWithRows ->
            sectionsWithRows.map { it.toNotebookSection() }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get all sections with their rows (sync)
     */
    suspend fun getAllSectionsSync(): List<NotebookSection> {
        return notebookDao.getAllSectionsWithRowsSync().map { it.toNotebookSection() }
    }

    /**
     * Get section by ID with its rows
     */
    suspend fun getSectionById(sectionId: Long): NotebookSection? {
        return notebookDao.getSectionWithRows(sectionId)?.toNotebookSection()
    }

    /**
     * Create a new section
     */
    suspend fun createSection(name: String, mode: NotebookMode): Result<Long> {
        return try {
            val existing = notebookDao.getSectionByName(name)
            if (existing != null) {
                return Result.failure(Exception("Section with name '$name' already exists"))
            }

            val maxOrderIndex = notebookDao.getMaxSectionOrderIndex() ?: -1
            val section = NotebookSectionEntity(
                name = name,
                mode = mode.value,
                orderIndex = maxOrderIndex + 1
            )

            val id = notebookDao.insertSection(section)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update section name or mode
     */
    suspend fun updateSection(section: NotebookSection): Result<Unit> {
        return try {
            notebookDao.updateSection(section.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete section (cascade deletes rows)
     */
    suspend fun deleteSection(sectionId: Long): Result<Unit> {
        return try {
            notebookDao.deleteSectionById(sectionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Move section up
     */
    suspend fun moveSectionUp(sectionId: Long): Result<Unit> {
        return try {
            val sections = notebookDao.getAllSectionsWithRowsSync()
            val currentIndex = sections.indexOfFirst { it.section.id == sectionId }

            if (currentIndex <= 0) {
                return Result.failure(Exception("Cannot move section up - already at top"))
            }

            val currentSection = sections[currentIndex].section
            val previousSection = sections[currentIndex - 1].section

            notebookDao.updateSection(currentSection.copy(orderIndex = previousSection.orderIndex))
            notebookDao.updateSection(previousSection.copy(orderIndex = currentSection.orderIndex))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Move section down
     */
    suspend fun moveSectionDown(sectionId: Long): Result<Unit> {
        return try {
            val sections = notebookDao.getAllSectionsWithRowsSync()
            val currentIndex = sections.indexOfFirst { it.section.id == sectionId }

            if (currentIndex < 0 || currentIndex >= sections.size - 1) {
                return Result.failure(Exception("Cannot move section down - already at bottom"))
            }

            val currentSection = sections[currentIndex].section
            val nextSection = sections[currentIndex + 1].section

            notebookDao.updateSection(currentSection.copy(orderIndex = nextSection.orderIndex))
            notebookDao.updateSection(nextSection.copy(orderIndex = currentSection.orderIndex))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ROW OPERATIONS ====================

    suspend fun addRow(
        sectionId: Long,
        sphValue: String,
        cylValue: String,
        pairs: Int,
        mode: NotebookMode
    ): Result<Long> {
        return try {
            val maxOrderIndex = notebookDao.getMaxRowOrderIndex(sectionId) ?: -1
            val row = NotebookRowEntity(
                sectionId = sectionId,
                sphValue = sphValue,
                cylValue = cylValue,
                pairs = pairs,
                originalFormat = mode.value,
                orderIndex = maxOrderIndex + 1
            )

            val id = notebookDao.insertRow(row)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRow(row: NotebookRow): Result<Unit> {
        return try {
            val entity = notebookDao.getRowById(row.id)
                ?: return Result.failure(Exception("Row not found"))

            notebookDao.updateRow(entity.copy(
                sphValue = row.sphValue,
                cylValue = row.cylValue,
                pairs = row.pairs,
                isCopy = row.isCopy,
                isOrdered = row.isOrdered,
                isDelete = row.isDelete,
                updatedAt = System.currentTimeMillis()
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCopyFlag(rowId: Long): Result<Unit> {
        return try {
            val row = notebookDao.getRowById(rowId)
                ?: return Result.failure(Exception("Row not found"))

            notebookDao.updateRow(row.copy(
                isCopy = !row.isCopy,
                updatedAt = System.currentTimeMillis()
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleOrderedFlag(rowId: Long): Result<Unit> {
        return try {
            val row = notebookDao.getRowById(rowId)
                ?: return Result.failure(Exception("Row not found"))

            notebookDao.updateRow(row.copy(
                isOrdered = !row.isOrdered,
                updatedAt = System.currentTimeMillis()
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleDeleteFlag(rowId: Long): Result<Unit> {
        return try {
            val row = notebookDao.getRowById(rowId)
                ?: return Result.failure(Exception("Row not found"))

            notebookDao.updateRow(row.copy(
                isDelete = !row.isDelete,
                updatedAt = System.currentTimeMillis()
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRow(rowId: Long): Result<Unit> {
        return try {
            notebookDao.deleteRowById(rowId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== CLIPBOARD OPERATIONS ====================

    /**
     * Get all copied rows for clipboard with pagination
     * Added flowOn to ensure heavy sorting and mapping logic doesn't block UI
     */
    fun getClipboardData(page: Int = 1, rowsPerPage: Int = 20): Flow<ClipboardData> {
        return notebookDao.getAllCopiedRows().combine(
            notebookDao.getAllSectionsWithRows()
        ) { copiedRows, sectionsWithRows ->
            val sorted = sortClipboardRows(copiedRows)
            val sectionMap = sectionsWithRows.associate { it.section.id to it.section.name }

            val clipboardRows = sorted.mapIndexed { index, row ->
                ClipboardRow(
                    id = row.id,
                    sectionName = sectionMap[row.sectionId] ?: "",
                    sphValue = row.sphValue,
                    cylValue = row.cylValue,
                    pairs = row.pairs,
                    originalFormat = NotebookMode.fromString(row.originalFormat),
                    globalIndex = index + 1
                )
            }

            val totalRows = clipboardRows.size
            val totalPages = if (totalRows > 0) (totalRows + rowsPerPage - 1) / rowsPerPage else 1
            val validPage = page.coerceIn(1, totalPages)
            val startIndex = (validPage - 1) * rowsPerPage
            val endIndex = minOf(startIndex + rowsPerPage, totalRows)

            val pagedRows = if (startIndex < totalRows) {
                clipboardRows.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            ClipboardData(
                rows = pagedRows,
                totalRows = totalRows,
                currentPage = validPage,
                totalPages = totalPages,
                rowsPerPage = rowsPerPage
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun clearClipboard(): Result<Unit> {
        return try {
            notebookDao.clearAllCopyFlags()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markClipboardAsOrdered(): Result<Unit> {
        return try {
            notebookDao.markCopiedRowsAsOrdered()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== BULK OPERATIONS ====================

    suspend fun deleteCopiedRowsInSection(sectionId: Long): Result<Unit> {
        return try {
            val rows = notebookDao.getRowsBySectionSync(sectionId)
            val copiedIds = rows.filter { it.isCopy }.map { it.id }

            if (copiedIds.isNotEmpty()) {
                notebookDao.deleteRowsByIds(copiedIds)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOrderedRowsInSection(sectionId: Long): Result<Unit> {
        return try {
            val rows = notebookDao.getRowsBySectionSync(sectionId)
            val orderedIds = rows.filter { it.isOrdered }.map { it.id }

            if (orderedIds.isNotEmpty()) {
                notebookDao.deleteRowsByIds(orderedIds)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMarkedRowsInSection(sectionId: Long): Result<Unit> {
        return try {
            notebookDao.deleteMarkedRowsInSection(sectionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllCopyInSection(sectionId: Long, isCopy: Boolean): Result<Unit> {
        return try {
            val rows = notebookDao.getRowsBySectionSync(sectionId)
            val updatedRows = rows.map { it.copy(isCopy = isCopy) }
            notebookDao.updateRows(updatedRows)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllOrderedInSection(sectionId: Long, isOrdered: Boolean): Result<Unit> {
        return try {
            if (isOrdered) {
                notebookDao.markAllRowsAsOrdered(sectionId)
            } else {
                notebookDao.clearOrderedFlagsInSection(sectionId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllDeleteInSection(sectionId: Long, isDelete: Boolean): Result<Unit> {
        return try {
            if (isDelete) {
                notebookDao.markAllRowsForDelete(sectionId)
            } else {
                notebookDao.clearDeleteFlagsInSection(sectionId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== RESET OPERATIONS ====================

    suspend fun resetToDefault(): Result<Unit> {
        return try {
            val defaultSectionNames = getDefaultSections()
            val sections = defaultSectionNames.mapIndexed { index, name ->
                NotebookSectionEntity(
                    name = name,
                    mode = "sph-cyl",
                    orderIndex = index
                )
            }

            notebookDao.resetToDefaultSections(sections)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initializeDefaultSectionsIfNeeded(): Result<Unit> {
        return try {
            val count = notebookDao.getSectionCount()
            if (count == 0) {
                resetToDefault()
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== STATISTICS ====================

    suspend fun getStatistics(): NotebookStatistics {
        return try {
            NotebookStatistics(
                totalSections = notebookDao.getSectionCount(),
                totalRows = notebookDao.getTotalRowCount(),
                totalCopiedRows = notebookDao.getCopiedRowCount(),
                totalOrderedRows = notebookDao.getOrderedRowCount(),
                totalMarkedForDelete = notebookDao.getMarkedForDeleteCount()
            )
        } catch (e: Exception) {
            NotebookStatistics()
        }
    }

    // ==================== HELPER METHODS ====================

    private fun sortClipboardRows(rows: List<NotebookRowEntity>): List<NotebookRowEntity> {
        val sphOnly = mutableListOf<NotebookRowEntity>()
        val cylOnly = mutableListOf<NotebookRowEntity>()
        val combined = mutableListOf<NotebookRowEntity>()

        rows.forEach { row ->
            val sphNum = row.sphValue.toDoubleOrNull()
            val cylNum = row.cylValue.toDoubleOrNull()
            val sphIsZero = sphNum == null || sphNum == 0.0
            val cylIsZero = cylNum == null || cylNum == 0.0

            when {
                !sphIsZero && !cylIsZero -> combined.add(row)
                !sphIsZero -> sphOnly.add(row)
                !cylIsZero -> cylOnly.add(row)
                else -> sphOnly.add(row)
            }
        }

        sphOnly.sortWith(compareBy { it.sphValue.toDoubleOrNull() ?: 0.0 })
        cylOnly.sortWith(compareBy { it.cylValue.toDoubleOrNull() ?: 0.0 })
        combined.sortWith(
            compareBy<NotebookRowEntity> { it.sphValue.toDoubleOrNull() ?: 0.0 }
                .thenBy { it.cylValue.toDoubleOrNull() ?: 0.0 }
        )

        return sphOnly + cylOnly + combined
    }
}

private fun SectionWithRows.toNotebookSection(): NotebookSection {
    return section.toSection(rows)
}
