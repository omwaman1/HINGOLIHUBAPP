<?php
/**
 * Hello Hingoli API - Main Router (with debug output)
 */

// Enable debug output FIRST before anything else
error_reporting(E_ALL);
ini_set('display_errors', 1);
ob_start(); // Buffer output to avoid header issues

// Include required files
try {
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
} catch (Throwable $e) {
    ob_end_clean();
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['error' => 'Include failed: ' . $e->getMessage()]);
    exit;
}

// Clear any buffered output
ob_end_clean();

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

// Simple test response - just to verify the router is reached
if ($segments[0] === 'test' || empty($segments)) {
    successResponse([
        'version' => '1.0.0', 
        'name' => 'Hello Hingoli API',
        'debug' => 'reached router',
        'path' => $path,
        'segments' => $segments
    ]);
    exit;
}

echo json_encode(['error' => 'Not implemented', 'path' => $path]);
