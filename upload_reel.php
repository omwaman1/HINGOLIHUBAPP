<?php
/**
 * Upload video to Cloudflare R2
 * Run: php upload_reel.php
 */

// R2 Configuration
$r2Config = [
    'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
    'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
    'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
    'bucket' => 'hello-hingoli-bucket',
    'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
];

// Video file to upload
$localFile = 'C:\\ssh\\Video by omavlogs [DCgaq0rxD9z].mp4';
$r2Path = 'reels/reel_DCgaq0rxD9z.mp4';

if (!file_exists($localFile)) {
    die("Error: File not found: $localFile\n");
}

echo "Uploading: $localFile\n";
echo "To: reels/reel_DCgaq0rxD9z.mp4\n";
echo "Size: " . round(filesize($localFile) / 1024 / 1024, 2) . " MB\n\n";

// Read file content
$fileContent = file_get_contents($localFile);
$contentType = 'video/mp4';

// Upload to R2
$result = uploadFileToR2($r2Config, $r2Path, $fileContent, $contentType);

if ($result['success']) {
    $publicUrl = $r2Config['publicUrl'] . '/' . $r2Path;
    echo "✅ Upload successful!\n";
    echo "Public URL: $publicUrl\n";
} else {
    echo "❌ Upload failed: " . $result['error'] . "\n";
}

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
    
    // Create canonical request
    $payloadHash = hash('sha256', $fileContent);
    
    $canonicalUri = '/' . $objectKey;
    $canonicalQueryString = '';
    
    $canonicalHeaders = "content-type:$contentType\n" .
                        "host:$bucket.$host\n" .
                        "x-amz-content-sha256:$payloadHash\n" .
                        "x-amz-date:$date\n";
    
    $signedHeaders = 'content-type;host;x-amz-content-sha256;x-amz-date';
    
    $canonicalRequest = "PUT\n$canonicalUri\n$canonicalQueryString\n$canonicalHeaders\n$signedHeaders\n$payloadHash";
    
    // Create string to sign
    $credentialScope = "$dateStamp/$region/$service/aws4_request";
    $stringToSign = "$algorithm\n$date\n$credentialScope\n" . hash('sha256', $canonicalRequest);
    
    // Calculate signature
    $kDate = hash_hmac('sha256', $dateStamp, 'AWS4' . $config['secretAccessKey'], true);
    $kRegion = hash_hmac('sha256', $region, $kDate, true);
    $kService = hash_hmac('sha256', $service, $kRegion, true);
    $kSigning = hash_hmac('sha256', 'aws4_request', $kService, true);
    $signature = hash_hmac('sha256', $stringToSign, $kSigning);
    
    // Create authorization header
    $authorizationHeader = "$algorithm Credential={$config['accessKeyId']}/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature";
    
    // Make request with cURL
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
        CURLOPT_TIMEOUT => 300,
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
