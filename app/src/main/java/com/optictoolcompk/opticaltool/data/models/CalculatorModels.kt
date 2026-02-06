package com.optictoolcompk.opticaltool.data.models

// Data classes
data class EyeResults(
    val finalDistance: String,
    val transposedDistance: String? = null,
    val finalNear: String? = null,
    val transposedNear: String? = null
)

data class CalculationResults(
    val right: EyeResults? = null,
    val left: EyeResults? = null
)