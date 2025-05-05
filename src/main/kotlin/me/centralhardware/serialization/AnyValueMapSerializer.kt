package me.centralhardware.serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import java.util.LinkedHashMap

/**
 * Custom serializer for LinkedHashMap<String, Any?> to handle serialization of Any values
 */
@OptIn(ExperimentalSerializationApi::class)
class AnyValueMapSerializer : KSerializer<LinkedHashMap<String, Any?>> {
    // Descriptor for the Map<String, Any?>
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Map<String, Any?>") {
        element("map", JsonObject.serializer().descriptor)
    }

    // Serialize LinkedHashMap<String, Any?> to JSON
    override fun serialize(encoder: Encoder, value: LinkedHashMap<String, Any?>) {
        val jsonEncoder = encoder as? JsonEncoder ?: throw SerializationException("This serializer can only be used with JSON")
        val json = jsonEncoder.json

        val jsonObject = buildJsonObject {
            value.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, JsonPrimitive(value))
                    is Number -> put(key, JsonPrimitive(value))
                    is Boolean -> put(key, JsonPrimitive(value))
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val mapValue = value as Map<String, Any?>
                        // Convert to LinkedHashMap
                        val linkedMapValue = LinkedHashMap<String, Any?>()
                        linkedMapValue.putAll(mapValue)
                        put(key, json.encodeToJsonElement(this@AnyValueMapSerializer, linkedMapValue))
                    }
                    is List<*> -> {
                        val jsonArray = buildJsonArray {
                            value.forEach { item ->
                                when (item) {
                                    is String -> add(JsonPrimitive(item))
                                    is Number -> add(JsonPrimitive(item))
                                    is Boolean -> add(JsonPrimitive(item))
                                    else -> add(JsonPrimitive(item.toString()))
                                }
                            }
                        }
                        put(key, jsonArray)
                    }
                    null -> put(key, JsonNull)
                    else -> put(key, JsonPrimitive(value.toString()))
                }
            }
        }

        encoder.encodeSerializableValue(JsonObject.serializer(), jsonObject)
    }

    // Deserialize JSON to LinkedHashMap<String, Any?>
    override fun deserialize(decoder: Decoder): LinkedHashMap<String, Any?> {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("This serializer can only be used with JSON")
        val json = jsonDecoder.json
        val jsonElement = jsonDecoder.decodeJsonElement()

        if (jsonElement !is JsonObject) throw SerializationException("Expected JsonObject")

        val result = LinkedHashMap<String, Any?>()

        jsonElement.forEach { (key, value) ->
            result[key] = when (value) {
                is JsonPrimitive -> {
                    when {
                        value.isString -> value.content
                        value.content == "true" || value.content == "false" -> value.content.toBoolean()
                        value.content.toDoubleOrNull() != null -> {
                            val doubleValue = value.content.toDouble()
                            if (doubleValue.toInt().toDouble() == doubleValue) doubleValue.toInt() else doubleValue
                        }
                        else -> value.content
                    }
                }
                is JsonObject -> json.decodeFromJsonElement(this, value)
                is JsonArray -> {
                    value.map { element ->
                        when (element) {
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
                            else -> element.toString()
                        }
                    }
                }
                JsonNull -> null
                else -> value.toString()
            }
        }

        return result
    }
}
