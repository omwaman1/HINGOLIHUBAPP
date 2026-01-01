<?php
/**
 * Bulk Upload Reels to Cloudflare R2
 * Run: C:\xampp\php\php.exe bulk_upload_reels.php
 */

ini_set('max_execution_time', 0);
ini_set('memory_limit', '512M');

// R2 Configuration
$r2Config = [
    'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
    'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
    'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
    'bucket' => 'hello-hingoli-bucket',
    'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
];

// Database Configuration
$dbConfig = [
    'host' => 'gateway01.ap-southeast-1.prod.aws.tidbcloud.com',
    'port' => 4000,
    'user' => '39rSBGEWyaX8SaD.root',
    'password' => 'lOUBAGjTSM0SvHIt',
    'database' => 'hellohingoli'
];

// Local reels folder
$reelsFolder = 'C:\\Users\\Meeting\\Desktop\\MH\\reels';

echo "=== Bulk Reel Upload to R2 ===\n\n";

// Connect to database
try {
    $dsn = "mysql:host={$dbConfig['host']};port={$dbConfig['port']};dbname={$dbConfig['database']};charset=utf8mb4";
    $options = [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::MYSQL_ATTR_SSL_VERIFY_SERVER_CERT => false,
        PDO::MYSQL_ATTR_SSL_CA => null
    ];
    $db = new PDO($dsn, $dbConfig['user'], $dbConfig['password'], $options);
    echo "✅ Database connected\n";
} catch (Exception $e) {
    die("❌ Database connection failed: " . $e->getMessage() . "\n");
}

// Get all video files
$files = glob($reelsFolder . DIRECTORY_SEPARATOR . "*.mp4");
$totalFiles = count($files);
echo "📁 Found $totalFiles video files\n\n";

$uploaded = 0;
$failed = 0;
$skipped = 0;

foreach ($files as $index => $filePath) {
    $filename = basename($filePath);
    $reelId = pathinfo($filename, PATHINFO_FILENAME); // e.g., "DCgaq0rxD9z"
    $progress = $index + 1;
    
    echo "[$progress/$totalFiles] Processing: $filename\n";
    
    // Check if already in database
    $stmt = $db->prepare("SELECT reel_id, video_url FROM reels WHERE video_url LIKE ?");
    $stmt->execute(['%' . $reelId . '%']);
    $existing = $stmt->fetch();
    
    if ($existing && $existing['video_url']) {
        echo "  ⏭️ Skipped (already uploaded)\n";
        $skipped++;
        continue;
    }
    
    // Read file
    $fileSize = filesize($filePath);
    $fileSizeMB = round($fileSize / 1024 / 1024, 2);
    echo "  📤 Uploading {$fileSizeMB}MB...";
    
    $fileContent = file_get_contents($filePath);
    if (!$fileContent) {
        echo " ❌ Failed to read file\n";
        $failed++;
        continue;
    }
    
    // Upload to R2
    $r2Path = "reels/{$reelId}.mp4";
    $result = uploadFileToR2($r2Config, $r2Path, $fileContent, 'video/mp4');
    
    if (!$result['success']) {
        echo " ❌ Upload failed: " . ($result['error'] ?? 'Unknown') . "\n";
        $failed++;
        continue;
    }
    
    $videoUrl = $r2Config['publicUrl'] . '/' . $r2Path;
    echo " ✅ Uploaded\n";
    
    // Insert/update in database
    $instagramUrl = "https://www.instagram.com/reel/{$reelId}/";
    $title = "Reel {$reelId}";
    
    // Check if exists in DB
    $stmt = $db->prepare("SELECT reel_id FROM reels WHERE instagram_url LIKE ?");
    $stmt->execute(['%' . $reelId . '%']);
    $existingReel = $stmt->fetch();
    
    if ($existingReel) {
        // Update existing
        $stmt = $db->prepare("UPDATE reels SET video_url = ?, updated_at = NOW() WHERE reel_id = ?");
        $stmt->execute([$videoUrl, $existingReel['reel_id']]);
        echo "  💾 Updated database record\n";
    } else {
        // Insert new
        $stmt = $db->prepare("INSERT INTO reels (instagram_url, video_url, title, sort_order, status, created_at) VALUES (?, ?, ?, ?, 'active', NOW())");
        $stmt->execute([$instagramUrl, $videoUrl, $title, $index + 1]);
        echo "  💾 Inserted new database record\n";
    }
    
    $uploaded++;
    
    // Free memory
    unset($fileContent);
}

echo "\n=== Complete ===\n";
echo "✅ Uploaded: $uploaded\n";
echo "⏭️ Skipped: $skipped\n";
echo "❌ Failed: $failed\n";

/**
 * Upload file to Cloudflare R2 (S3-compatible)
 */
function uploadFileToR2(array $config, string $objectKey, string $fileContent, string $contentType): array {
    $host = str_replace('https://', '', $config['endpoint']);
    $bucket = $config['bucket'];
    $region = 'auto';
    $service = 's3';
    $algorithm = 'AWS4-HMAC-SHA256';
    
    $date = gmdate('Ymd\THis\Z');
    $dateStamp = gmdate('Ymd');
    
    $payloadHash = hash('sha256', $fileContent);
    $canonicalUri = '/' . $objectKey;
    $canonicalQueryString = '';
    
    $canonicalHeaders = "content-type:$contentType\n" .
                        "host:$bucket.$host\n" .
                        "x-amz-content-sha256:$payloadHash\n" .
                        "x-amz-date:$date\n";
    
    $signedHeaders = 'content-type;host;x-amz-content-sha256;x-amz-date';
    
    $canonicalRequest = "PUT\n$canonicalUri\n$canonicalQueryString\n$canonicalHeaders\n$signedHeaders\n$payloadHash";
    
    $credentialScope = "$dateStamp/$region/$service/aws4_request";
    $stringToSign = "$algorithm\n$date\n$credentialScope\n" . hash('sha256', $canonicalRequest);
    
    $kDate = hash_hmac('sha256', $dateStamp, 'AWS4' . $config['secretAccessKey'], true);
    $kRegion = hash_hmac('sha256', $region, $kDate, true);
    $kService = hash_hmac('sha256', $service, $kRegion, true);
    $kSigning = hash_hmac('sha256', 'aws4_request', $kService, true);
    $signature = hash_hmac('sha256', $stringToSign, $kSigning);
    
    $authorizationHeader = "$algorithm Credential={$config['accessKeyId']}/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature";
    
    $url = "https://$bucket.$host/$objectKey";
    
    $ch = curl_init();
    curl_setopt_array($ch, [
        CURLOPT_URL => $url,
        CURLOPT_PUT => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => [
            "Content-Type: $contentType",
            "Host: $bucket.$host",
            "x-amz-content-sha256: $payloadHash",
            "x-amz-date: $date",
            "Authorization: $authorizationHeader",
            "Content-Length: " . strlen($fileContent)
        ],
        CURLOPT_POSTFIELDS => $fileContent,
        CURLOPT_TIMEOUT => 600,
        CURLOPT_CUSTOMREQUEST => 'PUT'
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($httpCode === 200 || $httpCode === 201) {
        return ['success' => true];
    }
    
    return ['success' => false, 'error' => "HTTP $httpCode: $response $error"];
}
