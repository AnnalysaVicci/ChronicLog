package com.anna.chroniclog.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FdaDrug(
    @SerializedName("brand_name")
    val brandNames: List<String>?,
    @SerializedName("generic_name")
    val genericNames: List<String>?,
    @SerializedName("manufacturer_name")
    val manufacturer: List<String>?,
    @SerializedName("product_type")
    val productType: List<String>?
) : Serializable {

    // Helper to get the first name available for the UI
    fun getDisplayName(): String {
        return brandNames?.firstOrNull() ?: genericNames?.firstOrNull() ?: "Unknown Drug"
    }

    override fun equals(other: Any?): Boolean =
        if (other is FdaDrug) {
            getDisplayName() == other.getDisplayName()
        } else {
            false
        }

    override fun hashCode(): Int {
        return getDisplayName().hashCode()
    }
}