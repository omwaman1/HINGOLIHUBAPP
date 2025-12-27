package com.hingoli.hub.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for compressing and optimizing images before upload.
 * 
 * Features:
 * - Resizes images to max dimensions (default 1920x1080)
 * - Compresses to WebP format for better compression
 * - Handles EXIF rotation to fix orientation
 * - Significantly reduces file size while maintaining quality
 */
object ImageCompressor {
    
    private const val DEFAULT_MAX_WIDTH = 1920
    private const val DEFAULT_MAX_HEIGHT = 1080
    private const val DEFAULT_QUALITY = 80 // 0-100, higher = better quality but larger file
    
    /**
     * Compress and optimize an image from URI
     * 
     * @param context Android context
     * @param imageUri The source image URI
     * @param maxWidth Maximum width (default 1920)
     * @param maxHeight Maximum height (default 1080)
     * @param quality Compression quality 0-100 (default 80)
     * @param useWebP Use WebP format for better compression (default true)
     * @return Compressed image file, or null on error
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = DEFAULT_MAX_WIDTH,
        maxHeight: Int = DEFAULT_MAX_HEIGHT,
        quality: Int = DEFAULT_QUALITY,
        useWebP: Boolean = true
    ): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return null
            
            // Get original dimensions without loading full bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // Calculate sample size for efficient loading
            val sampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            
            // Load bitmap with sample size
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val newInputStream = context.contentResolver.openInputStream(imageUri)
                ?: return null
            var bitmap = BitmapFactory.decodeStream(newInputStream, null, loadOptions)
                ?: return null
            newInputStream.close()
            
            // Handle EXIF rotation
            try {
                val exifInputStream = context.contentResolver.openInputStream(imageUri)
                if (exifInputStream != null) {
                    val exif = ExifInterface(exifInputStream)
                    val rotation = getRotationFromExif(exif)
                    if (rotation != 0) {
                        bitmap = rotateBitmap(bitmap, rotation)
                    }
                    exifInputStream.close()
                }
            } catch (e: Exception) {
                // Ignore EXIF errors
            }
            
            // Scale down if still too large
            if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                bitmap = scaleBitmap(bitmap, maxWidth, maxHeight)
            }
            
            // Compress to file
            val extension = if (useWebP) "webp" else "jpg"
            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.${extension}")
            val outputStream = FileOutputStream(outputFile)
            
            val format = if (useWebP) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            } else {
                Bitmap.CompressFormat.JPEG
            }
            
            bitmap.compress(format, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Clean up bitmap
            bitmap.recycle()
            
            // Log compression result
            val originalSize = getFileSize(context, imageUri)
            val compressedSize = outputFile.length()
            val savedPercent = if (originalSize > 0) {
                ((originalSize - compressedSize) * 100 / originalSize).toInt()
            } else 0
            
            android.util.Log.d("ImageCompressor", 
                "Compressed: ${formatFileSize(originalSize)} → ${formatFileSize(compressedSize)} (saved $savedPercent%)")
            
            outputFile
        } catch (e: Exception) {
            android.util.Log.e("ImageCompressor", "Compression failed", e)
            null
        }
    }
    
    /**
     * Get file size from URI
     */
    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = ByteArrayOutputStream()
                input.copyTo(bytes)
                bytes.size().toLong()
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Format file size to human readable string
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }
    
    /**
     * Calculate optimal sample size for bitmap loading
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && 
                   (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Scale bitmap to fit within max dimensions while maintaining aspect ratio
     */
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth / ratioBitmap).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
    
    /**
     * Get rotation degrees from EXIF data
     */
    private fun getRotationFromExif(exif: ExifInterface): Int {
        return when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
    
    /**
     * Rotate bitmap by specified degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
