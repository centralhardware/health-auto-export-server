package me.centralhardware.models

import kotlinx.serialization.Serializable

/**
 * Heart Rate Notifications metric
 */
@Serializable
data class HeartRateNotificationsMetric(
    override val name: String = "Heart Rate Notifications",
    override val units: String? = null,
    val data: List<HeartRateNotification>
) : HealthMetric()

/**
 * Heart Rate Notification data
 */
@Serializable
data class HeartRateNotification(
    val start: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val end: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val threshold: Double? = null, // For high and low heart rate notifications
    val heartRate: List<HeartRateNotificationData>? = null,
    val heartRateVariation: List<HeartRateVariationData>? = null
)

/**
 * Heart Rate data for notifications
 */
@Serializable
data class HeartRateNotificationData(
    val hr: Double,
    val units: String = "bpm",
    val timestamp: TimestampInterval
)

/**
 * Heart Rate Variation data
 */
@Serializable
data class HeartRateVariationData(
    val hrv: Double,
    val units: String = "ms",
    val timestamp: TimestampInterval
)

/**
 * Timestamp interval
 */
@Serializable
data class TimestampInterval(
    val start: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val end: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val interval: Interval
)

/**
 * Interval
 */
@Serializable
data class Interval(
    val duration: Double,
    val units: String = "s" // seconds
)

/**
 * Symptoms metric
 */
@Serializable
data class SymptomsMetric(
    override val name: String = "Symptoms",
    override val units: String? = null,
    val data: List<SymptomData>
) : HealthMetric()

/**
 * Symptom data
 */
@Serializable
data class SymptomData(
    val start: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val end: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val name: String,
    val severity: String,
    val userEntered: Boolean,
    val source: String
)

/**
 * State of Mind metric
 */
@Serializable
data class StateOfMindMetric(
    override val name: String = "State of Mind",
    override val units: String? = null,
    val data: List<StateOfMindData>
) : HealthMetric()

/**
 * State of Mind data
 */
@Serializable
data class StateOfMindData(
    val id: String,
    val start: String,
    val end: String,
    val kind: String,
    val labels: List<String>,
    val associations: List<String>,
    val valence: Double,
    val valenceClassification: Int,
    val metadata: Map<String, String>
)

/**
 * ECG metric
 */
@Serializable
data class ECGMetric(
    override val name: String = "ECG",
    override val units: String? = null,
    val data: List<ECGData>
) : HealthMetric()

/**
 * ECG data
 */
@Serializable
data class ECGData(
    val start: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val end: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val classification: String, // "Sinus Rhythm" | "Atrial Fibrillation" | etc.
    val severity: String,
    val averageHeartRate: Double,
    val numberOfVoltageMeasurements: Int,
    val voltageMeasurements: List<VoltageMeasurement>,
    val samplingFrequency: Double, // Hz
    val source: String
)

/**
 * Voltage Measurement
 */
@Serializable
data class VoltageMeasurement(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val voltage: Double,
    val units: String
)