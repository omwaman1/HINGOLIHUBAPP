package com.hingoli.hub.util

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper to generate app signature hash for SMS Retriever API
 * The hash must be included in SMS for auto-detection to work
 */
@Singleton
class AppSignatureHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AppSignatureHelper"
        private const val HASH_TYPE = "SHA-256"
        private const val NUM_HASHED_BYTES = 9
        private const val NUM_BASE64_CHAR = 11
    }

    /**
     * Get app signatures - this hash must be at the end of your SMS
     */
    fun getAppSignatures(): List<String> {
        val appSignatures = mutableListOf<String>()
        
        try {
            val packageName = context.packageName
            val signatures = getSignatures(context, packageName)
            
            for (signature in signatures) {
                val hash = hash(packageName, signature)
                if (hash != null) {
                    appSignatures.add(hash)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get app signatures: ${e.message}")
        }
        
        return appSignatures
    }
    
    /**
     * Print hash to logcat - use this during development
     */
    fun printHash() {
        val signatures = getAppSignatures()
        Log.d(TAG, "============ SMS RETRIEVER HASH ============")
        if (signatures.isEmpty()) {
            Log.w(TAG, "⚠️ No signatures found! This may be a debug build issue.")
            Log.w(TAG, "⚠️ Try: Build → Generate Signed Bundle/APK first")
        } else {
            signatures.forEachIndexed { index, hash ->
                Log.d(TAG, "✅ HASH[$index]: $hash")
            }
            Log.d(TAG, "")
            Log.d(TAG, "📋 Your SMS format must be:")
            Log.d(TAG, "<#> Your HINGOLI HUB OTP is XXXXXX")
            Log.d(TAG, "${signatures[0]}")
        }
        Log.d(TAG, "============================================")
    }
    
    @Suppress("DEPRECATION")
    private fun getSignatures(context: Context, packageName: String): Array<android.content.pm.Signature> {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                packageInfo.signatures ?: emptyArray()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package not found: $packageName")
            emptyArray()
        }
    }
    
    private fun hash(packageName: String, signature: android.content.pm.Signature): String? {
        val appInfo = "$packageName ${signature.toCharsString()}"
        
        return try {
            val messageDigest = MessageDigest.getInstance(HASH_TYPE)
            messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            
            var hashSignature = messageDigest.digest()
            
            // Truncate to 9 bytes
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES)
            
            // Base64 encode and take first 11 chars
            var base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR)
            
            Log.d(TAG, "Generated hash: $base64Hash")
            base64Hash
        } catch (e: Exception) {
            Log.e(TAG, "Hash generation error: ${e.message}")
            null
        }
    }
}
