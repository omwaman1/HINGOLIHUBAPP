<?php
/**
 * Firebase Notification Helper
 * Send push notifications via Firebase Realtime Database
 */

/**
 * Get Firebase access token using service account
 * Uses JWT to get OAuth2 access token from Google
 */
function getFirebaseAccessToken(): ?string {
    static $cachedToken = null;
    static $tokenExpiry = 0;
    
    // Return cached token if still valid (with 5 min buffer)
    if ($cachedToken && time() < ($tokenExpiry - 300)) {
        return $cachedToken;
    }
    
    // Load service account (in parent apiv5/ directory)
    $serviceAccountPath = __DIR__ . '/../firebase-service-account.json';
    if (!file_exists($serviceAccountPath)) {
        error_log("Firebase service account not found at: $serviceAccountPath");
        return null;
    }
    
    $serviceAccount = json_decode(file_get_contents($serviceAccountPath), true);
    if (!$serviceAccount) {
        error_log("Failed to parse Firebase service account JSON");
        return null;
    }
    
    // Create JWT with URL-safe Base64 encoding
    $now = time();
    $jwtHeader = rtrim(strtr(base64_encode(json_encode(['alg' => 'RS256', 'typ' => 'JWT'])), '+/', '-_'), '=');
    $jwtClaims = rtrim(strtr(base64_encode(json_encode([
        'iss' => $serviceAccount['client_email'],
        'sub' => $serviceAccount['client_email'],
        'aud' => 'https://oauth2.googleapis.com/token',
        'iat' => $now,
        'exp' => $now + 3600,
        'scope' => 'https://www.googleapis.com/auth/firebase.database https://www.googleapis.com/auth/userinfo.email'
    ])), '+/', '-_'), '=');
    
    // Sign JWT with private key
    $signatureInput = "{$jwtHeader}.{$jwtClaims}";
    $privateKey = openssl_pkey_get_private($serviceAccount['private_key']);
    if (!$privateKey) {
        error_log("Failed to load Firebase private key");
        return null;
    }
    
    openssl_sign($signatureInput, $signature, $privateKey, OPENSSL_ALGO_SHA256);
    $jwtSignature = rtrim(strtr(base64_encode($signature), '+/', '-_'), '=');
    $jwt = "{$jwtHeader}.{$jwtClaims}.{$jwtSignature}";
    
    // Exchange JWT for access token
    $ch = curl_init();
    curl_setopt_array($ch, [
        CURLOPT_URL => 'https://oauth2.googleapis.com/token',
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ]),
        CURLOPT_HTTPHEADER => ['Content-Type: application/x-www-form-urlencoded'],
        CURLOPT_TIMEOUT => 10
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode !== 200) {
        error_log("Failed to get Firebase access token: HTTP $httpCode - $response");
        return null;
    }
    
    $tokenData = json_decode($response, true);
    if (!isset($tokenData['access_token'])) {
        error_log("Firebase token response missing access_token: $response");
        return null;
    }
    
    $cachedToken = $tokenData['access_token'];
    $tokenExpiry = $now + ($tokenData['expires_in'] ?? 3600);
    
    return $cachedToken;
}

/**
 * Send push notification to user via Firebase Realtime Database
 */
function sendFirebaseNotification(int $userId, string $title, string $body, string $type = 'general', array $extraData = []): bool {
    // Firebase Realtime Database URL (from google-services.json)
    $firebaseUrl = 'https://hellohingoliapp-default-rtdb.asia-southeast1.firebasedatabase.app';
    
    // Get access token from service account
    $accessToken = getFirebaseAccessToken();
    if (!$accessToken) {
        error_log("Firebase notification failed: Could not get access token");
        return false;
    }
    
    // Generate unique notification ID (no dots, Firebase doesn't allow them in paths)
    $notificationId = 'notif_' . bin2hex(random_bytes(8));
    
    // Prepare notification data
    $notificationData = array_merge([
        'title' => $title,
        'body' => $body,
        'type' => $type,
        'timestamp' => time() * 1000, // milliseconds
        'createdAt' => date('c')
    ], $extraData);
    
    // Write to /notifications/{userId}/{notificationId}
    $url = "{$firebaseUrl}/notifications/{$userId}/{$notificationId}.json";
    
    $ch = curl_init();
    curl_setopt_array($ch, [
        CURLOPT_URL => $url,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_CUSTOMREQUEST => 'PUT',
        CURLOPT_POSTFIELDS => json_encode($notificationData),
        CURLOPT_HTTPHEADER => [
            'Content-Type: application/json',
            'Authorization: Bearer ' . $accessToken
        ],
        CURLOPT_TIMEOUT => 10
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode >= 200 && $httpCode < 300) {
        error_log("Firebase notification sent to user $userId: $title");
        return true;
    } else {
        error_log("Firebase notification failed for user $userId: HTTP $httpCode - $response");
        return false;
    }
}

/**
 * Send listing approved notification to user
 */
function sendListingApprovedNotification(int $userId, string $listingTitle): bool {
    return sendFirebaseNotification(
        $userId,
        'Listing Approved! / नोंदणी मंजूर!',
        "Your listing \"$listingTitle\" has been approved! Check it in the app.\nतुमची \"$listingTitle\" नोंदणी मंजूर झाली! अॅपमध्ये पहा.",
        'listing_approved',
        ['listingTitle' => $listingTitle]
    );
}
