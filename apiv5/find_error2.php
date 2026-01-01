<?php
/**
 * Error Finder v2 - Load functions incrementally
 * This will try to include the file and catch any runtime errors
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

// Don't send headers - let index.php do that
ob_start();

$results = [];

try {
    // Load the full index.php file
    // If we get here without error, the file loads fine
    include_once __DIR__ . '/index.php';
    $results['include'] = 'SUCCESS - index.php loaded without fatal errors';
} catch (Throwable $e) {
    $results['include'] = 'ERROR: ' . $e->getMessage();
    $results['file'] = $e->getFile();
    $results['line'] = $e->getLine();
    $results['trace'] = $e->getTraceAsString();
}

// Clear any output from index.php
ob_end_clean();

// Now output our results
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

echo json_encode($results, JSON_PRETTY_PRINT);
