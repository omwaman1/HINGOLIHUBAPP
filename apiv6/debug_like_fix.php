<?php
// Quick debug for like API on server
error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/helpers/jwt.php';

header('Content-Type: text/plain');

echo "=== DEBUG LIKE API ===\n\n";

// 1. Check if $currentUserId fix exists in index.php
echo "1. Checking index.php for currentUserId fix... ";
$indexContent = file_get_contents(__DIR__ . '/index.php');
if (strpos($indexContent, '$currentUserId = $authUser[\'user_id\']') !== false) {
    echo "FOUND ✓\n";
} else {
    echo "NOT FOUND - index.php is outdated!\n";
}

// 2. Check if decodeJWT function exists
echo "2. Checking for decodeJWT function... ";
if (function_exists('decodeJWT')) {
    echo "EXISTS ✓\n";
} else {
    echo "MISSING - jwt.php is outdated!\n";
}

// 3. Test requireAuth
echo "3. Testing requireAuth manually... ";
try {
    // Simulate a token
    $testPayload = [
        'user_id' => 9595340263,
        'username' => 'Test User',
        'type' => 'access'
    ];
    $token = generateJWT($testPayload);
    $decoded = decodeJWT($token);
    echo "OK - user_id=" . $decoded['user_id'] . "\n";
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}

// 4. Test database
echo "4. Testing database connection... ";
$db = getDB();
echo "OK\n";

// 5. Test like insert
echo "5. Testing like insert... ";
try {
    $userId = 9595340263;
    $reelId = 30011;
    
    // Delete any existing
    $db->prepare("DELETE FROM reel_likes WHERE user_id = ? AND reel_id = ?")->execute([$userId, $reelId]);
    
    // Insert
    $db->prepare("INSERT INTO reel_likes (user_id, reel_id) VALUES (?, ?)")->execute([$userId, $reelId]);
    
    // Update count
    $db->prepare("UPDATE reels SET likes_count = likes_count + 1 WHERE reel_id = ?")->execute([$reelId]);
    
    // Check
    $stmt = $db->prepare("SELECT * FROM reel_likes WHERE user_id = ? AND reel_id = ?");
    $stmt->execute([$userId, $reelId]);
    $like = $stmt->fetch();
    
    if ($like) {
        echo "OK - Like saved! ID=" . $like['id'] . "\n";
    } else {
        echo "FAILED - Like not found\n";
    }
    
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}

echo "\n=== DONE ===\n";
