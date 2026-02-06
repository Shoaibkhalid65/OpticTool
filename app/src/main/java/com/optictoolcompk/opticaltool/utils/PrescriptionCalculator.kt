package com.optictoolcompk.opticaltool.utils

import com.optictoolcompk.opticaltool.data.models.CalculationResults
import com.optictoolcompk.opticaltool.data.models.EyeResults
import java.util.Locale
import kotlin.math.abs

// Calculation Helper Object
object PrescriptionCalculator {

    // Format numbers with + sign for positive values
    private fun formatNumber(value: Double): String {
        return if (value >= 0) "+%.2f".format(value) else "%.2f".format(value)
    }

    // Validate Axis input
    fun validateAxis(axis: String, cyl: String): String {
        val axisNum = axis.toDoubleOrNull()
        val cylNum = cyl.toDoubleOrNull()

        if (cylNum != null && cylNum != 0.0 &&
            (axisNum == null || axisNum < 1 || axisNum > 180)) {
            return "Axis must be between 1 and 180"
        }
        return ""
    }

    // Calculate Distance Vision
    private fun calculateDistanceVision(
        sph: Double?,
        cyl: Double?,
        axis: Double?
    ): Triple<Double, Double, Double> {
        // Handle case where only Sph is provided
        if ((cyl == null || cyl == 0.0) && sph != null) {
            return Triple(sph, 0.0, 0.0)
        }

        // Handle case where only Cyl and Axis are provided
        if (sph == null && cyl != null && axis != null) {
            return Triple(0.0, cyl, axis)
        }

        val s = sph ?: 0.0
        val c = cyl ?: 0.0
        val a = axis ?: 0.0

        val distanceSph: Double
        val distanceCyl: Double
        val distanceAxis: Double

        // New Case: When Sph is positive and lower than negative Cyl (no transposition)
        if (s > 0 && c < 0 && abs(s) < abs(c)) {
            distanceSph = s
            distanceCyl = c
            distanceAxis = a
        }
        // Case 1: When Sph is negative and CYL is positive
        else if (s < 0 && c > 0) {
            distanceSph = s + c
            distanceCyl = -c
            distanceAxis = ((a + 90) % 180).let { if (it == 0.0) 180.0 else it }
        }
        // Case 2: When Sph is positive and Cyl is negative
        else if (s > 0 && c < 0) {
            distanceSph = s + c
            distanceCyl = -c
            distanceAxis = ((a + 90) % 180).let { if (it == 0.0) 180.0 else it }
        }
        // Case 3: All other cases (same signs)
        else {
            distanceSph = s
            distanceCyl = c
            distanceAxis = a
        }

        return Triple(distanceSph, distanceCyl, distanceAxis)
    }

    // Calculate Near Vision
    private fun calculateNearVision(
        sph: Double?,
        cyl: Double?,
        axis: Double?,
        add: Double
    ): Triple<Double, Double, Double> {
        // Handle case where only Sph is provided
        if ((cyl == null || cyl == 0.0) && sph != null) {
            return Triple(sph + add, 0.0, 0.0)
        }

        // Handle case where only Cyl and Axis are provided
        if (sph == null && cyl != null && axis != null) {
            return Triple(add, cyl, axis)
        }

        val s = sph ?: 0.0
        val c = cyl ?: 0.0
        val a = axis ?: 0.0

        return Triple(s + add, c, a)
    }

    // Check if SPH and CYL have same signs
    private fun hasSameSigns(sph: Double, cyl: Double): Boolean {
        return (sph >= 0 && cyl >= 0) || (sph <= 0 && cyl <= 0)
    }

    // Transpose prescription to positive cylinder form if needed
    private fun transposeIfOppositeSigns(
        sph: Double,
        cyl: Double,
        axis: Double
    ): Triple<Double, Double, Double> {
        if ((sph > 0 && cyl < 0) || (sph < 0 && cyl > 0)) {
            val newSph = sph + cyl
            val newCyl = -cyl
            val newAxis = ((axis + 90) % 180).let { if (it == 0.0) 180.0 else it }
            return Triple(newSph, newCyl, newAxis)
        }
        return Triple(sph, cyl, axis)
    }

    // Format the final result string
    private fun formatResult(sph: Double, cyl: Double, axis: Double): String {
        return "${formatNumber(sph)}/${formatNumber(cyl)} ${axis.toInt()}°"
    }

    // Calculate results for one eye
    fun calculateEyeResults(
        sph: String,
        cyl: String,
        axis: String,
        add: Double
    ): EyeResults? {
        val sphNum = sph.toDoubleOrNull()
        val cylNum = cyl.toDoubleOrNull()
        val axisNum = axis.toDoubleOrNull()

        // Check if at least Sph or (Cyl and Axis) is provided
        val hasSph = sphNum != null
        val hasCyl = cylNum != null && cylNum != 0.0
        val hasAxis = axisNum != null

        if (!hasSph && !(hasCyl && hasAxis)) {
            return null
        }

        // Default Cyl to 0 if not provided
        val finalCyl = cylNum ?: 0.0

        // Default Axis to 0 if not provided
        val finalAxis = axisNum ?: 0.0

        // Calculate distance vision
        val (distanceSph, distanceCyl, distanceAxis) =
            calculateDistanceVision(sphNum, finalCyl, finalAxis)

        var finalDistance = ""
        var transposedDistance: String? = null

        // Check if we should show transposed distance
        val originalHasOppositeSigns =
            (distanceSph > 0 && distanceCyl < 0) || (distanceSph < 0 && distanceCyl > 0)
        val (transposedSph, transposedCyl, transposedAxis) =
            transposeIfOppositeSigns(distanceSph, distanceCyl, distanceAxis)
        val transposedHasSameSigns = hasSameSigns(transposedSph, transposedCyl)

        if (originalHasOppositeSigns && !transposedHasSameSigns) {
            // Show both versions
            if (abs(distanceCyl) > abs(distanceSph)) {
                finalDistance = formatResult(distanceSph, distanceCyl, distanceAxis)
                transposedDistance = formatResult(transposedSph, transposedCyl, transposedAxis)
            } else {
                finalDistance = formatResult(transposedSph, transposedCyl, transposedAxis)
                transposedDistance = formatResult(distanceSph, distanceCyl, distanceAxis)
            }
        } else if (originalHasOppositeSigns) {
            // Only show the transposed version
            finalDistance = formatResult(transposedSph, transposedCyl, transposedAxis)
        } else {
            // Just show the original result
            finalDistance = formatResult(distanceSph, distanceCyl, distanceAxis)
        }

        var finalNear: String? = null
        var transposedNear: String? = null

        // Calculate near vision if addition is provided
        if (add != 0.0) {
            val (nearSph, nearCyl, nearAxis) =
                calculateNearVision(sphNum, finalCyl, finalAxis, add)

            val nearOriginalHasOppositeSigns =
                (nearSph > 0 && nearCyl < 0) || (nearSph < 0 && nearCyl > 0)
            val (nearTransposedSph, nearTransposedCyl, nearTransposedAxis) =
                transposeIfOppositeSigns(nearSph, nearCyl, nearAxis)
            val nearTransposedHasSameSigns = hasSameSigns(nearTransposedSph, nearTransposedCyl)

            if (nearOriginalHasOppositeSigns && !nearTransposedHasSameSigns) {
                // Show both versions
                if (abs(nearCyl) > abs(nearSph)) {
                    finalNear = formatResult(nearSph, nearCyl, nearAxis)
                    transposedNear = formatResult(nearTransposedSph, nearTransposedCyl, nearTransposedAxis)
                } else {
                    finalNear = formatResult(nearTransposedSph, nearTransposedCyl, nearTransposedAxis)
                    transposedNear = formatResult(nearSph, nearCyl, nearAxis)
                }
            } else if (nearOriginalHasOppositeSigns) {
                // Only show the transposed version
                finalNear = formatResult(nearTransposedSph, nearTransposedCyl, nearTransposedAxis)
            } else {
                // Just show the original result
                finalNear = formatResult(nearSph, nearCyl, nearAxis)
            }
        }

        return EyeResults(finalDistance, transposedDistance, finalNear, transposedNear)
    }

    // Main calculate function
    fun calculate(
        rightSph: String,
        rightCyl: String,
        rightAxis: String,
        leftSph: String,
        leftCyl: String,
        leftAxis: String,
        add: String
    ): CalculationResults? {
        val addValue = add.toDoubleOrNull() ?: 0.0

        // Validate at least one eye has either Sph or (Cyl and Axis)
        val rightEyeFilled = rightSph.isNotEmpty() ||
                (rightCyl.isNotEmpty() && rightAxis.isNotEmpty())
        val leftEyeFilled = leftSph.isNotEmpty() ||
                (leftCyl.isNotEmpty() && leftAxis.isNotEmpty())

        if (!rightEyeFilled && !leftEyeFilled) {
            return null
        }

        val rightResults = if (rightEyeFilled) {
            calculateEyeResults(rightSph, rightCyl, rightAxis, addValue)
        } else null

        val leftResults = if (leftEyeFilled) {
            calculateEyeResults(leftSph, leftCyl, leftAxis, addValue)
        } else null

        return CalculationResults(rightResults, leftResults)
    }
}

// Updated value generation functions with consistent formatting
fun generateSphValues(): List<String> {
    val values = mutableListOf<String>()
    var i = -24.0
    while (i <= 24.0) {
        values.add(String.format(Locale.getDefault(),"%.2f", i))
        i += 0.25
    }
    return values
}

fun generateCylValues(): List<String> {
    val values = mutableListOf<String>()
    var i = -6.0
    while (i <= 6.0) {
        values.add(String.format(Locale.getDefault(),"%.2f", i))
        i += 0.25
    }
    return values
}

fun generateAddValues(): List<String> {
    val values = mutableListOf<String>()
    var i = 0.25
    while (i <= 4.0) {
        values.add(String.format(Locale.getDefault(),"%.2f", i))
        i += 0.25
    }
    return values
}