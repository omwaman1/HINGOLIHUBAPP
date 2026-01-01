package com.hingoli.hub.data.model

import com.google.gson.annotations.SerializedName

/**
 * App statistics for home screen display
 */
data class AppStats(
    @SerializedName("users")
    val users: Int = 0,
    
    @SerializedName("businesses")
    val businesses: Int = 0,
    
    @SerializedName("services")
    val services: Int = 0,
    
    @SerializedName("jobs")
    val jobs: Int = 0,
    
    @SerializedName("old_products")
    val oldProducts: Int = 0,
    
    @SerializedName("new_products")
    val newProducts: Int = 0
)
