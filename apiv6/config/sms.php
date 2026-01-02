<?php
/**
 * SMS Gateway OTP Configuration
 * 
 * Uses a dedicated Android phone as SMS gateway via Firebase FCM.
 * OTP requests are written to Firebase Realtime Database, which triggers
 * a Cloud Function to send FCM to the gateway app.
 */

// Firebase Configuration
define('FIREBASE_DATABASE_URL', 'https://hellohingoliapp-default-rtdb.asia-southeast1.firebasedatabase.app');
define('FIREBASE_API_KEY', 'AIzaSyDHZn6FWg-LBvTPkv11iiGoka0CNaxsEy8');

// OTP Settings
define('OTP_LENGTH', 6);                  // 6-digit OTP
define('OTP_EXPIRY_MINUTES', 5);          // OTP valid for 5 minutes
define('OTP_MAX_ATTEMPTS', 3);            // Max verification attempts

/**
 * Generate a random OTP
 */
function generateOTP(int $length = OTP_LENGTH): string {
    $otp = '';
    for ($i = 0; $i < $length; $i++) {
        $otp .= random_int(0, 9);
    }
    return $otp;
}

/**
 * Send OTP via SMS Gateway (Firebase -> FCM -> Gateway App -> SMS)
 * 
 * @param string $phone Phone number (10 digits without country code)
 * @param string $otp The OTP code to send
 * @return array ['success' => bool, 'message' => string, 'request_id' => string|null]
 */
function sendOtpViaGateway(string $phone, string $otp): array {
    // Clean phone number - remove non-digits first
    $phone = preg_replace('/[^0-9]/', '', $phone);
    
    // Only strip country code if it results in exactly 10 digits
    // This prevents stripping 91 from phones that naturally start with 91 (like 9156233733)
    if (strlen($phone) == 12 && substr($phone, 0, 2) === '91') {
        $phone = substr($phone, 2); // Remove 91 country code
    } elseif (strlen($phone) == 11 && substr($phone, 0, 1) === '0') {
        $phone = substr($phone, 1); // Remove leading 0
    }
    
    if (strlen($phone) !== 10) {
        return ['success' => false, 'message' => 'Invalid phone number (got ' . strlen($phone) . ' digits)', 'request_id' => null];
    }
    
    // Generate unique request ID (remove dots - Firebase doesn't allow them in paths)
    $requestId = str_replace('.', '_', uniqid('otp_', true));
    
    // Write OTP request to Firebase Realtime Database
    // The Cloud Function will pick this up and send FCM to the gateway
    $url = FIREBASE_DATABASE_URL . '/otp_requests/' . $requestId . '.json';
    
    $data = [
        'phone' => $phone,
        'otp' => $otp,
        'status' => 'pending',
        'createdAt' => time() * 1000 // JavaScript timestamp
    ];
    
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json'
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);
    // SSL settings for Hostinger
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    $curlErrno = curl_errno($ch);
    curl_close($ch);
    
    // Detailed logging
    error_log("Firebase OTP - URL: $url");
    error_log("Firebase OTP - Phone: $phone, OTP: $otp, RequestId: $requestId");
    error_log("Firebase OTP - HTTP: $httpCode, Response: $response");
    
    if ($error) {
        error_log("Firebase write cURL error ($curlErrno): $error");
        return ['success' => false, 'message' => "Connection error: $error", 'request_id' => null];
    }
    
    if ($httpCode === 200) {
        return [
            'success' => true,
            'message' => 'OTP sent successfully',
            'request_id' => $requestId
        ];
    }
    
    error_log("Firebase error: HTTP $httpCode - $response");
    return ['success' => false, 'message' => "HTTP $httpCode: $response", 'request_id' => null];
}

/**
 * Verify OTP locally (we generate and store it ourselves)
 * The gateway just delivers the SMS, verification is done server-side
 */
function verifyOtpLocally(string $storedHash, string $userOtp): bool {
    return password_verify($userOtp, $storedHash);
}

// ============================================
// Legacy functions (kept for backup/fallback)
// ============================================

define('FAST2SMS_API_KEY', 'LQC7fTMjsARta1BP54omgFH9ki6qlYXGWNEIb8ve2ZundSK0UcgIabfvqFmXuQk1MYtiWs8VpjGhxoSA');

/**
 * Send OTP via Fast2SMS (Legacy - backup)
 */
function sendSmsOTP(string $phone, string $otp): array {
    $url = 'https://www.fast2sms.com/dev/bulkV2';
    
    // Clean phone number - remove non-digits first
    $phone = preg_replace('/[^0-9]/', '', $phone);
    
    // Only strip country code if it results in exactly 10 digits
    if (strlen($phone) == 12 && substr($phone, 0, 2) === '91') {
        $phone = substr($phone, 2);
    } elseif (strlen($phone) == 11 && substr($phone, 0, 1) === '0') {
        $phone = substr($phone, 1);
    }
    
    if (strlen($phone) !== 10) {
        return ['success' => false, 'message' => 'Invalid phone number', 'request_id' => null];
    }
    
    $message = "Your Hello Hingoli OTP is: $otp. Valid for " . OTP_EXPIRY_MINUTES . " min. Do not share.";
    
    $postData = [
        'route' => 'q',
        'message' => $message,
        'language' => 'english',
        'flash' => 0,
        'numbers' => $phone
    ];
    
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($postData));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'authorization: ' . FAST2SMS_API_KEY
    ]);
    
    $response = curl_exec($ch);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($error) {
        return ['success' => false, 'message' => 'Failed to send OTP', 'request_id' => null];
    }
    
    $result = json_decode($response, true);
    
    if (isset($result['return']) && $result['return'] === true) {
        return ['success' => true, 'message' => 'OTP sent successfully', 'request_id' => $result['request_id'] ?? null];
    }
    
    return ['success' => false, 'message' => $result['message'] ?? 'Unknown error', 'request_id' => null];
}
