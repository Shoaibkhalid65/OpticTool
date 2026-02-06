package com.optictoolcompk.opticaltool.data.models

import androidx.room.*

// ==================== ROOM ENTITIES ====================

/**
 * Main entity for storing notebook sections (headers)
 * Each section contains multiple rows of glasses data
 */
@Entity(
    tableName = "notebook_sections",
    indices = [
        Index(value = ["name"]),
        Index(value = ["orderIndex"])
    ]
)
data class NotebookSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String, // e.g., "WT55", "CR Hardcoate", etc.
    val mode: String = "sph-cyl", // "sph-cyl" or "kt"
    val orderIndex: Int, // For maintaining custom order

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Cloud sync flags (for future use)
    val syncedWithCloud: Boolean = false,
    val cloudId: String? = null,
    val needsSync: Boolean = true
)

/**
 * Entity for individual rows within a section
 * Each row represents a glasses specification
 */
@Entity(
    tableName = "notebook_rows",
    foreignKeys = [
        ForeignKey(
            entity = NotebookSectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sectionId"),
        Index("orderIndex")
    ]
)
data class NotebookRowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sectionId: Long, // Foreign key to NotebookSectionEntity

    // Values
    val sphValue: String, // SPH/Distance value (can be positive or negative)
    val cylValue: String, // CYL/ADD value (can be positive or negative)
    val pairs: Int = 0, // Number of pairs

    // Flags
    val isCopy: Boolean = false, // Marked for clipboard
    val isOrdered: Boolean = false, // Marked as ordered
    val isDelete: Boolean = false, // Marked for deletion

    // Original format when row was created
    val originalFormat: String = "sph-cyl", // "sph-cyl" or "kt"

    val orderIndex: Int, // For maintaining custom order within section

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Cloud sync flags (for future use)
    val syncedWithCloud: Boolean = false,
    val cloudId: String? = null,
    val needsSync: Boolean = true
)

// ==================== UI MODELS ====================

/**
 * UI model for a notebook section with its rows
 */
data class NotebookSection(
    val id: Long = 0,
    val name: String = "",
    val mode: NotebookMode = NotebookMode.SPH_CYL,
    val rows: List<NotebookRow> = emptyList(),
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val rowCount: Int
        get() = rows.size

    val copiedRowsCount: Int
        get() = rows.count { it.isCopy }

    val orderedRowsCount: Int
        get() = rows.count { it.isOrdered }
}

/**
 * UI model for a single row in the notebook
 */
data class NotebookRow(
    val id: Long = 0,
    val sectionId: Long = 0,
    val sphValue: String = "",
    val cylValue: String = "",
    val pairs: Int = 0,
    val isCopy: Boolean = false,
    val isOrdered: Boolean = false,
    val isDelete: Boolean = false,
    val originalFormat: NotebookMode = NotebookMode.SPH_CYL,
    val orderIndex: Int = 0
) {
    /**
     * Get formatted display text based on original format
     */
    fun getFormattedNumber(): String {
        val sphNum = sphValue.toDoubleOrNull()
        val cylNum = cylValue.toDoubleOrNull()
        val sphIsZero = sphNum == null || sphNum == 0.0
        val cylIsZero = cylNum == null || cylNum == 0.0

        return when (originalFormat) {
            NotebookMode.KT -> {
                // KT Mode: Distance / ADD format
                when {
                    !cylIsZero && sphIsZero -> "0.00 Add $cylValue"
                    !cylIsZero && !sphIsZero -> "$sphValue Add $cylValue"
                    !sphIsZero -> "Distance $sphValue"
                    else -> ""
                }
            }
            NotebookMode.SPH_CYL -> {
                // SPH/CYL Mode
                when {
                    !sphIsZero && !cylIsZero -> "$sphValue / $cylValue"
                    !sphIsZero -> "SPH $sphValue"
                    !cylIsZero -> "CYL $cylValue"
                    else -> ""
                }
            }
        }
    }

    private fun formatNumber(value: String): String {
        val num = value.toDoubleOrNull() ?: return ""
        return value
//        return if (num > 0) "+$value" else value
    }
}

/**
 * Enum for notebook mode (SPH/CYL or KT)
 */
enum class NotebookMode(val value: String, val displayName: String) {
    SPH_CYL("sph-cyl", "SPH / CYL"),
    KT("kt", "KT");

    companion object {
        fun fromString(value: String): NotebookMode {
            return entries.find { it.value == value } ?: SPH_CYL
        }
    }
}

/**
 * Model for clipboard (copied rows)
 */
data class ClipboardData(
    val rows: List<ClipboardRow> = emptyList(),
    val totalRows: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val rowsPerPage: Int = 20
)

/**
 * Individual row in clipboard with section name
 */
data class ClipboardRow(
    val id: Long,
    val sectionName: String,
    val sphValue: String,
    val cylValue: String,
    val pairs: Int,
    val originalFormat: NotebookMode,
    val globalIndex: Int // For displaying Sr. number in clipboard
) {
    fun getFormattedNumber(): String {
        val sphNum = sphValue.toDoubleOrNull()
        val cylNum = cylValue.toDoubleOrNull()
        val sphIsZero = sphNum == null || sphNum == 0.0
        val cylIsZero = cylNum == null || cylNum == 0.0

        return when (originalFormat) {
            NotebookMode.KT -> {
                when {
                    !cylIsZero && sphIsZero -> "0.00 Add $cylValue"
                    !cylIsZero && !sphIsZero -> "$sphValue Add $cylValue"
                    !sphIsZero -> "Distance $sphValue"
                    else -> ""
                }
            }
            NotebookMode.SPH_CYL -> {
                when {
                    !sphIsZero && !cylIsZero -> "$sphValue / $cylValue"
                    !sphIsZero -> "SPH $sphValue"
                    !cylIsZero -> "CYL $cylValue"
                    else -> ""
                }
            }
        }
    }

    private fun formatNumber(value: String): String {
        val num = value.toDoubleOrNull() ?: return ""
        return value
//        return if (num > 0) "+$value" else value
    }
}

/**
 * Statistics for notebook
 */
data class NotebookStatistics(
    val totalSections: Int = 0,
    val totalRows: Int = 0,
    val totalCopiedRows: Int = 0,
    val totalOrderedRows: Int = 0,
    val totalMarkedForDelete: Int = 0
)

/**
 * Options for section dropdown
 */
data class SectionOption(
    val id: Long,
    val name: String,
    val isViewAll: Boolean = false
)

// ==================== TYPE CONVERTERS ====================

class NotebookTypeConverters {
    @TypeConverter
    fun fromNotebookMode(mode: NotebookMode): String {
        return mode.value
    }

    @TypeConverter
    fun toNotebookMode(value: String): NotebookMode {
        return NotebookMode.fromString(value)
    }
}

// ==================== EXTENSIONS ====================

/**
 * Convert entity to UI model
 */
fun NotebookSectionEntity.toSection(rows: List<NotebookRowEntity>): NotebookSection {
    return NotebookSection(
        id = id,
        name = name,
        mode = NotebookMode.fromString(mode),
        rows = rows.map { it.toRow() },
        orderIndex = orderIndex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Convert UI model to entity
 */
fun NotebookSection.toEntity(): NotebookSectionEntity {
    return NotebookSectionEntity(
        id = if (id == 0L) 0 else id,
        name = name,
        mode = mode.value,
        orderIndex = orderIndex,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
        syncedWithCloud = false,
        cloudId = null,
        needsSync = true
    )
}

/**
 * Convert row entity to UI model
 */
fun NotebookRowEntity.toRow(): NotebookRow {
    return NotebookRow(
        id = id,
        sectionId = sectionId,
        sphValue = sphValue,
        cylValue = cylValue,
        pairs = pairs,
        isCopy = isCopy,
        isOrdered = isOrdered,
        isDelete = isDelete,
        originalFormat = NotebookMode.fromString(originalFormat),
        orderIndex = orderIndex
    )
}

/**
 * Convert UI row to entity
 */
fun NotebookRow.toEntity(sectionId: Long): NotebookRowEntity {
    return NotebookRowEntity(
        id = if (id == 0L) 0 else id,
        sectionId = sectionId,
        sphValue = sphValue,
        cylValue = cylValue,
        pairs = pairs,
        isCopy = isCopy,
        isOrdered = isOrdered,
        isDelete = isDelete,
        originalFormat = originalFormat.value,
        orderIndex = orderIndex,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        syncedWithCloud = false,
        cloudId = null,
        needsSync = true
    )
}

// ==================== DEFAULT DATA ====================

/**
 * Get default sections for new notebook
 */
fun getDefaultSections(): List<String> {
    return listOf(
        "WT55",
        "CR Hardcoate",
        "CR Multicoated Green",
        "Blue Cut",
        "WT KT Round",
        "PG KT Round"
    )
}