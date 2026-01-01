<?php
/**
 * Debug - Test Router
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

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

// Now try setting headers and simple success response
try {
    successResponse(['test' => 'ok', 'message' => 'This works!']);
} catch (Throwable $e) {
    echo json_encode(['error' => $e->getMessage(), 'trace' => $e->getTraceAsString()]);
}
