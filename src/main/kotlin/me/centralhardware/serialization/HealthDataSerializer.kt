package me.centralhardware.serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import me.centralhardware.models.*

/**
 * Custom serializer for HealthData class to handle polymorphic serialization of metrics
 */
@OptIn(ExperimentalSerializationApi::class)
class HealthDataSerializer : KSerializer<HealthData> {
    // Delegate serializers
    private val workoutListSerializer = serializer<List<Workout>>()

    // Descriptor for the HealthData class
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("HealthData") {
        element("metrics", JsonArray.serializer().descriptor)
        element("workouts", workoutListSerializer.descriptor)
    }

    // Serialize HealthData to JSON
    override fun serialize(encoder: Encoder, value: HealthData) {
        // We're only concerned with deserialization, so we can use the default serialization
        val jsonEncoder = encoder as? JsonEncoder ?: throw SerializationException("This serializer can only be used with JSON")
        val json = jsonEncoder.json

        val metricsJson = json.encodeToJsonElement(serializer<List<HealthMetric>>(), value.metrics)
        val workoutsJson = json.encodeToJsonElement(workoutListSerializer, value.workouts)

        val jsonObject = buildJsonObject {
            put("metrics", metricsJson)
            put("workouts", workoutsJson)
        }

        encoder.encodeSerializableValue(JsonObject.serializer(), jsonObject)
    }

    // Deserialize JSON to HealthData
    override fun deserialize(decoder: Decoder): HealthData {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("This serializer can only be used with JSON")
        val json = jsonDecoder.json
        val jsonElement = jsonDecoder.decodeJsonElement()

        if (jsonElement !is JsonObject) throw SerializationException("Expected JsonObject")

        // Extract workouts
        val workouts = json.decodeFromJsonElement(workoutListSerializer, jsonElement["workouts"] ?: JsonArray(emptyList()))

        // Extract metrics - this is where we handle the polymorphic serialization
        val metricsJson = jsonElement["metrics"] ?: JsonArray(emptyList())
        val metrics = if (metricsJson is JsonArray) {
            metricsJson.mapNotNull { metricElement ->
                if (metricElement !is JsonObject) return@mapNotNull null

                // Determine the type of metric based on its properties
                when {
                    metricElement["systolic"] != null && metricElement["diastolic"] != null -> 
                        json.decodeFromJsonElement(BloodPressureMetric.serializer(), metricElement)
                    metricElement["Min"] != null && metricElement["Avg"] != null && metricElement["Max"] != null -> 
                        json.decodeFromJsonElement(HeartRateMetric.serializer(), metricElement)
                    metricElement["sleepStart"] != null && metricElement["sleepEnd"] != null -> 
                        json.decodeFromJsonElement(SleepAnalysisMetric.serializer(), metricElement)
                    metricElement["mealTime"] != null -> 
                        json.decodeFromJsonElement(BloodGlucoseMetric.serializer(), metricElement)
                    metricElement["Protection Used"] != null || metricElement["Protection Not Used"] != null -> 
                        json.decodeFromJsonElement(SexualActivityMetric.serializer(), metricElement)
                    metricElement["value"] != null && metricElement["name"]?.jsonPrimitive?.content == "Handwashing" -> 
                        json.decodeFromJsonElement(HandwashingMetric.serializer(), metricElement)
                    metricElement["value"] != null && metricElement["name"]?.jsonPrimitive?.content == "Toothbrushing" -> 
                        json.decodeFromJsonElement(ToothbrushingMetric.serializer(), metricElement)
                    metricElement["reason"] != null -> 
                        json.decodeFromJsonElement(InsulinDeliveryMetric.serializer(), metricElement)
                    else -> 
                        // Default to CommonHealthMetric for any other metric
                        try {
                            json.decodeFromJsonElement(CommonHealthMetric.serializer(), metricElement)
                        } catch (e: Exception) {
                            // If we can't deserialize as CommonHealthMetric, return null
                            null
                        }
                }
            }
        } else {
            emptyList()
        }

        return HealthData(metrics, workouts)
    }
}
