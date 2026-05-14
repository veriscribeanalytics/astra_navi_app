package com.astranavi.app.data.api

import com.astranavi.app.data.model.LockedContent
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type

class AnalyzeFullDeserializer(
    private val targetClass: Class<*>,
    private val fallbackGson: Gson = Gson()
) : JsonDeserializer<Any> {

    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: com.google.gson.JsonDeserializationContext
    ): Any {
        val actualJson = if (json.isJsonPrimitive && json.asString.isNotBlank()) {
            try {
                JsonParser.parseString(json.asString)
            } catch (_: Exception) {
                json
            }
        } else {
            json
        }

        if (!actualJson.isJsonObject) {
            return fallbackGson.fromJson(actualJson, targetClass)
        }

        val jsonObj = actualJson.asJsonObject
        val lockedSections = mutableMapOf<String, LockedContent>()

        val lockedVulnerableFields = listOf(
            "dasha", "ashtakavarga", "planet_strength_ranking", "transits", "key_themes"
        )

        for (field in lockedVulnerableFields) {
            val fieldElement = jsonObj.get(field)
            if (fieldElement != null && fieldElement.isJsonObject) {
                val fieldObj = fieldElement.asJsonObject
                val lockedElement = fieldObj.get("locked")
                if (lockedElement != null && lockedElement.isJsonPrimitive) {
                    try {
                        if (lockedElement.asBoolean) {
                            val messageElement = fieldObj.get("message")
                            val message = if (messageElement != null && messageElement.isJsonPrimitive)
                                messageElement.asString else null
                            lockedSections[field] = LockedContent(locked = true, message = message)
                            jsonObj.remove(field)
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }

        if (lockedSections.isNotEmpty()) {
            val lockedSectionsJson = fallbackGson.toJsonTree(lockedSections)
            jsonObj.add("lockedSections", lockedSectionsJson)
        }

        return fallbackGson.fromJson(jsonObj, targetClass)
    }
}