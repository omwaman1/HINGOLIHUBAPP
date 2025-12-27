package com.hingoli.hub.data.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class Listing(
    @SerializedName("listing_id")
    val listingId: Long,
    
    @SerializedName("listing_type")
    val listingType: String, // "services", "selling", "business", "jobs"
    
    @SerializedName("condition")
    val condition: String? = "old", // "old" or "new" (for selling items)
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("price")
    val price: Double? = null,
    
    @SerializedName("price_negotiable")
    val priceNegotiable: Boolean = false,
    
    @SerializedName("currency")
    val currency: String = "INR",
    
    @SerializedName("category_id")
    val categoryId: Int,
    
    @SerializedName("subcategory_id")
    val subcategoryId: Int? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("state")
    val state: String? = null,
    
    @SerializedName("main_image_url")
    val mainImageUrl: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("status")
    val status: String = "active",
    
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    
    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    
    @SerializedName("view_count")
    val viewCount: Int = 0,
    
    @SerializedName("review_count")
    val reviewCount: Int = 0,
    
    @SerializedName("avg_rating")
    val avgRating: Double = 0.0,
    
    @SerializedName("experience_years")
    val experienceYears: Int? = null,
    
    // Service-specific fields for listing cards
    @SerializedName("price_min")
    val priceMin: Double? = null,
    
    @SerializedName("price_max")
    val priceMax: Double? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    // Job-specific fields for listing cards
    @SerializedName("salary_min")
    val salaryMin: Double? = null,
    
    @SerializedName("salary_max")
    val salaryMax: Double? = null,
    
    @SerializedName("salary_period")
    val salaryPeriod: String? = null,
    
    @SerializedName("employment_type")
    val employmentType: String? = null,
    
    @SerializedName("work_location_type")
    val workLocationType: String? = null,
    
    @SerializedName("vacancies")
    val vacancies: Int? = null,
    
    // User info (joined)
    @SerializedName("user")
    val user: User? = null,
    
    // Images
    @SerializedName("images")
    val images: List<ListingImage>? = null,
    
    // Videos
    @SerializedName("videos")
    val videos: List<ListingVideo>? = null,
    
    // Type-specific data will be in child objects
    @SerializedName("service_details")
    val serviceDetails: ServiceDetails? = null,
    
    @SerializedName("business_details")
    val businessDetails: BusinessDetails? = null,
    
    @SerializedName("job_details")
    val jobDetails: JobDetails? = null
)

data class ListingImage(
    @SerializedName("image_id")
    val imageId: Long,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("sort_order")
    val sortOrder: Int = 0
)

data class ListingVideo(
    @SerializedName("video_id")
    val videoId: Long,
    
    @SerializedName("video_url")
    val videoUrl: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("duration_seconds")
    val durationSeconds: Int? = null
)

data class ServiceDetails(
    @SerializedName("service_type")
    val serviceType: String? = null, // one_time, recurring, contract, hourly
    
    @SerializedName("experience_years")
    val experienceYears: Int? = null,
    
    @SerializedName("availability")
    val availability: String? = null,
    
    @SerializedName("service_area_radius_km")
    val serviceAreaRadiusKm: Int? = null,
    
    @SerializedName("hourly_rate")
    val hourlyRate: Double? = null,
    
    @SerializedName("price_min")
    val priceMin: Double? = null,
    
    @SerializedName("price_max")
    val priceMax: Double? = null
)

data class BusinessDetails(
    @SerializedName("business_name")
    val businessName: String? = null,
    
    @SerializedName("industry")
    val industry: String? = null,
    
    @SerializedName("business_type")
    val businessType: String? = null,
    
    @SerializedName("established_year")
    val establishedYear: Int? = null,
    
    @SerializedName("employee_count")
    val employeeCount: String? = null,
    
    @SerializedName("website_url")
    val websiteUrl: String? = null,
    
    @SerializedName("business_email")
    val businessEmail: String? = null,
    
    @SerializedName("business_phone")
    val businessPhone: String? = null
)

data class JobDetails(
    @SerializedName("job_title")
    val jobTitle: String? = null,
    
    @SerializedName("employment_type")
    val employmentType: String? = null, // full_time, part_time, contract, internship, freelance
    
    @SerializedName("salary_min")
    val salaryMin: Double? = null,
    
    @SerializedName("salary_max")
    val salaryMax: Double? = null,
    
    @SerializedName("salary_period")
    val salaryPeriod: String? = null, // hourly, daily, weekly, monthly, yearly
    
    @SerializedName("experience_required_years")
    val experienceRequiredYears: Int? = null,
    
    @SerializedName("education_required")
    val educationRequired: String? = null,
    
    @SerializedName("is_remote")
    val isRemote: Boolean? = null,
    
    @SerializedName("work_location_type")
    val workLocationType: String? = null, // on_site, remote, hybrid
    
    @SerializedName("vacancies")
    val vacancies: Int? = null,
    
    @SerializedName("application_deadline")
    val applicationDeadline: String? = null,
    
    @SerializedName("skills_required")
    val skillsRequired: String? = null
)
