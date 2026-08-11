package com.chtmed.articles.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maps the "user" object embedded in every DEV.to article response.
 * https://developers.forem.com/api/v1#tag/articles
 */
@Serializable
data class UserDto(
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("profile_image") val profileImage: String? = null,
    @SerialName("profile_image_90") val profileImage90: String? = null
)
