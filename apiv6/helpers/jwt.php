<?php
/**
 * JWT Helper Functions
 * Simple JWT implementation without external dependencies
 */

require_once __DIR__ . '/../config/jwt.php';

/**
 * Base64 URL encode
 */
function base64url_encode(string $data): string {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

/**
 * Base64 URL decode
 */
function base64url_decode(string $data): string {
    return base64_decode(strtr($data, '-_', '+/'));
}

/**
 * Generate JWT token
 */
function generateJWT(array $payload, int $expiry = null): string {
    $header = [
        'typ' => 'JWT',
        'alg' => JWT_ALGORITHM
    ];
    
    // Only set iat and exp if not already present
    if (!isset($payload['iat'])) {
        $payload['iat'] = time();
    }
    if (!isset($payload['exp'])) {
        $payload['exp'] = time() + ($expiry ?? JWT_ACCESS_EXPIRY);
    }
    $payload['iss'] = JWT_ISSUER;
    
    $headerEncoded = base64url_encode(json_encode($header));
    $payloadEncoded = base64url_encode(json_encode($payload));
    
    $signature = hash_hmac('sha256', "$headerEncoded.$payloadEncoded", JWT_SECRET, true);
    $signatureEncoded = base64url_encode($signature);
    
    return "$headerEncoded.$payloadEncoded.$signatureEncoded";
}

/**
 * Validate and decode JWT token
 */
function validateJWT(string $token): ?array {
    $parts = explode('.', $token);
    if (count($parts) !== 3) {
        return null;
    }
    
    [$headerEncoded, $payloadEncoded, $signatureEncoded] = $parts;
    
    // Verify signature
    $expectedSignature = base64url_encode(
        hash_hmac('sha256', "$headerEncoded.$payloadEncoded", JWT_SECRET, true)
    );
    
    if (!hash_equals($expectedSignature, $signatureEncoded)) {
        return null;
    }
    
    $payload = json_decode(base64url_decode($payloadEncoded), true);
    
    // Check expiration
    if (!isset($payload['exp']) || $payload['exp'] < time()) {
        return null;
    }
    
    return $payload;
}

/**
 * Decode JWT token (alias for validateJWT that throws on error)
 * Used by getReels and other optional auth endpoints
 */
function decodeJWT(string $token): array {
    $result = validateJWT($token);
    if ($result === null) {
        throw new Exception('Invalid or expired token');
    }
    return $result;
}

/**
 * Get user from Authorization header
 */
function getAuthUser(): ?array {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';
    
    if (empty($authHeader) || !preg_match('/Bearer\s+(.+)/', $authHeader, $matches)) {
        return null;
    }
    
    return validateJWT($matches[1]);
}

/**
 * Require authentication
 */
function requireAuth(): array {
    $user = getAuthUser();
    
    if ($user === null) {
        http_response_code(401);
        echo json_encode([
            'success' => false,
            'message' => 'Unauthorized - Invalid or expired token'
        ]);
        exit;
    }
    
    return $user;
}

/**
 * Generate auth tokens for user
 */
function generateAuthTokens(int $userId, string $username, ?string $phone): array {
    $accessPayload = [
        'user_id' => $userId,
        'username' => $username,
        'type' => 'access'
    ];
    
    $refreshPayload = [
        'user_id' => $userId,
        'type' => 'refresh'
    ];
    
    return [
        'access_token' => generateJWT($accessPayload, JWT_ACCESS_EXPIRY),
        'refresh_token' => generateJWT($refreshPayload, JWT_REFRESH_EXPIRY),
        'expires_in' => JWT_ACCESS_EXPIRY,
        'token_type' => 'Bearer',
        'user' => [
            'user_id' => $userId,
            'username' => $username,
            'phone' => $phone
        ]
    ];
}
