<?php
/**
 * Incremental Test - Find which line causes the 500 error
 * We load the index.php file content and evaluate it in chunks
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

// Load config and helpers first (we know these work)
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

// Now try to read and detect error in index.php
$indexFile = file_get_contents(__DIR__ . '/index.php');
$lines = explode("\n", $indexFile);
$totalLines = count($lines);

echo json_encode([
    'status' => 'check',
    'total_lines' => $totalLines,
    'file_size' => strlen($indexFile),
    'message' => 'Loaded index.php successfully, ' . $totalLines . ' lines'
]);

// Try to identify any parse errors by checking PHP syntax
$tempFile = tempnam(sys_get_temp_dir(), 'php_check_');
file_put_contents($tempFile, $indexFile);

$output = [];
$returnCode = 0;
exec('php -l ' . escapeshellarg($tempFile) . ' 2>&1', $output, $returnCode);
unlink($tempFile);

echo json_encode([
    'syntax_check' => $returnCode === 0 ? 'OK' : 'ERROR',
    'output' => implode("\n", $output)
]);

// If syntax is OK, the issue is runtime. Let's check memory usage
echo json_encode([
    'memory_limit' => ini_get('memory_limit'),
    'memory_used' => memory_get_usage(true),
    'max_execution_time' => ini_get('max_execution_time')
]);
