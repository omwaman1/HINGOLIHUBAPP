<?php
/**
 * Debug endpoint to identify the error
 * This will be replaced after debugging
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

echo json_encode([
    'status' => 'debug_step_1',
    'php_version' => phpversion()
]);

// Try including files one by one
try {
    require_once __DIR__ . '/config/database.php';
    echo json_encode(['status' => 'debug_step_2', 'message' => 'database.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'database.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/config/jwt.php';
    echo json_encode(['status' => 'debug_step_3', 'message' => 'jwt.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'jwt.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/config/sms.php';
    echo json_encode(['status' => 'debug_step_4', 'message' => 'sms.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'sms.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/helpers/response.php';
    echo json_encode(['status' => 'debug_step_5', 'message' => 'response.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'response.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/helpers/jwt.php';
    echo json_encode(['status' => 'debug_step_6', 'message' => 'helpers/jwt.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'helpers/jwt.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/helpers/transformers.php';
    echo json_encode(['status' => 'debug_step_7', 'message' => 'transformers.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'transformers.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/helpers/query_builder.php';
    echo json_encode(['status' => 'debug_step_8', 'message' => 'query_builder.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'query_builder.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/helpers/categories.php';
    echo json_encode(['status' => 'debug_step_9', 'message' => 'categories.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'categories.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/helpers/otp.php';
    echo json_encode(['status' => 'debug_step_10', 'message' => 'otp.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'otp.php failed: ' . $e->getMessage()]);
    exit;
}

try {
    require_once __DIR__ . '/helpers/reviews.php';
    echo json_encode(['status' => 'debug_step_11', 'message' => 'reviews.php loaded']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'reviews.php failed: ' . $e->getMessage()]);
    exit;
}

// Test DB connection
try {
    $db = getDB();
    $stmt = $db->query('SELECT 1');
    echo json_encode(['status' => 'debug_step_12', 'message' => 'Database connected successfully!']);
} catch (Throwable $e) {
    echo json_encode(['error' => 'Database connection failed: ' . $e->getMessage()]);
    exit;
}

echo json_encode(['status' => 'all_ok', 'message' => 'All includes and DB connection working!']);
