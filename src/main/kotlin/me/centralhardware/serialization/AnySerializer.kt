package me.centralhardware.serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

/**
 * Custom serializer for Any objects
 */
@OptIn(ExperimentalSerializationApi::class)
class AnySerializer : KSerializer<Any> {
    // Descriptor for Any
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("kotlin.Any")

    // Serialize Any to JSON
    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as? JsonEncoder ?: throw SerializationException("This serializer can only be used with JSON")
        
        when (value) {
            is String -> jsonEncoder.encodeString(value)
            is Number -> when (value) {
                is Int -> jsonEncoder.encodeInt(value)
                is Long -> jsonEncoder.encodeLong(value)
                is Float -> jsonEncoder.encodeFloat(value)
                is Double -> jsonEncoder.encodeDouble(value)
                else -> jsonEncoder.encodeString(value.toString())
            }
            is Boolean -> jsonEncoder.encodeBoolean(value)
            is Map<*, *> -> {
                // Convert to string representation
                jsonEncoder.encodeString(value.toString())
            }
            is List<*> -> {
                // Convert to string representation
                jsonEncoder.encodeString(value.toString())
            }
            else -> jsonEncoder.encodeString(value.toString())
        }
    }

    // Deserialize JSON to Any
    override fun deserialize(decoder: Decoder): Any {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("This serializer can only be used with JSON")
        val element = jsonDecoder.decodeJsonElement()
        
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.content == "true" || element.content == "false" -> element.content.toBoolean()
                    element.content.toDoubleOrNull() != null -> {
                        val doubleValue = element.content.toDouble()
                        if (doubleValue.toInt().toDouble() == doubleValue) doubleValue.toInt() else doubleValue
                    }
                    else -> element.content
                }
            }
            is JsonObject -> element.toString()
            is JsonArray -> element.toString()
            else -> element.toString()
        }
    }
}