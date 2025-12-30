package com.hingoli.hub.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // Uses BuildConfig to switch between dev (debug) and prod (release) URLs
    private val BASE_URL = com.hingoli.hub.BuildConfig.API_BASE_URL
    
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val token = tokenManager.getAccessToken()
            android.util.Log.d("AuthInterceptor", "🔑 Token status: ${if (token != null) "Present (${token.length} chars)" else "NULL"}")
            android.util.Log.d("AuthInterceptor", "📍 Request URL: ${chain.request().url}")
            
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            
            // Add Authorization header if token exists
            if (token != null) {
                android.util.Log.d("AuthInterceptor", "✅ Adding Authorization header")
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                android.util.Log.w("AuthInterceptor", "⚠️ NO TOKEN - Request will be unauthenticated!")
            }
            
            // Only add Content-Type: application/json if not already set (e.g., multipart requests)
            // Check if Content-Type is already present in the request
            val hasContentType = originalRequest.header("Content-Type") != null ||
                                 originalRequest.body?.contentType() != null
            
            if (!hasContentType) {
                requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8")
            }
            
            // Always request UTF-8 encoded responses for proper Marathi/Unicode support
            requestBuilder.addHeader("Accept-Charset", "UTF-8")
            requestBuilder.addHeader("Accept", "application/json; charset=utf-8")
            
            chain.proceed(requestBuilder.build())
        }
    }
    
    @Provides
    @Singleton
    fun provideTokenAuthenticator(tokenManager: TokenManager): okhttp3.Authenticator {
        return okhttp3.Authenticator { _, response ->
            // Don't retry if we've already tried to refresh
            if (response.request.header("Authorization-Retry") != null) {
                return@Authenticator null
            }
            
            // Only refresh if we got a 401
            if (response.code != 401) {
                return@Authenticator null
            }
            
            val refreshToken = tokenManager.getRefreshToken() ?: return@Authenticator null
            
            // Make a synchronous call to refresh the token
            try {
                val refreshClient = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                val refreshRequest = okhttp3.Request.Builder()
                    .url("${BASE_URL}auth/refresh")
                    .addHeader("Authorization", "Bearer $refreshToken")
                    .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                    .build()
                
                val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                
                if (refreshResponse.isSuccessful) {
                    val body = refreshResponse.body?.string()
                    // Parse the response to get new tokens
                    val gson = com.google.gson.Gson()
                    val tokenResponse = gson.fromJson(body, TokenRefreshResponse::class.java)
                    
                    if (tokenResponse?.data?.accessToken != null) {
                        // Save the new tokens
                        runBlocking {
                            tokenManager.saveTokens(
                                tokenResponse.data.accessToken,
                                tokenResponse.data.refreshToken ?: refreshToken
                            )
                        }
                        
                        // Retry the original request with the new token
                        return@Authenticator response.request.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer ${tokenResponse.data.accessToken}")
                            .addHeader("Authorization-Retry", "true")
                            .build()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TokenAuthenticator", "Failed to refresh token", e)
            }
            
            null
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        tokenAuthenticator: okhttp3.Authenticator,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // HTTP Response Cache - 10MB for faster repeat requests
        val cache = okhttp3.Cache(
            directory = context.cacheDir.resolve("http_cache"),
            maxSize = 10L * 1024 * 1024 // 10 MB
        )
        
        // SSL Certificate Pinning for security against MITM attacks
        // Using Let's Encrypt ISRG Root X1 and R3 intermediate certificate pins
        val certificatePinner = CertificatePinner.Builder()
            // ISRG Root X1 (Let's Encrypt root)
            .add("hellohingoli.com", "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=")
            // Let's Encrypt R3 intermediate
            .add("hellohingoli.com", "sha256/jQJTbIh0grw0/1TkHSumWb+Fs0Ggogr621gT3PvPKG0=")
            // Let's Encrypt E1 intermediate (ECDSA backup)
            .add("hellohingoli.com", "sha256/J2/oqMTsdhFWW/n85tys6b4yDBtb6idZayIEBx7QTxA=")
            .build()
        
        return OkHttpClient.Builder()
            .cache(cache)
            .certificatePinner(certificatePinner)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): coil.ImageLoader {
        return coil.ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}

// Helper class for token refresh response parsing
data class TokenRefreshData(
    @com.google.gson.annotations.SerializedName("access_token") val accessToken: String?,
    @com.google.gson.annotations.SerializedName("refresh_token") val refreshToken: String?
)

data class TokenRefreshResponse(
    val success: Boolean,
    val data: TokenRefreshData?
)

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
    }
    
    fun getAccessToken(): String? {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[ACCESS_TOKEN_KEY]
            }.first()
        }
    }
    
    suspend fun getAccessTokenFlow() = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }
    
    fun getRefreshToken(): String? {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[REFRESH_TOKEN_KEY]
            }.first()
        }
    }
    
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        android.util.Log.d("TokenManager", "💾 SAVING TOKENS - Access: ${accessToken.take(20)}..., Refresh: ${refreshToken.take(20)}...")
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
        android.util.Log.d("TokenManager", "✅ Tokens saved to DataStore")
    }
    
    suspend fun saveUser(userId: Long, userName: String, phone: String?) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId.toString()
            preferences[USER_NAME_KEY] = userName
            phone?.let { preferences[USER_PHONE_KEY] = it }
        }
        
        // Also save to SharedPreferences for background service access
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong("user_id", userId)
            .putString("user_name", userName)
            .apply()
    }
    
    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        
        // Clear SharedPreferences too
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY] != null
        }.first()
    }
    
    suspend fun getUserId(): Long? {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]?.toLongOrNull()
        }.first()
    }
    
    suspend fun getUserName(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }.first()
    }
    
    suspend fun getUserPhone(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_PHONE_KEY]
        }.first()
    }
    
    /**
     * Check if user profile is complete (not auto-generated username)
     * Auto-generated usernames look like: User1234_abc123
     */
    suspend fun isProfileComplete(): Boolean {
        val userName = getUserName() ?: return false
        // Profile is incomplete if username starts with "User" and contains "_"
        // (auto-generated format: User{last4digits}_{randomSuffix})
        val isAutoGenerated = userName.startsWith("User") && userName.contains("_")
        return !isAutoGenerated
    }
    
    /**
     * Update the cached username after profile update
     */
    suspend fun updateUserName(newUserName: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = newUserName
        }
        
        // Also update SharedPreferences for background service access
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("user_name", newUserName)
            .apply()
    }
}
