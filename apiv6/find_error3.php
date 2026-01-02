<?php
/**
 * Error Finder v3 - Test by splitting index.php content
 * Evaluates the index.php in portions to find where the error is
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

// Get chunk parameter (which part to test)
$chunk = isset($_GET['chunk']) ? (int)$_GET['chunk'] : 0;
$chunkSize = 1000; // lines per chunk

// Read index.php
$indexContent = file_get_contents(__DIR__ . '/index.php');
$lines = explode("\n", $indexContent);
$totalLines = count($lines);

// Calculate chunk boundaries
$startLine = $chunk * $chunkSize;
$endLine = min(($chunk + 1) * $chunkSize, $totalLines);

// Get lines to test
$testLines = array_slice($lines, $startLine, $endLine - $startLine);
$testCode = "<?php\n" . implode("\n", $testLines);

// Output metadata
$result = [
    'total_lines' => $totalLines,
    'chunk' => $chunk,
    'start_line' => $startLine,
    'end_line' => $endLine,
    'lines_in_chunk' => count($testLines),
    'total_chunks' => ceil($totalLines / $chunkSize)
];

// Write to temp file and try to include it
$tempFile = sys_get_temp_dir() . '/test_chunk_' . $chunk . '.php';
file_put_contents($tempFile, $testCode);

// Try to include it
try {
    // Use output buffering to catch any output
    ob_start();
    $includeResult = @include $tempFile;
    $output = ob_get_clean();
    
    $result['status'] = 'SUCCESS';
    $result['include_result'] = $includeResult;
    $result['output_length'] = strlen($output);
    
} catch (ParseError $e) {
    ob_end_clean();
    $result['status'] = 'PARSE_ERROR';
    $result['error'] = $e->getMessage();
    $result['error_line'] = $e->getLine();
} catch (Throwable $e) {
    ob_end_clean();
    $result['status'] = 'ERROR';
    $result['error'] = $e->getMessage();
    $result['error_line'] = $e->getLine();
    $result['error_file'] = $e->getFile();
}

// Clean up temp file
@unlink($tempFile);

echo json_encode($result, JSON_PRETTY_PRINT);
