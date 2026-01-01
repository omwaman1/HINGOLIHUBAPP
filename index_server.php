<?php
/**
 * Hello Hingoli API - Main Router
 * Upload this entire 'api' folder to your Hostinger public_html directory
 */

// Error reporting (disable in production)
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);

// Include required files
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/config/jwt.php';
require_once __DIR__ . '/config/sms.php';
require_once __DIR__ . '/helpers/response.php';
require_once __DIR__ . '/helpers/jwt.php';
require_once __DIR__ . '/helpers/transformers.php';
require_once __DIR__ . '/helpers/query_builder.php';
require_once __DIR__ . '/helpers/categories.php';
require_once __DIR__ . '/helpers/otp.php';
require_once __DIR__ . '/helpers/reviews.php';

// Set headers
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: ' . CORS_ORIGIN);
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Get request info
$requestMethod = $_SERVER['REQUEST_METHOD'];
$requestUri = $_SERVER['REQUEST_URI'];

// Remove query string and base path
$path = parse_url($requestUri, PHP_URL_PATH);

// Remove /apiv4 or /api prefix if present (adjust based on your hosting setup)
$path = preg_replace('#^/apiv4#', '', $path);
$path = preg_replace('#^/api#', '', $path);
$path = trim($path, '/');
$segments = $path ? explode('/', $path) : [];

// Route the request
try {
    routeRequest($requestMethod, $segments);
} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    // Temporarily show actual error for debugging
    errorResponse('Database error: ' . $e->getMessage(), 500);
} catch (Exception $e) {
    error_log("Error: " . $e->getMessage());
    errorResponse('Error: ' . $e->getMessage(), 500);
}

/**
 * Main router function
 */
function routeRequest(string $method, array $segments): void {
    $resource = $segments[0] ?? '';
    
    switch ($resource) {
        case '':
            successResponse(['version' => '1.0.0', 'name' => 'Hello Hingoli API']);
            break;
            
        case 'auth':
            handleAuth($method, array_slice($segments, 1));
            break;
            
        case 'categories':
            handleCategories($method, array_slice($segments, 1));
            break;
            
        case 'listings':
            handleListings($method, array_slice($segments, 1));
            break;
            
        case 'cities':
            handleCities($method, array_slice($segments, 1));
            break;
            
        case 'user':
            handleUser($method, array_slice($segments, 1));
            break;
            
        case 'notifications':
            handleNotifications($method, array_slice($segments, 1));
            break;
            
        case 'banners':
            handleBanners($method, array_slice($segments, 1));
            break;
            
        case 'cart':
            handleCart($method, array_slice($segments, 1));
            break;
            
        case 'addresses':
            handleAddresses($method, array_slice($segments, 1));
            break;
            
        case 'orders':
            handleOrders($method, array_slice($segments, 1));
            break;
            
        case 'delivery':
            handleDelivery($method, array_slice($segments, 1));
            break;
            
        case 'enquiries':
            handleEnquiries($method, array_slice($segments, 1));
            break;
            
        case 'products':
            handleProducts($method, array_slice($segments, 1));
            break;
            
        case 'shop-categories':
            handleShopCategories($method, array_slice($segments, 1));
            break;
            
        case 'old-categories':
            handleOldCategories($method, array_slice($segments, 1));
            break;
            
        case 'old-products':
            handleOldProducts($method, array_slice($segments, 1));
            break;
            
        case 'webhook':
            handleWebhook($method, array_slice($segments, 1));
            break;
            
        case 'prefetch':
            handlePrefetch($method);
            break;
            
        case 'debug':
            handleDebug();
            break;
            
        case 'app-config':
            handleAppConfig($method, array_slice($segments, 1));
            break;
            
        default:
            errorResponse('Endpoint not found', 404);
    }
}

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

/**
 * Categories routes
 */
function handleCategories(string $method, array $segments): void {
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
    }
    
    $categoryId = $segments[0] ?? null;
    $subResource = $segments[1] ?? null;
    
    if ($categoryId === null) {
        // GET /categories
        getCategories();
    } elseif ($subResource === 'subcategories') {
        // GET /categories/{id}/subcategories
        getSubcategories((int)$categoryId);
    } else {
        // GET /categories/{id}
        getCategoryById((int)$categoryId);
    }
}

/**
 * Get categories list
 * Now uses transformCategory from transformers.php
 */
function getCategories(): void {
    $type = getQueryParam('type');
    $parentId = getQueryParam('parent_id');
    
    $db = getDB();
    $sql = "SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth, 
                   icon_url, image_url, description, listing_count
            FROM categories 
            WHERE is_active = 1";
    $params = [];
    
    if ($type) {
        $sql .= " AND listing_type = ?";
        $params[] = $type;
    }
    
    if ($parentId !== null) {
        if ($parentId === '0' || $parentId === '') {
            $sql .= " AND parent_id IS NULL";
        } else {
            $sql .= " AND parent_id = ?";
            $params[] = (int)$parentId;
        }
    } else {
        // By default, return only top-level categories
        $sql .= " AND parent_id IS NULL";
    }
    
    $sql .= " ORDER BY sort_order, name";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $categories = $stmt->fetchAll();
    
    // Use transformer for consistent response format
    successResponse(array_map('transformCategory', $categories));
}

/**
 * Get category by ID
 * Now uses transformCategory from transformers.php
 */
function getCategoryById(int $categoryId): void {
    $db = getDB();
    $stmt = $db->prepare("
        SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth,
               icon_url, image_url, description, listing_count
        FROM categories 
        WHERE category_id = ? AND is_active = 1
    ");
    $stmt->execute([$categoryId]);
    $category = $stmt->fetch();
    
    if (!$category) {
        errorResponse('Category not found', 404);
    }
    
    successResponse(transformCategory($category));
}

/**
 * Get subcategories
 * Now uses transformCategory from transformers.php
 */
function getSubcategories(int $parentId): void {
    $db = getDB();
    $stmt = $db->prepare("
        SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth,
               icon_url, image_url, description, listing_count
        FROM categories 
        WHERE parent_id = ? AND is_active = 1
        ORDER BY sort_order, name
    ");
    $stmt->execute([$parentId]);
    $categories = $stmt->fetchAll();
    
    successResponse(array_map('transformCategory', $categories));
}

/**
 * Shop Categories routes: GET /shop-categories
 * Dedicated category system for e-commerce/shop section
 * Now uses unified category helpers
 */
function handleShopCategories(string $method, array $segments): void {
    handleCategoriesForTable($method, $segments, 'shop_categories', 'getShopCategoryProducts');
}

// getShopCategories removed - now handled by getCategoriesFromTable('shop_categories')

// getShopCategoryById removed - now handled by getCategoryByIdFromTable('shop_categories', $id)
// getShopSubcategories removed - now handled by getSubcategoriesFromTable('shop_categories', $parentId)

/**
 * Get products in a shop category
 */
function getShopCategoryProducts(int $categoryId): void {
    $db = getDB();
    $page = max(1, (int)(getQueryParam('page') ?? 1));
    $limit = min(50, max(10, (int)(getQueryParam('limit') ?? 20)));
    $offset = ($page - 1) * $limit;
    
    $stmt = $db->prepare("SELECT id FROM shop_categories WHERE (id = ? OR parent_id = ?) AND is_active = 1");
    $stmt->execute([$categoryId, $categoryId]);
    $categoryIds = array_column($stmt->fetchAll(), 'id');
    
    if (empty($categoryIds)) {
        errorResponse('Category not found', 404);
    }
    
    $placeholders = str_repeat('?,', count($categoryIds) - 1) . '?';
    $stmt = $db->prepare("
        SELECT sp.product_id, sp.product_name, sp.description, 
               sp.price, sp.discounted_price, sp.image_url,
               sp.stock_qty, sp.sell_online, sp.condition,
               sc.name as category_name, l.title as shop_name
        FROM shop_products sp
        LEFT JOIN shop_categories sc ON sp.shop_category_id = sc.id
        LEFT JOIN listings l ON sp.listing_id = l.listing_id
        WHERE sp.shop_category_id IN ($placeholders) AND sp.is_active = 1
        ORDER BY sp.sort_order, sp.product_name
        LIMIT ? OFFSET ?
    ");
    $params = array_merge($categoryIds, [$limit, $offset]);
    $stmt->execute($params);
    $products = $stmt->fetchAll();
    
    $stmt = $db->prepare("SELECT COUNT(*) as total FROM shop_products WHERE shop_category_id IN ($placeholders) AND is_active = 1");
    $stmt->execute($categoryIds);
    $total = (int)$stmt->fetch()['total'];
    
    $products = array_map(function($p) {
        return [
            'product_id' => (int)$p['product_id'],
            'product_name' => $p['product_name'],
            'description' => $p['description'],
            'price' => (float)$p['price'],
            'discounted_price' => $p['discounted_price'] ? (float)$p['discounted_price'] : null,
            'image_url' => $p['image_url'],
            'stock_qty' => $p['stock_qty'] ? (int)$p['stock_qty'] : null,
            'sell_online' => (bool)$p['sell_online'],
            'condition' => $p['condition'],
            'category_name' => $p['category_name'],
            'shop_name' => $p['shop_name']
        ];
    }, $products);
    
    successResponse([
        'products' => $products,
        'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total, 'total_pages' => ceil($total / $limit)]
    ]);
}

// =============================================================================
// OLD CATEGORIES (for used/second-hand items)
// =============================================================================

/**
 * Old Categories routes: GET /old-categories
 * Now uses unified category helpers
 */
function handleOldCategories(string $method, array $segments): void {
    handleCategoriesForTable($method, $segments, 'old_categories');
}

// getOldCategories removed - now handled by getCategoriesFromTable('old_categories')

// getOldCategoryById removed - now handled by getCategoryByIdFromTable('old_categories', $id)
// getOldSubcategories removed - now handled by getSubcategoriesFromTable('old_categories', $parentId)

// =============================================================================
// OLD PRODUCTS (used/second-hand items sold by individuals)
// =============================================================================

/**
 * Old Products routes: GET/POST/PUT/DELETE /old-products
 * Also supports /old-products/{id}/reviews for reviews
 */
function handleOldProducts(string $method, array $segments): void {
    $productId = $segments[0] ?? null;
    $subResource = $segments[1] ?? null;
    
    // Handle sub-resources (reviews)
    if ($productId !== null && $subResource !== null) {
        if ($subResource === 'reviews') {
            switch ($method) {
                case 'GET':
                    getReviewsFor('old_product', (int)$productId);
                    break;
                case 'POST':
                    addReviewFor('old_product', (int)$productId);
                    break;
                default:
                    errorResponse('Method not allowed', 405);
            }
            return;
        }
        errorResponse('Sub-resource not found', 404);
    }
    
    switch ($method) {
        case 'GET':
            if ($productId === null) {
                getOldProducts();
            } else {
                getOldProductById((int)$productId);
            }
            break;
        case 'POST':
            requireAuth();
            createOldProduct();
            break;
        case 'PUT':
            requireAuth();
            if ($productId === null) {
                errorResponse('Product ID required', 400);
            }
            updateOldProduct((int)$productId);
            break;
        case 'DELETE':
            requireAuth();
            if ($productId === null) {
                errorResponse('Product ID required', 400);
            }
            deleteOldProduct((int)$productId);
            break;
        default:
            errorResponse('Method not allowed', 405);
    }
}

/**
 * Get old products list with filters
 * Reads from old_products table (C2C marketplace for used items)
 */
function getOldProducts(): void {
    $db = getDB();
    
    // Use standardized pagination params
    list($page, $perPage) = getPaginationParams();
    $offset = ($page - 1) * $perPage;
    
    $categoryId = getQueryParam('category_id');
    $city = getQueryParam('city');
    $search = getQueryParam('search');
    $userId = getQueryParam('user_id');
    $minPrice = getQueryParam('min_price');
    $maxPrice = getQueryParam('max_price');
    
    // Read from old_products table (for used/second-hand items)
    $sql = "SELECT p.product_id, p.user_id, p.product_name, p.description, 
                   p.price, p.original_price, p.image_url, p.condition,
                   p.old_category_id, p.age_months, p.has_warranty, p.warranty_months,
                   p.has_bill, p.reason_for_selling, p.brand, p.model,
                   p.city, p.pincode, p.show_phone, p.accept_offers,
                   p.status, p.view_count, p.inquiry_count,
                   p.created_at, p.updated_at,
                   u.username as seller_name, u.phone as seller_phone, u.avatar_url as seller_avatar,
                   c.name as category_name, c.name_mr as category_name_mr
            FROM old_products p
            LEFT JOIN users u ON p.user_id = u.user_id
            LEFT JOIN old_categories c ON p.old_category_id = c.id
            WHERE p.status = 'active'";
    $countSql = "SELECT COUNT(*) as total FROM old_products p WHERE p.status = 'active'";
    $params = [];
    $countParams = [];
    
    if ($categoryId) {
        // Check if this is a subcategory (has a parent_id) or main category
        $catStmt = $db->prepare("SELECT parent_id FROM old_categories WHERE id = ?");
        $catStmt->execute([(int)$categoryId]);
        $catRow = $catStmt->fetch();
        
        if ($catRow && $catRow['parent_id'] !== null) {
            // This is a subcategory - filter by subcategory_id
            $sql .= " AND p.subcategory_id = ?";
            $countSql .= " AND p.subcategory_id = ?";
        } else {
            // This is a main category - filter by old_category_id
            $sql .= " AND p.old_category_id = ?";
            $countSql .= " AND p.old_category_id = ?";
        }
        $params[] = (int)$categoryId;
        $countParams[] = (int)$categoryId;
    }
    
    if ($city) {
        $sql .= " AND p.city = ?";
        $countSql .= " AND p.city = ?";
        $params[] = $city;
        $countParams[] = $city;
    }
    
    if ($search) {
        $sql .= " AND (p.product_name LIKE ? OR p.description LIKE ? OR p.brand LIKE ? OR p.model LIKE ?)";
        $countSql .= " AND (p.product_name LIKE ? OR p.description LIKE ? OR p.brand LIKE ? OR p.model LIKE ?)";
        $searchTerm = "%$search%";
        $params[] = $searchTerm;
        $params[] = $searchTerm;
        $params[] = $searchTerm;
        $params[] = $searchTerm;
        $countParams[] = $searchTerm;
        $countParams[] = $searchTerm;
        $countParams[] = $searchTerm;
        $countParams[] = $searchTerm;
    }
    
    if ($userId) {
        $sql .= " AND p.user_id = ?";
        $countSql .= " AND p.user_id = ?";
        $params[] = (int)$userId;
        $countParams[] = (int)$userId;
    }
    
    if ($minPrice) {
        $sql .= " AND p.price >= ?";
        $countSql .= " AND p.price >= ?";
        $params[] = (float)$minPrice;
        $countParams[] = (float)$minPrice;
    }
    
    if ($maxPrice) {
        $sql .= " AND p.price <= ?";
        $countSql .= " AND p.price <= ?";
        $params[] = (float)$maxPrice;
        $countParams[] = (float)$maxPrice;
    }
    
    $sql .= " ORDER BY p.created_at DESC LIMIT $perPage OFFSET $offset";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $products = $stmt->fetchAll();
    
    $stmt = $db->prepare($countSql);
    $stmt->execute($countParams);
    $total = (int)$stmt->fetch()['total'];
    
    // Transform products to match OldProduct model expected by app
    $transformedProducts = array_map(function($p) {
        return [
            'product_id' => (int)$p['product_id'],
            'user_id' => (int)$p['user_id'],
            'product_name' => $p['product_name'],
            'description' => $p['description'],
            'price' => (float)$p['price'],
            'original_price' => $p['original_price'] ? (float)$p['original_price'] : null,
            'image_url' => $p['image_url'],
            'condition' => $p['condition'] ?? 'good',
            'age_months' => $p['age_months'] ? (int)$p['age_months'] : null,
            'has_warranty' => (bool)$p['has_warranty'],
            'warranty_months' => $p['warranty_months'] ? (int)$p['warranty_months'] : null,
            'has_bill' => (bool)$p['has_bill'],
            'reason_for_selling' => $p['reason_for_selling'],
            'brand' => $p['brand'],
            'model' => $p['model'],
            'city' => $p['city'],
            'pincode' => $p['pincode'],
            'show_phone' => (bool)$p['show_phone'],
            'accept_offers' => (bool)$p['accept_offers'],
            'status' => $p['status'],
            'view_count' => (int)$p['view_count'],
            'inquiry_count' => (int)$p['inquiry_count'],
            'created_at' => $p['created_at'],
            'updated_at' => $p['updated_at'],
            'seller_name' => $p['seller_name'],
            'seller_phone' => $p['seller_phone'],
            'seller' => [
                'name' => $p['seller_name'],
                'phone' => $p['show_phone'] ? $p['seller_phone'] : null,
                'avatar_url' => $p['seller_avatar']
            ],
            'category_name' => $p['category_name'],
            'category_name_mr' => $p['category_name_mr']
        ];
    }, $products);
    
    successResponse([
        'products' => $transformedProducts,
        'pagination' => [
            'page' => $page, 
            'per_page' => $perPage, 
            'total' => $total, 
            'total_pages' => (int)ceil($total / $perPage)
        ]
    ]);
}

/**
 * Get single old product by ID
 */
function getOldProductById(int $productId): void {
    $db = getDB();
    
    // Increment view count
    $db->prepare("UPDATE old_products SET view_count = view_count + 1 WHERE product_id = ?")->execute([$productId]);
    
    $stmt = $db->prepare("
        SELECT p.*, u.username as seller_name, u.phone as seller_phone, u.avatar_url as seller_avatar,
               c.name as category_name, c.name_mr as category_name_mr
        FROM old_products p
        LEFT JOIN users u ON p.user_id = u.user_id
        LEFT JOIN old_categories c ON p.old_category_id = c.id
        WHERE p.product_id = ?
    ");
    $stmt->execute([$productId]);
    $p = $stmt->fetch();
    
    if (!$p) {
        errorResponse('Product not found', 404);
    }
    
    $product = [
        'product_id' => (int)$p['product_id'],
        'user_id' => (int)$p['user_id'],
        'product_name' => $p['product_name'],
        'description' => $p['description'],
        'category_id' => $p['old_category_id'] ? (int)$p['old_category_id'] : null,
        'subcategory_id' => $p['subcategory_id'] ? (int)$p['subcategory_id'] : null,
        'price' => (float)$p['price'],
        'original_price' => $p['original_price'] ? (float)$p['original_price'] : null,
        'image_url' => $p['image_url'],
        'additional_images' => $p['additional_images'] ? json_decode($p['additional_images'], true) : [],
        'condition' => $p['condition'],
        'age_months' => $p['age_months'] ? (int)$p['age_months'] : null,
        'has_warranty' => (bool)$p['has_warranty'],
        'warranty_months' => $p['warranty_months'] ? (int)$p['warranty_months'] : null,
        'has_bill' => (bool)$p['has_bill'],
        'reason_for_selling' => $p['reason_for_selling'],
        'brand' => $p['brand'],
        'model' => $p['model'],
        'city' => $p['city'],
        'pincode' => $p['pincode'],
        'show_phone' => (bool)$p['show_phone'],
        'accept_offers' => (bool)$p['accept_offers'],
        'status' => $p['status'],
        'view_count' => (int)$p['view_count'],
        'inquiry_count' => (int)$p['inquiry_count'],
        'category_name' => $p['category_name'],
        'category_name_mr' => $p['category_name_mr'],
        'seller' => [
            'name' => $p['seller_name'],
            'phone' => $p['show_phone'] ? $p['seller_phone'] : null,
            'avatar_url' => $p['seller_avatar']
        ],
        'created_at' => $p['created_at']
    ];
    
    successResponse($product);
}

/**
 * Create new old product listing
 */
function createOldProduct(): void {
    $userId = getCurrentUserId();
    $db = getDB();
    
    // Handle multipart form data or JSON
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if (strpos($contentType, 'multipart/form-data') !== false) {
        $data = $_POST;
    } else {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
    }
    
    // Validation
    $productName = trim($data['product_name'] ?? '');
    $price = (float)($data['price'] ?? 0);
    $categoryId = (int)($data['old_category_id'] ?? 0);
    
    if (empty($productName)) {
        errorResponse('Product name is required', 400);
    }
    if ($price <= 0) {
        errorResponse('Valid price is required', 400);
    }
    if ($categoryId <= 0) {
        errorResponse('Category is required', 400);
    }
    
    // Handle image upload
    $imageUrl = null;
    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $imageUrl = uploadImage($_FILES['image'], 'old_products');
    }
    
    $subcategoryId = isset($data['subcategory_id']) && $data['subcategory_id'] > 0 
        ? (int)$data['subcategory_id'] 
        : null;
    
    $stmt = $db->prepare("
        INSERT INTO old_products (
            user_id, product_name, description, old_category_id, subcategory_id, price, original_price,
            image_url, `condition`, age_months, has_warranty, warranty_months, has_bill,
            reason_for_selling, brand, model, city, pincode, show_phone, accept_offers
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ");
    
    $stmt->execute([
        $userId,
        $productName,
        $data['description'] ?? null,
        $categoryId,
        $subcategoryId,
        $price,
        $data['original_price'] ?? null,
        $imageUrl,
        $data['condition'] ?? 'good',
        $data['age_months'] ?? null,
        $data['has_warranty'] ?? 0,
        $data['warranty_months'] ?? null,
        $data['has_bill'] ?? 0,
        $data['reason_for_selling'] ?? null,
        $data['brand'] ?? null,
        $data['model'] ?? null,
        $data['city'] ?? 'Hingoli',
        $data['pincode'] ?? null,
        $data['show_phone'] ?? 1,
        $data['accept_offers'] ?? 1
    ]);
    
    $productId = $db->lastInsertId();
    
    // Update category product count
    $db->prepare("UPDATE old_categories SET product_count = product_count + 1 WHERE id = ?")->execute([$categoryId]);
    
    successResponse(['product_id' => (int)$productId, 'message' => 'Product listed successfully'], 201);
}

/**
 * Update old product
 */
function updateOldProduct(int $productId): void {
    $userId = getCurrentUserId();
    $db = getDB();
    
    // Check ownership
    $stmt = $db->prepare("SELECT user_id FROM old_products WHERE product_id = ?");
    $stmt->execute([$productId]);
    $product = $stmt->fetch();
    
    if (!$product) {
        errorResponse('Product not found', 404);
    }
    if ((int)$product['user_id'] !== $userId) {
        errorResponse('Not authorized to edit this product', 403);
    }
    
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if (strpos($contentType, 'multipart/form-data') !== false) {
        $data = $_POST;
    } else {
        $data = json_decode(file_get_contents('php://input'), true) ?? [];
    }
    
    $updates = [];
    $params = [];
    
    $allowedFields = ['product_name', 'description', 'price', 'original_price', 'condition',
                      'age_months', 'has_warranty', 'warranty_months', 'has_bill', 
                      'reason_for_selling', 'brand', 'model', 'city', 'pincode',
                      'show_phone', 'accept_offers', 'status'];
    
    foreach ($allowedFields as $field) {
        if (isset($data[$field])) {
            $updates[] = "`$field` = ?";
            $params[] = $data[$field];
        }
    }
    
    // Handle image upload
    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $imageUrl = uploadImage($_FILES['image'], 'old_products');
        $updates[] = "image_url = ?";
        $params[] = $imageUrl;
    }
    
    if (empty($updates)) {
        errorResponse('No fields to update', 400);
    }
    
    // Check auto-moderation setting for products
    // If OFF, set status to 'pending' when user edits (requires admin re-approval)
    $stmt = $db->prepare("SELECT setting_value FROM settings WHERE setting_key = 'auto_moderation_products'");
    $stmt->execute();
    $autoModProducts = $stmt->fetchColumn() === 'true';
    
    if (!$autoModProducts) {
        // Override status to 'pending' - requires admin approval
        $updates[] = "`status` = ?";
        $params[] = 'pending';
    }
    
    $params[] = $productId;
    $sql = "UPDATE old_products SET " . implode(', ', $updates) . " WHERE product_id = ?";
    $db->prepare($sql)->execute($params);
    
    $message = $autoModProducts 
        ? 'Product updated successfully' 
        : 'Product updated! Changes are pending admin approval.';
    
    successResponse(['message' => $message, 'status' => $autoModProducts ? 'active' : 'pending']);
}

/**
 * Delete old product
 */
function deleteOldProduct(int $productId): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    $db = getDB();
    
    $stmt = $db->prepare("SELECT user_id, old_category_id FROM old_products WHERE product_id = ?");
    $stmt->execute([$productId]);
    $product = $stmt->fetch();
    
    if (!$product) {
        errorResponse('Product not found', 404);
    }
    if ((int)$product['user_id'] !== $userId) {
        errorResponse('Not authorized to delete this product', 403);
    }
    
    // Soft delete
    $db->prepare("UPDATE old_products SET status = 'deleted' WHERE product_id = ?")->execute([$productId]);
    
    // Update category count (only if count > 0 to avoid UNSIGNED underflow)
    if ($product['old_category_id']) {
        $db->prepare("UPDATE old_categories SET product_count = product_count - 1 WHERE id = ? AND product_count > 0")->execute([$product['old_category_id']]);
    }
    
    successResponse(['message' => 'Product deleted successfully']);
}

/**
 * Cities routes: GET /cities
 */
function handleCities(string $method, array $segments): void {
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
    }
    
    getCities();
}

/**
 * Get cities list
 */
function getCities(): void {
    $stateId = getQueryParam('state_id');
    
    $db = getDB();
    $sql = "SELECT city_id, state_id, name, name_mr, slug, is_popular, listing_count
            FROM cities 
            WHERE is_active = 1";
    $params = [];
    
    if ($stateId) {
        $sql .= " AND state_id = ?";
        $params[] = (int)$stateId;
    }
    
    $sql .= " ORDER BY is_popular DESC, sort_order, name";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $cities = $stmt->fetchAll();
    
    // Convert to proper types
    $cities = array_map(function($city) {
        return [
            'city_id' => (int)$city['city_id'],
            'state_id' => (int)$city['state_id'],
            'name' => $city['name'],
            'name_mr' => $city['name_mr'],
            'slug' => $city['slug'],
            'is_popular' => (bool)$city['is_popular'],
            'listing_count' => (int)$city['listing_count']
        ];
    }, $cities);
    
    successResponse($cities);
}

/**
 * User routes: GET /user/listings
 */
function handleUser(string $method, array $segments): void {
    $action = $segments[0] ?? '';
    
    // Require authentication for user routes (returns array with user_id)
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    switch ($action) {
        case 'profile':
            if ($method === 'GET') {
                getUserProfile($userId);
            } elseif ($method === 'PUT') {
                updateUserProfile($userId);
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        case 'listings':
            if ($method === 'GET') {
                getUserListings($userId);
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        default:
            errorResponse('User endpoint not found', 404);
    }
}

/**
 * Get authenticated user's listings (including products)
 * Supports ?type= filter: all, services, business, jobs, selling
 */
function getUserListings(int $userId): void {
    $db = getDB();
    $typeFilter = $_GET['type'] ?? 'all';
    
    $results = [];
    
    // Fetch regular listings (services, business, jobs)
    if ($typeFilter === 'all' || in_array($typeFilter, ['services', 'business', 'jobs'])) {
        $sql = "
            SELECT l.listing_id, l.listing_type, l.title, l.description,
                   l.category_id, l.subcategory_id,
                   l.location, l.city, l.state, l.main_image_url, l.user_id,
                   l.status, l.is_verified, l.is_featured, l.view_count,
                   l.review_count, l.avg_rating, l.created_at,
                   c.name as category_name
            FROM listings l
            LEFT JOIN categories c ON l.category_id = c.category_id
            WHERE l.user_id = ?
        ";
        $params = [$userId];
        
        // Apply type filter if not 'all'
        if ($typeFilter !== 'all') {
            $sql .= " AND l.listing_type = ?";
            $params[] = $typeFilter;
        } else {
            // Exclude 'selling' type as those are now in shop_products
            $sql .= " AND l.listing_type IN ('services', 'business', 'jobs')";
        }
        
        $sql .= " ORDER BY l.created_at DESC";
        
        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        $listings = $stmt->fetchAll();
        
        foreach ($listings as $l) {
            $results[] = [
                'listing_id' => (int)$l['listing_id'],
                'listing_type' => $l['listing_type'],
                'title' => $l['title'],
                'description' => $l['description'],
                'price' => null,
                'category_id' => (int)$l['category_id'],
                'subcategory_id' => $l['subcategory_id'] ? (int)$l['subcategory_id'] : null,
                'category_name' => $l['category_name'],
                'location' => $l['location'],
                'city' => $l['city'],
                'state' => $l['state'],
                'main_image_url' => $l['main_image_url'],
                'user_id' => (int)$l['user_id'],
                'is_verified' => (bool)$l['is_verified'],
                'is_featured' => (bool)$l['is_featured'],
                'view_count' => (int)$l['view_count'],
                'review_count' => (int)$l['review_count'],
                'avg_rating' => (float)$l['avg_rating'],
                'status' => $l['status'],
                'created_at' => $l['created_at']
            ];
        }
    }
    
    // Fetch products from shop_products (through user's business listings)
    if ($typeFilter === 'all' || $typeFilter === 'selling') {
        $stmt = $db->prepare("
            SELECT sp.product_id as listing_id, 'selling' as listing_type, 
                   sp.product_name as title, sp.description,
                   sp.price, sp.category_id, sp.subcategory_id,
                   c.name as category_name, sp.image_url as main_image_url,
                   sp.is_active, sp.`condition`, sp.created_at,
                   l.user_id, l.city, l.state, l.location
            FROM shop_products sp
            INNER JOIN listings l ON sp.listing_id = l.listing_id
            LEFT JOIN categories c ON sp.category_id = c.category_id
            WHERE l.user_id = ? AND l.listing_type = 'business'
            ORDER BY sp.created_at DESC
        ");
        $stmt->execute([$userId]);
        $products = $stmt->fetchAll();
        
        foreach ($products as $p) {
            $results[] = [
                'listing_id' => (int)$p['listing_id'],
                'listing_type' => 'selling',
                'title' => $p['title'],
                'description' => $p['description'],
                'price' => $p['price'] ? (float)$p['price'] : null,
                'category_id' => (int)$p['category_id'],
                'subcategory_id' => $p['subcategory_id'] ? (int)$p['subcategory_id'] : null,
                'category_name' => $p['category_name'],
                'location' => $p['location'],
                'city' => $p['city'],
                'state' => $p['state'],
                'main_image_url' => $p['main_image_url'],
                'user_id' => (int)$p['user_id'],
                'is_verified' => false,
                'is_featured' => false,
                'view_count' => 0,
                'review_count' => 0,
                'avg_rating' => 0.0,
                'status' => $p['is_active'] ? 'active' : 'inactive',
                'created_at' => $p['created_at'],
                'condition' => $p['condition'] ?? 'new'
            ];
        }
    }
    
    // Fetch OLD products from old_products table (C2C marketplace for used items)
    if ($typeFilter === 'all' || $typeFilter === 'selling') {
        $stmt = $db->prepare("
            SELECT op.product_id as listing_id, 'selling' as listing_type, 
                   op.product_name as title, op.description,
                   op.price, op.old_category_id as category_id, NULL as subcategory_id,
                   oc.name as category_name, op.image_url as main_image_url,
                   op.status, op.created_at,
                   op.user_id, op.city, NULL as state, op.city as location
            FROM old_products op
            LEFT JOIN old_categories oc ON op.old_category_id = oc.id
            WHERE op.user_id = ? AND op.status != 'deleted'
            ORDER BY op.created_at DESC
        ");
        $stmt->execute([$userId]);
        $oldProducts = $stmt->fetchAll();
        
        foreach ($oldProducts as $p) {
            $results[] = [
                'listing_id' => (int)$p['listing_id'],
                'listing_type' => 'selling',
                'title' => $p['title'],
                'description' => $p['description'],
                'price' => $p['price'] ? (float)$p['price'] : null,
                'category_id' => (int)$p['category_id'],
                'subcategory_id' => null,
                'category_name' => $p['category_name'],
                'location' => $p['location'],
                'city' => $p['city'],
                'state' => $p['state'],
                'main_image_url' => $p['main_image_url'],
                'user_id' => (int)$p['user_id'],
                'is_verified' => false,
                'is_featured' => false,
                'view_count' => 0,
                'review_count' => 0,
                'avg_rating' => 0.0,
                'status' => $p['status'],
                'created_at' => $p['created_at'],
                'condition' => 'old'  // Always 'old' for old_products table
            ];
        }
    }
    
    // Sort all results by created_at descending
    usort($results, function($a, $b) {
        return strtotime($b['created_at']) - strtotime($a['created_at']);
    });
    
    successResponse($results);
}

/**
 * Get authenticated user's profile
 */
function getUserProfile(int $userId): void {
    $db = getDB();
    
    $stmt = $db->prepare("
        SELECT user_id, username, phone, email, avatar_url, gender, date_of_birth,
               is_verified, listing_count, avg_rating, created_at
        FROM users 
        WHERE user_id = ?
    ");
    $stmt->execute([$userId]);
    $user = $stmt->fetch();
    
    if (!$user) {
        errorResponse('User not found', 404);
    }
    
    $profile = [
        'user_id' => (int)$user['user_id'],
        'username' => $user['username'],
        'phone' => $user['phone'],
        'email' => $user['email'] ?? null,
        'avatar_url' => $user['avatar_url'] ?? null,
        'gender' => $user['gender'] ?? null,
        'date_of_birth' => $user['date_of_birth'] ?? null,
        'is_verified' => (bool)($user['is_verified'] ?? false),
        'listing_count' => (int)($user['listing_count'] ?? 0),
        'avg_rating' => (float)($user['avg_rating'] ?? 0),
        'created_at' => $user['created_at']
    ];
    
    successResponse($profile);
}

/**
 * Update authenticated user's profile
 */
function updateUserProfile(int $userId): void {
    $data = getJsonBody();
    
    $db = getDB();
    
    // Build update query dynamically based on provided fields
    $updates = [];
    $params = [];
    
    if (isset($data['username']) && !empty(trim($data['username']))) {
        $updates[] = "username = ?";
        $params[] = trim($data['username']);
    }
    
    if (isset($data['email'])) {
        $updates[] = "email = ?";
        $params[] = $data['email'] ?: null;
    }
    
    if (isset($data['gender'])) {
        $updates[] = "gender = ?";
        $params[] = $data['gender'] ?: null;
    }
    
    if (isset($data['date_of_birth'])) {
        $updates[] = "date_of_birth = ?";
        $params[] = $data['date_of_birth'] ?: null;
    }
    
    if (isset($data['avatar_url'])) {
        $updates[] = "avatar_url = ?";
        $params[] = $data['avatar_url'] ?: null;
    }
    
    // Handle password update (hash it securely)
    if (isset($data['password']) && !empty(trim($data['password']))) {
        $password = trim($data['password']);
        if (strlen($password) < 6) {
            errorResponse('Password must be at least 6 characters', 400);
        }
        $updates[] = "password_hash = ?";
        $params[] = password_hash($password, PASSWORD_DEFAULT);
    }
    
    if (empty($updates)) {
        errorResponse('No fields to update', 400);
    }
    
    $params[] = $userId;
    $sql = "UPDATE users SET " . implode(", ", $updates) . " WHERE user_id = ?";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    
    // Return updated profile
    getUserProfile($userId);
}

/**
 * Listings routes
 */
function handleListings(string $method, array $segments): void {
    $listingId = $segments[0] ?? null;
    $subResource = $segments[1] ?? null;
    $subResourceId = $segments[2] ?? null;
    
    // POST /listings - Create new listing
    if ($method === 'POST' && $listingId === null) {
        createListing();
        return;
    }
    
    // Handle sub-resource operations (price-list, images)
    if ($listingId !== null && $subResource !== null) {
        // Price List operations
        if ($subResource === 'price-list') {
            if ($method === 'POST') {
                addPriceListItem((int)$listingId);
                return;
            }
            if ($method === 'DELETE' && $subResourceId !== null) {
                deletePriceListItem((int)$listingId, (int)$subResourceId);
                return;
            }
        }
        
        // Gallery Images operations
        if ($subResource === 'images') {
            if ($method === 'POST') {
                addListingImage((int)$listingId);
                return;
            }
            if ($method === 'DELETE' && $subResourceId !== null) {
                deleteListingImage((int)$listingId, (int)$subResourceId);
                return;
            }
        }
        
        // Reviews operations
        if ($subResource === 'reviews') {
            if ($method === 'POST') {
                addListingReview((int)$listingId);
                return;
            }
        }
    }
    
    // POST /listings/{id} - Update existing listing (using POST for multipart file support)
    if ($method === 'POST' && $listingId !== null && $subResource === null) {
        updateListing((int)$listingId);
        return;
    }
    
    if ($method === 'PUT' && $listingId !== null) {
        // PUT /listings/{id} - Update existing listing (legacy)
        updateListing((int)$listingId);
        return;
    }
    
    if ($method === 'DELETE' && $listingId !== null && $subResource === null) {
        // DELETE /listings/{id} - Delete listing
        deleteListing((int)$listingId);
        return;
    }
    
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
    }
    
    if ($listingId === null) {
        // GET /listings
        getListings();
    } elseif ($subResource === 'price-list') {
        // GET /listings/{id}/price-list
        getListingPriceList((int)$listingId);
    } elseif ($subResource === 'reviews') {
        // GET /listings/{id}/reviews
        getListingReviews((int)$listingId);
    } else {
        // GET /listings/{id}
        getListingById((int)$listingId);
    }
}

/**
 * Create a new listing (POST /listings)
 * Handles multipart form data with optional image upload
 * Note: 'selling' type items are now stored in shop_products table
 */
function createListing(): void {
    // Check authentication - requireAuth returns user payload array with user_id
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    // Get form data (supports both JSON and multipart/form-data)
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    
    if (strpos($contentType, 'multipart/form-data') !== false) {
        $data = $_POST;
    } else {
        $data = getJsonBody();
    }
    
    // Validate required fields based on listing type
    // 'selling' type does NOT require city (products are sold online)
    $listingType = $data['listing_type'] ?? '';
    $condition = $data['condition'] ?? 'new';
    
    if ($listingType === 'selling') {
        // For selling: only need type, title, and a category
        // NEW products use shop_category_id, OLD products use category_id
        $hasCategory = !empty($data['shop_category_id']) || !empty($data['category_id']);
        $errors = validateRequired($data, ['listing_type', 'title']);
        if (!$hasCategory) {
            $errors['category'] = $condition === 'new' ? 'Shop category is required' : 'Category is required';
        }
    } else {
        // For other types: also need city
        $errors = validateRequired($data, ['listing_type', 'title', 'category_id', 'city']);
    }
    
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $title = trim($data['title']);
    $description = trim($data['description'] ?? ''); // Optional
    $categoryId = !empty($data['category_id']) ? (int)$data['category_id'] : null;
    $shopCategoryId = !empty($data['shop_category_id']) ? (int)$data['shop_category_id'] : null;
    $subcategoryId = !empty($data['subcategory_id']) ? (int)$data['subcategory_id'] : null;
    $price = !empty($data['price']) ? (float)$data['price'] : null;
    $location = $data['location'] ?? '';
    $city = $data['city'] ?? 'Hingoli'; // Default for selling type
    $state = $data['state'] ?? 'Maharashtra';
    $condition = in_array($data['condition'] ?? '', ['old', 'new']) ? $data['condition'] : 'new';
    $discountedPrice = !empty($data['discounted_price']) ? (float)$data['discounted_price'] : null;
    $stockQty = !empty($data['stock_qty']) ? (int)$data['stock_qty'] : 1;
    $sellOnline = isset($data['sell_online']) ? (in_array($data['sell_online'], ['1', 'true', true, 1], true) ? 1 : 0) : 1;
    $latitude = !empty($data['latitude']) ? (float)$data['latitude'] : null;
    $longitude = !empty($data['longitude']) ? (float)$data['longitude'] : null;
    
    // Handle image upload
    $mainImageUrl = null;
    if (isset($_FILES['main_image']) && $_FILES['main_image']['error'] === UPLOAD_ERR_OK) {
        $mainImageUrl = uploadImageToR2($_FILES['main_image']);
    }
    
    $db = getDB();
    
    try {
        $db->beginTransaction();
        
        // ========== SELLING ITEMS ==========
        if ($listingType === 'selling') {
            // Branch based on condition:
            // - NEW products -> shop_products table (B2C marketplace)
            // - OLD products -> old_products table (C2C marketplace for used items)
            
            if ($condition === 'old') {
                // ========== OLD/USED PRODUCTS -> OLD_PRODUCTS TABLE ==========
                // Old products are sold directly by users (C2C), no business listing needed
                
                $productId = getNextAvailableId($db, 'old_products', 'product_id');
                
                // Insert into old_products (user-to-user marketplace)
                $stmt = $db->prepare("
                    INSERT INTO old_products 
                    (product_id, user_id, product_name, description, old_category_id, subcategory_id, price, original_price, image_url, `condition`, city, status) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'good', ?, 'active')
                ");
                $stmt->execute([
                    $productId, $userId, $title, $description, $categoryId, $subcategoryId,
                    $price, $discountedPrice, $mainImageUrl, $city
                ]);
                
                $db->commit();
                
                successResponse([
                    'product_id' => $productId,
                    'user_id' => $userId,
                    'title' => $title,
                    'listing_type' => 'selling',
                    'condition' => 'old',
                    'status' => 'active'
                ], 'Used product listed successfully!');
                return;
            }
            
            // ========== NEW PRODUCTS -> SHOP_PRODUCTS TABLE ==========
            // For new items, insert into shop_products table
            // Each seller needs their own business listing
            
            // Find user's existing business listing
            $stmt = $db->prepare("SELECT listing_id FROM listings WHERE user_id = ? AND listing_type = 'business' AND status = 'active' LIMIT 1");
            $stmt->execute([$userId]);
            $businessListingId = $stmt->fetchColumn();
            
            // If no business, auto-create a personal business for this seller
            if (!$businessListingId) {
                // Get user's name for the business
                $stmt = $db->prepare("SELECT username, phone FROM users WHERE user_id = ?");
                $stmt->execute([$userId]);
                $userInfo = $stmt->fetch();
                $businessName = ($userInfo['username'] ?? 'Seller') . "'s Shop";
                
                // Get next available listing_id (reuses gaps)
                $businessListingId = getNextAvailableId($db, 'listings', 'listing_id');
                
                // Create personal business listing
                $stmt = $db->prepare("
                    INSERT INTO listings (listing_id, listing_type, title, description, category_id, city, state, user_id, status, is_verified)
                    VALUES (?, 'business', ?, 'Personal seller shop', 3, ?, 'Maharashtra', ?, 'active', 1)
                ");
                $stmt->execute([$businessListingId, $businessName, $city, $userId]);
                
                // Create business listing entry
                $db->prepare("INSERT INTO business_listings (listing_id, business_name) VALUES (?, ?)")
                    ->execute([$businessListingId, $businessName]);
                
                // Update user's listing count
                $db->prepare("UPDATE users SET listing_count = listing_count + 1 WHERE user_id = ?")->execute([$userId]);
            }
            
            // Get next available product_id (reuses gaps)
            $productId = getNextAvailableId($db, 'shop_products', 'product_id');
            
            // Check auto-moderation setting for products
            $stmt = $db->prepare("SELECT setting_value FROM settings WHERE setting_key = 'auto_moderation_products'");
            $stmt->execute();
            $autoModProducts = $stmt->fetchColumn() === 'true';
            $isActive = $autoModProducts ? 1 : 0;
            
            // Insert into shop_products (linked to user's own business) - always 'new' condition
            // Only using shop_category_id now (category_id is legacy/nullable)
            $stmt = $db->prepare("
                INSERT INTO shop_products 
                (product_id, listing_id, product_name, description, shop_category_id, subcategory_id, price, discounted_price, stock_qty, image_url, sell_online, `condition`, is_active) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'new', ?)
            ");
            $stmt->execute([
                $productId, $businessListingId, $title, $description, $shopCategoryId, $subcategoryId,
                $price, $discountedPrice, $stockQty, $mainImageUrl, $sellOnline, $isActive
            ]);
            
            $db->commit();
            
            $statusMessage = $autoModProducts 
                ? 'Product published successfully!' 
                : 'Product submitted for approval! It will be visible once approved by admin.';
            
            successResponse([
                'product_id' => $productId,
                'listing_id' => $businessListingId,
                'title' => $title,
                'listing_type' => 'selling',
                'condition' => 'new',
                'status' => $autoModProducts ? 'active' : 'pending'
            ], $statusMessage);
            return;
        }
        
        // ========== OTHER LISTING TYPES -> LISTINGS TABLE ==========
        // Insert main listing (for services, business, jobs)
        // Note: 'price' is NOT in the listings table - it's stored in type-specific tables
        
        // Check auto-moderation setting for listings
        $stmt = $db->prepare("SELECT setting_value FROM settings WHERE setting_key = 'auto_moderation_listings'");
        $stmt->execute();
        $autoModListings = $stmt->fetchColumn() === 'true';
        $listingStatus = $autoModListings ? 'active' : 'pending';
        
        // Get next available listing_id (reuses gaps from deleted listings)
        $listingId = getNextAvailableId($db, 'listings', 'listing_id');
        
        $stmt = $db->prepare("
            INSERT INTO listings 
            (listing_id, listing_type, title, description, category_id, subcategory_id, location, city, state, latitude, longitude, main_image_url, user_id, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ");
        $stmt->execute([
            $listingId, $listingType, $title, $description, $categoryId, $subcategoryId, 
            $location, $city, $state, $latitude, $longitude, $mainImageUrl, $userId, $listingStatus
        ]);
        
        // Insert type-specific data
        if ($listingType === 'services') {
            $experienceYears = !empty($data['experience_years']) ? (int)$data['experience_years'] : null;
            $priceMin = !empty($data['price_min']) ? (float)$data['price_min'] : null;
            $priceMax = !empty($data['price_max']) ? (float)$data['price_max'] : null;
            
            $stmt = $db->prepare("INSERT INTO services_listings (listing_id, experience_years, price_min, price_max) VALUES (?, ?, ?, ?)");
            $stmt->execute([$listingId, $experienceYears, $priceMin, $priceMax]);
        } elseif ($listingType === 'jobs') {
            $salaryMin = !empty($data['salary_min']) ? (float)$data['salary_min'] : null;
            $salaryMax = !empty($data['salary_max']) ? (float)$data['salary_max'] : null;
            $salaryPeriod = $data['salary_period'] ?? 'monthly';
            $employmentType = $data['employment_type'] ?? 'full_time';
            $remoteOption = $data['remote_option'] ?? 'on_site';
            $vacancies = !empty($data['vacancies']) ? (int)$data['vacancies'] : 1;
            $experience = !empty($data['experience_required']) ? (int)$data['experience_required'] : 0;
            $education = $data['education_required'] ?? null;
            
            $stmt = $db->prepare("
                INSERT INTO job_listings 
                (listing_id, job_title, employment_type, salary_min, salary_max, salary_period, experience_required_years, education_required, remote_option, vacancies) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ");
            $stmt->execute([
                $listingId, $title, $employmentType, $salaryMin, $salaryMax, 
                $salaryPeriod, $experience, $education, $remoteOption, $vacancies
            ]);
        } elseif ($listingType === 'business') {
            $businessName = $data['business_name'] ?? $title;
            $industry = $data['industry'] ?? null;
            $establishedYear = !empty($data['established_year']) ? (int)$data['established_year'] : null;
            $employeeCount = $data['employee_count'] ?? null;
            
            $stmt = $db->prepare("
                INSERT INTO business_listings (listing_id, business_name, industry, established_year, employee_count) 
                VALUES (?, ?, ?, ?, ?)
            ");
            $stmt->execute([$listingId, $businessName, $industry, $establishedYear, $employeeCount]);
        }
        
        // Update user listing count
        $db->prepare("UPDATE users SET listing_count = listing_count + 1 WHERE user_id = ?")->execute([$userId]);
        
        // Update category listing count
        $db->prepare("UPDATE categories SET listing_count = listing_count + 1 WHERE category_id = ?")->execute([$categoryId]);
        
        $db->commit();
        
        // Return created listing
        $statusMessage = $autoModListings 
            ? 'Listing published successfully!' 
            : 'Listing submitted for approval! It will be visible once approved by admin.';
        
        successResponse([
            'listing_id' => $listingId,
            'title' => $title,
            'listing_type' => $listingType,
            'status' => $listingStatus
        ], $statusMessage);
        
    } catch (Exception $e) {
        $db->rollBack();
        error_log("Create listing error: " . $e->getMessage());
        // Return detailed error for debugging (remove in production)
        errorResponse('Failed to create listing: ' . $e->getMessage(), 500);
    }
}

/**
 * Update an existing listing (PUT /listings/{id})
 * Handles multipart form data with optional image upload
 */
function updateListing(int $listingId): void {
    // Check authentication
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check ownership
    $stmt = $db->prepare("SELECT user_id, listing_type, main_image_url FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    if ((int)$listing['user_id'] !== $userId) {
        errorResponse('You can only edit your own listings', 403);
    }
    
    // Get form data - PUT requests require special handling for multipart
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    $data = [];
    
    if (strpos($contentType, 'multipart/form-data') !== false) {
        // PHP doesn't populate $_POST for PUT requests, so we need to parse manually
        // For PUT with multipart, we need to use a workaround
        
        // First, try $_POST (works if request is actually POST)
        if (!empty($_POST)) {
            $data = $_POST;
        } else {
            // For PUT, parse the input stream manually
            $putData = file_get_contents('php://input');
            
            // Extract boundary from content-type header
            if (preg_match('/boundary=(.*)$/i', $contentType, $matches)) {
                $boundary = $matches[1];
                
                // Split by boundary
                $blocks = preg_split("/-+$boundary/", $putData);
                array_pop($blocks); // Remove last empty element
                
                foreach ($blocks as $block) {
                    if (empty(trim($block))) continue;
                    
                    // Match field name
                    if (preg_match('/name="([^"]+)"/', $block, $nameMatch)) {
                        $name = $nameMatch[1];
                        
                        // Get value after double newline
                        $parts = preg_split("/\r\n\r\n/", $block, 2);
                        if (count($parts) >= 2) {
                            $value = rtrim($parts[1], "\r\n");
                            $data[$name] = $value;
                        }
                    }
                }
            }
        }
    } else {
        $data = getJsonBody();
    }
    
    $listingType = $listing['listing_type'];
    
    // Build update query dynamically
    $updates = [];
    $params = [];
    
    if (isset($data['title']) && !empty(trim($data['title']))) {
        $updates[] = "title = ?";
        $params[] = trim($data['title']);
    }
    
    if (isset($data['description'])) {
        $updates[] = "description = ?";
        $params[] = trim($data['description']);
    }
    
    // NOTE: 'price' column does not exist in listings table
    // For services, price is stored in services_listings.hourly_rate
    // For selling, it's in shop_products table
    // So we skip updating price on the main listings table
    
    if (isset($data['location'])) {
        $updates[] = "location = ?";
        $params[] = $data['location'];
    }
    
    // Category and subcategory updates (for business listings)
    if (isset($data['category_id']) && !empty($data['category_id'])) {
        $updates[] = "category_id = ?";
        $params[] = (int)$data['category_id'];
    }
    
    if (isset($data['subcategory_id'])) {
        $updates[] = "subcategory_id = ?";
        $params[] = !empty($data['subcategory_id']) ? (int)$data['subcategory_id'] : null;
    }
    
    if (isset($data['city'])) {
        $updates[] = "city = ?";
        $params[] = $data['city'];
    }
    
    if (isset($data['latitude'])) {
        $updates[] = "latitude = ?";
        $params[] = !empty($data['latitude']) ? (float)$data['latitude'] : null;
    }
    
    if (isset($data['longitude'])) {
        $updates[] = "longitude = ?";
        $params[] = !empty($data['longitude']) ? (float)$data['longitude'] : null;
    }
    
    if (isset($data['status']) && in_array($data['status'], ['active', 'inactive', 'pending'])) {
        $updates[] = "status = ?";
        $params[] = $data['status'];
    }
    
    // Handle image upload
    $mainImageUrl = $listing['main_image_url'];
    if (isset($_FILES['main_image']) && $_FILES['main_image']['error'] === UPLOAD_ERR_OK) {
        $newImageUrl = uploadImageToR2($_FILES['main_image']);
        if ($newImageUrl) {
            $mainImageUrl = $newImageUrl;
            $updates[] = "main_image_url = ?";
            $params[] = $mainImageUrl;
        }
    }
    
    // Debug: Log what we parsed
    error_log("updateListing: Content-Type: $contentType");
    error_log("updateListing: Parsed data keys: " . implode(", ", array_keys($data)));
    error_log("updateListing: Data dump: " . json_encode($data));
    
    if (empty($updates)) {
        errorResponse('No fields to update', 400);
    }
    
    // Check auto-moderation setting for listings
    // If OFF, set status to 'pending' when user edits (requires admin re-approval)
    $stmt = $db->prepare("SELECT setting_value FROM settings WHERE setting_key = 'auto_moderation_listings'");
    $stmt->execute();
    $autoModListings = $stmt->fetchColumn() === 'true';
    
    if (!$autoModListings) {
        // Add status = pending to the updates (override any user-provided status)
        $updates[] = "status = ?";
        $params[] = 'pending';
    }
    
    $updates[] = "updated_at = NOW()";
    $params[] = $listingId;
    
    try {
        $db->beginTransaction();
        
        // Update main listing
        $sql = "UPDATE listings SET " . implode(", ", $updates) . " WHERE listing_id = ?";
        error_log("updateListing: SQL: $sql");
        error_log("updateListing: Params: " . json_encode($params));
        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        
        // Update type-specific data
        if ($listingType === 'services') {
            $serviceUpdates = [];
            $serviceParams = [];
            
            if (isset($data['experience_years'])) {
                $serviceUpdates[] = "experience_years = ?";
                $serviceParams[] = (int)$data['experience_years'];
            }
            if (isset($data['price_min'])) {
                $serviceUpdates[] = "price_min = ?";
                $serviceParams[] = !empty($data['price_min']) ? (float)$data['price_min'] : null;
                error_log("updateListing: price_min = " . ($data['price_min'] ?? 'null'));
            }
            if (isset($data['price_max'])) {
                $serviceUpdates[] = "price_max = ?";
                $serviceParams[] = !empty($data['price_max']) ? (float)$data['price_max'] : null;
                error_log("updateListing: price_max = " . ($data['price_max'] ?? 'null'));
            }
            
            error_log("updateListing: Services update - fields: " . implode(", ", $serviceUpdates));
            error_log("updateListing: Services update - params: " . json_encode($serviceParams));
            
            if (!empty($serviceUpdates)) {
                $serviceParams[] = $listingId;
                $sql = "UPDATE services_listings SET " . implode(", ", $serviceUpdates) . " WHERE listing_id = ?";
                error_log("updateListing: Services SQL: $sql");
                $stmt = $db->prepare($sql);
                $stmt->execute($serviceParams);
            }
        } elseif ($listingType === 'jobs') {
            $jobUpdates = [];
            $jobParams = [];
            
            if (isset($data['salary_min'])) {
                $jobUpdates[] = "salary_min = ?";
                $jobParams[] = !empty($data['salary_min']) ? (float)$data['salary_min'] : null;
            }
            if (isset($data['salary_max'])) {
                $jobUpdates[] = "salary_max = ?";
                $jobParams[] = !empty($data['salary_max']) ? (float)$data['salary_max'] : null;
            }
            if (isset($data['salary_period'])) {
                $jobUpdates[] = "salary_period = ?";
                $jobParams[] = $data['salary_period'];
            }
            if (isset($data['employment_type'])) {
                $jobUpdates[] = "employment_type = ?";
                $jobParams[] = $data['employment_type'];
            }
            if (isset($data['remote_option'])) {
                $jobUpdates[] = "remote_option = ?";
                $jobParams[] = $data['remote_option'];
            }
            if (isset($data['vacancies'])) {
                $jobUpdates[] = "vacancies = ?";
                $jobParams[] = (int)$data['vacancies'];
            }
            if (isset($data['experience_required'])) {
                $jobUpdates[] = "experience_required_years = ?";
                $jobParams[] = (int)$data['experience_required'];
            }
            if (isset($data['education_required'])) {
                $jobUpdates[] = "education_required = ?";
                $jobParams[] = $data['education_required'];
            }
            
            if (!empty($jobUpdates)) {
                $jobParams[] = $listingId;
                $sql = "UPDATE job_listings SET " . implode(", ", $jobUpdates) . " WHERE listing_id = ?";
                $stmt = $db->prepare($sql);
                $stmt->execute($jobParams);
            }
        } elseif ($listingType === 'business') {
            $bizUpdates = [];
            $bizParams = [];
            
            if (isset($data['business_name'])) {
                $bizUpdates[] = "business_name = ?";
                $bizParams[] = $data['business_name'];
            }
            if (isset($data['industry'])) {
                $bizUpdates[] = "industry = ?";
                $bizParams[] = $data['industry'];
            }
            if (isset($data['established_year'])) {
                $bizUpdates[] = "established_year = ?";
                $bizParams[] = !empty($data['established_year']) ? (int)$data['established_year'] : null;
            }
            if (isset($data['employee_count'])) {
                $bizUpdates[] = "employee_count = ?";
                $bizParams[] = $data['employee_count'];
            }
            
            if (!empty($bizUpdates)) {
                $bizParams[] = $listingId;
                $sql = "UPDATE business_listings SET " . implode(", ", $bizUpdates) . " WHERE listing_id = ?";
                $stmt = $db->prepare($sql);
                $stmt->execute($bizParams);
            }
        }
        
        $db->commit();
        
        // Return updated listing with appropriate status message
        $statusMessage = $autoModListings 
            ? 'Listing updated successfully' 
            : 'Listing updated! Changes are pending admin approval.';
        
        successResponse([
            'listing_id' => $listingId,
            'status' => $autoModListings ? 'active' : 'pending'
        ], $statusMessage);
        
    } catch (Exception $e) {
        $db->rollBack();
        error_log("Update listing error: " . $e->getMessage());
        error_log("Update listing trace: " . $e->getTraceAsString());
        // Include error details in response for debugging
        errorResponse('Failed to update listing: ' . $e->getMessage(), 500);
    }
}

/**
 * Delete a listing (DELETE /listings/{id})
 */
function deleteListing(int $listingId): void {
    // Check authentication
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check ownership
    $stmt = $db->prepare("SELECT user_id, category_id FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    if ((int)$listing['user_id'] !== $userId) {
        errorResponse('You can only delete your own listings', 403);
    }
    
    try {
        $db->beginTransaction();
        
        // ===== DELETE IMAGES FROM R2 FIRST =====
        // Get main image URL
        $stmt = $db->prepare("SELECT main_image_url FROM listings WHERE listing_id = ?");
        $stmt->execute([$listingId]);
        $listingData = $stmt->fetch();
        if ($listingData && !empty($listingData['main_image_url'])) {
            deleteImageFromR2($listingData['main_image_url']);
        }
        
        // Get all gallery images
        $stmt = $db->prepare("SELECT image_url FROM listing_images WHERE listing_id = ?");
        $stmt->execute([$listingId]);
        $galleryImages = $stmt->fetchAll();
        foreach ($galleryImages as $img) {
            if (!empty($img['image_url'])) {
                deleteImageFromR2($img['image_url']);
            }
        }
        // ===== END R2 DELETION =====
        
        // Delete type-specific data
        $db->prepare("DELETE FROM services_listings WHERE listing_id = ?")->execute([$listingId]);
        $db->prepare("DELETE FROM job_listings WHERE listing_id = ?")->execute([$listingId]);
        $db->prepare("DELETE FROM business_listings WHERE listing_id = ?")->execute([$listingId]);
        $db->prepare("DELETE FROM listing_images WHERE listing_id = ?")->execute([$listingId]);
        $db->prepare("DELETE FROM listing_price_list WHERE listing_id = ?")->execute([$listingId]);
        
        // Delete associated reviews (cascade delete to prevent orphaned reviews)
        $db->prepare("DELETE FROM reviews WHERE listing_id = ? AND product_id IS NULL AND old_product_id IS NULL")->execute([$listingId]);
        
        // Delete main listing
        $db->prepare("DELETE FROM listings WHERE listing_id = ?")->execute([$listingId]);
        
        // Update user listing count
        $db->prepare("UPDATE users SET listing_count = listing_count - 1 WHERE user_id = ? AND listing_count > 0")->execute([$userId]);
        
        // Update category listing count
        if ($listing['category_id']) {
            $db->prepare("UPDATE categories SET listing_count = listing_count - 1 WHERE category_id = ? AND listing_count > 0")->execute([$listing['category_id']]);
        }
        
        $db->commit();
        
        successResponse(null, 'Listing deleted successfully');
        
    } catch (Exception $e) {
        $db->rollBack();
        error_log("Delete listing error: " . $e->getMessage());
        errorResponse('Failed to delete listing', 500);
    }
}

/**
 * Add price list item (POST /listings/{id}/price-list)
 */
function addPriceListItem(int $listingId): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check ownership
    $stmt = $db->prepare("SELECT user_id FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    if ((int)$listing['user_id'] !== $userId) {
        errorResponse('You can only modify your own listings', 403);
    }
    
    $data = getJsonBody();
    
    // Validate required fields
    $errors = validateRequired($data, ['item_name', 'price']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $itemName = trim($data['item_name']);
    $itemDescription = trim($data['item_description'] ?? '');
    $itemCategory = trim($data['item_category'] ?? '');
    $price = (float)$data['price'];
    $discountedPrice = !empty($data['discounted_price']) ? (float)$data['discounted_price'] : null;
    $durationMinutes = !empty($data['duration_minutes']) ? (int)$data['duration_minutes'] : null;
    
    // Get max sort order
    $stmt = $db->prepare("SELECT COALESCE(MAX(sort_order), -1) + 1 as next_order FROM listing_price_list WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $sortOrder = (int)$stmt->fetchColumn();
    
    $stmt = $db->prepare("
        INSERT INTO listing_price_list (listing_id, item_name, item_description, item_category, price, discounted_price, duration_minutes, sort_order) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ");
    $stmt->execute([$listingId, $itemName, $itemDescription, $itemCategory, $price, $discountedPrice, $durationMinutes, $sortOrder]);
    
    $itemId = (int)$db->lastInsertId();
    
    successResponse([
        'item_id' => $itemId,
        'item_name' => $itemName,
        'price' => $price
    ], 'Price list item added successfully');
}

/**
 * Delete price list item (DELETE /listings/{id}/price-list/{itemId})
 */
function deletePriceListItem(int $listingId, int $itemId): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check ownership
    $stmt = $db->prepare("SELECT user_id FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    if ((int)$listing['user_id'] !== $userId) {
        errorResponse('You can only modify your own listings', 403);
    }
    
    // Delete the item
    $stmt = $db->prepare("DELETE FROM listing_price_list WHERE item_id = ? AND listing_id = ?");
    $stmt->execute([$itemId, $listingId]);
    
    if ($stmt->rowCount() === 0) {
        errorResponse('Price list item not found', 404);
    }
    
    successResponse(null, 'Price list item deleted successfully');
}

/**
 * Add gallery image (POST /listings/{id}/images)
 */
function addListingImage(int $listingId): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check ownership
    $stmt = $db->prepare("SELECT user_id FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    if ((int)$listing['user_id'] !== $userId) {
        errorResponse('You can only modify your own listings', 403);
    }
    
    // Handle image upload
    if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
        errorResponse('No image file uploaded', 400);
    }
    
    $imageUrl = uploadImageToR2($_FILES['image']);
    
    if (!$imageUrl) {
        errorResponse('Failed to upload image', 500);
    }
    
    // Get max sort order
    $stmt = $db->prepare("SELECT COALESCE(MAX(sort_order), -1) + 1 as next_order FROM listing_images WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $sortOrder = (int)$stmt->fetchColumn();
    
    $stmt = $db->prepare("INSERT INTO listing_images (listing_id, image_url, sort_order) VALUES (?, ?, ?)");
    $stmt->execute([$listingId, $imageUrl, $sortOrder]);
    
    $imageId = (int)$db->lastInsertId();
    
    successResponse([
        'image_id' => $imageId,
        'image_url' => $imageUrl
    ], 'Image added successfully');
}

/**
 * Delete gallery image (DELETE /listings/{id}/images/{imageId})
 */
function deleteListingImage(int $listingId, int $imageId): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check ownership
    $stmt = $db->prepare("SELECT user_id FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    if ((int)$listing['user_id'] !== $userId) {
        errorResponse('You can only modify your own listings', 403);
    }
    
    // Delete the image record (TODO: optionally delete from R2 storage)
    $stmt = $db->prepare("DELETE FROM listing_images WHERE image_id = ? AND listing_id = ?");
    $stmt->execute([$imageId, $listingId]);
    
    if ($stmt->rowCount() === 0) {
        errorResponse('Image not found', 404);
    }
    
    successResponse(null, 'Image deleted successfully');
}

/**
 * Upload image to R2 (Cloudflare R2 storage)
 */
function uploadImageToR2(array $file, string $folder = 'listings'): ?string {
    // Check if file is valid
    if ($file['error'] !== UPLOAD_ERR_OK) {
        return null;
    }
    
    // Check file type
    $allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);
    
    if (!in_array($mimeType, $allowedTypes)) {
        return null;
    }
    
    // Generate unique filename with folder
    $extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    $filename = $folder . '/' . uniqid($folder . '_') . '_' . time() . '.' . $extension;
    
    // Read file content
    $fileContent = file_get_contents($file['tmp_name']);
    
    // R2 Configuration (same as admin.php)
    $r2Config = [
        'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
        'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
        'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
        'bucket' => 'hello-hingoli-bucket',
        'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
    ];
    
    // Upload to R2
    $result = uploadFileToR2($r2Config, $filename, $fileContent, $mimeType);
    
    if ($result['success']) {
        return $r2Config['publicUrl'] . '/' . $filename;
    }
    
    // Fallback to local upload if R2 fails
    $uploadDir = __DIR__ . '/uploads/';
    if (!is_dir($uploadDir . $folder . '/')) {
        mkdir($uploadDir . $folder . '/', 0755, true);
    }
    
    $localPath = $uploadDir . $filename;
    if (move_uploaded_file($file['tmp_name'], $localPath)) {
        return '/api/uploads/' . $filename;
    }
    
    return null;
}

/**
 * Upload file to Cloudflare R2 (S3-compatible)
 */
function uploadFileToR2($config, $key, $content, $contentType) {
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
    
    error_log("R2 Upload failed: HTTP {$httpCode}: {$response}");
    return ['success' => false, 'error' => $error ?: "HTTP {$httpCode}: {$response}"];
}

/**
 * Delete file from Cloudflare R2 (S3-compatible)
 */
function deleteFileFromR2($config, $key) {
    $endpoint = $config['endpoint'];
    $accessKeyId = $config['accessKeyId'];
    $secretAccessKey = $config['secretAccessKey'];
    $bucket = $config['bucket'];
    $region = 'auto'; // R2 uses 'auto' for region
    
    $host = parse_url($endpoint, PHP_URL_HOST);
    $url = "{$endpoint}/{$bucket}/{$key}";
    
    $date = gmdate('Ymd\THis\Z');
    $shortDate = gmdate('Ymd');
    $contentHash = hash('sha256', ''); // Empty content for DELETE
    
    // Canonical request
    $headers = [
        'host' => $host,
        'x-amz-content-sha256' => $contentHash,
        'x-amz-date' => $date
    ];
    ksort($headers);
    
    $signedHeaders = implode(';', array_keys($headers));
    $canonicalHeaders = '';
    foreach ($headers as $k => $v) {
        $canonicalHeaders .= "{$k}:{$v}\n";
    }
    
    $canonicalRequest = "DELETE\n/{$bucket}/{$key}\n\n{$canonicalHeaders}\n{$signedHeaders}\n{$contentHash}";
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
        CURLOPT_CUSTOMREQUEST => 'DELETE',
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => [
            "Authorization: {$authHeader}",
            "Host: {$host}",
            "x-amz-content-sha256: {$contentHash}",
            "x-amz-date: {$date}"
        ]
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);
    
    // 204 No Content is success for DELETE
    if ($httpCode >= 200 && $httpCode < 300) {
        error_log("R2 Delete success: {$key}");
        return ['success' => true];
    }
    
    error_log("R2 Delete failed: HTTP {$httpCode}: {$response}");
    return ['success' => false, 'error' => $error ?: "HTTP {$httpCode}: {$response}"];
}

/**
 * Delete image from R2 given its public URL
 * Extracts the key from the URL and deletes from R2
 */
function deleteImageFromR2(string $imageUrl): bool {
    if (empty($imageUrl)) {
        return false;
    }
    
    // R2 Configuration
    $r2Config = [
        'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
        'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
        'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
        'bucket' => 'hello-hingoli-bucket',
        'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
    ];
    
    // Extract key from URL
    // URL format: https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listing_xxx.webp
    $publicUrl = $r2Config['publicUrl'];
    if (strpos($imageUrl, $publicUrl) === 0) {
        $key = substr($imageUrl, strlen($publicUrl) + 1); // +1 for the /
        
        $result = deleteFileFromR2($r2Config, $key);
        return $result['success'];
    }
    
    // Not an R2 URL, no action needed
    return true;
}

/**
 * Get listings with filters
 */
function getListings(): void {
    $type = getQueryParam('type');
    $categoryId = getQueryParam('category_id');
    $subcategoryId = getQueryParam('subcategory_id');
    $city = getQueryParam('city');
    $search = getQueryParam('search');
    $condition = getQueryParam('condition'); // old or new
    $page = max(1, (int)getQueryParam('page', 1));
    $perPage = min(50, max(1, (int)getQueryParam('per_page', 20)));
    
    $db = getDB();
    
    // Build query - join services_listings and job_listings to get type-specific fields
    $sql = "SELECT l.listing_id, l.listing_type, l.title, l.description,
                   l.category_id, l.subcategory_id,
                   l.location, l.city, l.state, l.main_image_url, l.user_id,
                   l.status, l.is_verified, l.is_featured, l.view_count,
                   l.review_count, l.avg_rating, l.created_at,
                   c.name as category_name, c.slug as category_slug,
                   u.username as seller_name, u.avatar_url as seller_avatar,
                   sl.experience_years, sl.price_min, sl.price_max,
                   jl.salary_min, jl.salary_max, jl.salary_period, jl.employment_type, jl.remote_option,
                   jl.vacancies, jl.experience_required_years
            FROM listings l
            LEFT JOIN categories c ON l.category_id = c.category_id
            LEFT JOIN users u ON l.user_id = u.user_id
            LEFT JOIN services_listings sl ON l.listing_id = sl.listing_id
            LEFT JOIN job_listings jl ON l.listing_id = jl.listing_id
            WHERE l.status = 'active'";
    
    $countSql = "SELECT COUNT(*) FROM listings l WHERE l.status = 'active'";
    $params = [];
    $conditions = "";
    
    if ($type) {
        $conditions .= " AND l.listing_type = ?";
        $params[] = $type;
    }
    
    if ($categoryId) {
        $conditions .= " AND l.category_id = ?";
        $params[] = (int)$categoryId;
    }
    
    if ($subcategoryId) {
        $conditions .= " AND l.subcategory_id = ?";
        $params[] = (int)$subcategoryId;
    }
    
    if ($city) {
        $conditions .= " AND l.city = ?";
        $params[] = $city;
    }
    
    if ($search) {
        $conditions .= " AND (l.title LIKE ? OR l.description LIKE ?)";
        $searchTerm = "%$search%";
        $params[] = $searchTerm;
        $params[] = $searchTerm;
    }
    
    // condition filter removed - price/condition columns deleted from listings
    
    $sql .= $conditions . " ORDER BY l.is_featured DESC, l.created_at DESC";
    $countSql .= $conditions;
    
    // Get total count
    $stmt = $db->prepare($countSql);
    $stmt->execute($params);
    $total = (int)$stmt->fetchColumn();
    
    // Add pagination
    $offset = ($page - 1) * $perPage;
    $sql .= " LIMIT $perPage OFFSET $offset";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $listings = $stmt->fetchAll();
    
    // Format listings
    $listings = array_map(function($l) {
        return [
            'listing_id' => (int)$l['listing_id'],
            'listing_type' => $l['listing_type'],
            'title' => $l['title'],
            'description' => $l['description'],
            'category_id' => (int)$l['category_id'],
            'subcategory_id' => $l['subcategory_id'] ? (int)$l['subcategory_id'] : null,
            'category_name' => $l['category_name'],
            'location' => $l['location'],
            'city' => $l['city'],
            'state' => $l['state'],
            'main_image_url' => $l['main_image_url'],
            'user_id' => (int)$l['user_id'],
            'seller_name' => $l['seller_name'],
            'seller_avatar' => $l['seller_avatar'],
            'is_verified' => (bool)$l['is_verified'],
            'is_featured' => (bool)$l['is_featured'],
            'view_count' => (int)$l['view_count'],
            'review_count' => (int)$l['review_count'],
            'avg_rating' => (float)$l['avg_rating'],
            'experience_years' => isset($l['experience_years']) ? (int)$l['experience_years'] : (isset($l['experience_required_years']) ? (int)$l['experience_required_years'] : null),
            'price_min' => isset($l['price_min']) ? (float)$l['price_min'] : null,
            'price_max' => isset($l['price_max']) ? (float)$l['price_max'] : null,
            'created_at' => $l['created_at'],
            // Job-specific fields
            'salary_min' => isset($l['salary_min']) ? (float)$l['salary_min'] : null,
            'salary_max' => isset($l['salary_max']) ? (float)$l['salary_max'] : null,
            'salary_period' => $l['salary_period'] ?? null,
            'employment_type' => $l['employment_type'] ?? null,
            'work_location_type' => $l['remote_option'] ?? null,
            'vacancies' => isset($l['vacancies']) ? (int)$l['vacancies'] : null
        ];
    }, $listings);
    
    paginatedResponse($listings, $page, $perPage, $total);
}

/**
 * Get listing by ID
 */
function getListingById(int $listingId): void {
    $db = getDB();
    
    // Get main listing data
    $stmt = $db->prepare("
        SELECT l.*, 
               c.name as category_name, c.slug as category_slug,
               sc.name as subcategory_name,
               u.username as seller_name, u.avatar_url as seller_avatar,
               u.phone as seller_phone, u.avg_rating as seller_rating,
               u.listing_count as seller_listing_count, u.created_at as seller_joined,
               u.is_verified as seller_verified, u.response_rate as seller_response_rate
        FROM listings l
        LEFT JOIN categories c ON l.category_id = c.category_id
        LEFT JOIN categories sc ON l.subcategory_id = sc.category_id
        LEFT JOIN users u ON l.user_id = u.user_id
        WHERE l.listing_id = ?
    ");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    // Get images
    $stmt = $db->prepare("
        SELECT image_id, image_url, thumbnail_url, sort_order, alt_text
        FROM listing_images 
        WHERE listing_id = ?
        ORDER BY sort_order
    ");
    $stmt->execute([$listingId]);
    $images = $stmt->fetchAll();

    // Get videos
    $videos = [];
    try {
        $stmt = $db->prepare("
            SELECT video_id, video_url, thumbnail_url, title, duration_seconds 
            FROM listing_videos 
            WHERE listing_id = ?
            ORDER BY sort_order
        ");
        $stmt->execute([$listingId]);
        $videos = $stmt->fetchAll();
    } catch (Exception $e) {
        // Table might not exist yet, ignore error
        $videos = [];
    }
    
    // Get type-specific data
    $typeData = null;
    switch ($listing['listing_type']) {
        case 'services':
            $stmt = $db->prepare("SELECT * FROM services_listings WHERE listing_id = ?");
            $stmt->execute([$listingId]);
            $result = $stmt->fetch();
            $typeData = $result ?: null;
            break;
        case 'selling':
            $stmt = $db->prepare("SELECT * FROM selling_listings WHERE listing_id = ?");
            $stmt->execute([$listingId]);
            $result = $stmt->fetch();
            $typeData = $result ?: null;
            break;
        case 'business':
            $stmt = $db->prepare("SELECT * FROM business_listings WHERE listing_id = ?");
            $stmt->execute([$listingId]);
            $result = $stmt->fetch();
            $typeData = $result ?: null;
            break;
        case 'jobs':
            $stmt = $db->prepare("SELECT * FROM job_listings WHERE listing_id = ?");
            $stmt->execute([$listingId]);
            $result = $stmt->fetch();
            $typeData = $result ?: null;
            break;
    }
    
    // Increment view count
    $db->prepare("UPDATE listings SET view_count = view_count + 1 WHERE listing_id = ?")->execute([$listingId]);
    
    // Format response
    $result = [
        'listing_id' => (int)$listing['listing_id'],
        'listing_type' => $listing['listing_type'],
        'title' => $listing['title'],
        'description' => $listing['description'],
        'category' => [
            'id' => (int)$listing['category_id'],
            'name' => $listing['category_name'],
            'slug' => $listing['category_slug']
        ],
        'subcategory' => $listing['subcategory_id'] ? [
            'id' => (int)$listing['subcategory_id'],
            'name' => $listing['subcategory_name']
        ] : null,
        'location' => $listing['location'],
        'city' => $listing['city'],
        'state' => $listing['state'],
        'postal_code' => $listing['postal_code'],
        'latitude' => $listing['latitude'] ? (float)$listing['latitude'] : null,
        'longitude' => $listing['longitude'] ? (float)$listing['longitude'] : null,
        'main_image_url' => $listing['main_image_url'],
        'images' => array_map(function($img) {
            return [
                'image_id' => (int)$img['image_id'],
                'image_url' => $img['image_url'],
                'thumbnail_url' => $img['thumbnail_url'],
                'alt_text' => $img['alt_text']
            ];
        }, $images),
        'videos' => array_map(function($vid) {
            return [
                'video_id' => (int)$vid['video_id'],
                'video_url' => $vid['video_url'],
                'thumbnail_url' => $vid['thumbnail_url'],
                'title' => $vid['title'],
                'duration_seconds' => $vid['duration_seconds'] ? (int)$vid['duration_seconds'] : null
            ];
        }, $videos),
        'user' => [
            'user_id' => (int)$listing['user_id'],
            'username' => $listing['seller_name'],
            'avatar_url' => $listing['seller_avatar'],
            'phone' => $listing['seller_phone'],
            'avg_rating' => (float)$listing['seller_rating'],
            'listing_count' => (int)$listing['seller_listing_count'],
            'is_verified' => (bool)$listing['seller_verified'],
            'response_rate' => (float)($listing['seller_response_rate'] ?? 0)
        ],
        'view_count' => (int)$listing['view_count'] + 1,
        'review_count' => (int)$listing['review_count'],
        'avg_rating' => (float)$listing['avg_rating'],
        'is_verified' => (bool)$listing['is_verified'],
        'is_featured' => (bool)$listing['is_featured'],
        'status' => $listing['status'],
        'created_at' => $listing['created_at'],
        'updated_at' => $listing['updated_at'],
        'service_details' => ($listing['listing_type'] === 'services' && $typeData) ? [
            'service_type' => $typeData['service_type'] ?? null,
            'experience_years' => isset($typeData['experience_years']) ? (int)$typeData['experience_years'] : null,
            'availability' => $typeData['availability'] ?? null,
            'service_area_radius_km' => isset($typeData['service_area_radius_km']) ? (int)$typeData['service_area_radius_km'] : null,
            'hourly_rate' => isset($typeData['hourly_rate']) ? (float)$typeData['hourly_rate'] : null,
            'price_min' => isset($typeData['price_min']) ? (float)$typeData['price_min'] : null,
            'price_max' => isset($typeData['price_max']) ? (float)$typeData['price_max'] : null
        ] : null,
        'business_details' => ($listing['listing_type'] === 'business' && $typeData) ? [
            'business_name' => $typeData['business_name'] ?? null,
            'industry' => $typeData['industry'] ?? null,
            'business_type' => $typeData['business_type'] ?? null,
            'established_year' => isset($typeData['established_year']) ? (int)$typeData['established_year'] : null,
            'employee_count' => $typeData['employee_count'] ?? null,
            'website_url' => $typeData['website_url'] ?? null,
            'business_email' => $typeData['business_email'] ?? null,
            'business_phone' => $typeData['business_phone'] ?? null
        ] : null,
        'job_details' => ($listing['listing_type'] === 'jobs' && $typeData) ? [
            'job_title' => $typeData['job_title'] ?? null,
            'employment_type' => $typeData['employment_type'] ?? null,
            'salary_min' => isset($typeData['salary_min']) ? (float)$typeData['salary_min'] : null,
            'salary_max' => isset($typeData['salary_max']) ? (float)$typeData['salary_max'] : null,
            'salary_period' => $typeData['salary_period'] ?? null,
            'experience_required_years' => isset($typeData['experience_required_years']) ? (int)$typeData['experience_required_years'] : null,
            'education_required' => $typeData['education_required'] ?? null,
            'work_location_type' => $typeData['remote_option'] ?? null,
            'vacancies' => isset($typeData['vacancies']) ? (int)$typeData['vacancies'] : null,
            'application_deadline' => $typeData['application_deadline'] ?? null,
            'skills_required' => $typeData['required_skills'] ?? null
        ] : null
    ];
    
    successResponse($result);
}

/**
 * Get listing price list (for services)
 */
function getListingPriceList(int $listingId): void {
    $db = getDB();
    
    $stmt = $db->prepare("
        SELECT item_id, item_name, item_description, item_category,
               price, discounted_price, currency, duration_minutes,
               image_url, sort_order, is_popular
        FROM listing_price_list
        WHERE listing_id = ? AND is_active = 1
        ORDER BY item_category, sort_order
    ");
    $stmt->execute([$listingId]);
    $items = $stmt->fetchAll();
    
    $result = array_map(function($item) {
        return [
            'item_id' => (int)$item['item_id'],
            'item_name' => $item['item_name'],
            'item_description' => $item['item_description'],
            'item_category' => $item['item_category'],
            'price' => (float)$item['price'],
            'discounted_price' => $item['discounted_price'] ? (float)$item['discounted_price'] : null,
            'currency' => $item['currency'],
            'duration_minutes' => $item['duration_minutes'] ? (int)$item['duration_minutes'] : null,
            'image_url' => $item['image_url'],
            'is_popular' => (bool)$item['is_popular']
        ];
    }, $items);
    
    successResponse($result);
}

/**
 * Get listing reviews
 * Now uses unified getReviewsFor helper from reviews.php
 */
function getListingReviews(int $listingId): void {
    getReviewsFor('listing', $listingId);
}

/**
 * Add a review to a listing (POST /listings/{id}/reviews)
 */
function addListingReview(int $listingId): void {
    // Require authentication
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check if listing exists
    $stmt = $db->prepare("SELECT user_id FROM listings WHERE listing_id = ? AND status = 'active'");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    
    // Check if user is trying to review their own listing
    if ((int)$listing['user_id'] === $userId) {
        errorResponse('You cannot review your own listing', 403);
    }
    
    // Check if user has already reviewed this listing
    // Exclusion: only check listing reviews (where product_id IS NULL)
    $stmt = $db->prepare("SELECT review_id FROM reviews WHERE listing_id = ? AND reviewer_id = ? AND product_id IS NULL AND old_product_id IS NULL");
    $stmt->execute([$listingId, $userId]);
    if ($stmt->fetch()) {
        errorResponse('You have already reviewed this listing', 409);
    }
    
    // Get request data
    $data = getJsonBody();
    
    // Validate required fields
    $rating = isset($data['rating']) ? (int)$data['rating'] : 0;
    if ($rating < 1 || $rating > 5) {
        errorResponse('Rating must be between 1 and 5', 422);
    }
    
    $title = isset($data['title']) ? trim($data['title']) : null;
    $content = isset($data['content']) ? trim($data['content']) : null;
    
    try {
        $db->beginTransaction();
        
        // Get next available review_id (reuses gaps)
        $reviewId = getNextAvailableId($db, 'reviews', 'review_id');
        
        // Insert the review (auto-approved for now, you can change to is_approved = 0 for moderation)
        $stmt = $db->prepare("
            INSERT INTO reviews (review_id, listing_id, reviewer_id, rating, title, content, is_approved, created_at)
            VALUES (?, ?, ?, ?, ?, ?, 1, NOW())
        ");
        $stmt->execute([$reviewId, $listingId, $userId, $rating, $title, $content]);
        
        // Update listing's average rating and review count
        // Exclusion: only count listing reviews (where product_id IS NULL)
        $stmt = $db->prepare("
            UPDATE listings 
            SET review_count = review_count + 1,
                avg_rating = (
                    SELECT AVG(rating) FROM reviews 
                    WHERE listing_id = ? AND is_approved = 1 AND product_id IS NULL AND old_product_id IS NULL
                )
            WHERE listing_id = ?
        ");
        $stmt->execute([$listingId, $listingId]);
        
        $db->commit();
        
        // Get reviewer info for response
        $stmt = $db->prepare("SELECT username, avatar_url FROM users WHERE user_id = ?");
        $stmt->execute([$userId]);
        $reviewer = $stmt->fetch();
        
        // Return the created review
        successResponse([
            'review_id' => $reviewId,
            'rating' => $rating,
            'title' => $title,
            'content' => $content,
            'reviewer' => [
                'user_id' => $userId,
                'username' => $reviewer['username'] ?? 'User',
                'avatar_url' => $reviewer['avatar_url'] ?? null
            ],
            'helpful_count' => 0,
            'created_at' => date('Y-m-d H:i:s')
        ], 'Review submitted successfully');
        
    } catch (Exception $e) {
        $db->rollBack();
        error_log("Add review error: " . $e->getMessage());
        errorResponse('Failed to submit review', 500);
    }
}

/**
 * Get product reviews
 * GET /products/{id}/reviews
 * Now uses unified getReviewsFor helper from reviews.php
 */
function getProductReviews(int $productId): void {
    getReviewsFor('product', $productId);
}

/**
 * Add a review to a product (POST /products/{id}/reviews)
 * Supports multipart form data for image uploads
 */
function addProductReview(int $productId): void {
    // Require authentication
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Check if product exists and get owner info
    $stmt = $db->prepare("
        SELECT sp.product_id, l.user_id 
        FROM shop_products sp
        JOIN listings l ON sp.listing_id = l.listing_id
        WHERE sp.product_id = ?
    ");
    $stmt->execute([$productId]);
    $product = $stmt->fetch();
    
    if (!$product) {
        errorResponse('Product not found', 404);
    }
    
    // Check if user is trying to review their own product
    if ((int)$product['user_id'] === $userId) {
        errorResponse('You cannot review your own product', 403);
    }
    
    // Check if user has already reviewed this product
    // Exclusion: only check product reviews (where listing_id IS NULL)
    $stmt = $db->prepare("SELECT review_id FROM reviews WHERE product_id = ? AND reviewer_id = ? AND listing_id IS NULL AND old_product_id IS NULL");
    $stmt->execute([$productId, $userId]);
    if ($stmt->fetch()) {
        errorResponse('You have already reviewed this product', 409);
    }
    
    // Get request data (supports both JSON and multipart/form-data)
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if (strpos($contentType, 'multipart/form-data') !== false) {
        $data = $_POST;
    } else {
        $data = getJsonBody();
    }
    
    // Validate required fields
    $rating = isset($data['rating']) ? (int)$data['rating'] : 0;
    if ($rating < 1 || $rating > 5) {
        errorResponse('Rating must be between 1 and 5', 422);
    }
    
    $title = isset($data['title']) ? trim($data['title']) : null;
    $content = isset($data['content']) ? trim($data['content']) : null;
    
    // Handle image uploads (up to 3 images)
    $imageUrls = [];
    if (!empty($_FILES['images'])) {
        $maxImages = min(3, count($_FILES['images']['name']));
        for ($i = 0; $i < $maxImages; $i++) {
            if ($_FILES['images']['error'][$i] === UPLOAD_ERR_OK) {
                $file = [
                    'name' => $_FILES['images']['name'][$i],
                    'type' => $_FILES['images']['type'][$i],
                    'tmp_name' => $_FILES['images']['tmp_name'][$i],
                    'error' => $_FILES['images']['error'][$i],
                    'size' => $_FILES['images']['size'][$i]
                ];
                $uploadedUrl = uploadImageToR2($file, 'reviews');
                if ($uploadedUrl) {
                    $imageUrls[] = $uploadedUrl;
                }
            }
        }
    }
    // Also handle single image upload
    if (!empty($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $uploadedUrl = uploadImageToR2($_FILES['image'], 'reviews');
        if ($uploadedUrl) {
            $imageUrls[] = $uploadedUrl;
        }
    }
    
    $imagesJson = !empty($imageUrls) ? json_encode($imageUrls) : null;
    
    try {
        $db->beginTransaction();
        
        // Get next available review_id
        $reviewId = getNextAvailableId($db, 'reviews', 'review_id');
        
        // Insert the review (auto-approved for now)
        $stmt = $db->prepare("
            INSERT INTO reviews (review_id, product_id, reviewer_id, rating, title, content, images, is_approved, approval_status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, 1, 'approved', NOW())
        ");
        $stmt->execute([$reviewId, $productId, $userId, $rating, $title, $content, $imagesJson]);
        
        $db->commit();
        
        // Get reviewer info for response
        $stmt = $db->prepare("SELECT username, avatar_url FROM users WHERE user_id = ?");
        $stmt->execute([$userId]);
        $reviewer = $stmt->fetch();
        
        // Return the created review
        successResponse([
            'review_id' => $reviewId,
            'rating' => $rating,
            'title' => $title,
            'content' => $content,
            'images' => $imageUrls,
            'reviewer' => [
                'user_id' => $userId,
                'username' => $reviewer['username'] ?? 'User',
                'avatar_url' => $reviewer['avatar_url'] ?? null
            ],
            'helpful_count' => 0,
            'created_at' => date('Y-m-d H:i:s')
        ], 'Review submitted successfully');
        
    } catch (Exception $e) {
        $db->rollBack();
        error_log("Add product review error: " . $e->getMessage());
        errorResponse('Failed to submit review', 500);
    }
}

/**
 * Debug endpoint - DELETE IN PRODUCTION
 */
function handleDebug(): void {
    $result = [
        'php_version' => PHP_VERSION,
        'timestamp' => date('Y-m-d H:i:s'),
    ];
    
    try {
        $db = getDB();
        $result['database'] = 'Connected successfully';
        
        // Debug Categories
        $stmt = $db->query("SELECT category_id, name, slug, listing_type, parent_id FROM categories ORDER BY listing_type, name");
        $categories = $stmt->fetchAll();
        $result['categories'] = $categories;
        
        // Check users table
        $stmt = $db->query("SELECT user_id, username, phone, LEFT(password_hash, 30) as hash_preview, is_active FROM users LIMIT 5");
        $users = $stmt->fetchAll();
        $result['users'] = $users;
        $result['user_count'] = count($users);
        
        // Test password verification with a known hash
        $testHash = '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi';
        $result['password_verify_test'] = password_verify('password', $testHash) ? 'PASS' : 'FAIL';
        
        // If there's a user, test their password hash
        if (!empty($users)) {
            $stmt = $db->query("SELECT password_hash FROM users WHERE user_id = 1");
            $user = $stmt->fetch();
            if ($user) {
                $result['user1_hash_length'] = strlen($user['password_hash']);
                $result['user1_hash_valid_format'] = preg_match('/^\$2[ayb]\$/', $user['password_hash']) ? 'YES' : 'NO';
                $result['user1_password_test'] = password_verify('password', $user['password_hash']) ? 'PASS' : 'FAIL';
            }
        }
        
    } catch (Exception $e) {
        $result['database'] = 'Connection failed: ' . $e->getMessage();
    }
    
    successResponse($result);
}

// ============================================
// APP CONFIG / FORCE UPDATE ROUTES
// ============================================

/**
 * Handle app configuration requests (for force update, etc.)
 * GET /app-config - Get all config for app (version check, force update, etc.)
 * GET /app-config/version - Get just version info
 */
function handleAppConfig(string $method, array $segments): void {
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
        return;
    }
    
    $action = $segments[0] ?? '';
    
    $db = getDB();
    
    // Fetch all config values
    $stmt = $db->query("SELECT config_key, config_value FROM app_config");
    $configs = $stmt->fetchAll(PDO::FETCH_KEY_PAIR);
    
    $minVersion = $configs['min_version'] ?? '1.0.0';
    $latestVersion = $configs['latest_version'] ?? '1.0.0';
    $forceUpdate = ($configs['force_update'] ?? 'false') === 'true';
    $updateMessage = $configs['update_message'] ?? 'Please update the app';
    $updateMessageMr = $configs['update_message_mr'] ?? 'कृपया अॅप अपडेट करा';
    $playStoreUrl = $configs['play_store_url'] ?? 'https://play.google.com/store/apps/details?id=com.hingoli.hub';
    
    // Call service timing (admin-controlled hours when calls are allowed)
    $callStartHour = (int)($configs['call_start_hour'] ?? 8);  // Default 8 AM
    $callEndHour = (int)($configs['call_end_hour'] ?? 22);     // Default 10 PM
    $callTimingEnabled = ($configs['call_timing_enabled'] ?? 'true') === 'true';
    $callTimingMessage = $configs['call_timing_message'] ?? 'Call service available from 8 AM to 10 PM';
    $callTimingMessageMr = $configs['call_timing_message_mr'] ?? 'कॉल सेवा सकाळी 8 ते रात्री 10 वाजेपर्यंत उपलब्ध';
    
    // Get app version from request header (Android will send this)
    $appVersion = $_SERVER['HTTP_X_APP_VERSION'] ?? $_GET['version'] ?? null;
    
    // Check if update is required
    $updateRequired = false;
    if ($appVersion && version_compare($appVersion, $minVersion, '<')) {
        $updateRequired = true;
    }
    
    $response = [
        'min_version' => $minVersion,
        'latest_version' => $latestVersion,
        'force_update' => $forceUpdate || $updateRequired,
        'update_required' => $updateRequired,
        'update_message' => $updateMessage,
        'update_message_mr' => $updateMessageMr,
        'play_store_url' => $playStoreUrl,
        // Call timing configuration
        'call_timing_enabled' => $callTimingEnabled,
        'call_start_hour' => $callStartHour,
        'call_end_hour' => $callEndHour,
        'call_timing_message' => $callTimingMessage,
        'call_timing_message_mr' => $callTimingMessageMr
    ];
    
    // If checking specific version
    if ($action === 'check' && $appVersion) {
        $response['current_version'] = $appVersion;
        $response['is_latest'] = version_compare($appVersion, $latestVersion, '>=');
    }
    
    successResponse($response);
}

// ============================================
// NOTIFICATION ROUTES
// ============================================

/**
 * Notifications routes
 */
function handleNotifications(string $method, array $segments): void {
    $action = $segments[0] ?? '';
    
    switch ($action) {
        case 'register-token':
            if ($method === 'POST') {
                registerFcmToken();
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        case 'send':
            if ($method === 'POST') {
                sendNotification();
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        case 'settings':
            if ($method === 'GET') {
                getNotificationSettings();
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        case 'history':
            if ($method === 'GET') {
                getNotificationHistory();
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        case 'unread-count':
            if ($method === 'GET') {
                getUnreadCount();
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        case 'mark-read':
            if ($method === 'POST') {
                markNotificationsRead();
            } else {
                errorResponse('Method not allowed', 405);
            }
            break;
            
        default:
            errorResponse('Notification endpoint not found', 404);
    }
}

/**
 * Register FCM token for push notifications
 */
function registerFcmToken(): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['fcm_token']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    // Get user from token (optional - can also receive user_id in body)
    $userId = $data['user_id'] ?? null;
    
    // Try to get from auth header
    if (!$userId) {
        $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
        if (preg_match('/Bearer\s+(.*)$/i', $authHeader, $matches)) {
            $token = $matches[1];
            $payload = decodeJWT($token);
            $userId = $payload['sub'] ?? null;
        }
    }
    
    if (!$userId) {
        errorResponse('User ID required', 400);
    }
    
    $fcmToken = trim($data['fcm_token']);
    $deviceInfo = $data['device_info'] ?? null;
    
    $db = getDB();
    
    // Insert or update token
    $stmt = $db->prepare("
        INSERT INTO user_fcm_tokens (user_id, fcm_token, device_info)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE 
            user_id = VALUES(user_id),
            device_info = VALUES(device_info),
            updated_at = NOW()
    ");
    $stmt->execute([$userId, $fcmToken, $deviceInfo]);
    
    successResponse(['message' => 'FCM token registered successfully']);
}

/**
 * Send notification to all users (admin only) via Firebase
 * Uses existing Cloud Function at /notifications/{userId}
 */
function sendNotification(): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['title', 'body']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $title = trim($data['title']);
    $body = trim($data['body']);
    $deepLink = $data['deep_link'] ?? null;
    $listingId = $data['listing_id'] ?? null;
    $type = $data['type'] ?? 'admin_broadcast';
    
    $db = getDB();
    
    // Get all user IDs with FCM tokens
    $stmt = $db->query("SELECT DISTINCT user_id FROM user_fcm_tokens");
    $userIds = $stmt->fetchAll(PDO::FETCH_COLUMN);
    
    if (empty($userIds)) {
        errorResponse('No registered users found', 404);
    }
    
    // Send to each user via existing Cloud Function path
    $successCount = 0;
    $errors = [];
    
    foreach ($userIds as $userId) {
        $result = sendNotificationToUser(
            $userId, 
            $title, 
            $body, 
            $type, 
            $listingId, 
            $deepLink
        );
        
        if ($result['success']) {
            $successCount++;
        } else {
            $errors[] = "User $userId: " . $result['error'];
        }
    }
    
    // Log notification to history table
    $stmt = $db->prepare("
        INSERT INTO notifications (type, title, body, deep_link, listing_id, sent_count)
        VALUES (?, ?, ?, ?, ?, ?)
    ");
    $stmt->execute([$type, $title, $body, $deepLink, $listingId, $successCount]);
    $notificationId = $db->lastInsertId();
    
    // Save to each user's notification inbox
    foreach ($userIds as $userId) {
        $stmt = $db->prepare("
            INSERT INTO user_notifications (user_id, notification_id, title, body, type, deep_link, listing_id, is_read)
            VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)
        ");
        $stmt->execute([$userId, $notificationId, $title, $body, $type, $deepLink, $listingId]);
    }
    
    successResponse([
        'message' => 'Notification sent',
        'sent_to' => $successCount,
        'total_users' => count($userIds),
        'errors' => count($errors) > 0 ? $errors : null
    ]);
}

/**
 * Send notification to a single user via Firebase Realtime Database
 * This triggers the existing sendChatNotification Cloud Function
 */
function sendNotificationToUser(int $userId, string $title, string $body, string $type, ?int $listingId, ?string $deepLink): array {
    $firebaseUrl = "https://hellohingoli-default-rtdb.firebaseio.com/notifications/$userId.json";
    
    $notificationData = [
        'title' => $title,
        'body' => $body,
        'type' => $type,
        'listing_id' => $listingId ? (string)$listingId : '',
        'deep_link' => $deepLink ?? '',
        'timestamp' => time() * 1000, // milliseconds
    ];
    
    $ch = curl_init($firebaseUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($notificationData));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($httpCode >= 200 && $httpCode < 300) {
        return ['success' => true, 'response' => json_decode($response, true)];
    } else {
        return ['success' => false, 'error' => $error ?: "HTTP $httpCode"];
    }
}

/**
 * Get notification settings
 */
function getNotificationSettings(): void {
    $db = getDB();
    
    $stmt = $db->query("SELECT setting_key, setting_value, description FROM notification_settings");
    $settings = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Convert to key-value object
    $result = [];
    foreach ($settings as $setting) {
        $result[$setting['setting_key']] = [
            'value' => $setting['setting_value'],
            'description' => $setting['description']
        ];
    }
    
    successResponse($result);
}

/**
 * Get notification history (last one month only)
 * Uses user_notifications table for per-user notifications with read status
 */
function getNotificationHistory(): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    $page = max(1, (int)($_GET['page'] ?? 1));
    $limit = min(50, max(1, (int)($_GET['limit'] ?? 20)));
    $offset = ($page - 1) * $limit;
    
    $db = getDB();
    
    // Get notifications from user_notifications table for this user (last 30 days)
    $stmt = $db->prepare("
        SELECT id, title, body, type, deep_link, listing_id, is_read, created_at
        FROM user_notifications
        WHERE user_id = ?
          AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        ORDER BY created_at DESC
        LIMIT $limit OFFSET $offset
    ");
    $stmt->execute([$userId]);
    $notifications = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Get total count (only for last 30 days)
    $stmt = $db->prepare("
        SELECT COUNT(*) FROM user_notifications 
        WHERE user_id = ?
          AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    ");
    $stmt->execute([$userId]);
    $totalCount = (int)$stmt->fetchColumn();
    
    successResponse([
        'notifications' => $notifications,
        'page' => $page,
        'limit' => $limit,
        'total_count' => $totalCount,
        'has_more' => ($offset + count($notifications)) < $totalCount
    ]);
}

/**
 * Get unread notification and chat counts for badges
 */
function getUnreadCount(): void {
    $userId = requireAuth();
    $db = getDB();
    
    // Get unread notifications count
    $stmt = $db->prepare("SELECT COUNT(*) FROM user_notifications WHERE user_id = ? AND is_read = FALSE");
    $stmt->execute([$userId]);
    $notificationCount = (int)$stmt->fetchColumn();
    
    // Get unread chats count (from Firebase, we'll approximate by checking recent activity)
    // For now, return 0 - this can be enhanced later
    $chatCount = 0;
    
    successResponse([
        'notifications' => $notificationCount,
        'chats' => $chatCount
    ]);
}

/**
 * Mark notifications as read
 */
function markNotificationsRead(): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    $data = getJsonBody();
    
    $db = getDB();
    
    // If specific IDs provided, mark only those
    if (isset($data['notification_ids']) && is_array($data['notification_ids'])) {
        $ids = array_map('intval', $data['notification_ids']);
        $placeholders = implode(',', array_fill(0, count($ids), '?'));
        
        $stmt = $db->prepare("
            UPDATE user_notifications 
            SET is_read = TRUE 
            WHERE user_id = ? AND id IN ($placeholders)
        ");
        $stmt->execute(array_merge([$userId], $ids));
    } else {
        // Mark all as read
        $stmt = $db->prepare("UPDATE user_notifications SET is_read = TRUE WHERE user_id = ?");
        $stmt->execute([$userId]);
    }
    
    successResponse(['message' => 'Notifications marked as read']);
}

/**
 * Banners routes: GET /banners?placement=home_top
 */
function handleBanners(string $method, array $segments): void {
    $db = getDB();  // Get database connection directly
    
    // Debug: If ?debug=1, output debug info
    if (isset($_GET['debug'])) {
        error_reporting(E_ALL);
        ini_set('display_errors', 1);
    }
    
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
    }
    
    try {
        $placement = $_GET['placement'] ?? null;
        $city = $_GET['city'] ?? null;
        
        $sql = "SELECT banner_id, title, image_url, link_url, link_type, link_id, placement, sort_order
                FROM banners 
                WHERE is_active = 1 
                AND (start_date IS NULL OR start_date <= NOW())
                AND (end_date IS NULL OR end_date >= NOW())";
        $params = [];
        
        if ($placement) {
            $sql .= " AND placement = ?";
            $params[] = $placement;
        }
        
        if ($city) {
            $sql .= " AND (target_city IS NULL OR target_city = ?)";
            $params[] = $city;
        }
        
        $sql .= " ORDER BY sort_order ASC, banner_id DESC";
        
        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        $banners = $stmt->fetchAll();
        
        // Track views (fire and forget)
        if (!empty($banners)) {
            try {
                $ids = array_column($banners, 'banner_id');
                $placeholders = implode(',', array_fill(0, count($ids), '?'));
                $db->prepare("UPDATE banners SET view_count = view_count + 1 WHERE banner_id IN ($placeholders)")->execute($ids);
            } catch (Exception $e) {
                // Ignore view tracking errors
            }
        }
        
        successResponse($banners);
    } catch (Exception $e) {
        errorResponse('Failed to fetch banners: ' . $e->getMessage(), 500);
    }
}

/**
 * Prefetch endpoint - Returns ALL app data in a single request
 * GET /prefetch
 * 
 * Returns:
 * - Categories (services, business, selling, jobs) with subcategories embedded
 * - Listings (first page for each type)
 * - Banners (all placements)
 * - Cities
 */
function handlePrefetch(string $method): void {
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
    }
    
    $db = getDB();
    $city = $_GET['city'] ?? null;
    
    try {
        $result = [
            'categories' => [],
            'listings' => [],
            'shop_products' => [],
            'banners' => [],
            'cities' => []
        ];
        
        // ==================== CATEGORIES WITH SUBCATEGORIES ====================
        $listingTypes = ['services', 'business', 'jobs'];
        
        foreach ($listingTypes as $type) {
            // Get parent categories
            $stmt = $db->prepare("
                SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth,
                       icon_url, image_url, description, listing_count
                FROM categories 
                WHERE listing_type = ? AND parent_id IS NULL AND is_active = 1
                ORDER BY sort_order, name
            ");
            $stmt->execute([$type]);
            $parents = $stmt->fetchAll();
            
            // Get all subcategories for this type in one query
            $stmt = $db->prepare("
                SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth,
                       icon_url, image_url, description, listing_count
                FROM categories 
                WHERE listing_type = ? AND parent_id IS NOT NULL AND is_active = 1
                ORDER BY sort_order, name
            ");
            $stmt->execute([$type]);
            $allSubcats = $stmt->fetchAll();
            
            // Group subcategories by parent_id
            $subcatsByParent = [];
            foreach ($allSubcats as $sub) {
                $parentId = (int)$sub['parent_id'];
                if (!isset($subcatsByParent[$parentId])) {
                    $subcatsByParent[$parentId] = [];
                }
                $subcatsByParent[$parentId][] = [
                    'category_id' => (int)$sub['category_id'],
                    'parent_id' => $parentId,
                    'name' => $sub['name'],
                    'name_mr' => $sub['name_mr'],
                    'slug' => $sub['slug'],
                    'listing_type' => $sub['listing_type'],
                    'depth' => (int)$sub['depth'],
                    'icon_url' => $sub['icon_url'],
                    'image_url' => $sub['image_url'],
                    'description' => $sub['description'],
                    'listing_count' => (int)$sub['listing_count']
                ];
            }
            
            // Build categories with embedded subcategories
            $categoriesWithSubs = [];
            foreach ($parents as $parent) {
                $parentId = (int)$parent['category_id'];
                $categoriesWithSubs[] = [
                    'category_id' => $parentId,
                    'parent_id' => null,
                    'name' => $parent['name'],
                    'name_mr' => $parent['name_mr'],
                    'slug' => $parent['slug'],
                    'listing_type' => $parent['listing_type'],
                    'depth' => (int)$parent['depth'],
                    'icon_url' => $parent['icon_url'],
                    'image_url' => $parent['image_url'],
                    'description' => $parent['description'],
                    'listing_count' => (int)$parent['listing_count'],
                    'subcategories' => $subcatsByParent[$parentId] ?? []
                ];
            }
            
            $result['categories'][$type] = $categoriesWithSubs;
        }
        
        // ==================== OLD CATEGORIES (for used/second-hand items) ====================
        $oldCatStmt = $db->prepare("
            SELECT id, parent_id, name, name_mr, slug, icon, image_url
            FROM old_categories 
            WHERE is_active = 1
            ORDER BY COALESCE(parent_id, 0), name
        ");
        $oldCatStmt->execute();
        $oldCategories = $oldCatStmt->fetchAll();
        
        $result['categories']['old'] = array_map(function($cat) {
            return [
                'id' => (int)$cat['id'],
                'parent_id' => $cat['parent_id'] ? (int)$cat['parent_id'] : null,
                'name' => $cat['name'],
                'name_mr' => $cat['name_mr'],
                'slug' => $cat['slug'],
                'icon' => $cat['icon'],
                'image_url' => $cat['image_url']
            ];
        }, $oldCategories);
        
        // ==================== LISTINGS (First page for each type) ====================
        foreach ($listingTypes as $type) {
            $sql = "
                SELECT l.listing_id, l.listing_type, l.title, l.description,
                       l.category_id, l.subcategory_id, l.location, l.city, l.state,
                       l.latitude, l.longitude, l.main_image_url, l.user_id,
                       l.is_verified, l.is_featured, l.view_count, l.review_count,
                       l.avg_rating, l.created_at,
                       c.name as category_name, c.icon_url as category_icon,
                       u.username as user_name,
                       sl.experience_years, sl.price_min, sl.price_max,
                       jl.salary_min, jl.salary_max, jl.salary_period, jl.employment_type, 
                       jl.remote_option, jl.vacancies, jl.experience_required_years
                FROM listings l
                LEFT JOIN categories c ON l.category_id = c.category_id
                LEFT JOIN users u ON l.user_id = u.user_id
                LEFT JOIN services_listings sl ON l.listing_id = sl.listing_id
                LEFT JOIN job_listings jl ON l.listing_id = jl.listing_id
                WHERE l.listing_type = ? AND l.status = 'active'
            ";
            $params = [$type];
            
            if ($city) {
                $sql .= " AND l.city = ?";
                $params[] = $city;
            }
            
            $sql .= " ORDER BY l.is_featured DESC, l.created_at DESC LIMIT 20";
            
            $stmt = $db->prepare($sql);
            $stmt->execute($params);
            $listings = $stmt->fetchAll();
            
            $result['listings'][$type] = array_map(function($l) {
                return [
                    'listing_id' => (int)$l['listing_id'],
                    'listing_type' => $l['listing_type'],
                    'title' => $l['title'],
                    'description' => $l['description'],
                    'price' => null,
                    'category_id' => (int)$l['category_id'],
                    'subcategory_id' => $l['subcategory_id'] ? (int)$l['subcategory_id'] : null,
                    'category_name' => $l['category_name'],
                    'category_icon' => $l['category_icon'],
                    'location' => $l['location'],
                    'city' => $l['city'],
                    'state' => $l['state'],
                    'latitude' => $l['latitude'] ? (float)$l['latitude'] : null,
                    'longitude' => $l['longitude'] ? (float)$l['longitude'] : null,
                    'main_image_url' => $l['main_image_url'],
                    'user_id' => (int)$l['user_id'],
                    'user_name' => $l['user_name'],
                    'is_verified' => (bool)$l['is_verified'],
                    'is_featured' => (bool)$l['is_featured'],
                    'view_count' => (int)$l['view_count'],
                    'review_count' => (int)$l['review_count'],
                    'avg_rating' => (float)$l['avg_rating'],
                    'created_at' => $l['created_at'],
                    // Service fields
                    'experience_years' => isset($l['experience_years']) ? (int)$l['experience_years'] : (isset($l['experience_required_years']) ? (int)$l['experience_required_years'] : null),
                    'price_min' => isset($l['price_min']) ? (float)$l['price_min'] : null,
                    'price_max' => isset($l['price_max']) ? (float)$l['price_max'] : null,
                    // Job fields
                    'salary_min' => isset($l['salary_min']) ? (float)$l['salary_min'] : null,
                    'salary_max' => isset($l['salary_max']) ? (float)$l['salary_max'] : null,
                    'salary_period' => $l['salary_period'] ?? null,
                    'employment_type' => $l['employment_type'] ?? null,
                    'work_location_type' => $l['remote_option'] ?? null,
                    'vacancies' => isset($l['vacancies']) ? (int)$l['vacancies'] : null
                ];
            }, $listings);
        }
        
        // ==================== SHOP PRODUCTS (For selling feed) ====================
        $productSql = "
            SELECT sp.product_id, sp.listing_id, sp.product_name, sp.description,
                   sp.category_id, sp.subcategory_id, sp.price, sp.discounted_price,
                   sp.image_url, sp.stock_qty, sp.`condition`, sp.created_at,
                   l.title as business_name, l.city, l.user_id,
                   c.name as category_name,
                   sc.name as subcategory_name,
                   u.phone as business_phone
            FROM shop_products sp
            JOIN listings l ON sp.listing_id = l.listing_id
            LEFT JOIN categories c ON sp.category_id = c.category_id
            LEFT JOIN categories sc ON sp.subcategory_id = sc.category_id
            LEFT JOIN users u ON l.user_id = u.user_id
            WHERE sp.is_active = 1 AND sp.sell_online = 1 AND sp.`condition` = 'new' AND l.status = 'active'
        ";
        $productParams = [];
        
        if ($city) {
            $productSql .= " AND l.city = ?";
            $productParams[] = $city;
        }
        
        $productSql .= " ORDER BY sp.created_at DESC LIMIT 20";
        
        $stmt = $db->prepare($productSql);
        $stmt->execute($productParams);
        $products = $stmt->fetchAll();
        
        $result['shop_products'] = array_map(function($p) {
            return [
                'product_id' => (int)$p['product_id'],
                'listing_id' => (int)$p['listing_id'],
                'product_name' => $p['product_name'],
                'description' => $p['description'],
                'category_id' => (int)$p['category_id'],
                'category_name' => $p['category_name'],
                'subcategory_id' => $p['subcategory_id'] ? (int)$p['subcategory_id'] : null,
                'subcategory_name' => $p['subcategory_name'],
                'price' => (float)$p['price'],
                'discounted_price' => $p['discounted_price'] ? (float)$p['discounted_price'] : null,
                'image_url' => $p['image_url'],
                'stock_qty' => $p['stock_qty'] ? (int)$p['stock_qty'] : null,
                'condition' => $p['condition'],
                'business_name' => $p['business_name'],
                'business_phone' => $p['business_phone'],
                'city' => $p['city'],
                'user_id' => (int)$p['user_id'],
                'created_at' => $p['created_at']
            ];
        }, $products);
        
        // ==================== OLD PRODUCTS (For Buy/Sell Old section) ====================
        $oldProductSql = "
            SELECT sp.product_id, sp.listing_id, sp.product_name, sp.description,
                   sp.category_id, sp.subcategory_id, sp.price, sp.discounted_price,
                   sp.image_url, sp.stock_qty, sp.`condition`, sp.created_at,
                   l.title as business_name, l.city, l.user_id,
                   c.name as category_name,
                   sc.name as subcategory_name,
                   u.phone as business_phone
            FROM shop_products sp
            JOIN listings l ON sp.listing_id = l.listing_id
            LEFT JOIN categories c ON sp.category_id = c.category_id
            LEFT JOIN categories sc ON sp.subcategory_id = sc.category_id
            LEFT JOIN users u ON l.user_id = u.user_id
            WHERE sp.is_active = 1 AND sp.sell_online = 1 AND sp.`condition` = 'old' AND l.status = 'active'
        ";
        $oldProductParams = [];
        
        if ($city) {
            $oldProductSql .= " AND l.city = ?";
            $oldProductParams[] = $city;
        }
        
        $oldProductSql .= " ORDER BY sp.created_at DESC LIMIT 20";
        
        $stmt = $db->prepare($oldProductSql);
        $stmt->execute($oldProductParams);
        $oldProducts = $stmt->fetchAll();
        
        $result['old_products'] = array_map(function($p) {
            return [
                'product_id' => (int)$p['product_id'],
                'listing_id' => (int)$p['listing_id'],
                'product_name' => $p['product_name'],
                'description' => $p['description'],
                'category_id' => (int)$p['category_id'],
                'category_name' => $p['category_name'],
                'subcategory_id' => $p['subcategory_id'] ? (int)$p['subcategory_id'] : null,
                'subcategory_name' => $p['subcategory_name'],
                'price' => (float)$p['price'],
                'discounted_price' => $p['discounted_price'] ? (float)$p['discounted_price'] : null,
                'image_url' => $p['image_url'],
                'stock_qty' => $p['stock_qty'] ? (int)$p['stock_qty'] : null,
                'condition' => $p['condition'],
                'business_name' => $p['business_name'],
                'business_phone' => $p['business_phone'],
                'city' => $p['city'],
                'user_id' => (int)$p['user_id'],
                'created_at' => $p['created_at']
            ];
        }, $oldProducts);
        
        // ==================== BANNERS (All placements) ====================
        $placements = [
            'home_top', 'home_bottom',
            'services_top', 'services_bottom',
            'business_top', 'business_bottom',
            'selling_top', 'selling_bottom',
            'jobs_top', 'jobs_bottom',
            'listing_detail_bottom',
            'category_bottom',
            'search_bottom'
        ];
        
        foreach ($placements as $placement) {
            $sql = "SELECT banner_id, title, image_url, link_url, link_type, link_id, placement, sort_order
                    FROM banners 
                    WHERE is_active = 1 
                    AND placement = ?
                    AND (start_date IS NULL OR start_date <= NOW())
                    AND (end_date IS NULL OR end_date >= NOW())";
            $params = [$placement];
            
            if ($city) {
                $sql .= " AND (target_city IS NULL OR target_city = ?)";
                $params[] = $city;
            }
            
            $sql .= " ORDER BY sort_order ASC, banner_id DESC";
            
            $stmt = $db->prepare($sql);
            $stmt->execute($params);
            $banners = $stmt->fetchAll();
            
            $result['banners'][$placement] = array_map(function($b) {
                return [
                    'banner_id' => (int)$b['banner_id'],
                    'title' => $b['title'],
                    'image_url' => $b['image_url'],
                    'link_url' => $b['link_url'],
                    'link_type' => $b['link_type'],
                    'link_id' => $b['link_id'] ? (int)$b['link_id'] : null,
                    'placement' => $b['placement'],
                    'sort_order' => (int)$b['sort_order']
                ];
            }, $banners);
        }
        
        // ==================== CITIES ====================
        $stmt = $db->prepare("
            SELECT city_id, state_id, name, name_mr, slug, is_popular, listing_count
            FROM cities 
            WHERE is_active = 1
            ORDER BY is_popular DESC, sort_order, name
        ");
        $stmt->execute();
        $cities = $stmt->fetchAll();
        
        $result['cities'] = array_map(function($city) {
            return [
                'city_id' => (int)$city['city_id'],
                'state_id' => (int)$city['state_id'],
                'name' => $city['name'],
                'name_mr' => $city['name_mr'],
                'slug' => $city['slug'],
                'is_popular' => (bool)$city['is_popular'],
                'listing_count' => (int)$city['listing_count']
            ];
        }, $cities);
        
        successResponse($result);
        
    } catch (Exception $e) {
        errorResponse('Failed to prefetch data: ' . $e->getMessage(), 500);
    }
}

// ============================================
// CART ROUTES
// ============================================

function handleCart(string $method, array $segments): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    $cartItemId = isset($segments[0]) && is_numeric($segments[0]) ? (int)$segments[0] : null;
    
    switch ($method) {
        case 'GET':
            getCartItems($userId);
            break;
        case 'POST':
            addToCart($userId);
            break;
        case 'PUT':
            if ($cartItemId) {
                updateCartItem($userId, $cartItemId);
            } else {
                errorResponse('Cart item ID required', 400);
            }
            break;
        case 'DELETE':
            if ($cartItemId) {
                removeCartItem($userId, $cartItemId);
            } else {
                clearCart($userId);
            }
            break;
        default:
            errorResponse('Method not allowed', 405);
    }
}

function getCartItems(int $userId): void {
    $db = getDB();
    
    // Get cart items with shop product prices
    // First get shop products in cart
    $stmt = $db->prepare("
        SELECT ci.cart_item_id, ci.quantity, ci.created_at, ci.product_id, ci.listing_id,
               sp.product_name as title, sp.price, sp.image_url as main_image_url, sp.min_qty,
               l.user_id as seller_id, u.username as seller_name,
               'product' as item_type
        FROM cart_items ci
        JOIN shop_products sp ON ci.product_id = sp.product_id
        JOIN listings l ON sp.listing_id = l.listing_id
        JOIN users u ON l.user_id = u.user_id
        WHERE ci.user_id = ? AND ci.product_id IS NOT NULL AND sp.is_active = 1 AND l.status = 'active'
        ORDER BY ci.created_at DESC
    ");
    $stmt->execute([$userId]);
    $productItems = $stmt->fetchAll();
    
    // Then get legacy listing items in cart (if any)
    $stmt = $db->prepare("
        SELECT ci.cart_item_id, ci.quantity, ci.created_at, ci.product_id, ci.listing_id,
               l.title, 0 as price, l.main_image_url,
               l.user_id as seller_id, u.username as seller_name,
               1 as min_qty,
               'listing' as item_type
        FROM cart_items ci
        JOIN listings l ON ci.listing_id = l.listing_id
        JOIN users u ON l.user_id = u.user_id
        WHERE ci.user_id = ? AND ci.product_id IS NULL AND ci.listing_id IS NOT NULL AND l.status = 'active'
        ORDER BY ci.created_at DESC
    ");
    $stmt->execute([$userId]);
    $listingItems = $stmt->fetchAll();
    
    $allItems = array_merge($productItems, $listingItems);
    
    $cartItems = array_map(function($item) {
        $price = (float)$item['price'];
        $quantity = (int)$item['quantity'];
        $minQty = (int)($item['min_qty'] ?? 1);
        return [
            'cart_item_id' => (int)$item['cart_item_id'],
            'product_id' => $item['product_id'] ? (int)$item['product_id'] : null,
            'listing_id' => $item['listing_id'] ? (int)$item['listing_id'] : null,
            'title' => $item['title'],
            'price' => $price,
            'quantity' => $quantity,
            'min_qty' => $minQty,
            'main_image_url' => $item['main_image_url'],
            'seller_id' => (int)$item['seller_id'],
            'seller_name' => $item['seller_name'],
            'subtotal' => $price * $quantity,
            'item_type' => $item['item_type']
        ];
    }, $allItems);
    
    $total = array_sum(array_column($cartItems, 'subtotal'));
    
    successResponse([
        'items' => $cartItems,
        'item_count' => count($cartItems),
        'total' => $total
    ]);
}

function addToCart(int $userId): void {
    $data = getJsonBody();
    
    $listingId = isset($data['listing_id']) ? (int)$data['listing_id'] : null;
    $productId = isset($data['product_id']) ? (int)$data['product_id'] : null;
    $quantity = max(1, (int)($data['quantity'] ?? 1));
    
    if (!$listingId && !$productId) {
        errorResponse('Either listing_id or product_id is required', 400);
    }
    
    $db = getDB();
    
    // Handle shop product (product_id)
    if ($productId) {
        // Check if product exists and is active
        $stmt = $db->prepare("
            SELECT sp.product_id, sp.price, sp.product_name, sp.listing_id, sp.min_qty, l.user_id
            FROM shop_products sp
            JOIN listings l ON sp.listing_id = l.listing_id
            WHERE sp.product_id = ? AND sp.is_active = 1 AND l.status = 'active'
        ");
        $stmt->execute([$productId]);
        $product = $stmt->fetch();
        
        if (!$product) {
            errorResponse('Product not found or not available', 404);
        }
        
        // Can't add own product to cart
        if ((int)$product['user_id'] === $userId) {
            errorResponse('Cannot add your own product to cart', 400);
        }
        
        // Validate minimum order quantity
        $minQty = (int)($product['min_qty'] ?? 1);
        if ($quantity < $minQty) {
            errorResponse("Minimum order quantity for this product is $minQty", 400);
        }
        
        // Check if already in cart - update quantity instead
        $stmt = $db->prepare("SELECT cart_item_id, quantity FROM cart_items WHERE user_id = ? AND product_id = ?");
        $stmt->execute([$userId, $productId]);
        $existing = $stmt->fetch();
        
        if ($existing) {
            $newQty = (int)$existing['quantity'] + $quantity;
            // Also validate combined quantity meets minimum
            if ($newQty < $minQty) {
                $newQty = $minQty;
            }
            $stmt = $db->prepare("UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?");
            $stmt->execute([$newQty, $existing['cart_item_id']]);
            successResponse(['message' => 'Cart updated', 'cart_item_id' => (int)$existing['cart_item_id'], 'quantity' => $newQty]);
        } else {
            $stmt = $db->prepare("INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)");
            $stmt->execute([$userId, $productId, $quantity]);
            $cartItemId = (int)$db->lastInsertId();
            successResponse(['message' => 'Added to cart', 'cart_item_id' => $cartItemId, 'quantity' => $quantity], 201);
        }
        return;
    }
    
    // Legacy: Handle listing_id
    // Check if listing exists and is active
    $stmt = $db->prepare("SELECT listing_id, user_id FROM listings WHERE listing_id = ? AND status = 'active'");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found or not available', 404);
    }
    
    // Can't add own listing to cart
    if ((int)$listing['user_id'] === $userId) {
        errorResponse('Cannot add your own listing to cart', 400);
    }
    
    // Check if already in cart - update quantity instead
    $stmt = $db->prepare("SELECT cart_item_id, quantity FROM cart_items WHERE user_id = ? AND listing_id = ?");
    $stmt->execute([$userId, $listingId]);
    $existing = $stmt->fetch();
    
    if ($existing) {
        $newQty = (int)$existing['quantity'] + $quantity;
        $stmt = $db->prepare("UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?");
        $stmt->execute([$newQty, $existing['cart_item_id']]);
        successResponse(['message' => 'Cart updated', 'cart_item_id' => (int)$existing['cart_item_id'], 'quantity' => $newQty]);
    } else {
        $stmt = $db->prepare("INSERT INTO cart_items (user_id, listing_id, quantity) VALUES (?, ?, ?)");
        $stmt->execute([$userId, $listingId, $quantity]);
        $cartItemId = (int)$db->lastInsertId();
        successResponse(['message' => 'Added to cart', 'cart_item_id' => $cartItemId, 'quantity' => $quantity], 201);
    }
}

function updateCartItem(int $userId, int $cartItemId): void {
    $data = getJsonBody();
    $quantity = max(1, (int)($data['quantity'] ?? 1));
    
    $db = getDB();
    
    // Get cart item with product min_qty
    $stmt = $db->prepare("
        SELECT ci.cart_item_id, ci.product_id, sp.min_qty 
        FROM cart_items ci 
        LEFT JOIN shop_products sp ON ci.product_id = sp.product_id 
        WHERE ci.cart_item_id = ? AND ci.user_id = ?
    ");
    $stmt->execute([$cartItemId, $userId]);
    $cartItem = $stmt->fetch();
    
    if (!$cartItem) {
        errorResponse('Cart item not found', 404);
    }
    
    // Validate minimum quantity
    $minQty = (int)($cartItem['min_qty'] ?? 1);
    if ($quantity < $minQty) {
        errorResponse("Minimum order quantity for this product is $minQty", 400);
    }
    
    $stmt = $db->prepare("UPDATE cart_items SET quantity = ? WHERE cart_item_id = ? AND user_id = ?");
    $stmt->execute([$quantity, $cartItemId, $userId]);
    
    successResponse(['message' => 'Quantity updated', 'quantity' => $quantity, 'min_qty' => $minQty]);
}

function removeCartItem(int $userId, int $cartItemId): void {
    $db = getDB();
    
    $stmt = $db->prepare("DELETE FROM cart_items WHERE cart_item_id = ? AND user_id = ?");
    $stmt->execute([$cartItemId, $userId]);
    
    if ($stmt->rowCount() === 0) {
        errorResponse('Cart item not found', 404);
    }
    
    successResponse(['message' => 'Item removed from cart']);
}

function clearCart(int $userId): void {
    $db = getDB();
    $db->prepare("DELETE FROM cart_items WHERE user_id = ?")->execute([$userId]);
    successResponse(['message' => 'Cart cleared']);
}

// ============================================
// ADDRESS ROUTES
// ============================================

function handleAddresses(string $method, array $segments): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    $addressId = isset($segments[0]) && is_numeric($segments[0]) ? (int)$segments[0] : null;
    $action = $segments[1] ?? null;
    
    switch ($method) {
        case 'GET':
            getAddresses($userId);
            break;
        case 'POST':
            addAddress($userId);
            break;
        case 'PUT':
            if ($addressId && $action === 'default') {
                setDefaultAddress($userId, $addressId);
            } elseif ($addressId) {
                updateAddress($userId, $addressId);
            } else {
                errorResponse('Address ID required', 400);
            }
            break;
        case 'DELETE':
            if ($addressId) {
                deleteAddress($userId, $addressId);
            } else {
                errorResponse('Address ID required', 400);
            }
            break;
        default:
            errorResponse('Method not allowed', 405);
    }
}

function getAddresses(int $userId): void {
    $db = getDB();
    
    $stmt = $db->prepare("
        SELECT * FROM user_addresses 
        WHERE user_id = ? 
        ORDER BY is_default DESC, created_at DESC
    ");
    $stmt->execute([$userId]);
    $addresses = $stmt->fetchAll();
    
    $result = array_map(function($addr) {
        return [
            'address_id' => (int)$addr['address_id'],
            'name' => $addr['name'],
            'phone' => $addr['phone'],
            'address_line1' => $addr['address_line1'],
            'address_line2' => $addr['address_line2'],
            'city' => $addr['city'],
            'state' => $addr['state'],
            'pincode' => $addr['pincode'],
            'is_default' => (bool)$addr['is_default']
        ];
    }, $addresses);
    
    successResponse($result);
}

function addAddress(int $userId): void {
    $data = getJsonBody();
    
    $errors = validateRequired($data, ['name', 'phone', 'address_line1', 'city', 'pincode']);
    if ($errors) {
        errorResponse('Validation failed', 422, $errors);
    }
    
    $db = getDB();
    
    // If this is first address, make it default
    $stmt = $db->prepare("SELECT COUNT(*) FROM user_addresses WHERE user_id = ?");
    $stmt->execute([$userId]);
    $isFirst = (int)$stmt->fetchColumn() === 0;
    
    $stmt = $db->prepare("
        INSERT INTO user_addresses (user_id, name, phone, address_line1, address_line2, city, state, pincode, is_default)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    ");
    $stmt->execute([
        $userId,
        trim($data['name']),
        trim($data['phone']),
        trim($data['address_line1']),
        trim($data['address_line2'] ?? ''),
        trim($data['city']),
        trim($data['state'] ?? 'Maharashtra'),
        trim($data['pincode']),
        $isFirst ? 1 : 0
    ]);
    
    $addressId = (int)$db->lastInsertId();
    successResponse(['message' => 'Address added', 'address_id' => $addressId], 201);
}

function updateAddress(int $userId, int $addressId): void {
    $data = getJsonBody();
    
    $db = getDB();
    
    // Verify ownership
    $stmt = $db->prepare("SELECT address_id FROM user_addresses WHERE address_id = ? AND user_id = ?");
    $stmt->execute([$addressId, $userId]);
    if (!$stmt->fetch()) {
        errorResponse('Address not found', 404);
    }
    
    $fields = [];
    $params = [];
    
    foreach (['name', 'phone', 'address_line1', 'address_line2', 'city', 'state', 'pincode'] as $field) {
        if (isset($data[$field])) {
            $fields[] = "$field = ?";
            $params[] = trim($data[$field]);
        }
    }
    
    if (empty($fields)) {
        errorResponse('No fields to update', 400);
    }
    
    $params[] = $addressId;
    $params[] = $userId;
    
    $sql = "UPDATE user_addresses SET " . implode(', ', $fields) . " WHERE address_id = ? AND user_id = ?";
    $db->prepare($sql)->execute($params);
    
    successResponse(['message' => 'Address updated']);
}

function setDefaultAddress(int $userId, int $addressId): void {
    $db = getDB();
    
    // Verify ownership
    $stmt = $db->prepare("SELECT address_id FROM user_addresses WHERE address_id = ? AND user_id = ?");
    $stmt->execute([$addressId, $userId]);
    if (!$stmt->fetch()) {
        errorResponse('Address not found', 404);
    }
    
    // Clear other defaults
    $db->prepare("UPDATE user_addresses SET is_default = 0 WHERE user_id = ?")->execute([$userId]);
    
    // Set new default
    $db->prepare("UPDATE user_addresses SET is_default = 1 WHERE address_id = ?")->execute([$addressId]);
    
    successResponse(['message' => 'Default address updated']);
}

function deleteAddress(int $userId, int $addressId): void {
    $db = getDB();
    
    $stmt = $db->prepare("DELETE FROM user_addresses WHERE address_id = ? AND user_id = ?");
    $stmt->execute([$addressId, $userId]);
    
    if ($stmt->rowCount() === 0) {
        errorResponse('Address not found', 404);
    }
    
    successResponse(['message' => 'Address deleted']);
}

// ============================================
// ORDER ROUTES
// ============================================

function handleOrders(string $method, array $segments): void {
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    $orderId = isset($segments[0]) && is_numeric($segments[0]) ? (int)$segments[0] : null;
    $action = $segments[1] ?? null;
    
    // Check for seller routes
    if ($segments[0] === 'seller') {
        handleSellerOrders($method, array_slice($segments, 1), $userId);
        return;
    }
    
    switch ($method) {
        case 'GET':
            if ($orderId) {
                getOrderById($userId, $orderId);
            } else {
                getUserOrders($userId);
            }
            break;
        case 'POST':
            if ($orderId && $action === 'verify') {
                verifyPayment($userId, $orderId);
            } else {
                createOrder($userId);
            }
            break;
        default:
            errorResponse('Method not allowed', 405);
    }
}

function createOrder(int $userId): void {
    $data = getJsonBody();
    
    if (empty($data['address_id'])) {
        errorResponse('address_id is required', 400);
    }
    
    $addressId = (int)$data['address_id'];
    $paymentMethod = in_array($data['payment_method'] ?? '', ['razorpay', 'cod']) ? $data['payment_method'] : 'razorpay';
    
    $db = getDB();
    
    // Verify address belongs to user
    $stmt = $db->prepare("SELECT * FROM user_addresses WHERE address_id = ? AND user_id = ?");
    $stmt->execute([$addressId, $userId]);
    $address = $stmt->fetch();
    if (!$address) {
        errorResponse('Address not found', 404);
    }
    
    // Get cart items - BOTH shop products and legacy listings
    // First, get shop product items
    $stmt = $db->prepare("
        SELECT ci.cart_item_id, ci.quantity, ci.product_id, ci.listing_id,
               sp.product_name as title, sp.price, sp.listing_id as business_listing_id,
               l.user_id as seller_id, 'product' as item_type
        FROM cart_items ci
        JOIN shop_products sp ON ci.product_id = sp.product_id
        JOIN listings l ON sp.listing_id = l.listing_id
        WHERE ci.user_id = ? AND ci.product_id IS NOT NULL AND sp.is_active = 1 AND l.status = 'active'
    ");
    $stmt->execute([$userId]);
    $productItems = $stmt->fetchAll();
    
    // Then, get legacy listing items (if any)
    $stmt = $db->prepare("
        SELECT ci.cart_item_id, ci.quantity, ci.product_id, ci.listing_id,
               l.title, 0 as price, ci.listing_id as business_listing_id,
               l.user_id as seller_id, 'listing' as item_type
        FROM cart_items ci
        JOIN listings l ON ci.listing_id = l.listing_id
        WHERE ci.user_id = ? AND ci.product_id IS NULL AND ci.listing_id IS NOT NULL AND l.status = 'active'
    ");
    $stmt->execute([$userId]);
    $listingItems = $stmt->fetchAll();
    
    $cartItems = array_merge($productItems, $listingItems);
    
    if (empty($cartItems)) {
        errorResponse('Cart is empty', 400);
    }
    
    // Calculate delivery estimate from pincode
    $deliveryEstimate = calculateDeliveryEstimate($address['pincode'] ?? '431513');
    
    // Calculate totals from shop_products prices
    $subtotal = 0;
    foreach ($cartItems as $item) {
        $subtotal += (float)$item['price'] * (int)$item['quantity'];
    }
    $shippingFee = $deliveryEstimate['shipping_fee'];
    $totalAmount = $subtotal + $shippingFee;
    
    try {
        $db->beginTransaction();
        
        // Generate order number
        $orderNumber = 'HH' . date('Ymd') . strtoupper(substr(uniqid(), -6));
        
        // Create order with delivery estimate
        $stmt = $db->prepare("
            INSERT INTO orders (order_number, user_id, address_id, subtotal, shipping_fee, total_amount, 
                               payment_method, payment_status, order_status, estimated_delivery_date, delivery_time_slot)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', 'pending', ?, ?)
        ");
        $stmt->execute([
            $orderNumber, $userId, $addressId, $subtotal, $shippingFee, $totalAmount, $paymentMethod,
            $deliveryEstimate['estimated_date'], $deliveryEstimate['delivery_time_slot']
        ]);
        $orderId = (int)$db->lastInsertId();
        
        // Create order items
        $stmt = $db->prepare("
            INSERT INTO order_items (order_id, listing_id, seller_id, quantity, price)
            VALUES (?, ?, ?, ?, ?)
        ");
        foreach ($cartItems as $item) {
            // For product items, use business_listing_id as the listing_id
            $listingId = $item['item_type'] === 'product' 
                ? (int)$item['business_listing_id'] 
                : (int)$item['listing_id'];
            $stmt->execute([
                $orderId,
                $listingId,
                (int)$item['seller_id'],
                (int)$item['quantity'],
                (float)$item['price']
            ]);
        }
        
        // Clear cart
        $db->prepare("DELETE FROM cart_items WHERE user_id = ?")->execute([$userId]);
        
        $db->commit();
        
        // For Razorpay, create Razorpay order
        $razorpayOrderId = null;
        $razorpayError = null;
        if ($paymentMethod === 'razorpay') {
            $razorpayResult = createRazorpayOrder($orderId, $totalAmount);
            $razorpayOrderId = $razorpayResult['id'];
            $razorpayError = $razorpayResult['error'];
            
            if ($razorpayOrderId) {
                $db->prepare("UPDATE orders SET razorpay_order_id = ? WHERE order_id = ?")->execute([$razorpayOrderId, $orderId]);
            } else {
                // Log the error but still return the order (user can retry payment)
                error_log("Razorpay order creation failed for order $orderId: $razorpayError");
            }
        } else {
            // COD - mark as confirmed
            $db->prepare("UPDATE orders SET order_status = 'confirmed' WHERE order_id = ?")->execute([$orderId]);
        }
        
        successResponse([
            'order_id' => $orderId,
            'order_number' => $orderNumber,
            'total_amount' => $totalAmount,
            'payment_method' => $paymentMethod,
            'razorpay_order_id' => $razorpayOrderId,
            'razorpay_error' => $razorpayError
        ], 201);
        
    } catch (Exception $e) {
        $db->rollBack();
        errorResponse('Failed to create order: ' . $e->getMessage(), 500);
    }
}

function createRazorpayOrder(int $orderId, float $amount): array {
    // Razorpay API credentials
    $keyId = 'rzp_live_RrqH1rKPqejvOQ';
    $keySecret = 'n25EPEXV9V6N5hMgb5tcgdXV';
    
    $amountInPaise = (int)($amount * 100);
    
    // Razorpay has a minimum amount of 100 paise (₹1)
    if ($amountInPaise < 100) {
        return ['id' => null, 'error' => 'Minimum order amount is ₹1'];
    }
    
    $orderData = [
        'amount' => $amountInPaise,
        'currency' => 'INR',
        'receipt' => 'order_' . $orderId,
        'notes' => ['order_id' => (string)$orderId]
    ];
    
    error_log("Creating Razorpay order for orderId: $orderId, amount: $amount (paise: $amountInPaise)");
    error_log("Request data: " . json_encode($orderData));
    
    $ch = curl_init('https://api.razorpay.com/v1/orders');
    curl_setopt($ch, CURLOPT_USERPWD, $keyId . ':' . $keySecret);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($orderData));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    // SSL options - try without verification if server has SSL issues
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    $curlErrno = curl_errno($ch);
    curl_close($ch);
    
    error_log("Razorpay API response - HTTP Code: $httpCode, Response: $response, cURL Error: $curlError (errno: $curlErrno)");
    
    // Handle cURL errors
    if ($curlErrno !== 0) {
        error_log("cURL error occurred: $curlError");
        return ['id' => null, 'error' => "Network error: $curlError"];
    }
    
    if ($httpCode === 200) {
        $result = json_decode($response, true);
        $razorpayOrderId = $result['id'] ?? null;
        error_log("Razorpay order created successfully: $razorpayOrderId");
        return ['id' => $razorpayOrderId, 'error' => null];
    }
    
    // Parse Razorpay error
    $errorMsg = "Razorpay API error (HTTP $httpCode)";
    $responseData = json_decode($response, true);
    if ($responseData && isset($responseData['error'])) {
        $errorMsg = $responseData['error']['description'] ?? $responseData['error']['code'] ?? $errorMsg;
    }
    
    error_log("Razorpay order creation FAILED - HTTP $httpCode: $response");
    return ['id' => null, 'error' => $errorMsg];
}

function verifyPayment(int $userId, int $orderId): void {
    $data = getJsonBody();
    
    if (empty($data['razorpay_payment_id']) || empty($data['razorpay_signature'])) {
        errorResponse('Payment verification data required', 400);
    }
    
    $db = getDB();
    
    // Get order
    $stmt = $db->prepare("SELECT * FROM orders WHERE order_id = ? AND user_id = ?");
    $stmt->execute([$orderId, $userId]);
    $order = $stmt->fetch();
    
    if (!$order) {
        errorResponse('Order not found', 404);
    }
    
    // Verify signature
    $keySecret = 'n25EPEXV9V6N5hMgb5tcgdXV';
    $expectedSignature = hash_hmac('sha256', $order['razorpay_order_id'] . '|' . $data['razorpay_payment_id'], $keySecret);
    
    if ($expectedSignature === $data['razorpay_signature']) {
        // Payment verified
        $stmt = $db->prepare("
            UPDATE orders 
            SET payment_status = 'paid', razorpay_payment_id = ?, order_status = 'confirmed'
            WHERE order_id = ?
        ");
        $stmt->execute([$data['razorpay_payment_id'], $orderId]);
        
        successResponse(['message' => 'Payment verified', 'order_status' => 'confirmed']);
    } else {
        // Payment failed
        $db->prepare("UPDATE orders SET payment_status = 'failed' WHERE order_id = ?")->execute([$orderId]);
        errorResponse('Payment verification failed', 400);
    }
}

function getUserOrders(int $userId): void {
    $db = getDB();
    
    $stmt = $db->prepare("
        SELECT o.*, 
               (SELECT COUNT(*) FROM order_items WHERE order_id = o.order_id) as item_count
        FROM orders o
        WHERE o.user_id = ?
        ORDER BY o.created_at DESC
    ");
    $stmt->execute([$userId]);
    $orders = $stmt->fetchAll();
    
    $result = array_map(function($order) {
        // Build delivery message
        $deliveryMessage = null;
        if (!empty($order['estimated_delivery_date'])) {
            $estDate = strtotime($order['estimated_delivery_date']);
            $today = strtotime('today');
            $tomorrow = strtotime('tomorrow');
            
            if ($estDate == $today) {
                $deliveryMessage = 'Arriving today' . ($order['delivery_time_slot'] ? ', ' . $order['delivery_time_slot'] : '');
            } elseif ($estDate == $tomorrow) {
                $deliveryMessage = 'Arriving tomorrow' . ($order['delivery_time_slot'] ? ', ' . $order['delivery_time_slot'] : '');
            } else {
                $dayName = date('l, M j', $estDate);
                $deliveryMessage = "Arriving {$dayName}" . ($order['delivery_time_slot'] ? ', ' . $order['delivery_time_slot'] : '');
            }
        }
        
        return [
            'order_id' => (int)$order['order_id'],
            'order_number' => $order['order_number'],
            'total_amount' => (float)$order['total_amount'],
            'item_count' => (int)$order['item_count'],
            'payment_method' => $order['payment_method'],
            'payment_status' => $order['payment_status'],
            'order_status' => $order['order_status'],
            'estimated_delivery_date' => $order['estimated_delivery_date'] ?? null,
            'delivery_time_slot' => $order['delivery_time_slot'] ?? null,
            'delivery_message' => $deliveryMessage,
            'created_at' => $order['created_at']
        ];
    }, $orders);
    
    successResponse($result);
}

function getOrderById(int $userId, int $orderId): void {
    $db = getDB();
    
    // Get order
    $stmt = $db->prepare("
        SELECT o.*, a.name as address_name, a.phone as address_phone,
               a.address_line1, a.address_line2, a.city, a.state, a.pincode
        FROM orders o
        JOIN user_addresses a ON o.address_id = a.address_id
        WHERE o.order_id = ? AND o.user_id = ?
    ");
    $stmt->execute([$orderId, $userId]);
    $order = $stmt->fetch();
    
    if (!$order) {
        errorResponse('Order not found', 404);
    }
    
    // Get order items
    $stmt = $db->prepare("
        SELECT oi.*, l.title, l.main_image_url, u.username as seller_name
        FROM order_items oi
        JOIN listings l ON oi.listing_id = l.listing_id
        JOIN users u ON oi.seller_id = u.user_id
        WHERE oi.order_id = ?
    ");
    $stmt->execute([$orderId]);
    $items = $stmt->fetchAll();
    
    // Build delivery message
    $deliveryMessage = null;
    if (!empty($order['estimated_delivery_date'])) {
        $estDate = strtotime($order['estimated_delivery_date']);
        $today = strtotime('today');
        $tomorrow = strtotime('tomorrow');
        
        if ($estDate == $today) {
            $deliveryMessage = 'Arriving today' . ($order['delivery_time_slot'] ? ', ' . $order['delivery_time_slot'] : '');
        } elseif ($estDate == $tomorrow) {
            $deliveryMessage = 'Arriving tomorrow' . ($order['delivery_time_slot'] ? ', ' . $order['delivery_time_slot'] : '');
        } else {
            $dayName = date('l, M j', $estDate);
            $deliveryMessage = "Arriving {$dayName}" . ($order['delivery_time_slot'] ? ', ' . $order['delivery_time_slot'] : '');
        }
    }
    
    successResponse([
        'order_id' => (int)$order['order_id'],
        'order_number' => $order['order_number'],
        'subtotal' => (float)$order['subtotal'],
        'shipping_fee' => (float)$order['shipping_fee'],
        'total_amount' => (float)$order['total_amount'],
        'payment_method' => $order['payment_method'],
        'payment_status' => $order['payment_status'],
        'order_status' => $order['order_status'],
        'estimated_delivery_date' => $order['estimated_delivery_date'] ?? null,
        'delivery_time_slot' => $order['delivery_time_slot'] ?? null,
        'delivery_message' => $deliveryMessage,
        'created_at' => $order['created_at'],
        'address' => [
            'name' => $order['address_name'],
            'phone' => $order['address_phone'],
            'address_line1' => $order['address_line1'],
            'address_line2' => $order['address_line2'],
            'city' => $order['city'],
            'state' => $order['state'],
            'pincode' => $order['pincode']
        ],
        'items' => array_map(function($item) {
            return [
                'order_item_id' => (int)$item['order_item_id'],
                'listing_id' => (int)$item['listing_id'],
                'title' => $item['title'],
                'main_image_url' => $item['main_image_url'],
                'quantity' => (int)$item['quantity'],
                'price' => (float)$item['price'],
                'seller_name' => $item['seller_name'],
                'item_status' => $item['item_status']
            ];
        }, $items)
    ]);
}

function handleSellerOrders(string $method, array $segments, int $sellerId): void {
    $orderItemId = isset($segments[0]) && is_numeric($segments[0]) ? (int)$segments[0] : null;
    
    if ($method === 'GET') {
        getSellerOrders($sellerId);
    } elseif ($method === 'PUT' && $orderItemId) {
        updateOrderItemStatus($sellerId, $orderItemId);
    } else {
        errorResponse('Method not allowed', 405);
    }
}

function getSellerOrders(int $sellerId): void {
    $db = getDB();
    
    $stmt = $db->prepare("
        SELECT oi.*, o.order_number, o.created_at as order_date,
               l.title, l.main_image_url,
               a.name as buyer_name, a.phone as buyer_phone, a.city as buyer_city
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.order_id
        JOIN listings l ON oi.listing_id = l.listing_id
        JOIN user_addresses a ON o.address_id = a.address_id
        WHERE oi.seller_id = ?
        ORDER BY o.created_at DESC
    ");
    $stmt->execute([$sellerId]);
    $items = $stmt->fetchAll();
    
    $result = array_map(function($item) {
        return [
            'order_item_id' => (int)$item['order_item_id'],
            'order_number' => $item['order_number'],
            'title' => $item['title'],
            'main_image_url' => $item['main_image_url'],
            'quantity' => (int)$item['quantity'],
            'price' => (float)$item['price'],
            'item_status' => $item['item_status'],
            'buyer_name' => $item['buyer_name'],
            'buyer_phone' => $item['buyer_phone'],
            'buyer_city' => $item['buyer_city'],
            'order_date' => $item['order_date']
        ];
    }, $items);
    
    successResponse($result);
}

function updateOrderItemStatus(int $sellerId, int $orderItemId): void {
    $data = getJsonBody();
    
    $validStatuses = ['confirmed', 'shipped', 'delivered', 'cancelled'];
    if (empty($data['status']) || !in_array($data['status'], $validStatuses)) {
        errorResponse('Valid status required: ' . implode(', ', $validStatuses), 400);
    }
    
    $db = getDB();
    
    // Verify seller owns this item
    $stmt = $db->prepare("SELECT order_item_id FROM order_items WHERE order_item_id = ? AND seller_id = ?");
    $stmt->execute([$orderItemId, $sellerId]);
    if (!$stmt->fetch()) {
        errorResponse('Order item not found', 404);
    }
    
    $db->prepare("UPDATE order_items SET item_status = ? WHERE order_item_id = ?")->execute([$data['status'], $orderItemId]);
    
    successResponse(['message' => 'Status updated to ' . $data['status']]);
}

// ============================================
// DELIVERY ROUTES
// ============================================

function handleDelivery(string $method, array $segments): void {
    $action = $segments[0] ?? '';
    
    // Delivery App endpoints (POST/GET with various actions)
    switch ($action) {
        // ===== Delivery App Endpoints =====
        case 'register':
            if ($method === 'POST') deliveryRegister();
            else errorResponse('Method not allowed', 405);
            break;
            
        case 'send-otp':
            if ($method === 'POST') deliverySendOTP();
            else errorResponse('Method not allowed', 405);
            break;
            
        case 'verify-otp':
            if ($method === 'POST') deliveryVerifyOTP();
            else errorResponse('Method not allowed', 405);
            break;
            
        case 'available-orders':
            if ($method === 'GET') {
                $user = requireDeliveryAuth();
                getAvailableOrders($user['delivery_user_id']);
            } else errorResponse('Method not allowed', 405);
            break;
            
        case 'accept-order':
            if ($method === 'POST') {
                $user = requireDeliveryAuth();
                acceptOrder($user['delivery_user_id']);
            } else errorResponse('Method not allowed', 405);
            break;
            
        case 'cancel-order':
            if ($method === 'POST') {
                $user = requireDeliveryAuth();
                cancelOrder($user['delivery_user_id']);
            } else errorResponse('Method not allowed', 405);
            break;
            
        case 'my-orders':
            if ($method === 'GET') {
                $user = requireDeliveryAuth();
                getMyDeliveries($user['delivery_user_id']);
            } else errorResponse('Method not allowed', 405);
            break;
            
        case 'update-status':
            if ($method === 'POST') {
                $user = requireDeliveryAuth();
                updateDeliveryStatus($user['delivery_user_id']);
            } else errorResponse('Method not allowed', 405);
            break;
            
        case 'earnings':
            if ($method === 'GET') {
                $user = requireDeliveryAuth();
                getDeliveryEarnings($user['delivery_user_id']);
            } else errorResponse('Method not allowed', 405);
            break;
            
        case 'profile':
            $user = requireDeliveryAuth();
            if ($method === 'GET') getDeliveryProfile($user['delivery_user_id']);
            elseif ($method === 'PUT') updateDeliveryProfile($user['delivery_user_id']);
            else errorResponse('Method not allowed', 405);
            break;
        
        // ===== Original Delivery Check Endpoints =====
        case 'check':
            if ($method === 'GET') checkDeliveryPincode();
            else errorResponse('Method not allowed', 405);
            break;
            
        case 'pincodes':
            if ($method === 'GET') getServiceablePincodes();
            else errorResponse('Method not allowed', 405);
            break;
            
        default:
            errorResponse('Delivery endpoint not found', 404);
    }
}

/**
 * Check delivery estimate for a pincode
 * GET /delivery/check?pincode=431513
 */
function checkDeliveryPincode(): void {
    $pincode = getQueryParam('pincode');
    
    if (empty($pincode) || strlen($pincode) !== 6) {
        errorResponse('Valid 6-digit pincode required', 400);
    }
    
    $db = getDB();
    
    // Check if pincode is in our service area
    $stmt = $db->prepare("SELECT * FROM service_pincodes WHERE pincode = ? AND is_serviceable = 1");
    $stmt->execute([$pincode]);
    $pincodeData = $stmt->fetch();
    
    if (!$pincodeData) {
        // Not in our service area - still serviceable but slower
        successResponse([
            'serviceable' => true,
            'pincode' => $pincode,
            'city_name' => null,
            'delivery_days' => 5,
            'delivery_time' => null,
            'shipping_fee' => 100.00,
            'estimated_date' => date('Y-m-d', strtotime('+5 days')),
            'message' => 'Delivery in 5-7 business days'
        ]);
        return;
    }
    
    // Calculate estimated delivery date
    $deliveryDays = (int)$pincodeData['delivery_days'];
    $deliveryTime = $pincodeData['delivery_time'];
    $cutoffHour = (int)$pincodeData['cutoff_hour'];
    $currentHour = (int)date('H');
    
    // If past cutoff, add 1 day
    if ($currentHour >= $cutoffHour) {
        $deliveryDays++;
    }
    
    $estimatedDate = date('Y-m-d', strtotime("+{$deliveryDays} days"));
    
    // Generate user-friendly message
    if ($deliveryDays === 0) {
        $message = "Get it by today, {$deliveryTime}";
    } elseif ($deliveryDays === 1) {
        $message = "Get it by tomorrow, {$deliveryTime}";
    } else {
        $dayName = date('l', strtotime($estimatedDate));
        $message = "Get it by {$dayName}, {$deliveryTime}";
    }
    
    successResponse([
        'serviceable' => true,
        'pincode' => $pincode,
        'city_name' => $pincodeData['city_name'],
        'delivery_days' => $deliveryDays,
        'delivery_time' => $deliveryTime,
        'shipping_fee' => (float)$pincodeData['shipping_fee'],
        'estimated_date' => $estimatedDate,
        'message' => $message
    ]);
}

/**
 * Get all serviceable pincodes
 * GET /delivery/pincodes
 */
function getServiceablePincodes(): void {
    $db = getDB();
    
    $stmt = $db->query("
        SELECT pincode, city_name, delivery_days, delivery_time, shipping_fee
        FROM service_pincodes 
        WHERE is_serviceable = 1
        ORDER BY delivery_days, city_name
    ");
    $pincodes = $stmt->fetchAll();
    
    // Group by delivery days
    $result = [
        'same_day' => [],
        'next_day' => [],
        'standard' => []
    ];
    
    foreach ($pincodes as $p) {
        $item = [
            'pincode' => $p['pincode'],
            'city_name' => $p['city_name'],
            'delivery_time' => $p['delivery_time'],
            'shipping_fee' => (float)$p['shipping_fee']
        ];
        
        if ((int)$p['delivery_days'] === 0) {
            $result['same_day'][] = $item;
        } elseif ((int)$p['delivery_days'] === 1) {
            $result['next_day'][] = $item;
        } else {
            $result['standard'][] = $item;
        }
    }
    
    successResponse($result);
}

/**
 * Calculate estimated delivery for an order (helper function)
 */
function calculateDeliveryEstimate(string $pincode): array {
    $db = getDB();
    
    $stmt = $db->prepare("SELECT * FROM service_pincodes WHERE pincode = ? AND is_serviceable = 1");
    $stmt->execute([$pincode]);
    $pincodeData = $stmt->fetch();
    
    $deliveryDays = 5; // Default for unknown pincodes
    $deliveryTime = null;
    $shippingFee = 100.00;
    
    if ($pincodeData) {
        $deliveryDays = (int)$pincodeData['delivery_days'];
        $deliveryTime = $pincodeData['delivery_time'];
        $cutoffHour = (int)$pincodeData['cutoff_hour'];
        $shippingFee = (float)$pincodeData['shipping_fee'];
        
        // If past cutoff, add 1 day
        if ((int)date('H') >= $cutoffHour) {
            $deliveryDays++;
        }
    }
    
    $estimatedDate = date('Y-m-d', strtotime("+{$deliveryDays} days"));
    
    return [
        'estimated_date' => $estimatedDate,
        'delivery_time_slot' => $deliveryTime ? "by {$deliveryTime}" : null,
        'shipping_fee' => $shippingFee
    ];
}

// ============================================
// ENQUIRIES ROUTES
// ============================================

/**
 * Enquiries router
 * POST /enquiries - Log a contact attempt (call/chat/contact_form)
 * GET /enquiries - Get enquiries for current user (their listings)
 */
function handleEnquiries(string $method, array $segments): void {
    switch ($method) {
        case 'POST':
            createEnquiry();
            break;
        case 'GET':
            getEnquiries();
            break;
        default:
            errorResponse('Method not allowed', 405);
    }
}

/**
 * Create an enquiry (log contact attempt)
 * POST /enquiries
 * Body: { listing_id, enquiry_type: "call"|"chat"|"contact_form", message?: string }
 */
function createEnquiry(): void {
    $db = getDB();
    
    error_log("createEnquiry: Starting enquiry creation");
    
    // Auth is optional - guests can also make enquiries
    $userId = null;
    $userName = 'Guest';
    $userPhone = '';
    
    try {
        $auth = getAuthUser();
        if ($auth && isset($auth['user_id'])) {
            $userId = $auth['user_id'];
            error_log("createEnquiry: Authenticated user_id=$userId");
            
            // Get user details
            $stmt = $db->prepare("SELECT username, phone, email FROM users WHERE user_id = ?");
            $stmt->execute([$userId]);
            $user = $stmt->fetch();
            if ($user) {
                $userName = $user['username'] ?? 'User';
                $userPhone = $user['phone'] ?? '';
            }
        } else {
            error_log("createEnquiry: Guest enquiry (no auth token)");
        }
    } catch (Exception $e) {
        error_log("createEnquiry: Guest enquiry (auth error: " . $e->getMessage() . ")");
        // Guest enquiry - continue
    }
    
    $data = getJsonBody();
    error_log("createEnquiry: Request data = " . json_encode($data));
    
    if (empty($data['listing_id'])) {
        error_log("createEnquiry: Missing listing_id");
        errorResponse('Listing ID is required', 400);
    }
    
    $listingId = (int)$data['listing_id'];
    $enquiryType = $data['enquiry_type'] ?? 'contact_form';
    $message = $data['message'] ?? null;
    
    // Validate enquiry type
    $validTypes = ['call', 'chat', 'contact_form', 'whatsapp'];
    if (!in_array($enquiryType, $validTypes)) {
        $enquiryType = 'contact_form';
    }
    
    // Get listing info for context
    $stmt = $db->prepare("SELECT title, user_id as owner_id FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        error_log("createEnquiry: Listing $listingId not found");
        errorResponse('Listing not found', 404);
    }
    
    // Don't log enquiry if user is contacting their own listing
    if ($userId && $listing['owner_id'] == $userId) {
        error_log("createEnquiry: Own listing, skipping");
        successResponse(['message' => 'Own listing, not logged']);
        return;
    }
    
    // Check for duplicate within last 5 minutes (prevent spam)
    $stmt = $db->prepare("
        SELECT enquiry_id FROM enquiries 
        WHERE listing_id = ? 
        AND (user_id = ? OR (user_id IS NULL AND phone = ?))
        AND enquiry_type = ?
        AND created_at > DATE_SUB(NOW(), INTERVAL 5 MINUTE)
    ");
    $stmt->execute([$listingId, $userId, $userPhone, $enquiryType]);
    if ($stmt->fetch()) {
        error_log("createEnquiry: Duplicate enquiry detected, skipping");
        // Already logged recently
        successResponse(['message' => 'Already logged']);
        return;
    }
    
    // Insert enquiry
    error_log("createEnquiry: Inserting - listing=$listingId, user=$userId, name=$userName, phone=$userPhone, type=$enquiryType");
    $stmt = $db->prepare("
        INSERT INTO enquiries (listing_id, user_id, name, phone, message, enquiry_type, status)
        VALUES (?, ?, ?, ?, ?, ?, 'new')
    ");
    $stmt->execute([
        $listingId,
        $userId,
        $userName,
        $userPhone,
        $message,
        $enquiryType
    ]);
    
    $enquiryId = $db->lastInsertId();
    error_log("createEnquiry: SUCCESS - enquiry_id=$enquiryId");
    
    successResponse([
        'success' => true,
        'enquiry_id' => (int)$enquiryId,
        'message' => 'Enquiry logged successfully'
    ]);
}

/**
 * Get enquiries for listings owned by current user 
 * GET /enquiries
 */
function getEnquiries(): void {
    $auth = requireAuth();
    $userId = $auth['user_id'];
    $db = getDB();
    
    $stmt = $db->prepare("
        SELECT e.*, l.title as listing_title
        FROM enquiries e
        JOIN listings l ON e.listing_id = l.listing_id
        WHERE l.user_id = ?
        ORDER BY e.created_at DESC
        LIMIT 100
    ");
    $stmt->execute([$userId]);
    $enquiries = $stmt->fetchAll();
    
    successResponse(['enquiries' => $enquiries]);
}

// ============================================
// RAZORPAY WEBHOOK ROUTES
// ============================================

/**
 * Webhook router
 */
function handleWebhook(string $method, array $segments): void {
    $action = $segments[0] ?? '';
    
    // Webhooks are always POST
    if ($method !== 'POST') {
        errorResponse('Method not allowed', 405);
    }
    
    switch ($action) {
        case 'razorpay':
            handleRazorpayWebhook();
            break;
        default:
            errorResponse('Webhook endpoint not found', 404);
    }
}

/**
 * Handle Razorpay webhook events
 * Endpoint: POST /webhook/razorpay
 * 
 * Handles:
 * - payment.captured: Payment successful, update order to paid
 * - payment.failed: Payment failed, log for reference
 * - refund.created: Refund processed, update order status
 */
function handleRazorpayWebhook(): void {
    // Get raw payload (don't use getJsonBody as we need raw for signature)
    $payload = file_get_contents('php://input');
    
    // Log for debugging
    error_log("Razorpay Webhook received: " . substr($payload, 0, 500) . "...");
    
    // Get webhook signature from header
    $signature = $_SERVER['HTTP_X_RAZORPAY_SIGNATURE'] ?? '';
    
    // Razorpay webhook secret (set this in Razorpay Dashboard > Webhooks)
    // TODO: Move this to config file
    $webhookSecret = 'hellohingoli_webhook_secret_2024'; 
    
    // Verify signature
    $expectedSignature = hash_hmac('sha256', $payload, $webhookSecret);
    
    if (!hash_equals($expectedSignature, $signature)) {
        error_log("Razorpay webhook: Invalid signature. Expected: $expectedSignature, Got: $signature");
        http_response_code(401);
        echo json_encode(['success' => false, 'error' => 'Invalid signature']);
        exit;
    }
    
    $event = json_decode($payload, true);
    if (!$event) {
        error_log("Razorpay webhook: Invalid JSON payload");
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Invalid payload']);
        exit;
    }
    
    $eventType = $event['event'] ?? '';
    error_log("Razorpay webhook verified: $eventType");
    
    switch ($eventType) {
        case 'payment.captured':
            handlePaymentCaptured($event);
            break;
            
        case 'payment.authorized':
            // For auto-capture disabled accounts - treat like captured
            handlePaymentCaptured($event);
            break;
            
        case 'payment.failed':
            handlePaymentFailed($event);
            break;
            
        case 'order.paid':
            // Alternative to payment.captured - handles order-level payment
            handleOrderPaid($event);
            break;
            
        case 'refund.created':
        case 'refund.processed':
            handleRefundCreated($event);
            break;
            
        case 'refund.failed':
            handleRefundFailed($event);
            break;
            
        case 'payment.dispute.created':
            handleDisputeCreated($event);
            break;
            
        case 'payment.dispute.won':
            handleDisputeResolved($event, 'won');
            break;
            
        case 'payment.dispute.lost':
            handleDisputeResolved($event, 'lost');
            break;
            
        case 'payment.dispute.closed':
        case 'payment.dispute.under_review':
        case 'payment.dispute.action_required':
            // Log these but no action needed
            error_log("Razorpay webhook: Dispute event - $eventType");
            http_response_code(200);
            echo json_encode(['success' => true, 'status' => 'logged', 'event' => $eventType]);
            exit;
            
        default:
            // Acknowledge but ignore unknown events
            error_log("Razorpay webhook: Ignoring event type: $eventType");
            http_response_code(200);
            echo json_encode(['success' => true, 'status' => 'ignored', 'event' => $eventType]);
            exit;
    }
}

/**
 * Handle payment.captured event
 * This is the most important webhook - marks order as paid
 */
function handlePaymentCaptured(array $event): void {
    $db = getDB();
    
    $payment = $event['payload']['payment']['entity'] ?? [];
    $razorpayOrderId = $payment['order_id'] ?? '';
    $razorpayPaymentId = $payment['id'] ?? '';
    $amount = floatval($payment['amount'] ?? 0) / 100; // Convert from paise to rupees
    $method = $payment['method'] ?? ''; // upi, card, netbanking, wallet
    $email = $payment['email'] ?? '';
    $contact = $payment['contact'] ?? '';
    
    error_log("Webhook payment.captured - Order: $razorpayOrderId, Payment: $razorpayPaymentId, Amount: ₹$amount");
    
    if (empty($razorpayOrderId) || empty($razorpayPaymentId)) {
        error_log("Webhook: Missing order_id or payment_id");
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Missing required fields']);
        exit;
    }
    
    // Find order by razorpay_order_id
    $stmt = $db->prepare("SELECT * FROM orders WHERE razorpay_order_id = ?");
    $stmt->execute([$razorpayOrderId]);
    $order = $stmt->fetch();
    
    if (!$order) {
        error_log("Webhook: Order not found for razorpay_order_id: $razorpayOrderId");
        // Return 200 anyway to prevent Razorpay from retrying
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'order_not_found', 'message' => 'Order not in database']);
        exit;
    }
    
    // Idempotency check - only update if not already paid
    if ($order['payment_status'] === 'paid') {
        error_log("Webhook: Order {$order['order_id']} already marked as paid (idempotency check)");
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'already_paid', 'order_id' => $order['order_id']]);
        exit;
    }
    
    // Verify amount matches (optional but recommended)
    $expectedAmount = floatval($order['total_amount']);
    if (abs($amount - $expectedAmount) > 0.01) {
        error_log("Webhook: Amount mismatch - Expected: ₹$expectedAmount, Got: ₹$amount");
        // Still process but log the discrepancy
    }
    
    // Update order to paid status
    $stmt = $db->prepare("
        UPDATE orders 
        SET payment_status = 'paid', 
            razorpay_payment_id = ?, 
            order_status = 'confirmed',
            payment_method_detail = ?,
            webhook_verified = 1,
            webhook_verified_at = NOW()
        WHERE order_id = ?
    ");
    $stmt->execute([$razorpayPaymentId, $method, $order['order_id']]);
    
    error_log("Webhook: Order {$order['order_id']} marked as PAID via webhook (₹$amount, method: $method)");
    
    // Send push notification to user about successful payment
    try {
        sendPaymentConfirmationNotification($order['user_id'], $order['order_id'], $order['order_number'], $amount);
    } catch (Exception $e) {
        error_log("Webhook: Failed to send notification - " . $e->getMessage());
    }
    
    http_response_code(200);
    echo json_encode([
        'success' => true, 
        'status' => 'payment_captured',
        'order_id' => $order['order_id'],
        'order_number' => $order['order_number']
    ]);
    exit;
}

/**
 * Handle payment.failed event
 * Log the failure - user might retry so don't immediately mark as failed
 */
function handlePaymentFailed(array $event): void {
    $db = getDB();
    
    $payment = $event['payload']['payment']['entity'] ?? [];
    $razorpayOrderId = $payment['order_id'] ?? '';
    $razorpayPaymentId = $payment['id'] ?? '';
    $errorCode = $payment['error_code'] ?? 'UNKNOWN';
    $errorDescription = $payment['error_description'] ?? 'Unknown error';
    $errorReason = $payment['error_reason'] ?? '';
    
    error_log("Webhook payment.failed - Order: $razorpayOrderId, Error: $errorCode - $errorDescription ($errorReason)");
    
    if (empty($razorpayOrderId)) {
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'missing_order_id']);
        exit;
    }
    
    // Find order
    $stmt = $db->prepare("SELECT * FROM orders WHERE razorpay_order_id = ?");
    $stmt->execute([$razorpayOrderId]);
    $order = $stmt->fetch();
    
    if ($order && $order['payment_status'] === 'pending') {
        // Log the failure but don't mark as failed yet (user might retry)
        // Only mark as failed if order is old (e.g., > 30 minutes)
        $orderAge = time() - strtotime($order['created_at']);
        
        if ($orderAge > 1800) { // 30 minutes
            $stmt = $db->prepare("
                UPDATE orders 
                SET payment_status = 'failed',
                    payment_error = ?
                WHERE order_id = ? AND payment_status = 'pending'
            ");
            $stmt->execute(["$errorCode: $errorDescription", $order['order_id']]);
            error_log("Webhook: Order {$order['order_id']} marked as FAILED (stale order)");
        } else {
            error_log("Webhook: Payment failed for order {$order['order_id']} but order is recent - user may retry");
        }
    }
    
    http_response_code(200);
    echo json_encode(['success' => true, 'status' => 'acknowledged']);
    exit;
}

/**
 * Handle refund.created event
 * Update order status when refund is processed
 */
function handleRefundCreated(array $event): void {
    $db = getDB();
    
    $refund = $event['payload']['refund']['entity'] ?? [];
    $razorpayPaymentId = $refund['payment_id'] ?? '';
    $refundAmount = floatval($refund['amount'] ?? 0) / 100; // Convert from paise
    $refundId = $refund['id'] ?? '';
    $status = $refund['status'] ?? '';
    
    error_log("Webhook refund.created - Payment: $razorpayPaymentId, Refund: $refundId, Amount: ₹$refundAmount");
    
    if (empty($razorpayPaymentId)) {
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'missing_payment_id']);
        exit;
    }
    
    // Find order by payment_id
    $stmt = $db->prepare("SELECT * FROM orders WHERE razorpay_payment_id = ?");
    $stmt->execute([$razorpayPaymentId]);
    $order = $stmt->fetch();
    
    if (!$order) {
        error_log("Webhook: Order not found for razorpay_payment_id: $razorpayPaymentId");
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'order_not_found']);
        exit;
    }
    
    // Determine if full or partial refund
    $orderAmount = floatval($order['total_amount']);
    $isFullRefund = abs($refundAmount - $orderAmount) < 0.01;
    $paymentStatus = $isFullRefund ? 'refunded' : 'partially_refunded';
    
    // Update order status
    $stmt = $db->prepare("
        UPDATE orders 
        SET payment_status = ?, 
            order_status = 'refunded',
            refund_id = ?,
            refund_amount = ?
        WHERE order_id = ?
    ");
    $stmt->execute([$paymentStatus, $refundId, $refundAmount, $order['order_id']]);
    
    error_log("Webhook: Order {$order['order_id']} refunded - ₹$refundAmount ($paymentStatus)");
    
    // Notify user about refund
    try {
        sendRefundNotification($order['user_id'], $order['order_id'], $order['order_number'], $refundAmount);
    } catch (Exception $e) {
        error_log("Webhook: Failed to send refund notification - " . $e->getMessage());
    }
    
    http_response_code(200);
    echo json_encode([
        'success' => true, 
        'status' => 'refund_processed',
        'order_id' => $order['order_id'],
        'refund_amount' => $refundAmount
    ]);
    exit;
}

/**
 * Send payment confirmation notification
 */
function sendPaymentConfirmationNotification(int $userId, int $orderId, string $orderNumber, float $amount): void {
    $db = getDB();
    
    // Get user FCM tokens
    $stmt = $db->prepare("SELECT fcm_token FROM user_fcm_tokens WHERE user_id = ? AND fcm_token IS NOT NULL AND fcm_token != ''");
    $stmt->execute([$userId]);
    $tokens = $stmt->fetchAll(PDO::FETCH_COLUMN);
    
    if (empty($tokens)) {
        return;
    }
    
    $title = "💳 Payment Successful!";
    $body = "Your payment of ₹" . number_format($amount, 0) . " for order #$orderNumber has been confirmed.";
    
    // Save notification to history
    $stmt = $db->prepare("
        INSERT INTO notification_history (user_id, title, body, type, deep_link)
        VALUES (?, ?, ?, 'payment_success', ?)
    ");
    $stmt->execute([$userId, $title, $body, "order/$orderId"]);
    
    // Send FCM notification (if you have FCM configured)
    // sendFcmNotification($tokens, $title, $body, ['order_id' => $orderId]);
}

/**
 * Send refund notification
 */
function sendRefundNotification(int $userId, int $orderId, string $orderNumber, float $amount): void {
    $db = getDB();
    
    $title = "💸 Refund Processed";
    $body = "₹" . number_format($amount, 0) . " has been refunded for order #$orderNumber.";
    
    // Save notification to history
    $stmt = $db->prepare("
        INSERT INTO notification_history (user_id, title, body, type, deep_link)
        VALUES (?, ?, ?, 'refund', ?)
    ");
    $stmt->execute([$userId, $title, $body, "order/$orderId"]);
}

/**
 * Handle order.paid event
 * This is triggered when an order is fully paid (alternative to payment.captured)
 */
function handleOrderPaid(array $event): void {
    $db = getDB();
    
    $order = $event['payload']['order']['entity'] ?? [];
    $razorpayOrderId = $order['id'] ?? '';
    $amount = floatval($order['amount_paid'] ?? 0) / 100;
    $status = $order['status'] ?? '';
    
    error_log("Webhook order.paid - Order: $razorpayOrderId, Amount: ₹$amount, Status: $status");
    
    if (empty($razorpayOrderId)) {
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'missing_order_id']);
        exit;
    }
    
    // Find order by razorpay_order_id
    $stmt = $db->prepare("SELECT * FROM orders WHERE razorpay_order_id = ?");
    $stmt->execute([$razorpayOrderId]);
    $dbOrder = $stmt->fetch();
    
    if (!$dbOrder) {
        error_log("Webhook: Order not found for razorpay_order_id: $razorpayOrderId");
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'order_not_found']);
        exit;
    }
    
    // Only update if not already paid
    if ($dbOrder['payment_status'] !== 'paid') {
        $stmt = $db->prepare("
            UPDATE orders 
            SET payment_status = 'paid', 
                order_status = 'confirmed',
                webhook_verified = 1,
                webhook_verified_at = NOW()
            WHERE order_id = ?
        ");
        $stmt->execute([$dbOrder['order_id']]);
        
        error_log("Webhook: Order {$dbOrder['order_id']} marked as PAID via order.paid event");
        
        // Send notification
        try {
            sendPaymentConfirmationNotification($dbOrder['user_id'], $dbOrder['order_id'], $dbOrder['order_number'], $amount);
        } catch (Exception $e) {
            error_log("Webhook: Failed to send notification - " . $e->getMessage());
        }
    }
    
    http_response_code(200);
    echo json_encode(['success' => true, 'status' => 'order_paid_processed']);
    exit;
}

/**
 * Handle refund.failed event
 */
function handleRefundFailed(array $event): void {
    $db = getDB();
    
    $refund = $event['payload']['refund']['entity'] ?? [];
    $razorpayPaymentId = $refund['payment_id'] ?? '';
    $refundId = $refund['id'] ?? '';
    $errorCode = $refund['error_code'] ?? 'UNKNOWN';
    $errorDescription = $refund['error_description'] ?? 'Unknown error';
    
    error_log("Webhook refund.failed - Payment: $razorpayPaymentId, Refund: $refundId, Error: $errorCode - $errorDescription");
    
    if (empty($razorpayPaymentId)) {
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'missing_payment_id']);
        exit;
    }
    
    // Find order
    $stmt = $db->prepare("SELECT * FROM orders WHERE razorpay_payment_id = ?");
    $stmt->execute([$razorpayPaymentId]);
    $order = $stmt->fetch();
    
    if ($order) {
        // Log the refund failure - admin should be notified
        error_log("ALERT: Refund FAILED for Order {$order['order_id']} ({$order['order_number']}): $errorCode - $errorDescription");
        
        // Could send an alert to admin here
        // sendAdminAlert("Refund failed for order #{$order['order_number']}", "$errorCode: $errorDescription");
    }
    
    http_response_code(200);
    echo json_encode(['success' => true, 'status' => 'refund_failure_logged']);
    exit;
}

/**
 * Handle dispute created event (chargeback initiated)
 */
function handleDisputeCreated(array $event): void {
    $db = getDB();
    
    $dispute = $event['payload']['dispute']['entity'] ?? [];
    $payment = $event['payload']['payment']['entity'] ?? [];
    $razorpayPaymentId = $payment['id'] ?? $dispute['payment_id'] ?? '';
    $disputeId = $dispute['id'] ?? '';
    $amount = floatval($dispute['amount'] ?? 0) / 100;
    $reason = $dispute['reason_code'] ?? 'unknown';
    $phase = $dispute['phase'] ?? '';
    
    error_log("Webhook dispute.created - Payment: $razorpayPaymentId, Dispute: $disputeId, Amount: ₹$amount, Reason: $reason");
    
    if (empty($razorpayPaymentId)) {
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'missing_payment_id']);
        exit;
    }
    
    // Find order
    $stmt = $db->prepare("SELECT * FROM orders WHERE razorpay_payment_id = ?");
    $stmt->execute([$razorpayPaymentId]);
    $order = $stmt->fetch();
    
    if ($order) {
        // Update order with dispute info
        $stmt = $db->prepare("
            UPDATE orders 
            SET dispute_id = ?,
                dispute_status = 'open',
                dispute_reason = ?,
                dispute_amount = ?
            WHERE order_id = ?
        ");
        $stmt->execute([$disputeId, $reason, $amount, $order['order_id']]);
        
        error_log("ALERT: Dispute/Chargeback CREATED for Order {$order['order_id']} ({$order['order_number']}): ₹$amount - $reason");
        
        // Notify admin about the dispute
        // This is important - disputes require action within a time limit
        // sendAdminAlert("⚠️ Dispute created for order #{$order['order_number']}", "Amount: ₹$amount, Reason: $reason, Phase: $phase");
    }
    
    http_response_code(200);
    echo json_encode(['success' => true, 'status' => 'dispute_logged']);
    exit;
}

/**
 * Handle dispute resolved (won or lost)
 */
function handleDisputeResolved(array $event, string $outcome): void {
    $db = getDB();
    
    $dispute = $event['payload']['dispute']['entity'] ?? [];
    $payment = $event['payload']['payment']['entity'] ?? [];
    $razorpayPaymentId = $payment['id'] ?? $dispute['payment_id'] ?? '';
    $disputeId = $dispute['id'] ?? '';
    $amount = floatval($dispute['amount'] ?? 0) / 100;
    
    error_log("Webhook dispute.$outcome - Payment: $razorpayPaymentId, Dispute: $disputeId, Amount: ₹$amount");
    
    if (empty($razorpayPaymentId)) {
        http_response_code(200);
        echo json_encode(['success' => true, 'status' => 'missing_payment_id']);
        exit;
    }
    
    // Find order
    $stmt = $db->prepare("SELECT * FROM orders WHERE razorpay_payment_id = ?");
    $stmt->execute([$razorpayPaymentId]);
    $order = $stmt->fetch();
    
    if ($order) {
        // Update dispute status
        $stmt = $db->prepare("
            UPDATE orders 
            SET dispute_status = ?
            WHERE order_id = ?
        ");
        $stmt->execute([$outcome, $order['order_id']]);
        
        if ($outcome === 'lost') {
            // Dispute lost - money was taken back by bank
            $stmt = $db->prepare("
                UPDATE orders 
                SET payment_status = 'chargeback',
                    order_status = 'cancelled'
                WHERE order_id = ?
            ");
            $stmt->execute([$order['order_id']]);
            
            error_log("ALERT: Dispute LOST for Order {$order['order_id']} - ₹$amount charged back");
            
            // Notify user that their order is cancelled due to chargeback
            sendDisputeNotification($order['user_id'], $order['order_id'], $order['order_number'], $outcome, $amount);
        } else {
            error_log("Dispute WON for Order {$order['order_id']} - ₹$amount retained");
        }
    }
    
    http_response_code(200);
    echo json_encode(['success' => true, 'status' => "dispute_$outcome"]);
    exit;
}

/**
 * Send dispute notification to user
 */
function sendDisputeNotification(int $userId, int $orderId, string $orderNumber, string $outcome, float $amount): void {
    $db = getDB();
    
    if ($outcome === 'lost') {
        $title = "❌ Order Cancelled";
        $body = "Order #$orderNumber has been cancelled due to a payment dispute.";
    } else {
        $title = "✅ Dispute Resolved";
        $body = "The payment dispute for order #$orderNumber has been resolved in your favor.";
    }
    
    $stmt = $db->prepare("
        INSERT INTO notification_history (user_id, title, body, type, deep_link)
        VALUES (?, ?, ?, 'dispute', ?)
    ");
    $stmt->execute([$userId, $title, $body, "order/$orderId"]);
}

// ========== SHOP PRODUCTS API ==========

/**
 * Handle shop products routes
 */
function handleProducts(string $method, array $segments): void {
    $productId = $segments[0] ?? null;
    $subResource = $segments[1] ?? null;
    
    // POST /products - Add product to business listing (for showcase, sell_online=0)
    if ($method === 'POST' && $productId === null) {
        addBusinessProduct();
        return;
    }
    
    // GET /products - Get all products for selling feed (sell_online=1)
    if ($method === 'GET' && $productId === null) {
        getShopProductsForSale();
        return;
    }
    
    // Handle sub-resource operations (reviews)
    if ($productId !== null && $subResource !== null) {
        if ($subResource === 'reviews') {
            if ($method === 'GET') {
                getProductReviews((int)$productId);
                return;
            }
            if ($method === 'POST') {
                addProductReview((int)$productId);
                return;
            }
        }
        errorResponse('Invalid sub-resource', 400);
    }
    
    // GET /products/{id} - Get single product
    if ($method === 'GET' && $productId !== null) {
        getShopProduct((int)$productId);
        return;
    }
    
    // PUT /products/{id} - Update product (owner only)
    if ($method === 'PUT' && $productId !== null) {
        updateBusinessProduct((int)$productId);
        return;
    }
    
    // DELETE /products/{id} - Delete product (owner only)
    if ($method === 'DELETE' && $productId !== null) {
        deleteBusinessProduct((int)$productId);
        return;
    }
    
    errorResponse('Method not allowed', 405);
}

/**
 * Add product to business listing (POST /products)
 * Now accepts condition and sell_online parameters
 */
function addBusinessProduct(): void {
    $auth = requireAuth();
    $userId = $auth['user_id'];
    $db = getDB();
    
    // Get form data (supports multipart/form-data for image upload)
    $listingId = $_POST['listing_id'] ?? null;
    $productName = trim($_POST['product_name'] ?? '');
    $description = trim($_POST['description'] ?? '');
    $price = floatval($_POST['price'] ?? 0);
    $categoryId = $_POST['category_id'] ?? null;
    $subcategoryId = $_POST['subcategory_id'] ?? null;
    $condition = in_array($_POST['condition'] ?? '', ['old', 'new']) ? $_POST['condition'] : 'new';
    $sellOnline = isset($_POST['sell_online']) && $_POST['sell_online'] == '1' ? 1 : 0;
    
    // Validate required fields
    if (!$listingId) {
        errorResponse('Listing ID is required', 400);
    }
    if (empty($productName)) {
        errorResponse('Product name is required', 400);
    }
    if ($price <= 0) {
        errorResponse('Price must be greater than 0', 400);
    }
    
    // Verify user owns the listing
    $stmt = $db->prepare("SELECT user_id, listing_type, category_id FROM listings WHERE listing_id = ?");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();
    
    if (!$listing) {
        errorResponse('Listing not found', 404);
    }
    // Allow both business and services listings to add products/services
    if (!in_array($listing['listing_type'], ['business', 'services'])) {
        errorResponse('Products/services can only be added to business or service listings', 400);
    }
    if ($listing['user_id'] != $userId) {
        errorResponse('You can only add items to your own listing', 403);
    }
    
    // Use listing's category if not provided (required for database constraint)
    if (empty($categoryId)) {
        $categoryId = $listing['category_id'];
    }
    
    // Handle image upload
    $imageUrl = null;
    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $imageUrl = uploadImageToR2($_FILES['image']);
    } elseif (!empty($_POST['image_url'])) {
        $imageUrl = $_POST['image_url'];
    }
    
    // Get shop_category_id for new products
    $shopCategoryId = $_POST['shop_category_id'] ?? null;
    
    // Insert based on condition
    if ($condition === 'old') {
        // OLD products go to old_products table (with listing_id to link to business)
        $productId = getNextAvailableId($db, 'old_products', 'product_id');
        
        $stmt = $db->prepare("
            INSERT INTO old_products 
            (product_id, user_id, listing_id, product_name, description, old_category_id, subcategory_id, price, image_url, `condition`, city, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'good', ?, 'active')
        ");
        
        // Get city from listing
        $cityStmt = $db->prepare("SELECT city FROM listings WHERE listing_id = ?");
        $cityStmt->execute([$listingId]);
        $city = $cityStmt->fetchColumn() ?: 'Hingoli';
        
        $stmt->execute([
            $productId, $userId, $listingId, $productName, $description, $categoryId, $subcategoryId, $price, $imageUrl, $city
        ]);
        
        successResponse([
            'product_id' => $productId,
            'listing_id' => (int)$listingId,
            'product_name' => $productName,
            'price' => $price,
            'image_url' => $imageUrl,
            'condition' => 'old',
            'is_old_product' => true
        ], 'Product added successfully');
    } else {
        // NEW products go to shop_products table
        // Get next sort order
        $stmt = $db->prepare("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM shop_products WHERE listing_id = ?");
        $stmt->execute([$listingId]);
        $sortOrder = $stmt->fetchColumn();
        
        // Use shop_category_id for new products
        $effectiveCategoryId = $shopCategoryId ?? $categoryId;
        
        $stmt = $db->prepare("
            INSERT INTO shop_products 
            (listing_id, product_name, description, shop_category_id, subcategory_id, price, image_url, sell_online, `condition`, is_active, sort_order) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'new', 1, ?)
        ");
        $stmt->execute([
            $listingId, $productName, $description, $effectiveCategoryId, $subcategoryId, $price, $imageUrl, $sellOnline, $sortOrder
        ]);
        $productId = (int)$db->lastInsertId();
        
        successResponse([
            'product_id' => $productId,
            'listing_id' => (int)$listingId,
            'product_name' => $productName,
            'price' => $price,
            'image_url' => $imageUrl,
            'condition' => 'new',
            'sell_online' => (bool)$sellOnline
        ], 'Product added successfully');
    }
}

/**
 * Update product (PUT /products/{id})
 * Owner only - verified through listing ownership
 * Supports both JSON body and multipart form-data with image upload
 */
function updateBusinessProduct(int $productId): void {
    $auth = requireAuth();
    $userId = $auth['user_id'];
    $db = getDB();
    
    // Track which table the product is from
    $isOldProduct = false;
    
    // First, check shop_products table
    $stmt = $db->prepare("
        SELECT sp.*, l.user_id 
        FROM shop_products sp 
        JOIN listings l ON sp.listing_id = l.listing_id 
        WHERE sp.product_id = ?
    ");
    $stmt->execute([$productId]);
    $product = $stmt->fetch();
    
    // If not found in shop_products, check old_products
    if (!$product) {
        $stmt = $db->prepare("
            SELECT * FROM old_products WHERE product_id = ?
        ");
        $stmt->execute([$productId]);
        $product = $stmt->fetch();
        
        if ($product) {
            $isOldProduct = true;
        }
    }
    
    if (!$product) {
        errorResponse('Product not found', 404);
    }
    if ($product['user_id'] != $userId) {
        errorResponse('You can only update your own products', 403);
    }
    
    // Handle both JSON and multipart form-data
    // For multipart/form-data, use $_POST; for JSON, use getJsonBody()
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if (strpos($contentType, 'multipart/form-data') !== false) {
        // For PUT with multipart, PHP doesn't populate $_POST automatically
        // We need to parse it ourselves or use a workaround
        // Retrofit sends multipart data with PUT, but we can also accept POST-style data
        $data = $_POST;
        if (empty($data)) {
            // Try to parse multipart data from php://input for PUT requests
            $data = [];
            $rawData = file_get_contents('php://input');
            
            // Extract boundary from content type
            if (preg_match('/boundary=(.*)$/', $contentType, $matches)) {
                $boundary = $matches[1];
                $parts = explode('--' . $boundary, $rawData);
                
                foreach ($parts as $part) {
                    if (empty(trim($part)) || trim($part) === '--') continue;
                    
                    // Split headers and content
                    $segments = preg_split('/\r\n\r\n/', $part, 2);
                    if (count($segments) < 2) continue;
                    
                    $headers = $segments[0];
                    $content = rtrim($segments[1], "\r\n");
                    
                    // Extract field name
                    if (preg_match('/name="([^"]+)"/', $headers, $nameMatch)) {
                        $fieldName = $nameMatch[1];
                        
                        // Check if it's a file
                        if (preg_match('/filename="([^"]+)"/', $headers, $fileMatch)) {
                            // Handle file upload - store in temp
                            $filename = $fileMatch[1];
                            $tempFile = tempnam(sys_get_temp_dir(), 'upload_');
                            file_put_contents($tempFile, $content);
                            $_FILES[$fieldName] = [
                                'name' => $filename,
                                'type' => 'image/webp',
                                'tmp_name' => $tempFile,
                                'error' => UPLOAD_ERR_OK,
                                'size' => strlen($content)
                            ];
                        } else {
                            // Regular field
                            $data[$fieldName] = $content;
                        }
                    }
                }
            }
        }
    } else {
        $data = getJsonBody();
    }
    
    // Build update query dynamically based on provided fields
    $updates = [];
    $params = [];
    
    if (isset($data['product_name']) && !empty(trim($data['product_name']))) {
        $updates[] = 'product_name = ?';
        $params[] = trim($data['product_name']);
    }
    if (isset($data['description'])) {
        $updates[] = 'description = ?';
        $params[] = trim($data['description']);
    }
    if (isset($data['price']) && floatval($data['price']) > 0) {
        $updates[] = 'price = ?';
        $params[] = floatval($data['price']);
    }
    if (isset($data['discounted_price'])) {
        $updates[] = 'discounted_price = ?';
        $params[] = $data['discounted_price'] ? floatval($data['discounted_price']) : null;
    }
    if (isset($data['condition']) && in_array($data['condition'], ['new', 'old'])) {
        $updates[] = '`condition` = ?';
        $params[] = $data['condition'];
    }
    if (isset($data['sell_online'])) {
        $updates[] = 'sell_online = ?';
        // Handle both "1"/"0" strings and boolean values
        $sellOnline = $data['sell_online'];
        if (is_string($sellOnline)) {
            $params[] = ($sellOnline === '1' || strtolower($sellOnline) === 'true') ? 1 : 0;
        } else {
            $params[] = $sellOnline ? 1 : 0;
        }
    }
    if (isset($data['stock_qty'])) {
        $updates[] = 'stock_qty = ?';
        $params[] = intval($data['stock_qty']);
    }
    if (isset($data['category_id']) && intval($data['category_id']) > 0) {
        $updates[] = 'category_id = ?';
        $params[] = intval($data['category_id']);
    }
    if (isset($data['shop_category_id'])) {
        $updates[] = 'shop_category_id = ?';
        $params[] = intval($data['shop_category_id']) > 0 ? intval($data['shop_category_id']) : null;
    }
    if (isset($data['subcategory_id'])) {
        $updates[] = 'subcategory_id = ?';
        $params[] = intval($data['subcategory_id']) > 0 ? intval($data['subcategory_id']) : null;
    }
    if (isset($data['is_active'])) {
        $updates[] = 'is_active = ?';
        $params[] = $data['is_active'] ? 1 : 0;
    }
    
    // Handle image upload
    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
        $imageUrl = uploadImageToR2($_FILES['image']);
        if ($imageUrl) {
            $updates[] = 'image_url = ?';
            $params[] = $imageUrl;
        }
    } elseif (isset($data['image_url']) && !empty($data['image_url'])) {
        $updates[] = 'image_url = ?';
        $params[] = $data['image_url'];
    }
    
    if (empty($updates)) {
        errorResponse('No valid fields to update', 400);
    }
    
    // Check auto-moderation setting for products
    // If OFF, set is_active to 0 (pending) when user edits (requires admin re-approval)
    $stmt = $db->prepare("SELECT setting_value FROM settings WHERE setting_key = 'auto_moderation_products'");
    $stmt->execute();
    $autoModProducts = $stmt->fetchColumn() === 'true';
    
    // Add product_id to params
    $params[] = $productId;
    
    // Handle old_products table differently
    if ($isOldProduct) {
        // For old_products, update the table directly - no is_active, different field names
        // Map any condition values to old_products enum values
        $oldUpdates = [];
        $oldParams = [];
        
        if (isset($data['product_name']) && !empty(trim($data['product_name']))) {
            $oldUpdates[] = 'product_name = ?';
            $oldParams[] = trim($data['product_name']);
        }
        if (isset($data['description'])) {
            $oldUpdates[] = 'description = ?';
            $oldParams[] = trim($data['description']);
        }
        if (isset($data['price']) && floatval($data['price']) > 0) {
            $oldUpdates[] = 'price = ?';
            $oldParams[] = floatval($data['price']);
        }
        if (isset($data['original_price'])) {
            $oldUpdates[] = 'original_price = ?';
            $oldParams[] = floatval($data['original_price']);
        }
        if (isset($data['condition'])) {
            // old_products uses 'like_new', 'good', 'fair', 'poor' - map from app values
            $conditionMap = ['new' => 'like_new', 'like_new' => 'like_new', 'good' => 'good', 'fair' => 'fair', 'poor' => 'poor', 'old' => 'good'];
            $mappedCondition = $conditionMap[$data['condition']] ?? 'good';
            $oldUpdates[] = '`condition` = ?';
            $oldParams[] = $mappedCondition;
        }
        if (isset($data['category_id']) && intval($data['category_id']) > 0) {
            $oldUpdates[] = 'old_category_id = ?';
            $oldParams[] = intval($data['category_id']);
        }
        if (isset($data['subcategory_id'])) {
            $oldUpdates[] = 'subcategory_id = ?';
            $oldParams[] = intval($data['subcategory_id']) > 0 ? intval($data['subcategory_id']) : null;
        }
        
        // Handle image upload for old products
        if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
            $imageUrl = uploadImageToR2($_FILES['image']);
            if ($imageUrl) {
                $oldUpdates[] = 'image_url = ?';
                $oldParams[] = $imageUrl;
            }
        } elseif (isset($data['image_url']) && !empty($data['image_url'])) {
            $oldUpdates[] = 'image_url = ?';
            $oldParams[] = $data['image_url'];
        }
        
        if (empty($oldUpdates)) {
            errorResponse('No valid fields to update', 400);
        }
        
        $oldParams[] = $productId;
        $sql = "UPDATE old_products SET " . implode(', ', $oldUpdates) . ", updated_at = NOW() WHERE product_id = ?";
        $stmt = $db->prepare($sql);
        $stmt->execute($oldParams);
        
        // Return updated old product
        $stmt = $db->prepare("SELECT * FROM old_products WHERE product_id = ?");
        $stmt->execute([$productId]);
        $updatedProduct = $stmt->fetch();
        
        successResponse([
            'product_id' => (int)$updatedProduct['product_id'],
            'listing_id' => null,
            'product_name' => $updatedProduct['product_name'],
            'description' => $updatedProduct['description'],
            'price' => floatval($updatedProduct['price']),
            'original_price' => $updatedProduct['original_price'] ? floatval($updatedProduct['original_price']) : null,
            'image_url' => $updatedProduct['image_url'],
            'condition' => $updatedProduct['condition'],
            'is_old_product' => true,
            'product_source' => 'old'
        ], 'Product updated successfully');
        return;
    }
    
    // For shop_products, check auto-moderation
    if (!$autoModProducts) {
        // Override is_active to 0 (pending) - requires admin approval
        $updates[] = 'is_active = ?';
        $params[] = 0;
    }
    
    $sql = "UPDATE shop_products SET " . implode(', ', $updates) . ", updated_at = NOW() WHERE product_id = ?";
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    
    // Return updated product
    $stmt = $db->prepare("SELECT * FROM shop_products WHERE product_id = ?");
    $stmt->execute([$productId]);
    $updatedProduct = $stmt->fetch();
    
    successResponse([
        'product_id' => (int)$updatedProduct['product_id'],
        'listing_id' => (int)$updatedProduct['listing_id'],
        'product_name' => $updatedProduct['product_name'],
        'description' => $updatedProduct['description'],
        'price' => floatval($updatedProduct['price']),
        'discounted_price' => $updatedProduct['discounted_price'] ? floatval($updatedProduct['discounted_price']) : null,
        'image_url' => $updatedProduct['image_url'],
        'condition' => $updatedProduct['condition'],
        'sell_online' => (bool)$updatedProduct['sell_online'],
        'stock_qty' => $updatedProduct['stock_qty'] ? (int)$updatedProduct['stock_qty'] : null,
        'is_active' => (bool)$updatedProduct['is_active'],
        'is_old_product' => false,
        'product_source' => 'shop'
    ], $autoModProducts ? 'Product updated successfully' : 'Product updated! Changes are pending admin approval.');
}

/**
 * Delete product from business listing (owner only)
 * DELETE /products/{id}
 */
function deleteBusinessProduct(int $productId): void {
    $auth = requireAuth();
    $userId = $auth['user_id'];
    $db = getDB();
    
    // Get product and verify ownership
    $stmt = $db->prepare("
        SELECT sp.*, l.user_id 
        FROM shop_products sp 
        JOIN listings l ON sp.listing_id = l.listing_id 
        WHERE sp.product_id = ?
    ");
    $stmt->execute([$productId]);
    $product = $stmt->fetch();
    
    if (!$product) {
        errorResponse('Product not found', 404);
    }
    if ($product['user_id'] != $userId) {
        errorResponse('You can only delete your own products', 403);
    }
    
    // Delete product image from R2 first
    if (!empty($product['image_url'])) {
        deleteImageFromR2($product['image_url']);
    }
    
    // Delete associated reviews first (cascade delete to prevent orphaned reviews)
    $stmt = $db->prepare("DELETE FROM reviews WHERE product_id = ? AND listing_id IS NULL AND old_product_id IS NULL");
    $stmt->execute([$productId]);
    
    // Delete product from database
    $stmt = $db->prepare("DELETE FROM shop_products WHERE product_id = ?");
    $stmt->execute([$productId]);
    
    successResponse(['deleted' => true], 'Product deleted successfully');
}

/**
 * Get shop products available for sale (for selling feed)
 * GET /products?sell_online=1&category_id=X&page=1
 * Now uses standardized params and transformers
 */
function getShopProductsForSale(): void {
    $db = getDB();
    
    // Use standardized pagination params
    list($page, $perPage) = getPaginationParams();
    $offset = ($page - 1) * $perPage;
    
    // Use getQueryParam consistently
    $sellOnline = getQueryParam('sell_online', '1');
    $city = getQueryParam('city');
    // Note: condition parameter removed - shop_products is ALWAYS for new products
    // Old products use the /old-products API which reads from old_products table
    $listingId = getQueryParam('listing_id');
    $shopCategoryId = getQueryParam('shop_category_id');
    $search = getQueryParam('search');
    
    // When listing_id is provided, return ALL products for that listing (including sell_online=0)
    // Otherwise, only return products for sale (sell_online=1) for the selling feed
    if ($listingId) {
        // For a specific listing - return both new products (shop_products) and old products (old_products)
        // NEW products from shop_products
        $newProductsStmt = $db->prepare("
            SELECT sp.product_id, sp.listing_id, sp.product_name, sp.description,
                   sp.price, sp.discounted_price, sp.image_url, sp.stock_qty,
                   sp.sell_online, sp.condition, sp.created_at,
                   l.title as business_name, l.city, l.user_id,
                   COALESCE(shc.name, c.name) as category_name,
                   shc.name_mr as category_name_mr,
                   sc.name as subcategory_name,
                   u.phone as business_phone,
                   shc.id as shop_category_id,
                   0 as is_old_product,
                   'shop' as product_source
            FROM shop_products sp
            JOIN listings l ON sp.listing_id = l.listing_id
            LEFT JOIN shop_categories shc ON sp.shop_category_id = shc.id
            LEFT JOIN categories c ON sp.category_id = c.category_id
            LEFT JOIN categories sc ON sp.subcategory_id = sc.category_id
            LEFT JOIN users u ON l.user_id = u.user_id
            WHERE sp.is_active = 1 AND sp.listing_id = ?
            ORDER BY sp.created_at DESC
        ");
        $newProductsStmt->execute([$listingId]);
        $newProducts = $newProductsStmt->fetchAll();
        
        // OLD products from old_products (linked by listing_id)
        $oldProductsStmt = $db->prepare("
            SELECT op.product_id, op.listing_id, op.product_name, op.description,
                   op.price, NULL as discounted_price, op.image_url, NULL as stock_qty,
                   1 as sell_online, 'old' as `condition`, op.created_at,
                   l.title as business_name, l.city, op.user_id,
                   oc.name as category_name,
                   oc.name_mr as category_name_mr,
                   NULL as subcategory_name,
                   u.phone as business_phone,
                   NULL as shop_category_id,
                   1 as is_old_product,
                   'old' as product_source
            FROM old_products op
            LEFT JOIN listings l ON op.listing_id = l.listing_id
            LEFT JOIN old_categories oc ON op.old_category_id = oc.id
            LEFT JOIN users u ON op.user_id = u.user_id
            WHERE op.status = 'active' AND op.listing_id = ?
            ORDER BY op.created_at DESC
        ");
        $oldProductsStmt->execute([$listingId]);
        $oldProducts = $oldProductsStmt->fetchAll();
        
        // Merge and transform all products
        $allProducts = array_merge($newProducts, $oldProducts);
        
        // Sort by created_at DESC
        usort($allProducts, function($a, $b) {
            return strtotime($b['created_at']) - strtotime($a['created_at']);
        });
        
        $result = array_map('transformShopProduct', $allProducts);
        
        successResponse([
            'products' => $result,
            'pagination' => [
                'total' => count($allProducts),
                'page' => 1,
                'per_page' => count($allProducts),
                'total_pages' => 1
            ]
        ]);
        return;
    }
    
    // For selling feed - only products for sale (always new products)
    $where = ["sp.is_active = 1", "sp.sell_online = 1", "l.status = 'active'", "sp.`condition` = 'new'"];
    $params = [];
    
    // Use shop_category_id (new category system)
    if ($shopCategoryId) {
        $where[] = "sp.shop_category_id = ?";
        $params[] = $shopCategoryId;
    }
    
    if ($city) {
        $where[] = "l.city = ?";
        $params[] = $city;
    }
    
    // Search by product name
    if ($search) {
        $where[] = "(sp.product_name LIKE ? OR sp.description LIKE ?)";
        $params[] = "%$search%";
        $params[] = "%$search%";
    }
    
    $whereClause = implode(' AND ', $where);
    
    // Count total
    $countStmt = $db->prepare("
        SELECT COUNT(*) 
        FROM shop_products sp
        JOIN listings l ON sp.listing_id = l.listing_id
        WHERE $whereClause
    ");
    $countStmt->execute($params);
    $total = (int)$countStmt->fetchColumn();
    
    // Get products with business info
    // Note: LIMIT values embedded directly (TiDB doesn't support ? placeholders for LIMIT)
    $stmt = $db->prepare("
        SELECT sp.*, 
               l.title as business_name, l.city, l.main_image_url as business_image,
               l.user_id,
               COALESCE(shc.name, c.name) as category_name,
               shc.name_mr as category_name_mr,
               sc.name as subcategory_name,
               u.phone as business_phone,
               shc.id as shop_category_id,
               0 as is_old_product,
               'shop' as product_source
        FROM shop_products sp
        JOIN listings l ON sp.listing_id = l.listing_id
        LEFT JOIN shop_categories shc ON sp.shop_category_id = shc.id
        LEFT JOIN categories c ON sp.category_id = c.category_id
        LEFT JOIN categories sc ON sp.subcategory_id = sc.category_id
        LEFT JOIN users u ON l.user_id = u.user_id
        WHERE $whereClause
        ORDER BY sp.created_at DESC
        LIMIT $perPage OFFSET $offset
    ");
    $stmt->execute($params);
    $products = $stmt->fetchAll();
    
    // Use transformer for consistent response format
    $result = array_map('transformShopProduct', $products);
    
    successResponse([
        'products' => $result,
        'pagination' => [
            'total' => $total,
            'page' => $page,
            'per_page' => $perPage,
            'total_pages' => (int)ceil($total / $perPage)
        ]
    ]);
}

/**
 * Get single shop product
 * GET /products/{id}
 * Checks shop_products first, then falls back to old_products
 */
function getShopProduct(int $productId): void {
    $db = getDB();
    
    // First, check shop_products table
    $stmt = $db->prepare("
        SELECT sp.*, 
               l.title as business_name, l.city, l.main_image_url as business_image,
               l.listing_id, l.user_id,
               COALESCE(shc.name, c.name) as category_name,
               shc.name_mr as category_name_mr,
               COALESCE(shc_sub.name, sc.name) as subcategory_name,
               shc_sub.name_mr as subcategory_name_mr,
               u.phone as business_phone,
               shc.id as shop_category_id,
               shc.icon as shop_category_icon,
               'shop' as product_source
        FROM shop_products sp
        JOIN listings l ON sp.listing_id = l.listing_id
        LEFT JOIN shop_categories shc ON sp.shop_category_id = shc.id
        LEFT JOIN shop_categories shc_sub ON sp.subcategory_id = shc_sub.id
        LEFT JOIN categories c ON sp.category_id = c.category_id
        LEFT JOIN categories sc ON sp.subcategory_id = sc.category_id
        LEFT JOIN users u ON l.user_id = u.user_id
        WHERE sp.product_id = ? AND sp.is_active = 1
    ");
    $stmt->execute([$productId]);
    $p = $stmt->fetch();
    
    // If not found in shop_products, check old_products table
    if (!$p) {
        $stmt = $db->prepare("
            SELECT op.*,
                   op.product_name,
                   op.description,
                   op.price,
                   op.original_price,
                   op.image_url,
                   op.user_id,
                   op.city,
                   op.`condition` as item_condition,
                   oc.name as category_name,
                   oc.name_mr as category_name_mr,
                   u.username as seller_name,
                   u.phone as seller_phone,
                   'old' as product_source
            FROM old_products op
            LEFT JOIN old_categories oc ON op.old_category_id = oc.id
            LEFT JOIN users u ON op.user_id = u.user_id
            WHERE op.product_id = ? AND op.status = 'active'
        ");
        $stmt->execute([$productId]);
        $p = $stmt->fetch();
        
        if (!$p) {
            errorResponse('Product not found', 404);
        }
        
        // Return old product format
        successResponse([
            'product_id' => (int)$p['product_id'],
            'listing_id' => null, // Old products don't have listing_id
            'product_name' => $p['product_name'],
            'description' => $p['description'],
            'category_id' => $p['old_category_id'] ? (int)$p['old_category_id'] : null,
            'category_name' => $p['category_name'],
            'category_name_mr' => $p['category_name_mr'] ?? null,
            'subcategory_id' => $p['subcategory_id'] ? (int)$p['subcategory_id'] : null,
            'subcategory_name' => null, // TODO: Join with old_categories for subcategory name if needed
            'price' => (float)$p['price'],
            'original_price' => $p['original_price'] ? (float)$p['original_price'] : null,
            'discounted_price' => null,
            'image_url' => $p['image_url'],
            'additional_images' => $p['additional_images'] ? json_decode($p['additional_images'], true) : null,
            'stock_qty' => null,
            'min_qty' => 1,
            'sell_online' => false,
            'condition' => $p['item_condition'] ?? 'good',
            'is_old_product' => true,
            'product_source' => 'old',
            'brand' => $p['brand'] ?? null,
            'model' => $p['model'] ?? null,
            'age_months' => $p['age_months'] ? (int)$p['age_months'] : null,
            'has_warranty' => (bool)($p['has_warranty'] ?? false),
            'warranty_months' => $p['warranty_months'] ? (int)$p['warranty_months'] : null,
            'has_bill' => (bool)($p['has_bill'] ?? false),
            'reason_for_selling' => $p['reason_for_selling'] ?? null,
            'accept_offers' => (bool)($p['accept_offers'] ?? true),
            'business_name' => $p['seller_name'] ?? null,
            'business_phone' => $p['seller_phone'] ?? null,
            'city' => $p['city'],
            'user_id' => (int)$p['user_id'],
            'view_count' => (int)($p['view_count'] ?? 0),
            'inquiry_count' => (int)($p['inquiry_count'] ?? 0),
            'created_at' => $p['created_at']
        ]);
        return;
    }
    
    // Return shop product format
    successResponse([
        'product_id' => (int)$p['product_id'],
        'listing_id' => (int)$p['listing_id'],
        'product_name' => $p['product_name'],
        'description' => $p['description'],
        'category_id' => (int)$p['category_id'],
        'shop_category_id' => $p['shop_category_id'] ? (int)$p['shop_category_id'] : null,
        'category_name' => $p['category_name'],
        'category_name_mr' => $p['category_name_mr'] ?? null,
        'subcategory_id' => $p['subcategory_id'] ? (int)$p['subcategory_id'] : null,
        'subcategory_name' => $p['subcategory_name'],
        'price' => (float)$p['price'],
        'discounted_price' => $p['discounted_price'] ? (float)$p['discounted_price'] : null,
        'image_url' => $p['image_url'],
        'stock_qty' => $p['stock_qty'] ? (int)$p['stock_qty'] : null,
        'min_qty' => (int)($p['min_qty'] ?? 1),
        'sell_online' => (bool)$p['sell_online'],
        'condition' => $p['condition'] ?? 'new',
        'is_old_product' => false,
        'product_source' => 'shop',
        'business_name' => $p['business_name'],
        'business_phone' => $p['business_phone'],
        'city' => $p['city'],
        'user_id' => (int)$p['user_id'],
        'created_at' => $p['created_at']
    ]);
}

/**
 * Get products for a specific business listing
 * Used by listing detail endpoint
 */
function getListingProducts(int $listingId): array {
    $db = getDB();
    
    $stmt = $db->prepare("
        SELECT sp.*, 
               COALESCE(shc.name, c.name) as category_name, 
               sc.name as subcategory_name
        FROM shop_products sp
        LEFT JOIN shop_categories shc ON sp.shop_category_id = shc.id
        LEFT JOIN categories c ON sp.category_id = c.category_id
        LEFT JOIN categories sc ON sp.subcategory_id = sc.category_id
        WHERE sp.listing_id = ? AND sp.is_active = 1
        ORDER BY sp.sort_order, sp.product_name
    ");
    $stmt->execute([$listingId]);
    $products = $stmt->fetchAll();
    
    return array_map(function($p) {
        return [
            'product_id' => (int)$p['product_id'],
            'product_name' => $p['product_name'],
            'description' => $p['description'],
            'category_name' => $p['category_name'],
            'subcategory_name' => $p['subcategory_name'],
            'price' => (float)$p['price'],
            'discounted_price' => $p['discounted_price'] ? (float)$p['discounted_price'] : null,
            'image_url' => $p['image_url'],
            'stock_qty' => $p['stock_qty'] ? (int)$p['stock_qty'] : null,
            'sell_online' => (bool)$p['sell_online']
        ];
    }, $products);
}

// ============================================================================
// DELIVERY APP API FUNCTIONS
// ============================================================================

/**
 * Require delivery user authentication
 */
function requireDeliveryAuth(): array {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';
    
    if (empty($authHeader) || !preg_match('/Bearer\s+(.+)/', $authHeader, $matches)) {
        errorResponse('Authorization required', 401);
    }
    
    $payload = validateJWT($matches[1]);
    
    if (!$payload || ($payload['type'] ?? '') !== 'delivery_access') {
        errorResponse('Invalid delivery token', 401);
    }
    
    // Verify user is still active
    $db = getDB();
    $stmt = $db->prepare("SELECT * FROM delivery_users WHERE delivery_user_id = ? AND status = 'active'");
    $stmt->execute([$payload['delivery_user_id']]);
    $user = $stmt->fetch();
    
    if (!$user) {
        errorResponse('Delivery account not found or inactive', 401);
    }
    
    return $user;
}

/**
 * Generate JWT tokens for delivery user
 */
function generateDeliveryTokens(int $deliveryUserId, string $name, string $phone): array {
    $accessPayload = [
        'delivery_user_id' => $deliveryUserId,
        'name' => $name,
        'phone' => $phone,
        'type' => 'delivery_access',
        'iat' => time(),
        'exp' => time() + (60 * 60 * 24 * 7) // 7 days
    ];
    
    $refreshPayload = [
        'delivery_user_id' => $deliveryUserId,
        'type' => 'delivery_refresh',
        'iat' => time(),
        'exp' => time() + (60 * 60 * 24 * 30) // 30 days
    ];
    
    return [
        'access_token' => generateJWT($accessPayload),
        'refresh_token' => generateJWT($refreshPayload)
    ];
}

/**
 * Register new delivery user
 */
function deliveryRegister(): void {
    $data = getJsonBody();
    
    if (empty($data['phone']) || empty($data['name'])) {
        errorResponse('Phone and name are required', 400);
    }
    
    $phone = trim($data['phone']);
    $name = trim($data['name']);
    $vehicleType = $data['vehicle_type'] ?? 'bike';
    
    $db = getDB();
    
    // Check if already registered
    $stmt = $db->prepare("SELECT delivery_user_id, status FROM delivery_users WHERE phone = ?");
    $stmt->execute([$phone]);
    $existing = $stmt->fetch();
    
    if ($existing) {
        if ($existing['status'] === 'blocked') {
            errorResponse('This account has been blocked', 403);
        }
        errorResponse('Phone already registered. Please login instead.', 400);
    }
    
    // Generate and store OTP
    $otp = str_pad(random_int(0, 999999), 6, '0', STR_PAD_LEFT);
    $expiresAt = date('Y-m-d H:i:s', strtotime('+10 minutes'));
    
    // Store temp registration data
    $stmt = $db->prepare("DELETE FROM otp_verifications WHERE phone = ?");
    $stmt->execute([$phone]);
    
    $stmt = $db->prepare("
        INSERT INTO otp_verifications (phone, otp_code, purpose, expires_at)
        VALUES (?, ?, 'delivery_register', ?)
    ");
    $stmt->execute([$phone, password_hash($otp, PASSWORD_DEFAULT), $expiresAt]);
    
    // Store temp data in session/cache (using OTP table notes field or separate temp table)
    // For simplicity, we'll verify during OTP verification
    
    // Send OTP via SMS Gateway
    $result = sendOtpViaGateway($phone, $otp);
    
    if (!$result['success']) {
        errorResponse('Failed to send OTP: ' . $result['message'], 500);
    }
    
    successResponse([
        'message' => 'OTP sent successfully',
        'phone' => $phone,
        'name' => $name,
        'vehicle_type' => $vehicleType,
        'expires_in' => 600
    ]);
}

/**
 * Send OTP for delivery login (also works for new users - auto handles registration)
 */
function deliverySendOTP(): void {
    $data = getJsonBody();
    
    if (empty($data['phone'])) {
        errorResponse('Phone number is required', 400);
    }
    
    $phone = trim($data['phone']);
    $db = getDB();
    
    // Check if registered in delivery_users table
    $stmt = $db->prepare("SELECT delivery_user_id, status FROM delivery_users WHERE phone = ?");
    $stmt->execute([$phone]);
    $user = $stmt->fetch();
    
    $isNewUser = false;
    
    if (!$user) {
        // New user - will create account after OTP verification
        $isNewUser = true;
    } else {
        if ($user['status'] === 'blocked') {
            errorResponse('This account has been blocked', 403);
        }
        
        if ($user['status'] === 'inactive') {
            errorResponse('Account is inactive. Please contact support.', 403);
        }
    }
    
    // Generate OTP
    $otp = str_pad(random_int(0, 999999), 6, '0', STR_PAD_LEFT);
    $expiresAt = date('Y-m-d H:i:s', strtotime('+10 minutes'));
    
    // Store OTP
    $stmt = $db->prepare("DELETE FROM otp_verifications WHERE phone = ?");
    $stmt->execute([$phone]);
    
    $stmt = $db->prepare("
        INSERT INTO otp_verifications (phone, otp_code, purpose, expires_at)
        VALUES (?, ?, 'signup', ?)
    ");
    $stmt->execute([$phone, password_hash($otp, PASSWORD_DEFAULT), $expiresAt]);
    
    // Send OTP
    $result = sendOtpViaGateway($phone, $otp);
    
    if (!$result['success']) {
        errorResponse('Failed to send OTP: ' . $result['message'], 500);
    }
    
    successResponse([
        'message' => 'OTP sent successfully',
        'is_new_user' => $isNewUser,
        'expires_in' => 600
    ]);
}

/**
 * Verify OTP for delivery login/register
 */
function deliveryVerifyOTP(): void {
    $data = getJsonBody();
    
    if (empty($data['phone']) || empty($data['otp'])) {
        errorResponse('Phone and OTP are required', 400);
    }
    
    $phone = trim($data['phone']);
    $otp = trim($data['otp']);
    
    $db = getDB();
    
    // Get OTP record
    $stmt = $db->prepare("
        SELECT * FROM otp_verifications 
        WHERE phone = ? AND expires_at > NOW()
        ORDER BY created_at DESC LIMIT 1
    ");
    $stmt->execute([$phone]);
    $otpRecord = $stmt->fetch();
    
    if (!$otpRecord) {
        errorResponse('OTP expired or not found', 400);
    }
    
    if (!password_verify($otp, $otpRecord['otp_code'])) {
        errorResponse('Invalid OTP', 400);
    }
    
    // Delete used OTP
    $stmt = $db->prepare("DELETE FROM otp_verifications WHERE phone = ?");
    $stmt->execute([$phone]);
    
    // Check if user already exists in delivery_users
    $stmt = $db->prepare("SELECT * FROM delivery_users WHERE phone = ?");
    $stmt->execute([$phone]);
    $user = $stmt->fetch();
    
    if (!$user) {
        // New user - create account with default name
        $name = 'Delivery Partner';
        $vehicleType = 'bike';
        
        $stmt = $db->prepare("
            INSERT INTO delivery_users (phone, name, vehicle_type, status)
            VALUES (?, ?, ?, 'active')
        ");
        $stmt->execute([$phone, $name, $vehicleType]);
        $deliveryUserId = (int)$db->lastInsertId();
        
        $tokens = generateDeliveryTokens($deliveryUserId, $name, $phone);
        
        successResponse(array_merge($tokens, [
            'is_new_user' => true,
            'user' => [
                'delivery_user_id' => $deliveryUserId,
                'phone' => $phone,
                'name' => $name
            ]
        ]));
    } else {
        // Existing user - login
        $tokens = generateDeliveryTokens(
            (int)$user['delivery_user_id'],
            $user['name'],
            $user['phone']
        );
        
        successResponse(array_merge($tokens, [
            'is_new_user' => false,
            'user' => [
                'delivery_user_id' => (int)$user['delivery_user_id'],
                'phone' => $user['phone'],
                'name' => $user['name']
            ]
        ]));
    }
}

/**
 * Get available orders for delivery
 */
function getAvailableOrders(int $deliveryUserId): void {
    $db = getDB();
    
    // Include both paid and pending (COD) orders
    // Join via order_items.listing_id to get shop/seller pickup address
    $stmt = $db->query("
        SELECT o.order_id, o.order_number, o.total_amount, o.payment_method,
               o.order_status, o.payment_status, o.created_at, o.estimated_delivery_date, o.delivery_time_slot,
               a.name as customer_name, a.phone as customer_phone,
               a.address_line1, a.address_line2, a.city, a.pincode,
               l.title as shop_name, l.location as shop_location, l.city as shop_city,
               u.phone as shop_phone, u.username as shop_owner_name
        FROM orders o
        JOIN user_addresses a ON o.address_id = a.address_id
        LEFT JOIN (
            SELECT oi.order_id, MIN(oi.listing_id) as listing_id
            FROM order_items oi
            GROUP BY oi.order_id
        ) first_item ON o.order_id = first_item.order_id
        LEFT JOIN listings l ON first_item.listing_id = l.listing_id
        LEFT JOIN users u ON l.user_id = u.user_id
        WHERE o.order_status IN ('confirmed', 'processing')
          AND o.payment_status IN ('paid', 'pending')
          AND o.delivery_user_id IS NULL
        ORDER BY o.created_at ASC
    ");
    $orders = $stmt->fetchAll();
    
    $result = array_map(function($o) {
        $earnings = round((float)$o['total_amount'] * 0.10, 2); // 10% earnings
        
        // Use shop address if available, otherwise fallback to default
        $shopName = $o['shop_name'] ?? 'Hingoli Hub Warehouse';
        $shopLocation = $o['shop_location'] ?? 'Main Market, Near Bus Stand';
        $shopCity = $o['shop_city'] ?? 'Hingoli';
        $shopPhone = $o['shop_phone'] ?? '9595340263';
        
        return [
            'order_id' => (int)$o['order_id'],
            'order_number' => $o['order_number'],
            'total_amount' => (float)$o['total_amount'],
            'delivery_earnings' => $earnings,
            'payment_method' => $o['payment_method'],
            'payment_status' => $o['payment_status'],
            'order_status' => $o['order_status'],
            'created_at' => $o['created_at'],
            'estimated_delivery_date' => $o['estimated_delivery_date'],
            'delivery_time_slot' => $o['delivery_time_slot'],
            'customer' => [
                'name' => $o['customer_name'],
                'phone' => $o['customer_phone']
            ],
            'pickup_address' => [
                'label' => $shopName,
                'line1' => $shopLocation,
                'line2' => $o['shop_owner_name'] ?? 'Hingoli Hub Store',
                'city' => $shopCity,
                'postal_code' => '431513',
                'phone' => $shopPhone
            ],
            'delivery_address' => [
                'line1' => $o['address_line1'],
                'line2' => $o['address_line2'],
                'city' => $o['city'],
                'postal_code' => $o['pincode']
            ]
        ];
    }, $orders);
    
    successResponse($result);
}

/**
 * Accept an order for delivery
 */
function acceptOrder(int $deliveryUserId): void {
    $data = getJsonBody();
    
    if (empty($data['order_id'])) {
        errorResponse('Order ID is required', 400);
    }
    
    $orderId = (int)$data['order_id'];
    $db = getDB();
    
    // Check order is available (include both paid and pending/COD orders)
    $stmt = $db->prepare("
        SELECT * FROM orders 
        WHERE order_id = ? 
          AND order_status IN ('confirmed', 'processing')
          AND payment_status IN ('paid', 'pending')
          AND delivery_user_id IS NULL
    ");
    $stmt->execute([$orderId]);
    $order = $stmt->fetch();
    
    if (!$order) {
        errorResponse('Order not available or already taken', 400);
    }
    
    // Calculate earnings (10%)
    $earnings = round((float)$order['total_amount'] * 0.10, 2);
    
    // Accept the order
    $stmt = $db->prepare("
        UPDATE orders SET 
            delivery_user_id = ?,
            delivery_earnings = ?,
            delivery_accepted_at = NOW(),
            order_status = 'accepted'
        WHERE order_id = ? AND delivery_user_id IS NULL
    ");
    $stmt->execute([$deliveryUserId, $earnings, $orderId]);
    
    if ($stmt->rowCount() === 0) {
        errorResponse('Order was taken by another delivery partner', 409);
    }
    
    successResponse([
        'message' => 'Order accepted successfully',
        'order_id' => $orderId,
        'earnings' => $earnings
    ]);
}

/**
 * Cancel an accepted order
 */
function cancelOrder(int $deliveryUserId): void {
    $data = getJsonBody();
    
    if (empty($data['order_id'])) {
        errorResponse('Order ID is required', 400);
    }
    
    $orderId = (int)$data['order_id'];
    $db = getDB();
    
    // Check order belongs to this delivery user and is cancellable
    $stmt = $db->prepare("
        SELECT * FROM orders 
        WHERE order_id = ? 
          AND delivery_user_id = ?
          AND order_status = 'accepted'
    ");
    $stmt->execute([$orderId, $deliveryUserId]);
    
    if (!$stmt->fetch()) {
        errorResponse('Order not found or cannot be cancelled', 400);
    }
    
    // Return order to pool
    $stmt = $db->prepare("
        UPDATE orders SET 
            delivery_user_id = NULL,
            delivery_earnings = NULL,
            delivery_accepted_at = NULL,
            order_status = 'processing'
        WHERE order_id = ?
    ");
    $stmt->execute([$orderId]);
    
    successResponse(['message' => 'Order cancelled and returned to pool']);
}

/**
 * Get my deliveries
 */
function getMyDeliveries(int $deliveryUserId): void {
    $status = $_GET['status'] ?? 'active';
    
    $db = getDB();
    
    if ($status === 'active') {
        $statusFilter = "AND o.order_status IN ('accepted', 'out_for_delivery')";
    } elseif ($status === 'completed') {
        $statusFilter = "AND o.order_status = 'delivered'";
    } else {
        $statusFilter = "";
    }
    
    // Join via order_items.listing_id to get shop/seller pickup address
    $stmt = $db->prepare("
        SELECT o.order_id, o.order_number, o.total_amount, o.delivery_earnings,
               o.order_status, o.payment_status, o.payment_method, o.delivery_accepted_at, o.delivery_picked_at, o.delivered_at,
               o.user_id as customer_user_id,
               a.name as customer_name, a.phone as customer_phone,
               a.address_line1, a.address_line2, a.city, a.pincode,
               (SELECT COUNT(*) FROM order_items WHERE order_id = o.order_id) as items_count,
               l.title as shop_name, l.location as shop_location, l.city as shop_city,
               u.phone as shop_phone, u.username as shop_owner_name
        FROM orders o
        JOIN user_addresses a ON o.address_id = a.address_id
        LEFT JOIN (
            SELECT oi.order_id, MIN(oi.listing_id) as listing_id
            FROM order_items oi
            GROUP BY oi.order_id
        ) first_item ON o.order_id = first_item.order_id
        LEFT JOIN listings l ON first_item.listing_id = l.listing_id
        LEFT JOIN users u ON l.user_id = u.user_id
        WHERE o.delivery_user_id = ? $statusFilter
        ORDER BY o.delivery_accepted_at DESC
    ");
    $stmt->execute([$deliveryUserId]);
    $orders = $stmt->fetchAll();
    
    $result = array_map(function($o) {
        // Use shop address if available, otherwise fallback to default
        $shopName = $o['shop_name'] ?? 'Hingoli Hub Warehouse';
        $shopLocation = $o['shop_location'] ?? 'Main Market, Near Bus Stand';
        $shopCity = $o['shop_city'] ?? 'Hingoli';
        $shopPhone = $o['shop_phone'] ?? '9595340263';
        
        return [
            'order_id' => (int)$o['order_id'],
            'order_number' => $o['order_number'],
            'total_amount' => (float)$o['total_amount'],
            'delivery_earnings' => (float)$o['delivery_earnings'],
            'order_status' => $o['order_status'],
            'payment_status' => $o['payment_status'],
            'payment_method' => $o['payment_method'],
            'accepted_at' => $o['delivery_accepted_at'],
            'picked_at' => $o['delivery_picked_at'],
            'delivered_at' => $o['delivered_at'],
            'customer' => [
                'user_id' => (int)$o['customer_user_id'],
                'name' => $o['customer_name'],
                'phone' => $o['customer_phone']
            ],
            'pickup_address' => [
                'label' => $shopName,
                'line1' => $shopLocation,
                'line2' => $o['shop_owner_name'] ?? 'Hingoli Hub Store',
                'city' => $shopCity,
                'postal_code' => '431513',
                'phone' => $shopPhone
            ],
            'delivery_address' => [
                'line1' => $o['address_line1'],
                'line2' => $o['address_line2'],
                'city' => $o['city'],
                'postal_code' => $o['pincode']
            ],
            'items_count' => (int)$o['items_count']
        ];
    }, $orders);
    
    successResponse($result);
}

/**
 * Update delivery status
 */
function updateDeliveryStatus(int $deliveryUserId): void {
    $data = getJsonBody();
    
    if (empty($data['order_id']) || empty($data['status'])) {
        errorResponse('Order ID and status are required', 400);
    }
    
    $orderId = (int)$data['order_id'];
    $newStatus = $data['status'];
    
    $validStatuses = ['out_for_delivery', 'delivered'];
    if (!in_array($newStatus, $validStatuses)) {
        errorResponse('Invalid status. Use: out_for_delivery, delivered', 400);
    }
    
    $db = getDB();
    
    // Check order belongs to this user
    $stmt = $db->prepare("SELECT * FROM orders WHERE order_id = ? AND delivery_user_id = ?");
    $stmt->execute([$orderId, $deliveryUserId]);
    $order = $stmt->fetch();
    
    if (!$order) {
        errorResponse('Order not found', 404);
    }
    
    // Validate status transition
    $currentStatus = $order['order_status'];
    if ($newStatus === 'out_for_delivery' && $currentStatus !== 'accepted') {
        errorResponse('Can only start delivery from accepted status', 400);
    }
    if ($newStatus === 'delivered' && $currentStatus !== 'out_for_delivery') {
        errorResponse('Can only mark delivered from out_for_delivery status', 400);
    }
    
    // Update status
    $updateFields = ['order_status' => $newStatus];
    if ($newStatus === 'out_for_delivery') {
        $sql = "UPDATE orders SET order_status = ?, delivery_picked_at = NOW() WHERE order_id = ?";
    } else {
        $sql = "UPDATE orders SET order_status = ?, delivered_at = NOW() WHERE order_id = ?";
    }
    
    $stmt = $db->prepare($sql);
    $stmt->execute([$newStatus, $orderId]);
    
    // If delivered, update delivery user stats
    if ($newStatus === 'delivered') {
        $stmt = $db->prepare("
            UPDATE delivery_users SET 
                total_deliveries = total_deliveries + 1,
                total_earnings = total_earnings + ?
            WHERE delivery_user_id = ?
        ");
        $stmt->execute([$order['delivery_earnings'], $deliveryUserId]);
    }
    
    successResponse(['message' => 'Status updated to ' . $newStatus]);
}

/**
 * Get delivery earnings
 */
function getDeliveryEarnings(int $deliveryUserId): void {
    $db = getDB();
    
    // Get user totals
    $stmt = $db->prepare("SELECT total_deliveries, total_earnings FROM delivery_users WHERE delivery_user_id = ?");
    $stmt->execute([$deliveryUserId]);
    $user = $stmt->fetch();
    
    // Today's earnings
    $stmt = $db->prepare("
        SELECT COUNT(*) as deliveries, COALESCE(SUM(delivery_earnings), 0) as earnings
        FROM orders 
        WHERE delivery_user_id = ? AND order_status = 'delivered' AND DATE(delivered_at) = CURDATE()
    ");
    $stmt->execute([$deliveryUserId]);
    $today = $stmt->fetch();
    
    // This week
    $stmt = $db->prepare("
        SELECT COUNT(*) as deliveries, COALESCE(SUM(delivery_earnings), 0) as earnings
        FROM orders 
        WHERE delivery_user_id = ? AND order_status = 'delivered' AND YEARWEEK(delivered_at) = YEARWEEK(NOW())
    ");
    $stmt->execute([$deliveryUserId]);
    $week = $stmt->fetch();
    
    // This month
    $stmt = $db->prepare("
        SELECT COUNT(*) as deliveries, COALESCE(SUM(delivery_earnings), 0) as earnings
        FROM orders 
        WHERE delivery_user_id = ? AND order_status = 'delivered' AND MONTH(delivered_at) = MONTH(NOW()) AND YEAR(delivered_at) = YEAR(NOW())
    ");
    $stmt->execute([$deliveryUserId]);
    $month = $stmt->fetch();
    
    successResponse([
        'total' => [
            'deliveries' => (int)$user['total_deliveries'],
            'earnings' => (float)$user['total_earnings']
        ],
        'today' => [
            'deliveries' => (int)$today['deliveries'],
            'earnings' => (float)$today['earnings']
        ],
        'this_week' => [
            'deliveries' => (int)$week['deliveries'],
            'earnings' => (float)$week['earnings']
        ],
        'this_month' => [
            'deliveries' => (int)$month['deliveries'],
            'earnings' => (float)$month['earnings']
        ]
    ]);
}

/**
 * Get delivery profile
 */
function getDeliveryProfile(int $deliveryUserId): void {
    $db = getDB();
    
    $stmt = $db->prepare("SELECT * FROM delivery_users WHERE delivery_user_id = ?");
    $stmt->execute([$deliveryUserId]);
    $user = $stmt->fetch();
    
    successResponse([
        'delivery_user_id' => (int)$user['delivery_user_id'],
        'phone' => $user['phone'],
        'name' => $user['name'],
        'email' => $user['email'] ?? null,
        'address' => $user['address'] ?? null,
        'upi_id' => $user['upi_id'] ?? null,
        'vehicle_type' => $user['vehicle_type'],
        'vehicle_number' => $user['vehicle_number'],
        'status' => $user['status'],
        'total_deliveries' => (int)$user['total_deliveries'],
        'total_earnings' => (float)$user['total_earnings'],
        'member_since' => $user['created_at']
    ]);
}

/**
 * Update delivery profile
 */
function updateDeliveryProfile(int $deliveryUserId): void {
    $data = getJsonBody();
    $db = getDB();
    
    $updates = [];
    $params = [];
    
    if (isset($data['name'])) {
        $updates[] = 'name = ?';
        $params[] = $data['name'];
    }
    
    if (isset($data['vehicle_type'])) {
        $updates[] = 'vehicle_type = ?';
        $params[] = $data['vehicle_type'];
    }
    
    if (isset($data['vehicle_number'])) {
        $updates[] = 'vehicle_number = ?';
        $params[] = $data['vehicle_number'];
    }
    
    if (isset($data['email'])) {
        $updates[] = 'email = ?';
        $params[] = $data['email'];
    }
    
    if (isset($data['address'])) {
        $updates[] = 'address = ?';
        $params[] = $data['address'];
    }
    
    if (isset($data['upi_id'])) {
        $updates[] = 'upi_id = ?';
        $params[] = $data['upi_id'];
    }
    
    if (isset($data['fcm_token'])) {
        $updates[] = 'fcm_token = ?';
        $params[] = $data['fcm_token'];
    }
    
    if (empty($updates)) {
        errorResponse('No fields to update', 400);
    }
    
    $params[] = $deliveryUserId;
    $stmt = $db->prepare("UPDATE delivery_users SET " . implode(', ', $updates) . " WHERE delivery_user_id = ?");
    $stmt->execute($params);
    
    successResponse(['message' => 'Profile updated successfully']);
}

