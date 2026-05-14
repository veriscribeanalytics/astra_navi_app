package com.astranavi.app.data.api

import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.lang.reflect.Type

class FlexibleJsonDeserializer<T>(
    private val targetClass: Class<T>,
    private val fallbackGson: Gson = Gson()
) : JsonDeserializer<T> {

    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: com.google.gson.JsonDeserializationContext
    ): T {
        val actualJson = if (json.isJsonPrimitive && json.asString.isNotBlank()) {
            try {
                JsonParser.parseString(json.asString)
            } catch (_: Exception) {
                json
            }
        } else {
            return fallbackGson.fromJson(json, targetClass)
        }
        return fallbackGson.fromJson(actualJson, targetClass)
    }
}