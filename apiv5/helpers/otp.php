<?php
/**
 * OTP Helper Functions
 * Shared OTP functionality for main app and delivery app
 */

/**
 * Generate a random OTP code
 */
function generateOtpCode(int $length = 6): string {
    return str_pad(random_int(0, pow(10, $length) - 1), $length, '0', STR_PAD_LEFT);
}

/**
 * Store OTP in database
 * @param string $phone Phone number
 * @param string $otp Plain text OTP (will be hashed)
 * @param string $purpose Purpose: 'signup', 'login', 'reset_password'
 * @param int $expiryMinutes Minutes until OTP expires
 */
function storeOtp(string $phone, string $otp, string $purpose = 'signup', int $expiryMinutes = 10): void {
    $db = getDB();
    $expiresAt = date('Y-m-d H:i:s', strtotime("+$expiryMinutes minutes"));
    
    // Delete any existing OTPs for this phone
    $stmt = $db->prepare("DELETE FROM otp_verifications WHERE phone = ?");
    $stmt->execute([$phone]);
    
    // Store hashed OTP
    $stmt = $db->prepare("
        INSERT INTO otp_verifications (phone, otp_code, purpose, expires_at, attempts)
        VALUES (?, ?, ?, ?, 0)
    ");
    $stmt->execute([$phone, password_hash($otp, PASSWORD_DEFAULT), $purpose, $expiresAt]);
}

/**
 * Verify OTP from database
 * @param string $phone Phone number
 * @param string $otp OTP to verify
 * @return array ['valid' => bool, 'error' => string|null, 'purpose' => string|null]
 */
function verifyOtp(string $phone, string $otp): array {
    $db = getDB();
    
    // Get stored OTP
    $stmt = $db->prepare("
        SELECT otp_code, purpose, expires_at, attempts 
        FROM otp_verifications 
        WHERE phone = ?
    ");
    $stmt->execute([$phone]);
    $record = $stmt->fetch();
    
    if (!$record) {
        return ['valid' => false, 'error' => 'No OTP found for this phone', 'purpose' => null];
    }
    
    // Check expiry
    if (strtotime($record['expires_at']) < time()) {
        // Delete expired OTP
        $db->prepare("DELETE FROM otp_verifications WHERE phone = ?")->execute([$phone]);
        return ['valid' => false, 'error' => 'OTP has expired', 'purpose' => null];
    }
    
    // Check attempts
    if ((int)$record['attempts'] >= 5) {
        $db->prepare("DELETE FROM otp_verifications WHERE phone = ?")->execute([$phone]);
        return ['valid' => false, 'error' => 'Too many attempts. Please request a new OTP.', 'purpose' => null];
    }
    
    // Verify OTP
    if (!password_verify($otp, $record['otp_code'])) {
        // Increment attempts
        $db->prepare("UPDATE otp_verifications SET attempts = attempts + 1 WHERE phone = ?")->execute([$phone]);
        return ['valid' => false, 'error' => 'Invalid OTP', 'purpose' => null];
    }
    
    // Delete used OTP
    $db->prepare("DELETE FROM otp_verifications WHERE phone = ?")->execute([$phone]);
    
    return ['valid' => true, 'error' => null, 'purpose' => $record['purpose']];
}

/**
 * Send OTP to a phone number
 * @param string $phone Phone number
 * @param string $purpose Purpose for the OTP
 * @param int $expiryMinutes Expiry time in minutes
 * @return array Response with success status and message
 */
function sendOtpToPhone(string $phone, string $purpose = 'signup', int $expiryMinutes = 10): array {
    // Generate OTP
    $otp = generateOtpCode(defined('OTP_LENGTH') ? OTP_LENGTH : 6);
    
    // Store OTP
    storeOtp($phone, $otp, $purpose, $expiryMinutes);
    
    // Send via SMS Gateway
    $result = sendOtpViaGateway($phone, $otp);
    
    if (!$result['success']) {
        return [
            'success' => false,
            'message' => 'Failed to send OTP: ' . $result['message']
        ];
    }
    
    return [
        'success' => true,
        'message' => 'OTP sent to your phone via SMS',
        'expires_in' => $expiryMinutes * 60,
        'otp_length' => defined('OTP_LENGTH') ? OTP_LENGTH : 6
    ];
}
