package com.chtmed.articles.data.local

import androidx.room.TypeConverter

/** Stores a List<String> as a single column using a separator that can't appear in a tag. */
object Converters {
    private const val SEPARATOR = "|||"

    @TypeConverter
    @JvmStatic
    fun fromTagList(tags: List<String>): String = tags.joinToString(SEPARATOR)

    @TypeConverter
    @JvmStatic
    fun toTagList(raw: String): List<String> =
        if (raw.isEmpty()) emptyList() else raw.split(SEPARATOR)
}
