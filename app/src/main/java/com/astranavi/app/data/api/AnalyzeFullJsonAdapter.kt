package com.astranavi.app.data.api

import com.astranavi.app.data.model.AnalyzeFullResponse
import com.astranavi.app.data.model.AnalyzeFullWrapper
import com.astranavi.app.data.model.LockedContent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val lockedVulnerableFields = setOf(
    "dasha",
    "ashtakavarga",
    "planet_strength_ranking",
    "transits",
    "key_themes"
)

object AnalyzeFullResponseSerializer : KSerializer<AnalyzeFullResponse?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnalyzeFullResponseNullable")

    override fun serialize(encoder: Encoder, value: AnalyzeFullResponse?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        val baseSerializer = AnalyzeFullResponse.serializer()
        encoder.encodeSerializableValue(baseSerializer, value)
    }

    override fun deserialize(decoder: Decoder): AnalyzeFullResponse? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeSerializableValue(AnalyzeFullResponse.serializer().nullable)

        val element = jsonDecoder.decodeJsonElement()
        val unwrapped = element.unwrapStringEncodedJson()
        val jsonObject = unwrapped as? JsonObject ?: return null
        val normalized = normalizeAstrologyObject(jsonObject)
        return JsonConfig.json.decodeFromJsonElement(AnalyzeFullResponse.serializer(), normalized)
    }

    private fun normalizeAstrologyObject(jsonObject: JsonObject): JsonObject {
        val updated = jsonObject.toMutableMap()
        val lockedSections = mutableMapOf<String, LockedContent>()

        for (field in lockedVulnerableFields) {
            val fieldObject = updated[field] as? JsonObject ?: continue
            val locked = fieldObject["locked"]?.jsonPrimitive?.booleanOrNull ?: false
            if (locked) {
                val message = fieldObject["message"]?.jsonPrimitive?.contentOrNull
                lockedSections[field] = LockedContent(locked = true, message = message)
                updated.remove(field)
            }
        }

        if (lockedSections.isNotEmpty()) {
            val lockedJson = JsonConfig.json.encodeToJsonElement(
                serializer = MapSerializer(
                    String.serializer(),
                    LockedContent.serializer()
                ),
                value = lockedSections
            )
            updated["lockedSections"] = lockedJson
        }

        return JsonObject(updated)
    }

    private fun JsonElement.unwrapStringEncodedJson(): JsonElement {
        val primitive = this as? JsonPrimitive ?: return this
        if (!primitive.isString) return this
        val content = primitive.contentOrNull?.trim().orEmpty()
        if (content.isBlank()) return this
        return runCatching { JsonConfig.json.parseToJsonElement(content) }.getOrDefault(this)
    }
}

object AnalyzeFullJsonAdapter {

    fun decode(raw: String): AnalyzeFullWrapper {
        val root = JsonConfig.json.parseToJsonElement(raw).unwrapStringEncodedJson()
        val normalized = normalize(root)
        return JsonConfig.json.decodeFromJsonElement(AnalyzeFullWrapper.serializer(), normalized)
    }

    private fun normalize(element: JsonElement): JsonElement {
        val root = element as? JsonObject ?: return element

        val astrologyElement = root["astrologyData"] ?: return normalizeAstrologyObject(root)
        val normalizedAstrology = normalizeAstrologyObject(astrologyElement)
        if (normalizedAstrology === astrologyElement) return root

        val updated = root.toMutableMap()
        updated["astrologyData"] = normalizedAstrology
        return JsonObject(updated)
    }

    private fun normalizeAstrologyObject(element: JsonElement): JsonElement {
        val jsonObject = element as? JsonObject ?: return element
        val updated = jsonObject.toMutableMap()
        val lockedSections = mutableMapOf<String, LockedContent>()

        for (field in lockedVulnerableFields) {
            val fieldObject = updated[field] as? JsonObject ?: continue
            val locked = fieldObject["locked"]?.jsonPrimitive?.booleanOrNull ?: false
            if (locked) {
                val message = fieldObject["message"]?.jsonPrimitive?.contentOrNull
                lockedSections[field] = LockedContent(locked = true, message = message)
                updated.remove(field)
            }
        }

        if (lockedSections.isNotEmpty()) {
            val lockedJson = JsonConfig.json.encodeToJsonElement(
                serializer = MapSerializer(
                    String.serializer(),
                    LockedContent.serializer()
                ),
                value = lockedSections
            )
            updated["lockedSections"] = lockedJson
        }

        return JsonObject(updated)
    }

    private fun JsonElement.unwrapStringEncodedJson(): JsonElement {
        val primitive = this as? JsonPrimitive ?: return this
        if (!primitive.isString) return this
        val content = primitive.contentOrNull?.trim().orEmpty()
        if (content.isBlank()) return this
        return runCatching { JsonConfig.json.parseToJsonElement(content) }.getOrDefault(this)
    }
}
