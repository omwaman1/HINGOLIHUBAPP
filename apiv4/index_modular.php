<?php
/**
 * Hello Hingoli API - Main Router (Modular)
 * This is the slim router that includes handler files
 */

// Error reporting (disable in production)
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);

// Include config files
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/config/jwt.php';
require_once __DIR__ . '/config/sms.php';

// Include helper files
require_once __DIR__ . '/helpers/response.php';
require_once __DIR__ . '/helpers/jwt.php';
require_once __DIR__ . '/helpers/transformers.php';
require_once __DIR__ . '/helpers/query_builder.php';
require_once __DIR__ . '/helpers/categories.php';
require_once __DIR__ . '/helpers/otp.php';
require_once __DIR__ . '/helpers/reviews.php';

// Include route handlers (each file contains functions for one resource)
require_once __DIR__ . '/routes/auth.php';
require_once __DIR__ . '/routes/categories.php';
require_once __DIR__ . '/routes/listings.php';
require_once __DIR__ . '/routes/products.php';
require_once __DIR__ . '/routes/old_products.php';
require_once __DIR__ . '/routes/cart.php';
require_once __DIR__ . '/routes/orders.php';
require_once __DIR__ . '/routes/delivery.php';
require_once __DIR__ . '/routes/notifications.php';
require_once __DIR__ . '/routes/user.php';
require_once __DIR__ . '/routes/misc.php';

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
$path = preg_replace('#^/apiv4#', '', $path);
$path = preg_replace('#^/api#', '', $path);
$path = trim($path, '/');
$segments = $path ? explode('/', $path) : [];

// Route the request
try {
    routeRequest($requestMethod, $segments);
} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
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
