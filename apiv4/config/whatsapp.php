<?php
/**
 * WhatsApp Business API Configuration
 * 
 * Get these values from Meta Developer Console:
 * https://developers.facebook.com/apps/{app-id}/whatsapp-business/wa-dev-console
 */

// Meta WhatsApp Business API Credentials
define('WHATSAPP_PHONE_NUMBER_ID', '940457045807126');
define('WHATSAPP_ACCESS_TOKEN', 'EAAKBktKkzIQBQNmKBiQrmRMhrFYl8ogcBBPGhrDmwIhX8mYU2KXOd1Huhv2SDoOybC6ZCZCZCN2lcuONWhKbuVxmlVJeHtCVFfLGbS8d4bOURAYzAXwaaaOdrmphn7yGeJx0JnOmjhZCrZBFJI0Wk6EncqlccNWXNxGUPWEmC4C4jdMiiHOwfXjk5siUSVrK9MEC6sVpwMrQuuhDopJWDdOEoikqG3oaBb9BnRia4hWhjcUPq1VDvRdoMzPmqn9kil6sZCJV8cKIzOYB8lWRIvjwZDZD');
define('WHATSAPP_BUSINESS_ACCOUNT_ID', '858039156769067');

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
 * Send OTP via WhatsApp using Meta Business API
 * 
 * @param string $phone Phone number with country code (e.g., 919876543210)
 * @param string $otp The OTP code to send
 * @return array ['success' => bool, 'message' => string, 'message_id' => string|null]
 */
function sendWhatsAppOTP(string $phone, string $otp): array {
    $url = 'https://graph.facebook.com/v18.0/' . WHATSAPP_PHONE_NUMBER_ID . '/messages';
    
    // Format phone number (remove + if present)
    $phone = ltrim($phone, '+');
    
    // Send as plain text message (no template approval needed)
    $payload = [
        'messaging_product' => 'whatsapp',
        'recipient_type' => 'individual',
        'to' => $phone,
        'type' => 'text',
        'text' => [
            'preview_url' => false,
            'body' => "Your Hello Hingoli verification code is: *$otp*\n\nThis code expires in " . OTP_EXPIRY_MINUTES . " minutes. Do not share this code with anyone."
        ]
    ];
    
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Authorization: Bearer ' . WHATSAPP_ACCESS_TOKEN
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($error) {
        error_log("WhatsApp API cURL error: $error");
        return ['success' => false, 'message' => 'Failed to send OTP', 'message_id' => null];
    }
    
    $result = json_decode($response, true);
    
    if ($httpCode >= 200 && $httpCode < 300 && isset($result['messages'][0]['id'])) {
        return [
            'success' => true,
            'message' => 'OTP sent successfully',
            'message_id' => $result['messages'][0]['id']
        ];
    }
    
    $errorMsg = $result['error']['message'] ?? 'Unknown error';
    error_log("WhatsApp API error: " . json_encode($result));
    return ['success' => false, 'message' => $errorMsg, 'message_id' => null];
}
