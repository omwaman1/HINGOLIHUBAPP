<?php
// ========== FILE UPLOAD CONFIGURATION ==========
define('UPLOAD_DIR', __DIR__ . '/../../uploads/'); // Go up to apiv4/uploads
// Ensure dirs exist
if (!file_exists(UPLOAD_DIR)) @mkdir(UPLOAD_DIR, 0755, true);
if (!file_exists(UPLOAD_DIR . 'listings/')) @mkdir(UPLOAD_DIR . 'listings/', 0755, true);
if (!file_exists(UPLOAD_DIR . 'categories/')) @mkdir(UPLOAD_DIR . 'categories/', 0755, true);
if (!file_exists(UPLOAD_DIR . 'banners/')) @mkdir(UPLOAD_DIR . 'banners/', 0755, true);
if (!file_exists(UPLOAD_DIR . 'products/')) @mkdir(UPLOAD_DIR . 'products/', 0755, true);

define('MAX_FILE_SIZE', 10 * 1024 * 1024); // 10MB (before compression)
define('ALLOWED_TYPES', ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/avif']);
define('MAX_IMAGE_DIMENSION', 1200); // Max width/height
define('IMAGE_QUALITY', 80); // WebP/JPEG quality (1-100)

/**
 * Compress, resize, and convert image to WebP
 */
function processImage($tmpPath, $mimeType) {
    // Create image resource based on type
    switch ($mimeType) {
        case 'image/jpeg':
            $image = @imagecreatefromjpeg($tmpPath);
            break;
        case 'image/png':
            $image = @imagecreatefrompng($tmpPath);
            break;
        case 'image/webp':
            $image = @imagecreatefromwebp($tmpPath);
            break;
        case 'image/gif':
            $image = @imagecreatefromgif($tmpPath);
            break;
        case 'image/avif':
            // AVIF support (PHP 8.1+)
            if (function_exists('imagecreatefromavif')) {
                $image = @imagecreatefromavif($tmpPath);
            } else {
                // Fallback: try to use ImageMagick or return null
                return null;
            }
            break;
        default:
            return null;
    }
    
    if (!$image) {
        return null;
    }
    
    // Get original dimensions
    $origWidth = imagesx($image);
    $origHeight = imagesy($image);
    
    // Calculate new dimensions (maintain aspect ratio)
    $newWidth = $origWidth;
    $newHeight = $origHeight;
    
    if ($origWidth > MAX_IMAGE_DIMENSION || $origHeight > MAX_IMAGE_DIMENSION) {
        if ($origWidth > $origHeight) {
            $newWidth = MAX_IMAGE_DIMENSION;
            $newHeight = (int)($origHeight * (MAX_IMAGE_DIMENSION / $origWidth));
        } else {
            $newHeight = MAX_IMAGE_DIMENSION;
            $newWidth = (int)($origWidth * (MAX_IMAGE_DIMENSION / $origHeight));
        }
    }
    
    // Create resized image
    $resized = imagecreatetruecolor($newWidth, $newHeight);
    
    // Preserve transparency for PNG/WebP
    imagealphablending($resized, false);
    imagesavealpha($resized, true);
    $transparent = imagecolorallocatealpha($resized, 0, 0, 0, 127);
    imagefill($resized, 0, 0, $transparent);
    
    // Resize
    imagecopyresampled($resized, $image, 0, 0, 0, 0, $newWidth, $newHeight, $origWidth, $origHeight);
    
    // Convert to WebP and get binary content
    ob_start();
    imagewebp($resized, null, IMAGE_QUALITY);
    $webpContent = ob_get_clean();
    
    // Clean up
    imagedestroy($image);
    imagedestroy($resized);
    
    return $webpContent;
}

/**
 * Sanitize filename - remove spaces, special chars, limit length
 */
function sanitizeFilename($name) {
    // Remove spaces and special characters
    $name = preg_replace('/[^a-zA-Z0-9_-]/', '', str_replace(' ', '', $name));
    // Limit length to 50 chars
    $name = substr($name, 0, 50);
    // Lowercase
    return strtolower($name);
}

/**
 * Handle file upload to Cloudflare R2 with compression
 * @param array $file - $_FILES array element
 * @param string $folder - destination folder
 * @param string|null $customName - custom filename (without extension)
 */
function uploadImage($file, $folder = 'listings', $customName = null) {
    if (!isset($file) || $file['error'] === UPLOAD_ERR_NO_FILE) {
        return null;
    }
    
    if ($file['error'] !== UPLOAD_ERR_OK) {
        throw new Exception("Upload error code: " . $file['error']);
    }
    
    if ($file['size'] > MAX_FILE_SIZE) {
        throw new Exception("File too large. Maximum size is 10MB.");
    }
    
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);
    
    if (!in_array($mimeType, ALLOWED_TYPES)) {
        throw new Exception("Invalid file type. Only JPG, PNG, WebP, GIF allowed.");
    }
    
    // Process image (resize, compress, convert to WebP)
    $fileContent = processImage($file['tmp_name'], $mimeType);
    
    // Generate filename
    if ($customName) {
        $baseName = sanitizeFilename($customName);
        // Add timestamp for uniqueness
        $baseName .= '_' . time();
    } else {
        $baseName = uniqid() . '_' . time();
    }
    
    if ($fileContent === null) {
        // Fallback to original if GD fails
        $fileContent = file_get_contents($file['tmp_name']);
        $ext = pathinfo($file['name'], PATHINFO_EXTENSION);
        $filename = $folder . '/' . $baseName . '.' . strtolower($ext);
    } else {
        // Use .webp extension for compressed images
        $filename = $folder . '/' . $baseName . '.webp';
        $mimeType = 'image/webp';
    }
    
    // R2 Configuration
    $r2Config = [
        'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
        'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
        'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
        'bucket' => 'hello-hingoli-bucket',
        'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
    ];
    
    // Upload to R2
    $result = uploadToR2($r2Config, $filename, $fileContent, $mimeType);
    
    if (!$result['success']) {
        throw new Exception("R2 Upload failed: " . $result['error']);
    }
    
    return $r2Config['publicUrl'] . '/' . $filename;
}

/**
 * Handle multiple image upload
 * @param array $files - $_FILES array
 * @param string $folder - destination folder
 * @param string|null $baseName - base name for files (will append 1, 2, 3, etc.)
 */
function uploadMultipleImages($files, $folder = 'listings', $baseName = null) {
    $urls = [];
    
    if (!isset($files['name']) || !is_array($files['name'])) {
        return $urls;
    }
    
    $counter = 1;
    for ($i = 0; $i < count($files['name']); $i++) {
        if ($files['error'][$i] === UPLOAD_ERR_NO_FILE) continue;
        
        $file = [
            'name' => $files['name'][$i],
            'type' => $files['type'][$i],
            'tmp_name' => $files['tmp_name'][$i],
            'error' => $files['error'][$i],
            'size' => $files['size'][$i]
        ];
        
        // Generate numbered name for gallery images
        $customName = $baseName ? $baseName . $counter : null;
        
        $url = uploadImage($file, $folder, $customName);
        if ($url) {
            $urls[] = $url;
            $counter++;
        }
    }
    
    return $urls;
}

/**
 * Upload file to Cloudflare R2 (S3-compatible)
 */
function uploadToR2($config, $key, $content, $contentType) {
    try {
        $endpoint = $config['endpoint'];
        $accessKeyId = $config['accessKeyId'];
        $secretAccessKey = $config['secretAccessKey'];
        $bucket = $config['bucket'];
        $region = 'auto'; // R2 uses 'auto' for region
        
        $host = parse_url($endpoint, PHP_URL_HOST);
        $url = "{$endpoint}/{$bucket}/{$key}";
        
        $date = gmdate('Ymd\THis\Z');
        $shortDate = gmdate('Ymd');
        $contentHash = hash('sha256', $content);
        
        // Canonical request
        $headers = [
            'host' => $host,
            'x-amz-content-sha256' => $contentHash,
            'x-amz-date' => $date,
            'content-type' => $contentType
        ];
        ksort($headers);
        
        $signedHeaders = implode(';', array_keys($headers));
        $canonicalHeaders = '';
        foreach ($headers as $k => $v) {
            $canonicalHeaders .= "{$k}:{$v}\n";
        }
        
        $canonicalRequest = "PUT\n/{$bucket}/{$key}\n\n{$canonicalHeaders}\n{$signedHeaders}\n{$contentHash}";
        $canonicalRequestHash = hash('sha256', $canonicalRequest);
        
        // String to sign
        $scope = "{$shortDate}/{$region}/s3/aws4_request";
        $stringToSign = "AWS4-HMAC-SHA256\n{$date}\n{$scope}\n{$canonicalRequestHash}";
        
        // Signing key
        $kDate = hash_hmac('sha256', $shortDate, "AWS4{$secretAccessKey}", true);
        $kRegion = hash_hmac('sha256', $region, $kDate, true);
        $kService = hash_hmac('sha256', 's3', $kRegion, true);
        $kSigning = hash_hmac('sha256', 'aws4_request', $kService, true);
        $signature = hash_hmac('sha256', $stringToSign, $kSigning);
        
        // Authorization header
        $authHeader = "AWS4-HMAC-SHA256 Credential={$accessKeyId}/{$scope}, SignedHeaders={$signedHeaders}, Signature={$signature}";
        
        // Make request
        $ch = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_CUSTOMREQUEST => 'PUT',
            CURLOPT_POSTFIELDS => $content,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_HTTPHEADER => [
                "Authorization: {$authHeader}",
                "Content-Type: {$contentType}",
                "Host: {$host}",
                "x-amz-content-sha256: {$contentHash}",
                "x-amz-date: {$date}"
            ]
        ]);
        
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $error = curl_error($ch);
        curl_close($ch);
        
        if ($httpCode >= 200 && $httpCode < 300) {
            return ['success' => true, 'url' => $url];
        }
        
        return ['success' => false, 'error' => $error ?: "HTTP {$httpCode}: {$response}"];
    } catch (Exception $e) {
        return ['success' => false, 'error' => $e->getMessage()];
    }
}
?>
