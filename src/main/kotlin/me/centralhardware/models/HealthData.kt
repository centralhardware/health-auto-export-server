package me.centralhardware.models

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Main data structure for health data export
 */
@Serializable
data class HealthDataExport(
    val data: HealthData
)

/**
 * Container for metrics and workouts
 */
@Serializable
data class HealthData(
    val metrics: List<HealthMetric>,
    val workouts: List<Workout>
)

/**
 * Workout data
 */
@Serializable
data class Workout(
    val name: String,
    val start: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val end: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val heartRateData: List<HeartRatePoint>? = null,
    val heartRateRecovery: List<HeartRatePoint>? = null,
    val route: List<RoutePoint>? = null,
    val totalEnergy: EnergyValue? = null,
    val activeEnergy: EnergyValue? = null,
    val maxHeartRate: HeartRateValue? = null,
    val avgHeartRate: HeartRateValue? = null,
    val stepCount: StepValue? = null,
    val stepCadence: CadenceValue? = null,
    val totalSwimmingStrokeCount: CountValue? = null,
    val swimCadence: CadenceValue? = null,
    val distance: DistanceValue? = null,
    val speed: SpeedValue? = null,
    val flightsClimbed: CountValue? = null,
    val intensity: IntensityValue? = null,
    val temperature: TemperatureValue? = null,
    val humidity: HumidityValue? = null,
    val elevation: ElevationValue? = null
)

/**
 * Heart rate data point for workouts
 */
@Serializable
data class HeartRatePoint(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val qty: Double,
    val units: String = "count"
)

/**
 * Route point for workouts
 */
@Serializable
data class RoutePoint(
    val lat: Double,
    val lon: Double,
    val altitude: Double, // in meters
    val timestamp: String // Format: yyyy-MM-dd HH:mm:ss Z
)

/**
 * Energy value
 */
@Serializable
data class EnergyValue(
    val qty: Double,
    val units: String // e.g., "kcal", "kJ"
)

/**
 * Heart rate value
 */
@Serializable
data class HeartRateValue(
    val qty: Double,
    val units: String = "bpm"
)

/**
 * Step value
 */
@Serializable
data class StepValue(
    val qty: Double,
    val units: String = "steps"
)

/**
 * Cadence value
 */
@Serializable
data class CadenceValue(
    val qty: Double,
    val units: String = "spm" // steps per minute
)

/**
 * Count value
 */
@Serializable
data class CountValue(
    val qty: Double,
    val units: String = "count"
)

/**
 * Distance value
 */
@Serializable
data class DistanceValue(
    val qty: Double,
    val units: String // e.g., "km", "mi"
)

/**
 * Speed value
 */
@Serializable
data class SpeedValue(
    val qty: Double,
    val units: String // e.g., "km/h", "mph"
)

/**
 * Intensity value
 */
@Serializable
data class IntensityValue(
    val qty: Double,
    val units: String = "MET"
)

/**
 * Temperature value
 */
@Serializable
data class TemperatureValue(
    val qty: Double,
    val units: String // e.g., "°C", "°F"
)

/**
 * Humidity value
 */
@Serializable
data class HumidityValue(
    val qty: Double,
    val units: String = "%"
)

/**
 * Elevation value
 */
@Serializable
data class ElevationValue(
    val ascent: Double,
    val descent: Double,
    val units: String // e.g., "m", "ft"
)

/**
 * Base class for all health metrics
 */
@Serializable
sealed class HealthMetric {
    abstract val name: String
    abstract val units: String?
}

/**
 * Common format for most health metrics
 */
@Serializable
data class CommonHealthMetric(
    override val name: String,
    override val units: String,
    val data: List<CommonHealthData>
) : HealthMetric()

/**
 * Common health data point
 */
@Serializable
data class CommonHealthData(
    val qty: Double,
    val date: String // Format: yyyy-MM-dd HH:mm:ss Z
)

/**
 * Blood Pressure metric
 */
@Serializable
data class BloodPressureMetric(
    override val name: String = "Blood Pressure",
    override val units: String? = null,
    val data: List<BloodPressureData>
) : HealthMetric()

/**
 * Blood Pressure data point
 */
@Serializable
data class BloodPressureData(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val systolic: Double,
    val diastolic: Double
)

/**
 * Heart Rate metric
 */
@Serializable
data class HeartRateMetric(
    override val name: String = "Heart Rate",
    override val units: String? = null,
    val data: List<HeartRateData>
) : HealthMetric()

/**
 * Heart Rate data point
 */
@Serializable
data class HeartRateData(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val Min: Double,
    val Avg: Double,
    val Max: Double
)

/**
 * Sleep Analysis metric
 */
@Serializable
data class SleepAnalysisMetric(
    override val name: String = "Sleep Analysis",
    override val units: String? = null,
    val data: List<SleepAnalysisData>
) : HealthMetric()

/**
 * Sleep Analysis data point
 */
@Serializable
data class SleepAnalysisData(
    val date: String, // Format: yyyy-MM-dd
    val asleep: Double,
    val sleepStart: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val sleepEnd: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val sleepSource: String,
    val inBed: Double,
    val inBedStart: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val inBedEnd: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val inBedSource: String
)

/**
 * Blood Glucose metric
 */
@Serializable
data class BloodGlucoseMetric(
    override val name: String = "Blood Glucose",
    override val units: String? = null,
    val data: List<BloodGlucoseData>
) : HealthMetric()

/**
 * Blood Glucose data point
 */
@Serializable
data class BloodGlucoseData(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val qty: Double,
    val mealTime: String // "Before Meal" | "After Meal" | "Unspecified"
)

/**
 * Sexual Activity metric
 */
@Serializable
data class SexualActivityMetric(
    override val name: String = "Sexual Activity",
    override val units: String? = null,
    val data: List<SexualActivityData>
) : HealthMetric()

/**
 * Sexual Activity data point
 */
@Serializable
data class SexualActivityData(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val Unspecified: Double,
    val `Protection Used`: Double,
    val `Protection Not Used`: Double
)

/**
 * Handwashing metric
 */
@Serializable
data class HandwashingMetric(
    override val name: String = "Handwashing",
    override val units: String? = null,
    val data: List<HandwashingData>
) : HealthMetric()

/**
 * Handwashing data point
 */
@Serializable
data class HandwashingData(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val qty: Double,
    val value: String // "Complete" | "Incomplete"
)

/**
 * Toothbrushing metric
 */
@Serializable
data class ToothbrushingMetric(
    override val name: String = "Toothbrushing",
    override val units: String? = null,
    val data: List<ToothbrushingData>
) : HealthMetric()

/**
 * Toothbrushing data point
 */
@Serializable
data class ToothbrushingData(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val qty: Double,
    val value: String // "Complete" | "Incomplete"
)

/**
 * Insulin Delivery metric
 */
@Serializable
data class InsulinDeliveryMetric(
    override val name: String = "Insulin Delivery",
    override val units: String? = null,
    val data: List<InsulinDeliveryData>
) : HealthMetric()

/**
 * Insulin Delivery data point
 */
@Serializable
data class InsulinDeliveryData(
    val date: String, // Format: yyyy-MM-dd HH:mm:ss Z
    val qty: Double,
    val reason: String // "Bolus" | "Basal"
)
