package com.optictoolcompk.opticaltool.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val prescriptionNumber: String,
    val createdAt: Long,

    // Patient Info
    val patientName: String,
    val phone: String,
    val age: String,
    val city: String,

    // Right Eye
    val rightSph: String,
    val rightCyl: String,
    val rightAxis: String,
    val rightVa: String,

    // Left Eye
    val leftSph: String,
    val leftCyl: String,
    val leftAxis: String,
    val leftVa: String,

    // Additional
    val addPower: String,
    val ipdNear: String,
    val ipdDistance: String,
    val checkedBy: String,

    // Image path (stored in internal storage)
    val prescriptionImagePath: String = ""
)


data class PrescriptionFormData(
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
    val checkedBy: String = ""
)

enum class PrescriptionSortOption(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    NUMBER_ASC("Prescription # (Asc)"),
    NUMBER_DESC("Prescription # (Desc)")
}

data class PrescriptionFilter(
    val searchQuery: String = "",
    val sortBy: PrescriptionSortOption = PrescriptionSortOption.NEWEST_FIRST
)
