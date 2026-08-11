package com.chtmed.articles.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

/**
 * DEV.to is inconsistent about tag_list's shape: GET /articles returns it as a
 * JSON array, while GET /articles/{id} returns it as a single comma-separated
 * string (with the array instead under "tags"). Decoding it as a plain
 * List<String> works for the list endpoint but throws for article detail,
 * which is why the detail screen was failing to load. This serializer accepts
 * either shape.
 */
object FlexibleTagListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TagList", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): List<String> {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        return when (element) {
            is JsonArray -> element.map { it.jsonPrimitive.content }
            else -> element.jsonPrimitive.content
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        throw UnsupportedOperationException("FlexibleTagListSerializer is deserialize-only")
    }
}
