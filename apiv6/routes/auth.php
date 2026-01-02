<?php
/**
 * Auth Routes Handler
 * POST /auth/login, POST /auth/refresh, POST /auth/check-phone, etc.
 */

/**
 * Auth routes: POST /auth/login, POST /auth/refresh, POST /auth/check-phone, etc.
 */
function handleAuth(string $method, array $segments): void {
    $action = $segments[0] ?? '';
    
    if ($method !== 'POST') {
        errorResponse('Method not allowed', 405);
    }
    
    switch ($action) {
        case 'login':
            handleLogin();
            break;
            
        case 'refresh':
            handleRefreshToken();
            break;
            
        case 'check-phone':
            handleCheckPhone();
            break;
            
        case 'send-otp':
            handleSendOTP();
            break;
            
        case 'verify-otp':
            handleVerifyOTP();
            break;
            
        case 'reset-password':
            handleResetPassword();
            break;
            
        default:
            errorResponse('Auth endpoint not found', 404);
    }
}

/**
 * Get the next available ID for a table (reuses gaps from deletions)
 * @param PDO $db Database connection
 * @param string $table Table name
 * @param string $idColumn Primary key column name
 * @param int $minId Minimum ID to start from (optional)
 * @return int Next available ID
 */
function getNextAvailableId(PDO $db, string $table, string $idColumn, int $minId = 1): int {
    // Find the first gap in the sequence
    $stmt = $db->prepare("
        SELECT t1.{$idColumn} + 1 AS next_id
        FROM {$table} t1
        LEFT JOIN {$table} t2 ON t1.{$idColumn} + 1 = t2.{$idColumn}
        WHERE t2.{$idColumn} IS NULL 
          AND t1.{$idColumn} >= ?
        ORDER BY t1.{$idColumn}
        LIMIT 1
    ");
    $stmt->execute([$minId]);
    $result = $stmt->fetch();
    
    if ($result && $result['next_id']) {
        return (int)$result['next_id'];
    }
    
    // If no gap found, get max + 1
    $stmt = $db->prepare("SELECT COALESCE(MAX({$idColumn}), ?) AS max_id FROM {$table}");
    $stmt->execute([$minId - 1]);
    $maxResult = $stmt->fetch();
    
    return max($minId, (int)$maxResult['max_id'] + 1);
}

/**
 * Transform shop product array to standard response format
 * Handles both new products (shop_products) and old products (old_products)
 */
function transformShopProduct(array $p): array {
    return [
        'product_id' => (int)$p['product_id'],
        'listing_id' => isset($p['listing_id']) ? (int)$p['listing_id'] : null,
        'product_name' => $p['product_name'],
        'description' => $p['description'] ?? null,
        'category_id' => isset($p['category_id']) ? (int)$p['category_id'] : null,
        'shop_category_id' => isset($p['shop_category_id']) ? (int)$p['shop_category_id'] : null,
        'category_name' => $p['category_name'] ?? null,
        'category_name_mr' => $p['category_name_mr'] ?? null,
        'subcategory_id' => isset($p['subcategory_id']) ? (int)$p['subcategory_id'] : null,
        'subcategory_name' => $p['subcategory_name'] ?? null,
        'price' => (float)$p['price'],
        'discounted_price' => isset($p['discounted_price']) ? (float)$p['discounted_price'] : null,
        'image_url' => $p['image_url'] ?? null,
        'stock_qty' => isset($p['stock_qty']) ? (int)$p['stock_qty'] : null,
        'min_qty' => $p['min_qty'] ?? 1,
        'sell_online' => (bool)($p['sell_online'] ?? false),
        'condition' => $p['condition'] ?? 'new',
        'is_old_product' => (bool)($p['is_old_product'] ?? false),
        'product_source' => $p['product_source'] ?? 'shop',
        'business_name' => $p['business_name'] ?? null,
        'business_phone' => $p['business_phone'] ?? null,
        'city' => $p['city'] ?? null,
        'user_id' => isset($p['user_id']) ? (int)$p['user_id'] : null,
        'created_at' => $p['created_at'] ?? null
    ];
}

/**
 * Check if phone number exists in database
 */
function handleCheckPhone(): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['phone']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $phone = trim($data['phone']);
    
    $db = getDB();
    $stmt = $db->prepare("SELECT user_id, username FROM users WHERE phone = ?");
    $stmt->execute([$phone]);
    $user = $stmt->fetch();
    
    successResponse([
        'exists' => (bool)$user,
        'username' => $user ? $user['username'] : null
    ]);
}

/**
 * Send OTP via SMS Gateway
 * Generates OTP locally, sends to Firebase which triggers Cloud Function
 * to deliver SMS via gateway phone
 */
function handleSendOTP(): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['phone']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $phone = trim($data['phone']);
    $purpose = $data['purpose'] ?? 'signup'; // 'signup' or 'reset_password'
    
    // Generate OTP locally
    $otp = generateOTP();
    $expiresAt = date('Y-m-d H:i:s', strtotime('+' . OTP_EXPIRY_MINUTES . ' minutes'));
    
    $db = getDB();
    
    // Delete any existing OTPs for this phone
    $stmt = $db->prepare("DELETE FROM otp_verifications WHERE phone = ?");
    $stmt->execute([$phone]);
    
    // Store hashed OTP for local verification
    $stmt = $db->prepare("
        INSERT INTO otp_verifications (phone, otp_code, purpose, expires_at, attempts)
        VALUES (?, ?, ?, ?, 0)
    ");
    $stmt->execute([$phone, password_hash($otp, PASSWORD_DEFAULT), $purpose, $expiresAt]);
    
    // Send OTP via SMS Gateway (Firebase -> Cloud Function -> Gateway App -> SMS)
    $result = sendOtpViaGateway($phone, $otp);
    
    if (!$result['success']) {
        errorResponse('Failed to send OTP: ' . $result['message'], 500);
    }
    
    successResponse([
        'message' => 'OTP sent to your phone via SMS',
        'expires_in' => OTP_EXPIRY_MINUTES * 60,
        'otp_length' => OTP_LENGTH,
        'verification_method' => 'sms'
    ]);
}

/**
 * Verify OTP and login/signup (local verification)
 */
function handleVerifyOTP(): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['phone', 'otp']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $phone = trim($data['phone']);
    $otp = trim($data['otp']);
    
    $db = getDB();
    
    // Get verification record
    $stmt = $db->prepare("
        SELECT id, otp_code, purpose, expires_at, attempts 
        FROM otp_verifications 
        WHERE phone = ? AND expires_at > NOW()
        ORDER BY created_at DESC LIMIT 1
    ");
    $stmt->execute([$phone]);
    $otpRecord = $stmt->fetch();
    
    if (!$otpRecord) {
        errorResponse('OTP expired or not found. Please request a new OTP.', 400);
    }
    
    // Check attempts
    if ($otpRecord['attempts'] >= OTP_MAX_ATTEMPTS) {
        $stmt = $db->prepare("DELETE FROM otp_verifications WHERE id = ?");
        $stmt->execute([$otpRecord['id']]);
        errorResponse('Too many attempts. Please request a new OTP.', 429);
    }
    
    // Increment attempts
    $stmt = $db->prepare("UPDATE otp_verifications SET attempts = attempts + 1 WHERE id = ?");
    $stmt->execute([$otpRecord['id']]);
    
    // Verify OTP locally using password_verify
    if (!password_verify($otp, $otpRecord['otp_code'])) {
        $remaining = OTP_MAX_ATTEMPTS - ($otpRecord['attempts'] + 1);
        errorResponse("Invalid OTP. $remaining attempts remaining.", 400);
    }
    
    // OTP verified - delete the record
    $stmt = $db->prepare("DELETE FROM otp_verifications WHERE id = ?");
    $stmt->execute([$otpRecord['id']]);
    
    // Check if this is for password reset
    if ($otpRecord['purpose'] === 'reset_password') {
        // Generate a temporary token for password reset
        $resetToken = bin2hex(random_bytes(32));
        $stmt = $db->prepare("
            UPDATE users SET reset_token = ?, reset_token_expires = DATE_ADD(NOW(), INTERVAL 15 MINUTE)
            WHERE phone = ?
        ");
        $stmt->execute([$resetToken, $phone]);
        
        successResponse([
            'verified' => true,
            'purpose' => 'reset_password',
            'reset_token' => $resetToken
        ]);
        return;
    }
    
    // For signup/login - check if user exists
    $stmt = $db->prepare("SELECT user_id, username, phone, is_active FROM users WHERE phone = ?");
    $stmt->execute([$phone]);
    $user = $stmt->fetch();
    
    if ($user) {
        // Existing user - log them in
        if (!$user['is_active']) {
            errorResponse('Account is deactivated', 403);
        }
        
        $tokens = generateAuthTokens($user['user_id'], $user['username'], $user['phone']);
        
        $stmt = $db->prepare("UPDATE users SET last_active_at = NOW() WHERE user_id = ?");
        $stmt->execute([$user['user_id']]);
        
        successResponse(array_merge($tokens, ['is_new_user' => false]), 'Login successful');
    } else {
        // New user - create account with user-provided details or auto-generate
        $randomSuffix = bin2hex(random_bytes(3)); // 6 character hex string for uniqueness
        
        // Get user-provided signup data (optional - for signup form)
        $username = !empty(trim($data['username'] ?? '')) 
            ? trim($data['username']) 
            : ('User' . substr($phone, -4) . '_' . $randomSuffix);
        
        $email = !empty(trim($data['email'] ?? '')) 
            ? trim($data['email']) 
            : ($phone . '_' . $randomSuffix . '@temp.hellohingoli.com');
        
        $password = $data['password'] ?? '';
        $passwordHash = $password ? password_hash($password, PASSWORD_DEFAULT) : '';
        
        $gender = $data['gender'] ?? null;
        $dateOfBirth = $data['date_of_birth'] ?? null;
        
        // Validate email uniqueness if user-provided
        if (!empty(trim($data['email'] ?? ''))) {
            $stmt = $db->prepare("SELECT user_id FROM users WHERE email = ?");
            $stmt->execute([$email]);
            if ($stmt->fetch()) {
                errorResponse('Email already registered', 400);
            }
        }
        
        // Validate username uniqueness if user-provided
        if (!empty(trim($data['username'] ?? ''))) {
            $stmt = $db->prepare("SELECT user_id FROM users WHERE username = ?");
            $stmt->execute([$username]);
            if ($stmt->fetch()) {
                errorResponse('Username already taken', 400);
            }
        }
        
        // Use phone number as user_id
        $userId = (int)$phone; // Phone number becomes the user ID
        
        $stmt = $db->prepare("
            INSERT INTO users (user_id, phone, username, email, password_hash, gender, date_of_birth, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, 1, NOW())
        ");
        $stmt->execute([$userId, $phone, $username, $email, $passwordHash, $gender, $dateOfBirth]);
        
        $tokens = generateAuthTokens($userId, $username, $phone);
        
        successResponse(array_merge($tokens, ['is_new_user' => true]), 'Account created successfully');
    }
}

/**
 * Reset password after OTP verification
 */
function handleResetPassword(): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['phone', 'reset_token', 'new_password']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $phone = trim($data['phone']);
    $resetToken = $data['reset_token'];
    $newPassword = $data['new_password'];
    
    if (strlen($newPassword) < 6) {
        errorResponse('Password must be at least 6 characters', 400);
    }
    
    $db = getDB();
    
    // Verify reset token
    $stmt = $db->prepare("
        SELECT user_id FROM users 
        WHERE phone = ? AND reset_token = ? AND reset_token_expires > NOW()
    ");
    $stmt->execute([$phone, $resetToken]);
    $user = $stmt->fetch();
    
    if (!$user) {
        errorResponse('Invalid or expired reset token', 400);
    }
    
    // Update password and clear reset token
    $passwordHash = password_hash($newPassword, PASSWORD_DEFAULT);
    $stmt = $db->prepare("
        UPDATE users SET password_hash = ?, reset_token = NULL, reset_token_expires = NULL
        WHERE user_id = ?
    ");
    $stmt->execute([$passwordHash, $user['user_id']]);
    
    successResponse(['success' => true], 'Password reset successfully');
}

/**
 * Handle login
 */
function handleLogin(): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['phone', 'password']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $phone = trim($data['phone']);
    $password = $data['password'];
    
    $db = getDB();
    $stmt = $db->prepare("
        SELECT user_id, username, phone, password_hash, is_active 
        FROM users 
        WHERE phone = ? OR email = ?
    ");
    $stmt->execute([$phone, $phone]);
    $user = $stmt->fetch();
    
    if (!$user) {
        errorResponse('Invalid phone number or password', 401);
    }
    
    if (!$user['is_active']) {
        errorResponse('Account is deactivated', 403);
    }
    
    if (!password_verify($password, $user['password_hash'])) {
        errorResponse('Invalid phone number or password', 401);
    }
    
    // Generate tokens
    $tokens = generateAuthTokens($user['user_id'], $user['username'], $user['phone']);
    
    // Update last active
    $stmt = $db->prepare("UPDATE users SET last_active_at = NOW() WHERE user_id = ?");
    $stmt->execute([$user['user_id']]);
    
    successResponse($tokens, 'Login successful');
}

/**
 * Handle token refresh
 */
function handleRefreshToken(): void {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';
    
    if (empty($authHeader) || !preg_match('/Bearer\s+(.+)/', $authHeader, $matches)) {
        errorResponse('Refresh token required', 401);
    }
    
    $payload = validateJWT($matches[1]);
    
    if (!$payload || ($payload['type'] ?? '') !== 'refresh') {
        errorResponse('Invalid refresh token', 401);
    }
    
    $db = getDB();
    $stmt = $db->prepare("
        SELECT user_id, username, phone, is_active 
        FROM users 
        WHERE user_id = ?
    ");
    $stmt->execute([$payload['user_id']]);
    $user = $stmt->fetch();
    
    if (!$user || !$user['is_active']) {
        errorResponse('User not found or deactivated', 401);
    }
    
    $tokens = generateAuthTokens($user['user_id'], $user['username'], $user['phone']);
    successResponse($tokens, 'Token refreshed');
}
